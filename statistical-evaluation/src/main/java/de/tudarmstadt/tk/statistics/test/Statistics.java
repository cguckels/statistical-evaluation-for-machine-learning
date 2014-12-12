package de.tudarmstadt.tk.statistics.test;


/**
 * Copyright 2014
 * Telecooperation (TK) Lab
 * Technische Universit�t Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import de.tudarmstadt.tk.statistics.config.StatsConfig;
import de.tudarmstadt.tk.statistics.config.StatsConfigConstants;
import de.tudarmstadt.tk.statistics.helper.ImprovedDirectedGraph;
import de.tudarmstadt.tk.statistics.report.EvaluationResults;

/**
 * Class to perform statistical evaluation of machine-learning sample data
 * @author Guckelsberger, Schulz
 */
public class Statistics {	
    private static final Logger logger = LogManager.getLogger("Statistics");
    private StatsConfig config;
    
	public Statistics(StatsConfig config) {
		this.config=config;
	}

	/**
	 * Method to perform a statistical evaluation using
	 * {@link PipelineReportData} test results. It uses the {@link RBridge}
	 * class to perform statistics and stores the results in
	 * {@link EvaluationResult}.
	 * 
	 * @param measuresPerModel
	 *            The sample data to be evaluated as a two-dimensional HashMap.
	 *            On the outer level, the key represents the type of performance
	 *            measure. On the inner level, the key represents the index of
	 *            one particular model (=classifier+feature sets) and the value
	 *            the samples provided for this model and the performance
	 *            measure.
	 * @see de.tudarmstadt.tk.mugc.prototype.reports.AbstractPipelineReport#outputReport(java.util.List)
	 * @return An object of type {@Link EvaluationResults} with the
	 *         results of the statistical evaluation
	 */
	public EvaluationResults performStatisticalEvaluation(SampleData sampleData){

		EvaluationResults evalResults = new EvaluationResults();
		evalResults.setSampleData(sampleData);
		evalResults.setSignificanceLevel(config.getSignificanceLevels().get(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.low), config.getSignificanceLevels().get(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.medium), config.getSignificanceLevels().get(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.high) );
		evalResults.setIsBaselineEvaluation(sampleData.isBaselineEvaluation());
		int nModels = 0;

		// Perform statistical evaluation for all performance measures
		Iterator<Entry<String, ArrayList<ArrayList<Double>>>> it = sampleData.getSamples().entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, ArrayList<ArrayList<Double>>> entry = it.next();
			String measure = entry.getKey();
			evalResults.addMeasure(measure);
			ArrayList<ArrayList<Double>> valuesPerModel = entry.getValue();
			logger.log(Level.INFO, String.format("Evaluating %s samples.", measure));

			// Determine how many models there are to be compared
			nModels = valuesPerModel.size();

			// None or only one model -> nothing to compare!
			if (nModels <= 1) {
				System.err.println("Nothing to compare in statistical evaluation! Please check.");
				logger.log(Level.ERROR, "Nothing to compare in statistical evaluation! Please check.");
				return null;

				// Two or more: Prepare statistics evaluation
			} else if (nModels >= 2) {

				// Store values in a two-dimensional array
				double[][] samplesPerModel = new double[nModels][];
				for (int i = 0; i < valuesPerModel.size(); i++) {
					samplesPerModel[i] = new double[valuesPerModel.get(i).size()];
					for (int j = 0; j < valuesPerModel.get(i).size(); j++) {
						samplesPerModel[i][j] = valuesPerModel.get(i).get(j);
					}
				}
				
				// Use appropriate test as specified in config file, depending
				// on the number of comparisons
				try {
					if (nModels == 2) {// 2 models
						this.testTwoModels(evalResults, config.getRequiredTests(), samplesPerModel, measure);
					} else if (nModels > 2) {// Multiple models
						ArrayList<Double> averageSamplesPerModel = sampleData.getSamplesAverage().get(entry.getKey());
						this.testMultipleModels(evalResults, config.getRequiredTests(), config.getRequiredCorrections(), samplesPerModel, averageSamplesPerModel, measure, sampleData.isBaselineEvaluation());
					}
				} catch (Exception e) {
					logger.log(Level.ERROR, "Error while performing statistical tests. Aborting.");
					System.err.println("Error while performing statistical tests. Aborting.");
					return null;
				}
			}
		}

