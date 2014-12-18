package de.tudarmstadt.tk.statistics.test;

/**
 * Copyright 2014
 * Telecooperation (TK) Lab
 * Technische Universit�t Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

import de.tudarmstadt.tk.statistics.config.StatsConfigConstants;

/**
 * Wrapper class for performing statistics using R and the JRI bridge
 * (Singleton).
 * 
 * @author Guckelsberger, Schulz
 *
 *         JRI Setup
 * 
 *         Requirements: 1) R must be installed (Download from
 *         http://cran.r-mirror.de/) 2) Package "RJava" must be installed. Type
 *         into R console "install.package(RJava)".
 * 
 *         EITHER Command line in UNIX (store in .bashrc): 1) Add JARs
 *         JRIEngine.jar, JRI.jar, REngine.jar to CLASSPATH. Example: export
 *         CLASSPATH=.:/usr/lib/R/site-library/rJava/jri/ 2) Set R_HOME path.
 *         Example: export R_HOME=/usr/lib/R. Retrieve path by executing
 *         "R.home(component = "home")" in R 3) Add native JRI-library to
 *         LD_LIBRARY_PATH. It should be located within the same directory as
 *         the JAR's. Example: export
 *         LD_LIBRARY_PATH=/usr/lib/R/site-library/rJava/jri/
 * 
 *         OR Eclipse: 1) Add JARs to Project > Properties > Java Build Path >
 *         Libraries 2) Add JRI Library path to Run > Run Configurations > VM
 *         Arguments. Example:
 *         -Djava.library.path=.:/usr/lib/R/site-library/rJava/jri/ 3) Add the
 *         R_HOME variable to Run > Run Configurations > Environment. Example:
 *         New variable R_HOME with value /usr/lib/R
 *
 *         Tutorial on using R from Java:
 *         http://blog.comsysto.com/2013/07/10/java
 *         -r-integration-with-jri-for-on-demand-predictions/
 *
 */
public class Statistics {

	private Rengine engine = null;

	// Singleton
	private static volatile Statistics instance = null;

	private static final Logger logger = LogManager.getLogger("Statistics");

	/**
	 * (Internal) Create singleton instance of class RStatistics. Main goal:
	 * Create R-engine instance to be used for any of the statistics methods.
	 * Multiple calls share a common context which is maintained throughout the
	 * lifecycle of a Rengine instance.
	 * 
	 * @param chatty
	 *            If true, a event listener is attached to the R session to
	 *            track events and errors and print them to the log
	 */
	private Statistics(boolean chatty) {

		// Just making sure we have the right version of everything
		if (!Rengine.versionCheck()) {
			System.err.println("JRI: Version mismatch - Java files don't match library version.");
			System.exit(1);
		}

		// If "chatty"-flag set, attach listener to R engine to track events and
		// errors
		CallbackListener listener = null;
		if (chatty) {
			listener = new CallbackListener();
		}

		// Initialise R Engine.
		// First parameter: parameter list comprising --vanilla. Crucial to
		// initiate a clean R session
		// Second parameter enables/deactivates R main loop. In this case, we
		// only want to use R as calculation slave, thus deactivated it.
		// Third parameter used to attach listener and listen to R events, e.g.
		// errors
		engine = Rengine.getMainEngine();
		if (engine == null)
			engine = new Rengine(new String[] { "--vanilla" }, false, listener);

		// Wait until REngine-thread is ready
		if (!engine.waitForR()) {
			System.err.println("Cannot load R. Is the environment variable R_HOME set correctly?");
			System.exit(1);
		}

		// Initialize session
		try {
			// Set default CRAN repository for package installation
			engine.eval("options(repos = c(CRAN = 'http://cran.at.r-project.org'))");
			// Add function to R session to install packages only if not already
			// present. Use first specified library directory to store packages.
			engine.eval("usePackage <- function(p) { if (!is.element(p, installed.packages()[,1])) install.packages(p, dep = TRUE, lib=.libPaths()[1]); require(p, character.only = TRUE)}");
		} catch (Exception e) {
			String error = "Exception while performing R engine initialisation:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			e.printStackTrace();
		}

	}

	/**
	 * Create singleton instance of class RStatistics. Main goal: Create
	 * R-engine instance to be used for any of the statistics methods. Multiple
	 * calls share a common context which is maintained throughout the lifecycle
	 * of a Rengine instance.
	 * 
	 * @param chatty
	 *            If true, a event listener is attached to the R session to
	 *            track events and errors and print them to the log
	 * @return An instance of class RStatistics
	 */
	public static Statistics getInstance(boolean chatty) {
		if (instance == null) {
			synchronized (Statistics.class) {
				if (instance == null) {
					instance = new Statistics(chatty);
				}
			}
		}
		return instance;
	}

