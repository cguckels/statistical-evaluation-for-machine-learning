# Extending STATS\_ML by new tests #

This framework is highly extensible thanks to Java’s Reflection facilities. New statistical tests can be added by following these steps:

1. Add the new test, either as a wrapper method including R code or a as a native implementation, to the class _Statistics_. The method has to adhere to the following rules:
  * The name of the test method must start with “test”, followed by the test’s name.
  * An omnibus test must return an object of type _TestResult_, while a post- hoc test must return an object of class _PairwiseTestResult_. While the former can only represent a single p-value, statistic, etc., the latter can store several p-values from multiple comparisons. Tests in R often return objects of type htest or pairwise.htest. The method _AbstractTestResult toTestResult(String resourceName)_ can be used to translate these objects into their Java counterparts, by first transforming them into an object of class _AbstractTestResult_ and then casting them to either _TestResult_ or _PairwiseTestResult_.
  * The method must return null if the test fails.
  * Post-hoc tests must indicate in their results whether p-value corrections still have to be performed. For this, you can use the method _setRequiresPValueCorrection(bool requires)_ on the test object.
  * Omnibus tests for two samples must require two double arrays as sole parameters, one for each sample set. Omnibus tests for > 2 samples and the corresponding post-hoc tests must require a 2-dimensional double-array as sole parameter, where columns represent the different models and rows the samples.
  * The test should be implemented in a robust way, allowing to deal with empty or incomplete parameters, and include proper exception handling and logging.
  * The test should be properly documented. Add references if it resembles a less known approach.

2. Add the exact name of the new test (case sentitive!) to the respective category in StatsConfigConstants.java to make it available in the configuration.

3. Add a key-value pair from the test’s name to the test’s pretty-print name in _StatsConfigConstants_.

4. To use the test, add its (non pretty-print) name to the _config.xml_ file in the respective category, or add it to the configuration programmatically.

The following example shows the code required to call a dependent T-test in R:
```
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

			// Check if assumptions for this parametric test (normality of differences) are met 
                        // and store result in parent test
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
```