		// Perform evaluation on contingency matrix if appropriate test is
		// provided and there are only two models to be evaluated on a single
		// domain
		String nonParametricContingency = config.getRequiredTests().get(StatsConfigConstants.TEST_CLASSES.TwoSamplesNonParametricContingency);
		if (!nonParametricContingency.isEmpty() && nModels == 2) {
			int[][] contingency = sampleData.getContingencyMatrix();
			// Only available if two models were evaluated on a single domain
			if (contingency == null) {
				logger.log(Level.ERROR, "No contingency matrix provided for McNemar test! Test failed.");
				System.err.println("No contingency matrix provided for McNemar test! Test failed.");
			} else {
				RBridge stats = RBridge.getInstance(false);
				evalResults.setNonParametricTest(nonParametricContingency);
				evalResults.addMeasure("Contingency Table");
				TestResult result = null;
				try {
					Method m = RBridge.class.getMethod(String.format("test%s", nonParametricContingency), int[][].class);
					result = (TestResult) m.invoke(stats, contingency);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
				evalResults.addNonParametricTestResult(Pair.of(nonParametricContingency, (AbstractTestResult) result), "Contingency Table");
			}
		}

		return evalResults;

	}

	/**
	 * Perform statistical tests for comparing the performance of two models in
	 * R, using wrapper methods in {@link RBridge}
	 * 
	 * @param requiredTests
	 *            The tests required to perfom, as specified in the statistics
	 *            evaluation config file
	 * @param samples
	 *            A two-dimensional array of performance measure samples for the
	 *            different models/folds
	 */
	private void testTwoModels(EvaluationResults evalResults, HashMap<StatsConfigConstants.TEST_CLASSES, String> requiredTests, double[][] samples, String measure) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		RBridge stats = RBridge.getInstance(false);

		// Get required tests for two samples on one/multiple domains
		String testParametric = requiredTests.get(StatsConfigConstants.TEST_CLASSES.TwoSamplesParametric);
		String testNonParametric = requiredTests.get(StatsConfigConstants.TEST_CLASSES.TwoSamplesNonParametric);

		evalResults.setParametricTest(testParametric);
		evalResults.setNonParametricTest(testNonParametric);

		// Call corresponding parametric method using reflection
		logger.log(Level.INFO, String.format("Performing parametric omnibus test for comparing 2 models: %s", testParametric));
		TestResult result = null;
		Method m = RBridge.class.getMethod(String.format("test%s", testParametric), double[].class, double[].class);
		result = (TestResult) m.invoke(stats, samples[0], samples[1]);
		evalResults.addParametricTestResult(Pair.of(testParametric, (AbstractTestResult) result), measure);