	/**
	 * Transforms either a htest or pairwise.htest R object to its java
	 * equivalent TestResult and PairwiseTestResult
	 * 
	 * @param The
	 *            name of the R resource that represents the htest or
	 *            pairwise.htest object
	 * @return An instance of class AbstractTestResult that can be casted to
	 *         either TestResult or PairwiseTestResult
	 */
	private AbstractTestResult toTestResult(String resourceName) {

		String resultType = engine.eval(String.format("class(%s)", resourceName)).asString();
		if (resultType.equals("htest")) {
			REXP htest = engine.eval(resourceName);
			RList resultList = htest.asList();
			double p = resultList.at("p.value").asDouble();
			double statistic = resultList.at("statistic").asDouble();
			String method = resultList.at("method").asString();
			HashMap<String, Double> parameter = new HashMap<String, Double>();
			RList parameterList = engine.eval(String.format("names(%s$parameter)", resourceName)).asList();
			if (parameterList != null) {
				String[] keys = parameterList.keys();
				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					parameter.put(key, parameterList.at(key).asDouble());
				}
			}

			return new TestResult(method, parameter, p, statistic);

		} else if (resultType.equals("pairwise.htest")) {
			REXP pairwiseHtest = engine.eval(resourceName);
			RList resultList = pairwiseHtest.asList();
			double[][] p = resultList.at("p.value").asDoubleMatrix();
			double[][] statistic = null;
			if (resultList.at("statistic") != null) {
				statistic = resultList.at("statistic").asDoubleMatrix();
			}
			String method = resultList.at("method").asString();
			HashMap<String, Double> parameter = new HashMap<String, Double>();
			RList parameterList = engine.eval(String.format("names(%s$parameter)", resourceName)).asList();
			if (parameterList != null) {
				String[] keys = parameterList.keys();
				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					parameter.put(key, parameterList.at(key).asDouble());
				}
			}

