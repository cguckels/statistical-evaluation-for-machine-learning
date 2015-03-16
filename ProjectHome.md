# STATSREP-ML: Statistical Evaluation & Reporting Framework for Machine Learning Results #

STATSREP-ML is an open-source solution for automating the process of eval- uating machine-learning results. It calculates qualitative statistics, performs the appropriate tests and reports them in a comprehensive way. It largely, but not exclusively, relies on well-tested and robust statistics implementations in R, and uses the tests the machine-learning community largely agreed upon.

# Features of STATSREP-ML #
The features of STATSREP-ML are:
  * Straight-forward configuration, either programmatically or using an XML file.
  * Support for sample input from k-fold cross-validation, repeated cross-validation on one or multiple datasets, and train-test splits.
  * Support for two or >2 values of the independent variable, i.e. either classifiers or features. The appropriate tests for the number of groups are selected automatically.
  * Support for sample sets with two independent variables, by automatically splitting the data along a predefined fixed independent variable value.
  * Integration of both parametric and non-parametric omnibus- as well as post-hoc tests that are commonly used for comparing machine learning results.
  * Integration of specific parametric and non-parametric tests to compare multiple models against a baseline, and support for input data annotations to indicate the baseline model.
  * Automatic p-value correction and integration of several more and less conservative techniques for p-value adjustment.
  * Testing of parametric test assumptions such as normality and sphericity, allowing an easy application of these algorithms.
  * Generation of both a plain-text and a better structured \LaTeX\ report, comprising sample tables, qualitative statistics, basic graphs and the evaluation results.

# Available Tests and Correction Methods #
Currently, STATS-ML offers the following tests:
  * Parametric, omnibus: dependent T-test, repeated Measure One-Way ANOVA,
  * Parametric, post-hoc: Dunett test, Tukey HSD test
  * Non-parametric, omnibus: Wilcoxon Signed-Rank test, Friedman's test, McNemar test
  * Non-parametric, post-hoc: Nemenyi test, pairwise Wilcoxon Signed-Rank test
  * P-value correction methods: Bonferroni, Hochberg, Holm, Hommel, Benjamini-Hochberg, Benjamini-Yekuteli

Additional tests can be easily integrated by means of calling the corresponding R packages or by implementing them natively in Java.

# Usage #
Please note that full integration of STATSREP-ML is available e.g. in the DKPro Text Classification (TC) Framework. The following steps are required for using STATSREP-ML with an arbitrary data source. _Please check the technical paper and the Wiki pages for more detailed instructions._
### Data Preparation ###
Transform your model performance data, e.g. from WEKA, into the STATSREP-ML format. Please note that dots separate the dataset name and a unique value to indicate matching sub partitions of data, e.g. folds in a cross-validation.

```
Train;Test;Classifier;FeatureSet;Measure;Value;IsBaseline
Boston.1;Boston.1;LibLinear;nGrams: 2, nGrams: 3;Weighted F-Measure;0.955319;0
Boston.3;Boston.3;LibLinear;nGrams: 2, nGrams: 3;Weighted F-Measure;0.954770;0
Boston.5;Boston.5;LibLinear;nGrams: 2, nGrams: 3;Weighted F-Measure;0.955319;0
Boston.6;Boston.6;LibLinear;nGrams: 2, nGrams: 3;Weighted F-Measure;0.953485;0
Boston.2;Boston.2;LibLinear;nGrams: 2, nGrams: 3;Weighted F-Measure;0.944326;0
...
Boston.4;Boston.4;LibLinear;nGrams: 2;Weighted F-Measure;0.953901;1
Boston.1;Boston.1;LibLinear;nGrams: 2;Weighted F-Measure;0.952371;1
Boston.2;Boston.2;LibLinear;nGrams: 2;Weighted F-Measure;0.954965;1
...
Boston.1;Boston.1;LibLinear;nGrams: 2, nGrams: 4;Weighted F-Measure;0.96;0
Boston.3;Boston.3;LibLinear;nGrams: 2, nGrams: 4;Weighted F-Measure;0.945;0
Boston.5;Boston.5;LibLinear;nGrams: 2, nGrams: 4;Weighted F-Measure;0.951;0
```

### Setup ###
Either import STATSREP-ML in your Java project or extend the present sources. Use the following code for importing the data and triggering the evaluation with default parameters and tests:
```
StatsConfig config = StatsConfig.getInstance();
String csvPath = "src/main/resources/examples/CVFeaturesBaseline.csv";
String outputPath = "src/main/resources/examples/";
StatsProcessor.evaluateCV(config, csvPath, outputPath, ';');	
```

### Results ###
The results are processed into a latex- and plain-text report. Use them to refine e.g. your parameter settings and/or classifier and feature set selection. Here's a more elaborate example: [Download](http://doc.gold.ac.uk/~cguck001/STATSREP_ML/ExampleReport.pdf).

# How to Cite? #
If you use STATSREP-ML in research, please cite the following paper ([Download](http://tuprints.ulb.tu-darmstadt.de/4294/1/Report_Final.pdf)):

Christian Guckelsberger, Axel Schulz (2014). STATSREP-ML: Statistical Evaluation & Reporting Framework for Machine Learning Results. Technical Report. Published by tuprints http://tuprints.ulb.tu-darmstadt.de/id/eprint/4294.

# License #
While most  STATSREP-ML modules are available under the Apache Software License (ASL) version 2, there are a few modules that depend on external libraries and are thus licensed under the GPL. The license of each individual module is specified in its LICENSE file.

It must be pointed out that while the component's source code itself is licensed under the ASL or GPL, individual components might make use of third-party libraries or products that are not licensed under the ASL or GPL. Please make sure that you are aware of the third party licenses and respect them.


---

This project was initiated under the auspices of Prof. Dr. Max Mühlhäuser, Telecooperation Lab (TK), Technische Universität Darmstadt.