		// Always perform non-parametric alternative
		logger.log(Level.INFO, String.format("Performing non-parametric omnibus test for comparing 2 models: %s", testNonParametric));
		m = RBridge.class.getMethod(String.format("test%s", testNonParametric), double[].class, double[].class);
		result = (TestResult) m.invoke(stats, samples[0], samples[1]);
		evalResults.addNonParametricTestResult(Pair.of(testNonParametric, (AbstractTestResult) result), measure);
	}

	/**
	 * Perform statistical tests for comparing the performance of >2 models in
	 * R, using wrapper methods in {@link RBridge}
	 * 
	 * @param requiredTests
	 *            The tests required to perfom, as specified in the statistics
	 *            evaluation config file
	 * @param samples
	 *            A two-dimensional array of performance measure samples for the
	 *            different models/folds
	 * @param requiredCorrections
	 *            The corrections to be performed when doing multiple
	 *            comparisons testing, e.g. Bonferroni adjustment
	 */
	private void testMultipleModels(EvaluationResults evalResults, HashMap<StatsConfigConstants.TEST_CLASSES, String> requiredTests, List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections, double[][] samples, ArrayList<Double> averageSamplesPerModel, String measure, boolean isBaselineEvaluation)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		RBridge stats = RBridge.getInstance(true);
		// Get required tests for >2 samples
		String testParametric = requiredTests.get(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametric);
		String testNonParametric = requiredTests.get(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametric);

		String testPostHocParametric = null;
		String testPostHocNonParametric = null;
		if (!isBaselineEvaluation) {
			testPostHocParametric = requiredTests.get(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametricPosthoc);
			testPostHocNonParametric = requiredTests.get(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametricPostHoc);
		} else {
			testPostHocParametric = requiredTests.get(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametricPosthocBaseline);
			testPostHocNonParametric = requiredTests.get(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametricPostHocBaseline);
		}

		evalResults.setParametricTest(testParametric);
		evalResults.setNonParametricTest(testNonParametric);
		evalResults.setParametricPostHocTest(testPostHocParametric);
		evalResults.setNonParametricPostHocTest(testPostHocNonParametric);

		// Call corresponding parametric method using reflection
		logger.log(Level.INFO, String.format("Performing parametric omnibus test for comparing >2 models: %s", testParametric));
		Method m = RBridge.class.getMethod(String.format("test%s", testParametric), double[][].class);
		TestResult result = (TestResult) m.invoke(stats, samples);
		evalResults.addParametricTestResult(Pair.of(testParametric, (AbstractTestResult) result), measure);

		// If test successful, print result and call post-hoc test
		if (result != null && !Double.isNaN(result.getpValue())) {
			// Perform parametric post-hoc test
			logger.log(Level.INFO, String.format("Performing parametric post-hoc test: %s", testPostHocParametric));
			m = RBridge.class.getMethod(String.format("test%s", testPostHocParametric), double[][].class);
			PairwiseTestResult postHocResult = (PairwiseTestResult) m.invoke(stats, samples);

			if (postHocResult.getRequiresPValueCorrection()) {
				for (StatsConfigConstants.CORRECTION_VALUES s : requiredCorrections) {
					postHocResult.addPValueCorrections(s, stats.adjustP(postHocResult, s));
				}
			}

			// Determine ordering of significant differences between models,
			// based on unadjusted(!) p-values
			logger.log(Level.INFO, "Calculating chain of statistical significance via topological ordering");
			ImprovedDirectedGraph<Integer, DefaultEdge> graph = createSignificanceGraph(postHocResult, averageSamplesPerModel);
			HashMap<Integer, TreeSet<Integer>> ordering = calcOrderOfSignificantDifferences(graph);
			Set<DefaultEdge> e = graph.edgeSet();
			int[][] edgelist = new int[2][e.size()];
			int i = 0;
			for (DefaultEdge edge : e) {
				edgelist[0][i] = graph.getEdgeSource(edge);
				edgelist[1][i] = graph.getEdgeTarget(edge);
				i++;
			}
			evalResults.getParameticPostHocOrdering().put(measure, ordering);
			evalResults.getParameticPostHocEdgelist().put(measure, edgelist);

			evalResults.addParametricPostHocTestResult(Pair.of(testPostHocParametric, (AbstractTestResult) postHocResult), measure);
		}

		//Perform non-parametric tests
		logger.log(Level.INFO, String.format("Performing non-parametric omnibus test for comparing >2 models: %s", testParametric));
		m = RBridge.class.getMethod(String.format("test%s", testNonParametric), double[][].class);
		result = (TestResult) m.invoke(stats, samples);
		evalResults.addNonParametricTestResult(Pair.of(testNonParametric, (AbstractTestResult) result), measure);

		// If test successful, print result and call non-parametric post-hoc test
		if (result != null && !Double.isNaN(result.getpValue())) {
			logger.log(Level.INFO, String.format("Performing non-parametric post-hoc test: %s", testPostHocParametric));
			m = RBridge.class.getMethod(String.format("test%s", testPostHocNonParametric), double[][].class);
			PairwiseTestResult postHocResult = (PairwiseTestResult) m.invoke(stats, samples);
	
			if (postHocResult.getRequiresPValueCorrection()) {
				for (StatsConfigConstants.CORRECTION_VALUES s : requiredCorrections) {
					postHocResult.addPValueCorrections(s, stats.adjustP(postHocResult, s));
				}
			}
			
			// Determine ordering of significant differences between models,
			// based on unadjusted(!) p-values
			logger.log(Level.INFO, "Calculating chain of statistical significance via topological ordering");
			ImprovedDirectedGraph<Integer, DefaultEdge> graph = createSignificanceGraph(postHocResult, averageSamplesPerModel);
			HashMap<Integer, TreeSet<Integer>> ordering = calcOrderOfSignificantDifferences(graph);
			Set<DefaultEdge> e = graph.edgeSet();
			int[][] edgelist = new int[2][e.size()];
			int i = 0;
			for (DefaultEdge edge : e) {
				edgelist[0][i] = graph.getEdgeSource(edge);
				edgelist[1][i] = graph.getEdgeTarget(edge);
				i++;
			}
			evalResults.getNonParameticPostHocOrdering().put(measure, ordering);
			evalResults.getNonParameticPostHocEdgelist().put(measure, edgelist);
			
			evalResults.addNonParametricPostHocTestResult(Pair.of(testPostHocNonParametric, (AbstractTestResult) postHocResult), measure);
		}

	}

	/**
	 * Determine order of significant differences between models Cf. Eugster, M.
	 * J. A., Hothorn, T., & Leisch, F. (2008). Exploratory and inferential
	 * analysis of benchmark experiments. Retrieved from
	 * http://epub.ub.uni-muenchen.de/4134/ Determine topological ordering of a
	 * DAG of the significance model-pairs. In addition to Eugster et al., we
	 * also check whether relations between topological groups are valid.
	 *
	 * 
	 * @return a HashMap mapping from levels of the topological ordering to the
	 *         models on that level
	 */
	private HashMap<Integer, TreeSet<Integer>> calcOrderOfSignificantDifferences(ImprovedDirectedGraph<Integer, DefaultEdge> directedGraph) {

		// If nodes are on the same level of the graph, they are not
		// significantly different and form a group
		// One higher group is significantly different from all lower groups as
		// a whole (!), if there exists an edge from each element of the higher
		// group to each element of the lower groups
		HashMap<Integer, TreeSet<Integer>> ordering = directedGraph.getTopologicalOrder();
		int nodesLeft = 0;
		for (TreeSet<Integer> s : ordering.values()) {
			nodesLeft += s.size();
		}
		for (int level = 0; level < ordering.keySet().size() - 1; level++) {
			TreeSet<Integer> nodes = ordering.get(level);
			nodesLeft -= nodes.size();
			for (Integer n : nodes) {
				if (directedGraph.outDegreeOf(n) != nodesLeft) {
					// Not a valid ordering
					return null;
				}
			}
		}

		return ordering;
	}

	/**
	 * Build directed graph representing the significant differences between the
	 * models If the mean performance of a model A is significantly larger than
	 * the performance of model B, add an Edge A->B to the graph
	 * 
	 * @param testResult
	 *            The {@link PairwiseTestResult} from a parametric or
	 *            non-parametric post-hoc test of >2 models
	 * @param averageSamplesPerModel
	 *            The average sample values per model
	 * @return A {@link ImprovedDirectedGraph} representing the hierarchy of
	 *         significant differences between the models
	 */
	private ImprovedDirectedGraph<Integer, DefaultEdge> createSignificanceGraph(PairwiseTestResult testResult, ArrayList<Double> averageSamplesPerModel) {

		ImprovedDirectedGraph<Integer, DefaultEdge> directedGraph = new ImprovedDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
		double[][] pValues = testResult.getpValue();
		for (int i = 0; i < pValues.length + 1; i++) {
			directedGraph.addVertex(i);
		}

		for (int i = 0; i < pValues.length; i++) {
			for (int j = 0; j <= i; j++) {
				if(Double.isNaN(pValues[i][j])){
					continue;
				}
				if (pValues[i][j] <= config.getSignificanceLevels().get(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.medium)) {
					if (averageSamplesPerModel.get(i + 1) < averageSamplesPerModel.get(j)) {
						directedGraph.addEdge(i + 1, j);
					} else {
						directedGraph.addEdge(j, i + 1);
					}
				}
			}
		}

		return directedGraph;
	}


}