			return new PairwiseTestResult(method, parameter, p, statistic);
		}

		return null;
	}

	/**
	 * Wrapper method to perform the Kruskal-Wallis test in R. The
	 * Kruskal-Wallis test is a non-parametric alternative to ANOVA. It tests
	 * whether >2 samples originate from the same distribution. Null hypothesis:
	 * there is no entity in the group for which the performance is
	 * significantly different from the others.
	 * 
	 * @param values
	 *            Two-dimensional double array with >2 samples of performance
	 *            measures. E.g. columns=different models, rows=F-measures from
	 *            k-fold-cross-validation. Samples of unequal size must be
	 *            filled with NaNs!
	 * @return Instance of class TestResult, comprising p-Values, statistics,
	 *         the method applied, etc. and a method for human-friendly printing
	 */
	public TestResult testKruskalWallis(double[][] values) {

		if (values.length < 3) {
			String error = "Less than three samples given to Kruskal-Wallis-test. Please check input or use a more suitable test.";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		double[] v = flattenArray(values);
		TestResult result = null;

		try {
			// Pass array and 2d-array dimensions to R
			engine.assign("v", v);
			engine.assign("dimensions", new int[] { values.length, values[0].length });

			// Transform it into a matrix in R, as required by the following
			// methods
			engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");

			// Transform matrix into list, because the KW-test requires a list
			// if no factors are specified
			engine.eval("l <- as.list(data.frame(m))");

			// Perform KW-test on matrix
			engine.eval("res<-kruskal.test(l)");
			result = (TestResult) toTestResult("res");
			result.setStatisticType("H");

		} catch (Exception e) {
			String error = "Exception while performing the Kruskal-Wallis test in R:" + e + " Returning null!";
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;

	}

	/**
	 * Wrapper method to perform the Friedman test in R. The Friedman test is a
	 * non-parametric statistical test. Similar to the parametric repeated
	 * measures ANOVA, it is used to detect differences in treatments across
	 * multiple test attempts. Null hypothesis: there is no entity in the group
	 * for which the performance is significantly different from the others.
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns). Please
	 *            ensure that data is measured on at least two domains/folds!
	 * @return Instance of class TestResult, comprising p-Values, statistics,
	 *         the method applied, etc. and a method for human-friendly printing
	 */
	public TestResult testFriedman(double[][] values) {

		if (values.length == 0) {
			String error = "No samples for Friedman test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}
		if (values[0].length < 2) {
			String error = "The Friedman test is only suitable for repeated measured. Please provide appropriate input.";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		double[] v = flattenArray(values);
		TestResult result = null;

		try {
			// Pass array and 2d-array dimensions to R
			engine.assign("v", v);
			engine.assign("dimensions", new int[] { values.length, values[0].length });

			// Transform it into a matrix in R, as required by the
			// followin"The Friedman test is only suitable for repeated measured. Please provide appropriate input. Returning null."g
			// methods
			engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");

			// Perform Friedman-test on matrix
			engine.eval("res<-friedman.test(m)");
			result = (TestResult) toTestResult("res");
			result.setStatisticType("Q");

		} catch (Exception e) {
			String error = "Exception while performing the Friedman-test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;

	}

	/**
	 * Wrapper method to perform the Nemenyi test in R The Nemenyi test is a
	 * post-hoc test intended to find the groups of data that differ after a
	 * statistical test of multiple comparisons (such as the Friedman test) has
	 * rejected the null hypothesis. The test makes pair-wise tests of
	 * performance. Null hypothesis: the performance of the pairwise entities is
	 * similar.
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains (columns)
	 * @return Instance of class PairwiseTestResult, comprising pairwise
	 *         p-Values, statistics, the method applied, etc. and a method for
	 *         human-friendly printing
	 */
	public PairwiseTestResult testNemenyi(double[][] values) {

		if (values.length == 0) {
			String error = "No samples for Nemenyi test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		double[] v = flattenArray(values);
		PairwiseTestResult result = null;

		try {
			// Pass array and 2d-array dimensions to R
			engine.assign("v", v);
			engine.assign("dimensions", new int[] { values.length, values[0].length });

			// Transform it into a matrix in R, as required by the following
			// methods
			engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");

			// Initialise Nemenyi library. Load package from CRAN mirror, if
			// required.
			engine.eval("usePackage('PMCMR')");

			// Perform test on matrix (second parameter determines to how many
			// digits the samples are rounded)
			engine.eval("res<-posthoc.friedman.nemenyi.test(m)");
			result = (PairwiseTestResult) toTestResult("res");
			result.setRequiresPValueCorrection(false);
			result.setStatisticType("q");

		} catch (Exception e) {
			String error = "Exception while performing the Nemenyi-test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;
	}

	/**
	 * Wrapper method to perform McNemar's test in R McNemar's test is a
	 * non-parametric alternative to the t-test. The null hypothesis of marginal
	 * homogeneity states that the two marginal probabilities for each outcome
	 * are the same.
	 * 
	 * @param contingencies
	 *            : A two-dimensional contingency table which comprises the
	 *            outcomes of two tests on a sample of n subjects (Test 1
	 *            positive/negative vs. Test 2 positive/negative). Entries
	 *            should be nonnegative integers.
	 * @return Instance of class TestResult, comprising p-Values, statistics,
	 *         the method applied, etc. and a method for human-friendly printing
	 */
	public TestResult testMcNemar(int[][] contingencies) {

		if (contingencies.length == 0) {
			String error = "Empty contingency matrix for McNemar's test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		int[] v = flattenArray(contingencies);
		TestResult result = null;

		try {

			// Pass array and 2d-array dimensions to R
			engine.assign("v", v);
			engine.assign("dimensions", new int[] { contingencies.length, contingencies[0].length });

			// Transform it into a matrix in R, as required by the following
			// methods
			engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");
			engine.eval("res<-mcnemar.test(m, y = NULL, correct = TRUE)");

			result = (TestResult) toTestResult("res");
			result.setStatisticType("\\chi^2");

		} catch (Exception e) {
			String error = "Exception while performing McNemar-test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;
	}

	/**
	 * Wrapper method to perform the Mann-Whitney-U (equivalent to
	 * Mann–Whitney–Wilcoxon, Wilcoxon rank-sum, or
	 * Wilcoxon–Mann–Whitney) test in R A non-parametric alternative to the
	 * t-test. Null hypothesis: two populations are the same. Alternative
	 * hypothesis: a particular population tends to have larger values than the
	 * other.
	 * 
	 * @param x
	 *            : First sample of independent performance measures
	 * @param y
	 *            : Second sample of independent performance measures
	 * @return Instance of class TestResult, comprising p-Values, statistics,
	 *         the method applied, etc. and a method for human-friendly printing
	 */
	public TestResult testMannWhitneyU(double[] x, double[] y) {

		if (x.length == 0 || y.length == 0) {
			String error = "No samples for Mann-Whitney-U test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		TestResult result = null;

		try {

			// Pass array and 2d-array dimensions to R
			engine.assign("x", x);
			engine.assign("y", y);

			engine.eval("res<-wilcox.test(x,y)");

			result = (TestResult) toTestResult("res");
			result.setStatisticType("U");

		} catch (Exception e) {
			String error = "Exception while performing Mann-Whitney-U test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;
	}

	/**
	 * Wrapper method to perform the Wilcoxon Signed-Rank test in R A
	 * non-parametric alternative to the t-test for matched pairs. Null
	 * hypothesis: two populations have the same mean ranks. Alternative
	 * hypothesis: One population differs in its mean rank from the other.
	 * 
	 * @param x
	 *            : First sample of dependent performance measures
	 * @param y
	 *            : Second sample of dependent performance measures
	 * @return Instance of class TestResult, comprising p-Values, statistics,
	 *         the method applied, etc. and a method for human-friendly printing
	 */
	public TestResult testWilcoxonSignedRank(double[] x, double[] y) {

		if (x.length == 0 || y.length == 0) {
			String error = "No samples for Wilcoxon-Signed-Rank test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		TestResult result = null;

		try {

			// Pass arrays to R
			engine.assign("x", x);
			engine.assign("y", y);

			engine.eval("res<-wilcox.test(x,y,paired=TRUE, correct=FALSE)");

			result = (TestResult) toTestResult("res");
			result.setStatisticType("W");

		} catch (Exception e) {
			String error = "Exception while performing Wilcoxon-Signed-Rank test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;
	}

	public PairwiseTestResult testPairwiseWilcoxonSignedRank(double[][] values) {
		return this.testPairwiseWilcoxonSignedRank(values, true);
	}

	/**
	 * Wrapper method to perform the Wilcoxon Signed-Rank test in R for pairwise
	 * comparison of >2 groups.
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns).
	 * @param isBaselineEvaluation
	 *            If true, only compare each group against the first.
	 * @return Instance of class {@link PairwiseTestResult}, comprising pairwise
	 *         p-Values, statistics, the method applied, etc. and a method for
	 *         human-friendly printing
	 */
	public PairwiseTestResult testPairwiseWilcoxonSignedRank(double[][] values, boolean isBaselineEvaluation) {

		if (values.length == 0) {
			String error = "No samples for Pairwise Wilcoxon-Signed-Rank test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		PairwiseTestResult result = null;
		// Prepare arrays
		double[][] statistic = new double[values.length - 1][values.length - 1];
		double[][] pValue = new double[values.length - 1][values.length - 1];
		for (int c = 0; c < values.length - 1; c++) {
			Arrays.fill(pValue[c], Double.NaN);
			Arrays.fill(statistic[c], Double.NaN);
		}

		try {

			if (isBaselineEvaluation) {
				double[] baseline = values[0];
				engine.assign("b", baseline);
				for (int i = 1; i < values.length; i++) {
					double[] v = values[i];
					engine.assign("v", v);
					engine.eval("result<-wilcox.test(b,v,paired=TRUE, correct=FALSE)");
					pValue[i - 1][0] = engine.eval("result$p.value").asDouble();
					statistic[i - 1][0] = engine.eval("result$statistic").asDouble();
				}
			} else {
				for (int c = 0; c < values.length; c++) {
					for (int c1 = c + 1; c1 < values.length; c1++) {
						double[] v = values[c];
						double[] w = values[c1];
						engine.assign("v", v);
						engine.assign("w", w);
						engine.eval("result<-wilcox.test(v, w, paired=TRUE, correct=FALSE)");
						pValue[c1 - 1][c] = engine.eval("result$p.value").asDouble();
						statistic[c1 - 1][c] = engine.eval("result$statistic").asDouble();
					}
				}
			}

			result = new PairwiseTestResult("Pairwise Wilcoxon Signed-Rank test", new HashMap<String, Double>(), pValue, statistic);
			result.setRequiresPValueCorrection(true);
			result.setStatisticType("W");

		} catch (Exception e) {
			String error = "Exception while performing pairwise Wilcoxon signed-rank test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;

	}

	/**
	 * Wrapper method to perform a pairwise independent t-Test in R
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns). Please
	 *            ensure that data is measured on at least two domains/folds!
	 * @return Instance of class PairwiseTestResult, comprising pairwise
	 *         p-Values, statistics, the method applied, etc. and a method for
	 *         human-friendly printing
	 */
	public PairwiseTestResult testPairwiseIndependentT(double[][] values) {
		return testPairwiseT(values, false);
	}

	/**
	 * Wrapper method to perform a pairwise dependend t-Test in R
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns). Please
	 *            ensure that data is measured on at least two domains/folds!
	 * @return Instance of class PairwiseTestResult, comprising pairwise
	 *         p-Values, statistics, the method applied, etc. and a method for
	 *         human-friendly printing
	 */
	public PairwiseTestResult testPairwiseDependentT(double[][] values) {
		return testPairwiseT(values, true);
	}

	/**
	 * Wrapper method to perform the pairwise t-Test in R Corresponds to
	 * performing multiple t-tests to compare the means of pairwise samples.
	 * Null hypothesis: there are no statistically significant differences in
	 * the means of a pairwise sample.
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns). Please
	 *            ensure that data is measured on at least two domains/folds!
	 * @param paired
	 *            Specified whether individual samples should be treated as
	 *            paired (independent/dependent)
	 * @return Instance of class {@link PairwiseTestResult}, comprising pairwise
	 *         p-Values, statistics, the method applied, etc. and a method for
	 *         human-friendly printing
	 */
	public PairwiseTestResult testPairwiseT(double[][] values, boolean paired) {

		if (values.length == 0) {
			String error = "No samples for Pairwise t-test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		double[] v = flattenArray(values);
		PairwiseTestResult result = null;

		try {

			// Initialize required libraries
			engine.eval("usePackage('reshape2')");

			// Pass array and 2d-array dimensions to R
			engine.assign("v", v);
			engine.assign("dimensions", new int[] { values.length, values[0].length });

			// Add column with fold indices
			engine.eval("folds<-c(1:dimensions[2])");
			engine.eval("v<-c(folds,v)");
			engine.eval("dimensions[1]<-dimensions[1]+1");

			// Transform it into a matrix and then into a data frame
			engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");
			engine.eval("df <- as.data.frame(m)");
			engine.eval("names(df)<-c('Fold',1:(dimensions[1]-1))");

			// check pairwise normality assumption
			double[][] statistic = new double[values.length - 1][values.length - 1];
			double[][] pValue = new double[values.length - 1][values.length - 1];
			for (int c = 0; c < values.length - 1; c++) {
				Arrays.fill(pValue[c], Double.NaN);
				Arrays.fill(statistic[c], Double.NaN);
			}
			for (int c = 0; c < values.length; c++) {
				for (int c1 = c + 1; c1 < values.length; c1++) {
					engine.eval(String.format("diff<-df[%d]-df[%d]", c + 2, c1 + 2));
					if (engine.eval("res<-shapiro.test(diff[,1])") != null) {
						TestResult normalityTest = (TestResult) toTestResult("res");
						pValue[c1 - 1][c] = normalityTest.getpValue();
						statistic[c1 - 1][c] = normalityTest.getStatistic();
					}
				}
			}
			PairwiseTestResult normality = new PairwiseTestResult("Shapiro-Wilk normality test", null, pValue, statistic);
			normality.setStatisticType("W");

			// Transform data frame to long format and set models as grouping
			// factor
			engine.eval("df<-melt(df,id='Fold')");
			engine.eval("names(df)<-c('Fold','model','Performance')");
			engine.eval("df$model <- factor(df$model, labels = c(1:1:(dimensions[1]-1)))");

			// Perform pairwise t-test without corrections (these can be applied
			// later)
			engine.eval("res <- pairwise.t.test(df$Performance,df$model,paired=TRUE,p.adjust.method='none')");

			result = (PairwiseTestResult) toTestResult("res");
			result.getAssumptions().put("Normality", normality);
			result.setRequiresPValueCorrection(true);
			result.setStatisticType("t");

		} catch (Exception e) {
			String error = "Exception while performing the pairwise T-test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;

	}

	/**
	 * Wrapper method to perform the dependent t-test (paired-samples t-test) in
	 * R A parametric test which compares the means of two related groups to
	 * detect whether there are any statistically significant differences
	 * between these means. Null hypothesis: there are no statistically
	 * significant differences in means.
	 * 
	 * @param x
	 *            : First sample of dependent performance measures
	 * @param y
	 *            : Second sample of dependent performance measures
	 * @return Instance of class TestResult, comprising p-Values, statistics,
	 *         the method applied, etc. and a method for human-friendly printing
	 */
	public TestResult testDependentT(double[] x, double[] y) {

		if (x.length == 0 || y.length == 0) {
			String error = "No samples for dependend t-test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		TestResult result = null;
		TestResult normalityTest = null;

		try {
			// Pass arrays to R
			engine.assign("x", x);
			engine.assign("y", y);

			// Check if assumptions for this parametric test (normality of
			// differences) are met and store result in parent test
			engine.eval("diff<-x-y");
			if (engine.eval("res<-shapiro.test(diff)") != null) {
				normalityTest = (TestResult) toTestResult("res");
				normalityTest.setStatisticType("W");
			}

			engine.eval("res<-t.test(x,y,paired=TRUE)");
			result = (TestResult) toTestResult("res");
			result.getAssumptions().put("Normality", normalityTest);
			result.setStatisticType("t");

		} catch (Exception e) {
			String error = "Exception while performing dependent t-test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			// e.printStackTrace();
		}

		return result;

	}

	/**
	 * Wrapper method to perform the Repeated-Measures One-Way ANOVA in R A
	 * parametric test generalizing the dependent t-test to more than two
	 * groups. Null hypothesis: there is no entity in the group for which the
	 * performance is significantly different from the others.
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns). Please
	 *            ensure that data is measured on at least two domains/folds!
	 * @return Instance of class {@link TestResult}, comprising p-Values,
	 *         statistics, the method applied, etc. and a method for
	 *         human-friendly printing. The corrected p-values according to
	 *         Greenhouse-Geissner and Huynh-Feldt are comprised in the
	 *         parameters map.
	 */
	public TestResult testRepeatedMeasuresOneWayANOVA(double[][] values) {

		if (values.length == 0) {
			String error = "No samples for Repeated Measures One-Way ANOVA. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		double[] v = flattenArray(values);
		TestResult result = null;
		
		try{
			//Initialize required libraries
	        engine.eval("usePackage('reshape2')");
	        engine.eval("usePackage('ez')");
	        
	        //Set sum-to-zero convention for effect weights
	        engine.eval("options(contrasts=c('contr.sum','contr.poly'))");
	        
	        // Pass array and 2d-array dimensions to R
	        engine.assign("v", v);
	        engine.assign("dimensions", new int[]{values.length,values[0].length});
	        
	        // Transform it into a matrix in R, then transform the matrix into a data frame
	        engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");
	        engine.eval("df <- as.data.frame(m)");
	        engine.eval("names(df)<-c(1:dimensions[1])");
	        engine.eval("df<-melt(df)");
	        engine.eval("df[3]<-factor(c(1:dimensions[2]))");	        
	        engine.eval("names(df)<-c('Model','Performance','Dataset')");

	        //Perform ANOVA with the ezANOVA package
	        engine.eval("model <- ezANOVA(data=df, dv=.(Performance), wid=.(Dataset),within=.(Model), detailed=TRUE, type=3)");

	        // Fix names to ease extraction of data
	        engine.eval("names(model)<-c('ANOVA','Mauchlys','SphericityCorrections')");
	        
	        // Extract TestResult manually, because result data does not conform to any common class
	        double p = getDoubleOrNaN(engine.eval("get('ANOVA', model)$p[2]"));
	        double pMauchly = getDoubleOrNaN(engine.eval("get('Mauchlys', model)$p"));
	        double pCorrectedGreenhouseGeissner = getDoubleOrNaN(engine.eval("get('p[GG]',get('SphericityCorrections',model))"));
	        double pCorrectedHuynhFeldt = getDoubleOrNaN(engine.eval("get('p[HF]',get('SphericityCorrections',model))"));
	        double statistic = getDoubleOrNaN(engine.eval("get('ANOVA', model)$F[2]"));
	        double df = getDoubleOrNaN(engine.eval("get('ANOVA',model)$DFn[2]"));
	        String method = "Repeated Measures One-Way ANOVA";
	     	HashMap<String,Double> parameter = new HashMap<String,Double>();
			parameter.put("df", df);
			
			HashMap<String,Double> sphericityParameters = new HashMap<String,Double>();
			sphericityParameters.put("p_{GG}",pCorrectedGreenhouseGeissner);
			sphericityParameters.put("p_{HF}",pCorrectedHuynhFeldt);
			TestResult testSphericity = new TestResult("Mauchly's test", sphericityParameters, pMauchly, 0);
			testSphericity.setStatisticType("\\sigma^2");
			
	        result=new TestResult(method,parameter,p,statistic);
	        result.getAssumptions().put("Sphericity", testSphericity);
	        result.setStatisticType("F");
	        
		} catch (Exception e) {
			String error = "Exception while performing the Repeated-Measures One-Way ANOVA in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
		}

		return result;

	}

	/**
	 * Wrapper method to perform Dunett's test after a Repeated-Measures One-Way
	 * ANOVA in R A parametric post-hoc test after ANOVA for comparing different
	 * items against the first (the baseline).
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns). The first
	 *            item represents the baseline to be evaluated against. Please
	 *            ensure that data is measured on at least two domains/folds!
	 * @return Instance of class {@link PairwiseTestResult}, comprising
	 *         p-Values, statistics, the method applied, etc. and a method for
	 *         human-friendly printing.
	 */
	public PairwiseTestResult testDunett(double[][] values) {

		if (values.length == 0) {
			String error = "No samples for Dunett's test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		double[] v = flattenArray(values);
		PairwiseTestResult result = null;

		try {
			// Initialize required libraries
			engine.eval("usePackage('reshape2')");
			engine.eval("usePackage('nlme')");
			engine.eval("usePackage('multcomp')");

			// Set sum-to-zero convention for effect weights
			engine.eval("options(contrasts=c('contr.sum','contr.poly'))");

			// Pass array and 2d-array dimensions to R
			engine.assign("v", v);
			engine.assign("dimensions", new int[] { values.length, values[0].length });

			// Transform it into a matrix in R, then transform the matrix into a
			// data frame
			engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");
			engine.eval("df <- as.data.frame(m)");
			engine.eval("names(df)<-c(1:dimensions[1])");
			engine.eval("df<-melt(df)");
			engine.eval("df[3]<-factor(c(1:dimensions[2]))");
			engine.eval("names(df)<-c('Model','Performance','Dataset')");

			// Create linear mixed-effects model
			engine.eval("model <- lme(Performance ~ Model, random = ~1|Dataset/Model,data=df)");

			// Perform post-hoc test
			engine.eval("result<-summary(glht(model,linfct=mcp(Model='Dunnett')))");

			// Transfer results to Java data types
			// p-values: 2-1,3-1,...,n-1;3-2,3-3;4-3
			double[] p = engine.eval("get('pvalues',get('test', result))").asDoubleArray();
			double[] s = engine.eval("get('tstat',get('test', result))").asDoubleArray();
			double[][] pValue = new double[values.length - 1][values.length - 1];
			double[][] statistic = new double[values.length - 1][values.length - 1];

			for (int c = 0; c < pValue.length; c++) {
				Arrays.fill(pValue[c], Double.NaN);
				Arrays.fill(statistic[c], Double.NaN);
			}

			for (int c = 0; c < pValue.length; c++) {
				pValue[c][0] = p[c];
				statistic[c][0] = s[c];
			}

			String method = "Dunnett's test";
			result = new PairwiseTestResult(method, new HashMap<String, Double>(), pValue, statistic);
			result.setStatisticType("t");

		} catch (Exception e) {
			String error = "Exception while performing Dunnett's test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
		}

		return result;
	}

	/**
	 * Wrapper method to perform Tukey's test after a Repeated-Measures One-Way
	 * ANOVA in R A parametric post-hoc test after ANOVA for pairwise n:n
	 * comparisons.
	 * 
	 * @param values
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns). Please
	 *            ensure that data is measured on at least two domains/folds!
	 * @return Instance of class {@link PairwiseTestResult}, comprising
	 *         p-Values, statistics, the method applied, etc. and a method for
	 *         human-friendly printing.
	 */
	public PairwiseTestResult testTukey(double[][] values) {

		if (values.length == 0) {
			String error = "No samples for Tukey's test. Please check!";
			logger.log(Level.ERROR, error);
			System.err.println(error);
			return null;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		double[] v = flattenArray(values);
		PairwiseTestResult result = null;

		try {
			// Initialize required libraries
			engine.eval("usePackage('reshape2')");
			engine.eval("usePackage('nlme')");
			engine.eval("usePackage('multcomp')");

			// Set sum-to-zero convention for effect weights
			engine.eval("options(contrasts=c('contr.sum','contr.poly'))");

			// Pass array and 2d-array dimensions to R
			engine.assign("v", v);
			engine.assign("dimensions", new int[] { values.length, values[0].length });

			// Transform it into a matrix in R, then transform the matrix into a
			// data frame
			engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");
			engine.eval("df <- as.data.frame(m)");
			engine.eval("names(df)<-c(1:dimensions[1])");
			engine.eval("df<-melt(df)");
			engine.eval("df[3]<-factor(c(1:dimensions[2]))");
			engine.eval("names(df)<-c('Model','Performance','Dataset')");

			// Create linear mixed-effects model
			engine.eval("model2 <- lme(Performance ~ Model, random = ~1|Dataset/Model,data=df)");

			// Perform post-hoc test
			engine.eval("result<-summary(glht(model2,linfct=mcp(Model='Tukey')))");

			// Transfer results to Java data types
			engine.eval("get('pvalues',get('test', result))");
			double[] p = engine.eval("get('pvalues',get('test', result))").asDoubleArray();
			double[] s = engine.eval("get('tstat',get('test', result))").asDoubleArray();
			double[][] pValue = new double[values.length - 1][values.length - 1];
			double[][] statistic = new double[values.length - 1][values.length - 1];

			for (int c = 0; c < pValue.length; c++) {
				Arrays.fill(pValue[c], Double.NaN);
				Arrays.fill(statistic[c], Double.NaN);
			}

			int i = 0;
			for (int c = 0; c < pValue.length; c++) {
				for (int c1 = c + 1; c1 <= pValue.length; c1++) {
					pValue[c1 - 1][c] = p[i];
					statistic[c1 - 1][c] = s[i];
					i++;
				}
			}

			String method = "Tukey's test";
			result = new PairwiseTestResult(method, new HashMap<String, Double>(), pValue, statistic);
			result.setStatisticType("t");

		} catch (Exception e) {
			String error = "Exception while performing Tukey's test in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
		}

		return result;
	}

	/**
	 * Try to cast R expression to double. If not possible, return NaN
	 * 
	 * @param exp
	 *            an R expression
	 * @return either an ordinary double or Double.NaN, if conversion failed.
	 */
	private double getDoubleOrNaN(REXP exp) {
		try {
			if (exp.asDouble() == 0) {
				return exp.asInt();
			}
			return exp.asDouble();
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/**
	 * Wrapper method to adjust the p-values from multiple comparisons.
	 * 
	 * @param result
	 *            The resulting PairwiseTestResult from a multiple comparisons
	 *            test
	 * @param method
	 *            The method to be applied e.g. "bonferroni" for a conservative
	 *            adjustment
	 * @return an updated copy of PairwiseTestResult with adjusted p-values
	 */
	public double[][] adjustP(PairwiseTestResult result, StatsConfigConstants.CORRECTION_VALUES method) {

		// Extract p-values and flatten array, in order to pass to R
		double[] v = flattenArray(result.getpValue());

		try{
			
	        // Pass array and dimensions of origin matrix to R
	        engine.assign("v", v);
	        engine.assign("dimensions", new int[]{result.getpValue().length,result.getpValue()[0].length});
	        
	        engine.eval(String.format("adjusted <- p.adjust(v,method='%s')",method.name()));
			double[][] adjustedP = engine.eval("t(matrix(adjusted,nrow=dimensions[2]))").asDoubleMatrix();
			
			//Clone former result and change p-values with adjusted ones
			return adjustedP;

		} catch (Exception e) {
			String error = "Exception while adjusting pairwise p-values in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			return null;
		}

	}

	/**
	 * Wrapper method to create a qqplot in R, comparing the given samples to a
	 * normal distribution
	 * 
	 * @param samples
	 *            The samples to compare against a normal
	 * @param filename
	 *            The path where the plot should be stored
	 * @param model
	 *            The name of the model that generated the samples
	 * @param measure
	 *            The name of the performance measure the samples represent
	 * @return True if plotting and saving to file succeeded, false otherwise.
	 */
	public boolean plotQQNorm(double[] samples, String model, String measure, String filename) {

		try {
			// Pass array to R
			engine.assign("samples", samples);
			engine.eval(String.format("png(file='%s.png')", filename));
			engine.eval(String.format("qqnorm(samples, xlab='%s', ylab='%s')", model, measure));
			engine.eval("qqline(samples)");
			engine.eval("dev.off()");
		} catch (Exception e) {
			String error = "Exception while plotting QQ-Normal-plot in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			return false;
		}
		return true;
	}

	/**
	 * Wrapper method to create a Box-whisker diagram in R, comparing samples
	 * for different models
	 * 
	 * @param s
	 *            Individual performance results of different items (e.g. models
	 *            -> rows) over different domains or folds (columns). Please
	 *            ensure that data is measured on at least two domains/folds!
	 * @param min Minimum value on the y axis
	 * @param max Maximum value on the y axis
	 * @param filename
	 *            The path where the plot should be stored
	 * @param measure
	 *            The name of the performance measure the samples represent
	 * @return True if plotting and saving to file succeeded, false otherwise.
	 **/
	public boolean plotBoxWhisker(double[][] s, int min, int max, String filename, String measure) {

		if (s.length == 0) {
			System.err.println("Empty input matrix. Please check! Returning empty string.");
			return false;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		double[] v = flattenArray(s);

		try {
			engine.eval("usePackage('reshape2')");
			engine.eval("usePackage('ggplot2')");

			// Pass array and 2d-array dimensions to R
			engine.assign("v", v);
			engine.assign("dimensions", new int[] { s.length, s[0].length });

			// Transform it into a matrix in R, then transform the matrix into a
			// data frame
			engine.eval("m<-matrix(v,nrow = dimensions[2],ncol = dimensions[1])");
			engine.eval("df <- as.data.frame(m)");

			// Create dataframe head
			StringBuilder head = new StringBuilder();
			String sep = "";
			for (int i = 0; i < s.length; i++) {
				head.append(String.format("%s'M%d'", sep, i));
				sep = ",";
			}

			engine.eval(String.format("names(df)<-c(%s)", head));
			engine.eval("df<-melt(df)");
			engine.eval("df$variable <- factor(df$variable)");

			engine.eval(String.format("png(file='%s.png', height=600, width=1000, units='px')", filename));
			engine.eval(String.format("print(ggplot(df, aes(factor(variable), value)) + geom_boxplot() + stat_summary(fun.y=mean, colour='darkred', geom='point', shape=18, size=3,show_guide = FALSE) + theme(axis.title.x=element_blank())+scale_y_continuous(name='%s',limits=c(%d,%d)))",measure,min,max));
			engine.eval("dev.off()");

			/*
			 * engine.eval(String.format(
			 * "p<-ggplot(df, aes(factor(variable), value)) + geom_boxplot() + stat_summary(fun.y=mean, colour='darkred', geom='point', shape=18, size=3,show_guide = FALSE) + theme(axis.title.x=element_blank())+scale_y_continuous(name='%s',limits=c(0,1))"
			 * ,measure));
			 * engine.eval(String.format("ggsave('%s.png',plot=p,height=6,width=10)"
			 * ,filename));
			 */
		} catch (Exception e) {
			String error = "Exception while plotting a Box-Whisker-diagram in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			return false;
		}

		return true;
	}

	/**
	 * Wrapper-method for plotting a graph using R
	 * 
	 * @param edgelist
	 *            a 2-dimensional integer array representing edges. Each
	 *            subarray contains two numbers, indicating start- and endpoint
	 *            of one edge.
	 * @param filename
	 *            The place where the plot should be stored
	 * @return indicates whether the plot was successful or not
	 */
	public boolean plotGraph(int[][] edgelist, int nModels, String filename) {

		if (edgelist == null) {
			System.err.println("Empty input matrix. Please check! Returning empty string.");
			return false;
		}

		int[] vertices = new int[nModels];
		for (int i = 0; i < nModels; i++) {
			vertices[i] = i;
		}

		// Only one-dimensional arrays can be passed to R, thus flattening it
		// here and unfolding it within R again
		int[] flattenedEdges = flattenArray(edgelist);

		try {
			engine.eval("usePackage('igraph')");

			// Pass vertices to R
			engine.assign("vertices", vertices);

			if (flattenedEdges.length > 0) {
				// Pass edgelist and 2d-edgelist dimensions to R
				engine.assign("edgelist", flattenedEdges);
				engine.assign("dimensions", new int[] { 2, flattenedEdges.length / 2 });

				// Determine vertices that are not in the edgelist
				engine.eval("vertices<-setdiff(vertices,edgelist)");

				// Transform edgelist into a matrix in R
				engine.eval("edgelist<-matrix(edgelist,nrow = dimensions[2],ncol = dimensions[1])");

				// Create graph with edges, then add remaining nodes
				engine.eval("g <- graph.data.frame(edgelist)");
				engine.eval("g<-g + vertices(vertices)");
			} else {
				engine.eval("g <- graph.empty() + vertices(vertices)");
			}

			// Plot graph and save file
			engine.eval(String.format("png(file='%s.png')", filename));
			engine.eval("plot.igraph(g)");
			engine.eval("dev.off()");

		} catch (Exception e) {
			String error = "Exception while plotting a graph in R:" + e;
			logger.log(Level.ERROR, error);
			System.out.println(error);
			return false;
		}

		return true;
	}

	/**
	 * Flattens a 2-dimensional double array into a one-dimensional one
	 * 
	 * @param inputArray
	 *            a 2-dimensional double array
	 * @return a one-dimensional double array comprising all values from the
	 *         2-dimensional input array
	 */
	private double[] flattenArray(double[][] inputArray) {

		int totalSize = 0;
		for (double[] subArray : inputArray) {
			totalSize += subArray.length;
		}

		double[] flat = new double[totalSize];
		int c = 0;
		for (double[] subArray : inputArray) {
			for (double element : subArray) {
				flat[c] = element;
				c++;
			}
		}

		return flat;
	}

	/**
	 * Flattens a 2-dimensional integer array into a one-dimensional one
	 * 
	 * @param inputArray
	 *            a 2-dimensional integer array
	 * @return a one-dimensional integer array comprising all values from the
	 *         2-dimensional input array
	 */
	private int[] flattenArray(int[][] inputArray) {

		int totalSize = 0;
		for (int[] subArray : inputArray) {
			totalSize += subArray.length;
		}

		int[] flat = new int[totalSize];
		int c = 0;
		for (int[] subArray : inputArray) {
			for (int element : subArray) {
				flat[c] = element;
				c++;
			}
		}

		return flat;
	}

	/**
	 * Listener class to transfer the console output of the R environment to
	 * Java
	 * 
	 * @author Guckelsberger
	 */
	private class CallbackListener implements RMainLoopCallbacks {

		public void rWriteConsole(Rengine re, String text, int otype) {
			System.out.print(text);
		}

		public void rBusy(Rengine re, int which) {
			System.out.println("rBusy(" + which + ")");
		}

		public String rChooseFile(Rengine arg0, int arg1) {
			return null;
		}

		public void rFlushConsole(Rengine arg0) {
		}

		public void rLoadHistory(Rengine arg0, String arg1) {
		}

		public String rReadConsole(Rengine arg0, String arg1, int arg2) {
			return null;
		}

		public void rSaveHistory(Rengine arg0, String arg1) {
		}

		public void rShowMessage(Rengine arg0, String arg1) {
		}
	}

}
