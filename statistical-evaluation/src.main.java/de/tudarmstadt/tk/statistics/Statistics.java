package de.tudarmstadt.tk.statistics;

/**
 * Copyright 2014
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Guckelsberger, Schulz
 */
public class Statistics {

	private static final Logger logger = LogManager.getLogger("Statistics");

	private HashMap<String, String> requiredTests = null;
	private List<String> requiredCorrections = null;
	private boolean parametricAndNonParametric = true; // By default.
	private double significance_low = 1;
	private double significance_medium = 1;
	private double significance_high = 1;
	private static String CONFIG_FILE_PATH = "./config/stats_setup.cfg";

	public Statistics() {
		HashMap<String, Object> parameters;
		try {
			parameters = readParametersFromConfig();
			requiredTests = (HashMap<String, String>) parameters.get(StatsConfigConstants.TESTS);
			requiredCorrections = (List<String>) parameters.get(StatsConfigConstants.CORRECTIONS);
			significance_low = (double) parameters.get(StatsConfigConstants.SIGNIFICANCE_LOW);
			significance_medium = (double) parameters.get(StatsConfigConstants.SIGNIFICANCE_MEDIUM);
			significance_high = (double) parameters.get(StatsConfigConstants.SIGNIFICANCE_HIGH);
		} catch (FileNotFoundException e) {
			logger.log(Level.ERROR, "Statistics config file not found.");
			System.err.println("Statistics config file not found.");
			e.printStackTrace();
		} catch (JSONException e) {
			logger.log(Level.ERROR, "Error while reading statistics config file.");
			System.err.println("Error while reading statistics config file.");
			e.printStackTrace();
		}
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
	public EvaluationResults performStatisticalEvaluation(StatisticalEvaluationData sampleData) {

		EvaluationResults evalResults = new EvaluationResults();
		evalResults.setSampleData(sampleData);
		evalResults.setSignificanceLevel(significance_low, significance_medium, significance_high);
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
				/*
				 * Iterator<Entry<Integer, ArrayList<Double>>> ir =
				 * valuesPerModel.entrySet().iterator(); while (ir.hasNext()) {
				 * Entry<Integer, ArrayList<Double>> pairs = ir.next(); int
				 * modelIndex = pairs.getKey(); ArrayList<Double> sampleList =
				 * pairs.getValue(); double[] sampleArray = new
				 * double[sampleList.size()]; for(int j=0; j<sampleList.size();
				 * j++){ sampleArray[j]=sampleList.get(j).doubleValue(); }
				 * samplesPerModel[modelIndex]=sampleArray; }
				 */

				// Use appropriate test as specified in config file, depending
				// on the number of comparisons
				try {
					if (nModels == 2) {// 2 models
						this.testTwoModels(evalResults, requiredTests, samplesPerModel, measure);
					} else if (nModels > 2) {// Multiple models
						ArrayList<Double> averageSamplesPerModel = sampleData.getSamplesAverage().get(entry.getKey());
						this.testMultipleModels(evalResults, requiredTests, requiredCorrections, samplesPerModel, averageSamplesPerModel, measure, sampleData.isBaselineEvaluation());
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
		String nonParametricContingency = requiredTests.get(StatsConfigConstants.TWO_SAMPLES_NONPARAMETRIC_CONTINGENCY_TABLE);
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
	private void testTwoModels(EvaluationResults evalResults, HashMap<String, String> requiredTests, double[][] samples, String measure) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		RBridge stats = RBridge.getInstance(false);

		// Get required tests for two samples on one/multiple domains
		String testParametric = requiredTests.get(StatsConfigConstants.TWO_SAMPLES_PARAMETRIC);
		String testNonParametric = requiredTests.get(StatsConfigConstants.TWO_SAMPLES_NONPARAMETRIC);

		evalResults.setParametricTest(testParametric);
		evalResults.setNonParametricTest(testNonParametric);

		// Call corresponding parametric method using reflection
		logger.log(Level.INFO, String.format("Performing parametric omnibus test for comparing 2 models: %s", testParametric));
		TestResult result = null;
		Method m = RBridge.class.getMethod(String.format("test%s", testParametric), double[].class, double[].class);
		result = (TestResult) m.invoke(stats, samples[0], samples[1]);
		evalResults.addParametricTestResult(Pair.of(testParametric, (AbstractTestResult) result), measure);

		// If test worked out but both parametric and non-parametric testing
		// required, try non-parametric alternative
		if (result != null && parametricAndNonParametric) {
			result = null;
		}
		// If test didn't work out (e.g. assumptions violated), perform
		// non-parametric alternative
		if (result == null) {
			logger.log(Level.INFO, String.format("Performing non-parametric omnibus test for comparing 2 models: %s", testNonParametric));
			m = RBridge.class.getMethod(String.format("test%s", testNonParametric), double[].class, double[].class);
			result = (TestResult) m.invoke(stats, samples[0], samples[1]);
			evalResults.addNonParametricTestResult(Pair.of(testNonParametric, (AbstractTestResult) result), measure);
		}
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
	private void testMultipleModels(EvaluationResults evalResults, HashMap<String, String> requiredTests, List<String> requiredCorrections, double[][] samples, ArrayList<Double> averageSamplesPerModel, String measure, boolean isBaselineEvaluation)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		RBridge stats = RBridge.getInstance(true);
		// Get required tests for >2 samples
		String testParametric = requiredTests.get(StatsConfigConstants.MULTIPLE_SAMPLES_PARAMETRIC);
		String testNonParametric = requiredTests.get(StatsConfigConstants.MULTIPLE_SAMPLES_NONPARAMETRIC);

		String testPostHocParametric = null;
		String testPostHocNonParametric = null;
		if (!isBaselineEvaluation) {
			testPostHocParametric = requiredTests.get(StatsConfigConstants.MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC);
			testPostHocNonParametric = requiredTests.get(StatsConfigConstants.MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC);
		} else {
			testPostHocParametric = requiredTests.get(StatsConfigConstants.MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC_BASELINE);
			testPostHocNonParametric = requiredTests.get(StatsConfigConstants.MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC_BASELINE);
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
		if (result != null) {
			// Perform parametric post-hoc test
			logger.log(Level.INFO, String.format("Performing parametric post-hoc test: %s", testPostHocParametric));
			m = RBridge.class.getMethod(String.format("test%s", testPostHocParametric), double[][].class);
			PairwiseTestResult postHocResult = (PairwiseTestResult) m.invoke(stats, samples);

			if (postHocResult.getRequiresPValueCorrection()) {
				for (String s : requiredCorrections) {
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

		// If parametric test failed or an non-parametric test is requested as
		// well
		if (result == null || (result != null && parametricAndNonParametric)) {

			// Perform non-parametric test
			logger.log(Level.INFO, String.format("Performing non-parametric omnibus test for comparing >2 models: %s", testParametric));
			m = RBridge.class.getMethod(String.format("test%s", testNonParametric), double[][].class);
			result = (TestResult) m.invoke(stats, samples);
			evalResults.addNonParametricTestResult(Pair.of(testNonParametric, (AbstractTestResult) result), measure);

			// Perform non-parametric post-hoc test
			logger.log(Level.INFO, String.format("Performing non-parametric post-hoc test: %s", testPostHocParametric));
			m = RBridge.class.getMethod(String.format("test%s", testPostHocNonParametric), double[][].class);
			PairwiseTestResult postHocResult = (PairwiseTestResult) m.invoke(stats, samples);

			if (postHocResult.getRequiresPValueCorrection()) {
				for (String s : requiredCorrections) {
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
				if (pValues[i][j] <= this.significance_medium) {
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

	/**
	 * Read statistics evaluation parameter from the (JSON) config file
	 * 
	 * @param pathToConfigFile
	 *            path to the config file to use (including suffix .cfg)
	 * @return a HashMap with the parameters from the config file
	 * @throws JSONException
	 * @throws FileNotFoundException
	 * @see StatsConfigConstants
	 */
	public static HashMap<String, Object> readParametersFromConfig() throws FileNotFoundException, JSONException {

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		JSONObject config = new JSONObject(new JSONTokener(new FileReader(CONFIG_FILE_PATH)));
		List<String> measures = getListFromConfigFile(config.getJSONArray(StatsConfigConstants.MEASURES), StatsConfigConstants.MEASURE, Arrays.asList(StatsConfigConstants.MEASURE_VALUES));
		// List<String> measures =
		// getMeasuresFromConfigFile(config.getJSONArray(StatsConfigConstants.MEASURES));
		List<String> corrections = getListFromConfigFile(config.getJSONArray(StatsConfigConstants.CORRECTIONS), StatsConfigConstants.CORRECTION, Arrays.asList(StatsConfigConstants.CORRECTION_VALUES));
		HashMap<String, String> tests = getTestsFromConfigFile(config.getJSONArray(StatsConfigConstants.TESTS));

		parameters.put(StatsConfigConstants.SIGNIFICANCE_LOW, config.getDouble(StatsConfigConstants.SIGNIFICANCE_LOW));
		parameters.put(StatsConfigConstants.SIGNIFICANCE_MEDIUM, config.getDouble(StatsConfigConstants.SIGNIFICANCE_MEDIUM));
		parameters.put(StatsConfigConstants.SIGNIFICANCE_HIGH, config.getDouble(StatsConfigConstants.SIGNIFICANCE_HIGH));

		parameters.put(StatsConfigConstants.MEASURES, measures);
		parameters.put(StatsConfigConstants.CORRECTIONS, corrections);
		parameters.put(StatsConfigConstants.TESTS, tests);

		parameters.put(StatsConfigConstants.SELECT_BEST_N, config.getInt(StatsConfigConstants.SELECT_BEST_N));
		parameters.put(StatsConfigConstants.SELECT_BEST_N_BY_MEASURE, config.getString(StatsConfigConstants.SELECT_BEST_N_BY_MEASURE));

		// parameters.put(StatsConfigConstants.PARAMETRIC_AND_NONPARAMETRIC,
		// config.get(StatsConfigConstants.PARAMETRIC_AND_NONPARAMETRIC));
		return parameters;

	}

	/**
	 * Fetches a list of values from a JSON array and returns them as an array
	 * of type String
	 * 
	 * @param jsonArray
	 *            The JSON array with the required entries
	 * @param identifier
	 *            The name of the JSON array with the requires entries
	 * @param conformity
	 *            A list of allowed values to check whether the JSON array
	 *            entries conform
	 * @return a List of type String containing the keys from the JSON array,
	 *         conforming with the allowed values
	 * @see StatsConfigConstants
	 */
	private static List<String> getListFromConfigFile(JSONArray jsonArray, String identifier, List<String> conformity) {
		List<String> l = new ArrayList<String>();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonO;
			try {
				jsonO = jsonArray.getJSONObject(i);
				String name = jsonO.getString(identifier);
				if (conformity.contains(name)) {
					l.add(name);
				} else {
					String error = String.format("%s '%s' from statistics config file not allowed!", identifier, name);
					System.err.println(error);
					logger.log(Level.ERROR, error);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return l;
	}

	/**
	 * Retrieves the statistical tests defined in a JSON array as part of the
	 * statistics config file
	 * 
	 * @param testArray
	 *            The JSON array containing the test keys and valus
	 * @return a HashMap with String-keys representing the test category (e.g.
	 *         MULTIPLE_SAMPLES_SINGLE_DOMAIN_NONPARAMETRIC) and the respective
	 *         test as String-value
	 * @see StatsConfigConstants
	 */
	private static HashMap<String, String> getTestsFromConfigFile(JSONArray testArray) {

		HashMap<String, String> tests = new HashMap<String, String>();

		for (int i = 0; i < testArray.length(); i++) {
			try {
				JSONObject entry = testArray.getJSONObject(i);
				String key = JSONObject.getNames(entry)[0];
				tests.put(key, entry.optString(key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return tests;
	}

}
