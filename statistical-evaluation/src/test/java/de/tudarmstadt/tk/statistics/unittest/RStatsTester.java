package de.tudarmstadt.tk.statistics.unittest;

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

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.tk.statistics.config.StatsConfigConstants;
import de.tudarmstadt.tk.statistics.test.PairwiseTestResult;
import de.tudarmstadt.tk.statistics.test.Statistics;
import de.tudarmstadt.tk.statistics.test.TestResult;

/**
 * Test cases for performing statistical tests in R
 * 
 * @author Guckelsberger
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
public class RStatsTester {

	private final double EPSILON = 0.001;

	@Test
	public void testPlotting() {
		Statistics stats = Statistics.getInstance(true);
		stats.plotGraph(new int[][] { { 1, 2, 3, 4, 5, 6 }, { 2, 3, 4, 5, 6, 1 } }, 8, "graph");
		stats.plotQQNorm(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8 }, "M3", "F-Measure", "qqPlot");
		stats.plotBoxWhisker(new double[][] { { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8 }, { 0.3, 0.4, 0.5, 0.7, 0.6, 0.3, 0.4 }, { 0.2, 0.3, 0.5, 0.7 } }, 0, 1, "boxWhiskerPlot", "F-Measure");
	}

	@Test
	public void testFriedman() {

		/*
		 * Example from: Japkowicz/Shah (2011), Evaluating Learning Algorithms.
		 * Pages 255 ff. Individual performance results of different classifiers
		 * (rows) over different domains (columns)
		 */
		double[] classifierA = { 85.83, 85.91, 86.12, 85.82, 86.28, 86.42, 85.91, 86.10, 85.95, 86.12 };
		double[] classifierB = { 75.86, 73.18, 69.08, 74.05, 74.71, 65.90, 76.25, 75.10, 70.50, 73.95 };
		double[] classifierC = { 84.19, 85.91, 83.83, 85.11, 86.38, 81.20, 86.38, 86.75, 88.03, 87.18 };
		double[][] classifiers = { classifierA, classifierB, classifierC };

		Statistics stats = Statistics.getInstance(true);
		TestResult r = stats.testFriedman(classifiers);

		Assert.assertEquals(0.0005531, r.getpValue(), EPSILON);
	}

	@Test
	public void testNemenyi() {

		/*
		 * Example from: Japkowicz/Shah (2011), Evaluating Learning Algorithms.
		 * Pages 255 ff. Individual performance results of different classifiers
		 * (rows) over different domains (columns)
		 */
		double[] classifierA = { 85.83, 85.91, 86.12, 85.82, 86.28, 86.42, 85.91, 86.10, 85.95, 86.12 };
		double[] classifierB = { 75.86, 73.18, 69.08, 74.05, 74.71, 65.90, 76.25, 75.10, 70.50, 73.95 };
		double[] classifierC = { 84.19, 85.91, 83.83, 85.11, 86.38, 81.20, 86.38, 86.75, 88.03, 87.18 };
		double[][] classifiers = { classifierA, classifierB, classifierC };

		Statistics stats = Statistics.getInstance(true);
		PairwiseTestResult r = stats.testNemenyi(classifiers);

		double[][] actual = r.getpValue();

		try {
			Assert.assertTrue(actual[0][0] <= 0.05);
			Assert.assertTrue(actual[1][0] > 0.05);
			Assert.assertTrue(actual[1][1] <= 0.05);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("P-value-matrix in Namenyi-test result have different format than expected!");
		}

	}

	@Test
	public void testNemenyi2() {

		/*
		 * Example from: Zar, J. H. (2010), Biostatistical Analysis, Pages 216,
		 * 241. Distribution of flies (rows) in three different vegetation
		 * layers (columns)
		 */
		double[] herbs = { 14, 12.1, 9.6, 8.2, 10.2 };
		double[] shrubs = { 8.4, 5.1, 5.5, 6.6, 6.3 };
		double[] trees = { 6.9, 7.3, 5.8, 4.1, 5.4 };
		double[][] flies = { herbs, shrubs, trees };

		Statistics stats = Statistics.getInstance(true);
		PairwiseTestResult r = stats.testNemenyi(flies);
		double[][] actual = r.getpValue();

		try {
			Assert.assertTrue(actual[0][0] <= 0.1);
			Assert.assertTrue(actual[1][0] <= 0.05);
			Assert.assertTrue(actual[1][1] > 0.05);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("P-value-matrix in Namenyi-test result have different format than expected!");
		}

	}

	@Test
	public void testMcNemar() {

		/*
		 * Example from: Japkowicz/Shah (2011), Evaluating Learning Algorithms.
		 * Pages 226 ff.
		 */
		int[][] contingencies = { { 4, 11 }, { 2, 40 } };

		Statistics stats = Statistics.getInstance(true);
		TestResult r = stats.testMcNemar(contingencies);

		Assert.assertEquals(0.0265, r.getpValue(), EPSILON);

	}

	@Test
	public void testKruskalWallis() {

		/*
		 * Example from: Hollander & Wolfe (1973), 116. Mucociliary efficiency
		 * from the rate of removal of dust in normal subjects, subjects with
		 * obstructive airway disease, and subjects with asbestosis.
		 */

		double[] sampleA = { 2.9, 3.0, 2.5, 2.6, 3.2 }; // normal subjects
		double[] sampleB = { 3.8, 2.7, 4.0, 2.4, Double.NaN }; // with
																// obstructive
																// airway
																// disease
		double[] sampleC = { 2.8, 3.4, 3.7, 2.2, 2.0 }; // with asbestosis
		double[][] samples = { sampleA, sampleB, sampleC };

		Statistics stats = Statistics.getInstance(true);
		TestResult r = stats.testKruskalWallis(samples);

		Assert.assertEquals(0.68, r.getpValue(), EPSILON);
	}

	@Test
	public void testWilcoxonSignedRank() {

		/*
		 * Example from: Field/Miles/Field (2012), 655ff. (Made up) depressant
		 * effects of ecstasy consumed on different days
		 */

		double[] sampleA = { 15, 35, 16, 18, 19, 17, 27, 16, 13, 20 }; // on a
																		// sunday
		double[] sampleB = { 28, 35, 35, 24, 39, 32, 27, 29, 36, 35 }; // on a
																		// monday

		Statistics stats = Statistics.getInstance(true);
		TestResult r = stats.testWilcoxonSignedRank(sampleA, sampleB);

		Assert.assertEquals(0.01151, r.getpValue(), EPSILON);
	}

	@Test
	public void testMannWhitneyU() {

		/*
		 * Example from: Field/Miles/Field (2012), 655ff. (Made up) depressant
		 * effects of recreational drugs (ecstasy/alcohol)
		 */

		double[] sampleA = { 15, 35, 16, 18, 19, 17, 27, 16, 13, 20 }; // ecstasy
		double[] sampleB = { 16, 15, 20, 15, 16, 13, 14, 19, 18, 18 }; // alcohol

		Statistics stats = Statistics.getInstance(true);
		TestResult r = stats.testMannWhitneyU(sampleA, sampleB);

		Assert.assertEquals(0.2861, r.getpValue(), EPSILON);
	}

	@Test
	public void testDependentT() {

		/*
		 * Example from: Field/Miles/Field (2012), p. 389 ff. (Made up) anxiety
		 * raised by real and pictures of spiders
		 */

		double[] sampleA = { 30, 35, 45, 40, 50, 35, 55, 25, 30, 45, 40, 50 }; // real
		double[] sampleB = { 40, 35, 50, 55, 65, 55, 50, 35, 30, 50, 60, 39 }; // pictures

		Statistics stats = Statistics.getInstance(true);
		TestResult r = stats.testDependentT(sampleA, sampleB);

		Assert.assertEquals(0.03098, r.getpValue(), EPSILON);
	}

	@Test
	public void testRepeatedMeasuresOneWayANOVA() {

		/*
		 * Example from: Field/Miles/Field (2012), p. 563 ff. (Made up) time to
		 * retch after consuming different food types
		 */

		double[] sampleA = { 8, 9, 6, 5, 8, 7, 10, 12 }; // Stick insect
		double[] sampleB = { 7, 5, 2, 3, 4, 5, 2, 6 }; // Kangaroo testicle
		double[] sampleC = { 1, 2, 3, 1, 5, 6, 7, 8 }; // Fish eye
		double[] sampleD = { 6, 5, 8, 9, 8, 7, 2, 1 }; // Witchetty grub
		double[][] samples = { sampleA, sampleB, sampleC, sampleD };

		Statistics stats = Statistics.getInstance(true);
		TestResult r = stats.testRepeatedMeasuresOneWayANOVA(samples);

		Assert.assertEquals(0.025570, r.getpValue(), EPSILON);
	}

	@Test
	public void testPairwiseT() {

		/*
		 * Example from: Field/Miles/Field (2012), p. 563 ff. (Made up) time to
		 * retch after consuming different food types
		 */

		double[] sampleA = { 8, 9, 6, 5, 8, 7, 10, 12 }; // Stick insect
		double[] sampleB = { 7, 5, 2, 3, 4, 5, 2, 6 }; // Kangaroo testicle
		double[] sampleC = { 1, 2, 3, 1, 5, 6, 7, 8 }; // Fish eye
		double[] sampleD = { 6, 5, 8, 9, 8, 7, 2, 1 }; // Witchetty grub
		double[][] samples = { sampleA, sampleB, sampleC, sampleD };

		Statistics stats = Statistics.getInstance(true);
		PairwiseTestResult r = stats.testPairwiseDependentT(samples);

		double[][] expected = { { 0.00202 }, { 0.00094, 0.92007 }, { 0.22673, 0.29867, 0.40204 } };
		double[][] actual = r.getpValue();

		try {
			for (int i = 0; i < expected.length; i++) {
				for (int j = 0; j < expected[i].length; j++) {
					Assert.assertEquals(expected[i][j], actual[i][j], EPSILON);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("P-value-matrix in pairwise T-test result has different format than expected!");
		}
	}

	@Test
	public void testAdjustP() {

		/*
		 * Analogous to testPairwiseT, but result is used to test p-value
		 * correction with bonferroni adjustment Example from: Field/Miles/Field
		 * (2012), p. 563 ff. (Made up) time to retch after consuming different
		 * food types
		 */

		double[] sampleA = { 8, 9, 6, 5, 8, 7, 10, 12 }; // Stick insect
		double[] sampleB = { 7, 5, 2, 3, 4, 5, 2, 6 }; // Kangaroo testicle
		double[] sampleC = { 1, 2, 3, 1, 5, 6, 7, 8 }; // Fish eye
		double[] sampleD = { 6, 5, 8, 9, 8, 7, 2, 1 }; // Witchetty grub
		double[][] samples = { sampleA, sampleB, sampleC, sampleD };

		Statistics stats = Statistics.getInstance(true);
		PairwiseTestResult r = stats.testPairwiseDependentT(samples);

		double[][] actual = stats.adjustP(r, StatsConfigConstants.CORRECTION_VALUES.bonferroni);
		double[][] expected = { { 0.0121 }, { 0.0056, 1 }, { 1, 1, 1 } };

		try {
			for (int i = 0; i < expected.length; i++) {
				for (int j = 0; j < expected[i].length; j++) {
					Assert.assertEquals(expected[i][j], actual[i][j], EPSILON);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("P-value-matrix in p-value adjustment has different format than expected!");
		}

	}

}
