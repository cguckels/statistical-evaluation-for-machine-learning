package de.tudarmstadt.tk.statistics.report;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import de.tudarmstadt.tk.statistics.config.ReportTypes;
import de.tudarmstadt.tk.statistics.config.StatsConfigConstants;
import de.tudarmstadt.tk.statistics.test.AbstractTestResult;
import de.tudarmstadt.tk.statistics.test.PairwiseTestResult;
import de.tudarmstadt.tk.statistics.test.RBridge;
import de.tudarmstadt.tk.statistics.test.TestResult;

/**
 * Helper-class to write the results of a statistical evaluation, given as an
 * object of type {@link EvaluationResults} in a human-friendly way to disk
 * 
 * @author Guckelsberger, Schulz
 */
public class EvaluationResultsWriter {

	/*
	 * Pre-defined significance levels. Changes will affect what is deemed
	 * high/medium/low/not significant in the reports
	 */
	private double significance_low = 1;
	private double significance_medium = 1;
	private double significance_high = 1;
	EvaluationResults evalResults = null;

	public EvaluationResultsWriter(EvaluationResults evalResults) {
		this.evalResults = evalResults;
		this.significance_low = evalResults.getSignificance_low();
		this.significance_medium = evalResults.getSignificance_medium();
		this.significance_high = evalResults.getSignificance_high();
	}

	/**
	 * Escape characters in a string which have to be escaped in latex. Add an
	 * additional "\" in order to not sacrifice the escape sequence when writing
	 * it to the file.
	 * 
	 * @param s
	 *            A string with potential characters that need to be escaped
	 * @return A string with escaped characters ready to be written to a
	 *         document and handled by latex
	 */
	private String escapeLatexCharacters(String s) {

		s = s.replace("&", "\\&");
		s = s.replace("%", "\\%");
		s = s.replace("$", "\\$");
		s = s.replace("#", "\\#");
		s = s.replace("_", "\\_");
		s = s.replace("{", "\\{");
		s = s.replace("}", "\\}");
		s = s.replace("~", "\\~");
		s = s.replace("^", "\\^");

		return s;
	}

	/**
	 * Overloaded method to escape characters which have to be escaped in latex
	 * in the strings of a string array. Add an additional "\" in order to not
	 * sacrifice the escape sequence when writing it to the file.
	 * 
	 * @param s
	 *            a List with String elements
	 * @return a List with escaped Strings
	 */
	private List<String> escapeLatexCharacters(List<String> s) {
		List<String> escapedS = new ArrayList<String>();
		for (int i = 0; i < s.size(); i++) {
			escapedS.add(this.escapeLatexCharacters(s.get(i)));
		}
		return escapedS;
	}

	/**
	 * Creates a latex table without environment, captions etc. i.e. the
	 * tabular-environment only
	 * 
	 * @param header
	 *            The table header, i.e. the first row, as a String-array
	 * @param formatting
	 *            The table formatting, e.g. "|l|l|l|", indicating 3 columns,
	 *            aligned to the left, separated by vertical lines
	 * @param values
	 *            The values to be represented in the table in a 2d-String-array
	 *            (first rows, then columns)
	 * @return A string representing a latex table
	 */
	private String createLatexTabular(String[] header, String formatting, String[][] values) {

		StringBuilder tabular = new StringBuilder();

		/*
		 * //If this should be a table with only one column, e.g. in case of a
		 * baseline evaluation, overwrite the formatting if(twoColumnLayout){
		 * formatting ="|l|l|";
		 * 
		 * }
		 */
		tabular.append(String.format("\\begin{tabular}{%s} \\hline\n", formatting));

		// Header line
		for (int i = 0; i < header.length; i++) {
			String s = header[i];
			if (i == 0) {
				tabular.append(String.format("%s ", s));
			} else {
				tabular.append(String.format("& %s", s));
			}
		}
		tabular.append("\\\\ \\hline\n");

		// Content
		for (int j = 0; j < values.length; j++) {
			String[] row = values[j];
			for (int i = 0; i < row.length; i++) {
				String v = values[j][i];
				if (i == 0) {
					tabular.append(String.format("%s ", v));
				} else {
					tabular.append(String.format("& %s ", v));
				}
			}
			tabular.append("\\\\ \\hline\n");
		}

		tabular.append("\\end{tabular}\n");

		return tabular.toString();
	}

	/**
	 * Create a latex table with captions, label etc. i.e. a table environment
	 * comprising a tabular environment.
	 * 
	 * @param caption
	 *            The table's caption
	 * @param label
	 *            The table's label to be used for references
	 * @param header
	 *            The table header, i.e. the first row, as a String-array
	 * @param formatting
	 *            The table formatting, e.g. "|l|l|l|", indicating 3 columns,
	 *            aligned to the left, separated by vertical lines
	 * @param values
	 *            The values to be represented in the table in a 2d-String-array
	 *            (first rows, then columns)
	 * @return A string representing a latex table in a table environment
	 */
	private String createLatexTable(String caption, String label, String[] header, String formatting, String[][] values) {
		StringBuilder table = new StringBuilder();

		table.append("\n\\begin{table}[h!]\n");
		table.append("\\centering\n");
		table.append("\\adjustbox{max width=\\linewidth}{\n");
		table.append(createLatexTabular(header, formatting, values));
		table.append("}");// Close adjustbox environment
		table.append(String.format("\\caption{%s}\n", caption));
		table.append(String.format("\\label{%s}\n", label));
		table.append("\\end{table}\n\n");

		return table.toString();
	}

	private String createLatexLongTable(String caption, String label, String[] header, String formatting, String[][] values) {

		StringBuilder table = new StringBuilder();

		// Header line
		StringBuilder headerStr = new StringBuilder();
		for (int i = 0; i < header.length; i++) {
			String s = header[i];
			if (i == 0) {
				headerStr.append(String.format("%s ", s));
			} else {
				headerStr.append(String.format("& %s", s));
			}
		}
		headerStr.append("\\\\ \\hline\n");

		// Resizing not possible for multipage tables. Thus more columns,
		// smaller font
		if (values[0].length > 11) {
			table.append("\\tiny");
		} else if (values[0].length > 6) {
			table.append("\\scriptsize");
		}

		// Table head
		table.append(String.format("\\begin{longtable}{%s} \\hline\n", formatting));
		table.append(headerStr);
		table.append("\\endfirsthead\n");
		table.append("\\hline\n");
		table.append(headerStr);
		table.append("\\endhead");
		table.append("\\endfoot");
		table.append("\\endlastfoot");

		// Content
		for (int j = 0; j < values.length; j++) {
			String[] row = values[j];
			for (int i = 0; i < row.length; i++) {
				String v = values[j][i];
				if (i == 0) {
					table.append(String.format("%s ", v));
				} else {
					table.append(String.format("& %s ", v));
				}
			}
			table.append("\\\\ \\hline\n");
		}

		// Table bottom
		table.append(String.format("\\caption{%s}\n", caption));
		table.append(String.format("\\label{%s}\n", label));
		table.append("\\end{longtable}\n\n");
		table.append("\\normalsize");

		return table.toString();
	}

	/**
	 * Create a latex table environment with more than one latex latex table
	 * inside using subfigures
	 * 
	 * @param overallCaption
	 *            The caption for all tables
	 * @param subCaption
	 *            The caption for individual tables specified as String-arrays
	 * @param overallLabel
	 *            The label for all tables, to be used for referencing
	 * @param header
	 *            The table header, i.e. the first row, as a String-array
	 * @param formatting
	 *            The table formatting, e.g. "|l|l|l|", indicating 3 columns,
	 *            aligned to the left, separated by vertical lines
	 * @param values
	 *            The values to be represented in the table in a 3d-String-array
	 *            (subtable, rows, columns)
	 * @return A string representing several latex tables comprised in a single
	 *         table-environment
	 */
	private String createLatexSubTable(String overallCaption, String[] subCaption, String overallLabel, String[] header, String formatting, String[][][] overallValues) {

		StringBuilder table = new StringBuilder();
		table.append("\n\\begin{table}[h!]\n");
		table.append("\\centering\n");

		for (int i = 0; i < overallValues.length; i++) {
			StringBuilder tabular = new StringBuilder();
			String[][] values = overallValues[i];
			tabular.append(String.format("\\begin{subfigure}[b]{%dcm}\n", values[0].length + 2));
			tabular.append("\\centering\n");
			tabular.append("\\adjustbox{max width=\\linewidth}{\n");
			tabular.append(createLatexTabular(header, formatting, values));
			tabular.append("}");// Close adjustbox environment
			tabular.append(String.format("\\caption{%s}\n", subCaption[i]));
			tabular.append("\\end{subfigure}\n");
			table.append(tabular);
		}

		table.append(String.format("\\caption{%s}\n", overallCaption));
		table.append(String.format("\\label{%s}\n", overallLabel));
		table.append("\\end{table}\n\n");

		return table.toString();
	}

	/**
	 * Create a Latex-conforming String-representation of the test results,
	 * accounting for parameters and the test statistic (if available), p-Value
	 * and the closest significance level
	 * 
	 * @param r
	 *            the test result as {@link TestResult} object
	 * @param pThreshold
	 *            the significance level which is closes to the p-Value
	 * @return A Latex-conforming String-representation of the test results
	 */
	private String getTestResultRepresentation(TestResult r, double pThreshold) {

		StringBuilder parameters = new StringBuilder();
		Iterator<String> it = r.getParameter().keySet().iterator();
		while (it.hasNext()) {
			String parameter = it.next();
			double value = r.getParameter().get(parameter);
			parameters.append(String.format("%s=%.3f, ", parameter, value));
		}
		if (r.getStatisticType().isEmpty()) {
			return String.format("$%sp=%.3f, \\alpha=%.2f$", parameters, r.getpValue(), pThreshold);
		} else {
			return String.format("$%s%s=%.3f, p=%.3f, \\alpha=%.2f$", parameters, r.getStatisticType(), r.getStatistic(), r.getpValue(), pThreshold);
		}
	}

	/**
	 * Get a String representation of the models order, inferred from their
	 * pairwise p-values.
	 * 
	 * @param ordering
	 *            a HashMap mapping levels of topological order to sets of
	 *            models
	 * @return A string representing the models order or alternatively a message
	 *         that no order could be determined.
	 */
	private String getModelOrderingRepresentation(HashMap<Integer, TreeSet<Integer>> ordering) {

		if (ordering != null && ordering.size() > 1) {
			StringBuilder orderSequence = new StringBuilder();
			for (int level = 0; level < ordering.keySet().size(); level++) {
				TreeSet<Integer> s = ordering.get(level);

				if (s.size() == 0)
					return "These results do not allow for a strict ordering of all models.";

				int n = s.first();
				s.remove(n);
				orderSequence.append(String.format("(M%d", n));
				for (Integer node : ordering.get(level)) {
					orderSequence.append(String.format(",M%d", node));
				}
				orderSequence.append(")");

				if (level < ordering.keySet().size() - 1) {
					orderSequence.append("<");
				}
			}
			return String.format("These results allow for the follwing ordering of model performances: %s. ", orderSequence.toString());
		} else {
			return "These results do not allow for a strict ordering of all models. ";
		}
	}

	/**
	 * Creates a report of the statistical evaluation in the Latex-format
	 * 
	 * @param outputFolder
	 *            the folder where the report will be written later to store
	 *            related images etc. there
	 * @param evalResults
	 *            an object of type {@link EvaluationResults} comprising the
	 *            results of the statistical evaluation
	 * @return A String representing the report of the statistical evaluation in
	 *         Latex-format
	 */
	public String createLatexReport(File outputFolder) {
		// Set locale to English globally to make reports independent of the
		// machine thei're created on, e.g. use "." as decimal points on any
		// machine
		Locale.setDefault(Locale.ENGLISH);
		StringBuilder report = new StringBuilder();
		RBridge stats = RBridge.getInstance(true);
		HashMap<String, String> methodsSummary = new HashMap<String, String>();
		HashMap<String, HashMap<String, List<String>>> testSummary = new HashMap<String, HashMap<String, List<String>>>();
		ArrayList<String[]> figures = new ArrayList<String[]>();
		testSummary.put("Parametric", new HashMap<String, List<String>>());
		testSummary.put("Non-Parametric", new HashMap<String, List<String>>());
		String outputFolderPath = "";
		if (outputFolder != null) {
			outputFolderPath = outputFolder.getAbsolutePath();
		}

		//
		// Header
		//
		// Packages
		report.append("\\documentclass[a4paper,12pt]{article}\n");
		report.append("\\usepackage[english]{babel}\n");
		report.append("\\usepackage[utf8]{inputenc}\n");
		report.append("\\usepackage{graphicx}\n");
		report.append("\\usepackage{titlesec}\n");
		report.append("\\usepackage{caption}\n");
		report.append("\\usepackage{subcaption}\n");
		report.append("\\usepackage{adjustbox}\n");
		report.append("\\usepackage{placeins}\n");
		report.append("\\usepackage{longtable}\n");
		// Title definition
		report.append("\\titleformat*{\\section}{\\large\\bfseries}\n");
		report.append("\\titleformat*{\\subsection}{\\normalsize\\bfseries}\n");
		report.append("\\titleformat*{\\subsubsection}{\\vspace{-0.3cm}\\normalsize\\bfseries}\n");
		report.append("\\title{Statistical Evaluation Report}\n");
		report.append("\\date{\\vspace{-10ex}}\n");
		report.append("\\begin{document}\n");
		report.append("\\maketitle\n");

		//
		// Evaluation Overview
		//
		report.append("\\section{Evaluation Overview}");

		int nModels = evalResults.getSampleData().getModelMetadata().size();
		ArrayList<String> measures = evalResults.getMeasures();
		int nSamples = evalResults.getSampleData().getSamples().get(measures.get(0)).get(0).size();
		String ref = "tbl:models";

		// Separate training/testing datasets
		List<String> trainingDataList = new ArrayList<String>();
		List<String> testingDataList = new ArrayList<String>();
		List<Pair<String, String>> datasets = evalResults.getSampleData().getDatasetNames();
		Iterator<Pair<String, String>> itp = datasets.iterator();
		while (itp.hasNext()) {
			Pair<String, String> trainTest = itp.next();
			trainingDataList.add(trainTest.getKey());
			if (trainTest.getValue() != null) {
				testingDataList.add(trainTest.getValue());
			}
		}
		Set<String> trainingDataSet = new HashSet<String>(trainingDataList);
		Set<String> testingDataSet = new HashSet<String>(testingDataList);

		String pipelineDescription = null;
		String sampleOrigin = "per CV";

		ReportTypes pipelineType = this.evalResults.getSampleData().getPipelineType();
		switch (pipelineType) {
		// One-domain n-fold CV (ReportData=per Fold)
		case CV:
			pipelineDescription = String.format("%d-fold cross validation", evalResults.getSampleData().getnFolds());
			sampleOrigin = "per fold ";
			break;
		case MULTIPLE_CV:
			pipelineDescription = String.format("%dx%s repeated cross validation", evalResults.getSampleData().getnRepetitions(), evalResults.getSampleData().getnFolds());
			break;
		case CV_DATASET_LVL:
			pipelineDescription = String.format("%d-fold cross validation over %d datasets", evalResults.getSampleData().getnFolds(), trainingDataSet.size());
			break;
		case MULTIPLE_CV_DATASET_LVL:
			pipelineDescription = String.format("%dx%s repeated cross validation over %d datasets", evalResults.getSampleData().getnRepetitions(), evalResults.getSampleData().getnFolds(), trainingDataSet.size());
			sampleOrigin = "per dataset";
			break;
		case TRAIN_TEST_DATASET_LVL:
			// In the train/test scenario, the number of datasets only includes
			// distinct ones
			Set<String> allDataSets = new HashSet<String>(testingDataSet);
			allDataSets.addAll(trainingDataSet);
			pipelineDescription = String.format("Train/Test over %d datasets", allDataSets.size());
			sampleOrigin = "per dataset";
			break;
		default:
			pipelineDescription = "!unknown pipeline type!";
			sampleOrigin = "!unknown pipeline type!";
			break;
		}

		boolean isBaselineEvaluation = evalResults.isBaselineEvaluation();
		report.append(String.format("The system performed a %s for the %d models in Tbl \\ref{%s}. ", pipelineDescription, nModels, ref));
		if (isBaselineEvaluation) {
			report.append(String.format("The models were compared against the first baseline model. \n", pipelineDescription, nModels, ref));
		} else {
			report.append(String.format("The models were compared against each other. \n", pipelineDescription, nModels, ref));
		}

		String[][] values = new String[nModels][3];
		for (int r = 0; r < nModels; r++) {
			values[r][0] = String.format("M%d", r);
			// Remove package prefix for algorithms, e.g. shorten "trees.J48" to
			// "J48".
			String[] algorithm = evalResults.getSampleData().getModelMetadata().get(r).getKey().split("\\.");
			if (algorithm.length > 1) {
				values[r][1] = escapeLatexCharacters(algorithm[1]);
			} else {
				values[r][1] = escapeLatexCharacters(algorithm[0]);
			}
			values[r][2] = escapeLatexCharacters(evalResults.getSampleData().getModelMetadata().get(r).getValue());
		}

		String table = createLatexTable("Evaluated models with classifier algorithm and feature sets", ref, new String[] { "Index", "Algorithm", "Feature Set" }, "|l|l|p{11cm}|", values);
		report.append(table);

		// List test/training datasets. Consider the case when these sets are
		// different.
		if (testingDataSet.isEmpty()) {
			if (trainingDataSet.size() == 1) {
				report.append(String.format("The models were evaluated on the dataset %s. ", trainingDataList.get(0)));
			} else {
				report.append(String.format("The models were evaluated on the datasets %s. ", this.createEnumeration(trainingDataList)));
			}
		} else {
			if (trainingDataSet.size() == 1 && testingDataSet.size() == 1) {
				report.append(String.format("The models were trained on the dataset %s and tested on the dataset %s. ", trainingDataList.get(0), testingDataList.get(0)));
			} else if (trainingDataSet.size() > 1 && testingDataSet.size() == 1) {
				report.append(String.format("The models were trained on the datasets %s and tested on the dataset %s. ", this.createEnumeration(new ArrayList<String>(trainingDataSet)), testingDataList.get(0)));
			} else if (trainingDataSet.size() == 1 && testingDataSet.size() > 1) {
				report.append(String.format("The models were trained on the dataset %s and tested on the datasets %s. ", trainingDataList.get(0), this.createEnumeration(new ArrayList<String>(testingDataSet))));
			} else {
				report.append(String.format("The models were trained on the datasets %s and tested on the datasets %s. ", this.createEnumeration(new ArrayList<String>(trainingDataSet)), this.createEnumeration(new ArrayList<String>(testingDataSet))));
			}
		}
		report.append(String.format("Their performance was assessed with the %s", createEnumeration(measures)));
		report.append(". In the analysis, the models thus represent levels of the independent variable, while the performance measures are dependent variables.\n");

		//
		// Results (for each measure separately)
		//
		report.append("\\FloatBarrier\n"); // All previous floats must be placed
											// before this point
		report.append("\\section{Results}\n");
		report.append(String
				.format("Throughout the report, p-values are annotated if they are significant. While {\\footnotesize *} indicates low significance ($p<\\alpha=%.2f$), the annotations {\\footnotesize **} and {\\footnotesize ***} represent medium ($p<\\alpha=%.2f$) and high significance ($p<\\alpha=%.2f$).",
						significance_low, significance_medium, significance_high));

		for (int i = 0; i < measures.size(); i++) {
			/*
			 * Create table with samples for the current performance measure If
			 * samples are drawn over multiple datasets, transpose table
			 */
			String measure = measures.get(i);
			if (!evalResults.getSampleData().getSamples().containsKey(measure)) {
				continue;
			}
			ArrayList<ArrayList<Double>> measureSamples = evalResults.getSampleData().getSamples().get(measure);
			ArrayList<Double> averageMeasureSamples = evalResults.getSampleData().getSamplesAverage().get(measure);

			report.append("\\FloatBarrier\n");
			report.append(String.format("\\subsection{%s}\n", measure));
			ref = String.format("tbl:%s", measure.replaceAll("\\s", ""));
			report.append(String.format("The %s samples drawn from the %s and the %d models are presented in Tbl. \\ref{%s}.\n", measure, pipelineDescription, nModels, ref));

			// Plot Box-Whisker-Diagram of samples for the current measure and
			// add the figure to the appendix
			String filename = String.format("boxPlot%s", measure.replaceAll("\\s", ""));
			String path = String.format("%s%s%s", outputFolderPath, File.separator, filename);
			String pathR = this.fixSlashes(path);
			String figRef = String.format("fig:boxPlot%s", measure.replaceAll("\\s", ""));
			String caption = String.format("Box-Whisker-Plot of %s samples. Red dots indicate means.", measure);
			double[][] samples = new double[nModels][];
			for (int k = 0; k < nModels; k++) {
				ArrayList<Double> s = measureSamples.get(k);
				samples[k] = new double[s.size()];
				for (int j = 0; j < s.size(); j++) {
					samples[k][j] = s.get(j);
				}
			}
			boolean successful = stats.plotBoxWhisker(samples, pathR, measure);
			if (successful) {
				figures.add(new String[] { figRef, caption, filename });
				report.append(String.format("See Fig. \\ref{%s} for a Box-Whisker plot of these samples. ", figRef));
			}

			caption = String.format("Samples of the %s drawn from the %s and the %d models", measure, pipelineDescription, nModels);
			switch (pipelineType) {
			case CV:
			case MULTIPLE_CV:
				values = new String[nModels + 1][nSamples + 2];
				for (int r = 0; r <= nModels; r++) {
					// First line of table = Fold indices
					if (r == 0) {
						values[r][0] = "";
						values[r][nSamples + 1] = "";
						for (int f = 1; f <= nSamples; f++) {
							values[r][f] = Integer.toString(f);
						}
						// Next lines with model indices, samples per fold and
						// average measure over all samples
					} else {
						values[r][0] = String.format("M%d", (r - 1));
						values[r][nSamples + 1] = String.format("%.2f", averageMeasureSamples.get(r - 1) * 100);
						ArrayList<Double> s = measureSamples.get(r - 1);
						for (int j = 0; j < s.size(); j++) {
							values[r][j + 1] = String.format("%.2f", s.get(j) * 100);
						}
					}
				}
				if (values.length > 58) {
					table = createLatexLongTable(caption, ref, new String[] { "Classifier", String.format("\\multicolumn{%d}{|c|}{%s %s [\\%%]}", nSamples, measure, sampleOrigin), "Average" },
							String.format("|%s", StringUtils.repeat("l|", nSamples + 2)), values);
				} else {
					table = createLatexTable(caption, ref, new String[] { "Classifier", String.format("\\multicolumn{%d}{|c|}{%s %s [\\%%]}", nSamples, measure, sampleOrigin), "Average" },
							String.format("|%s", StringUtils.repeat("l|", nSamples + 2)), values);
				}
				break;

			case CV_DATASET_LVL:
			case MULTIPLE_CV_DATASET_LVL:
			case TRAIN_TEST_DATASET_LVL:
				values = new String[nSamples + 2][nModels + 1];
				// double[][] valuesNumeric = new double[nSamples][nModels];
				for (int r = 0; r <= nSamples + 1; r++) {
					// First line of table = Model indices
					if (r == 0) {
						values[r][0] = "";
						for (int j = 0; j < nModels; j++) {
							values[r][j + 1] = String.format("M%d", (j));
						}
						// Last line of table = average sums
					} else if (r == nSamples + 1) {
						values[r][0] = "Average";
						for (int j = 0; j < nModels; j++) {
							values[r][j + 1] = String.format("%.2f", averageMeasureSamples.get(j) * 100);
						}
						// Next lines with model indices, samples per fold and
						// average measure over all samples
					} else {
						// Only print both train- and test set if there is more
						// than one training set
						Pair<String, String> trainTest = evalResults.getSampleData().getDatasetNames().get(r - 1);
						if (pipelineType == ReportTypes.TRAIN_TEST_DATASET_LVL) {
							if (trainingDataSet.size() > 1) {
								values[r][0] = String.format("%s-%s", trainTest.getKey(), trainTest.getValue());
							} else {
								values[r][0] = trainTest.getValue();
							}
						} else {
							values[r][0] = trainTest.getKey();
						}
						for (int j = 0; j < nModels; j++) {
							ArrayList<Double> s = measureSamples.get(j);
							values[r][j + 1] = String.format("%.2f", s.get(r - 1) * 100);
						}
					}
				}
				if (values.length > 58) {
					table = createLatexLongTable(caption, ref, new String[] { "Dataset", String.format("\\multicolumn{%d}{|c|}{%s %s [\\%%]}", nModels, measure, sampleOrigin) }, String.format("|%s", StringUtils.repeat("l|", nModels + 1)), values);
				} else {
					table = createLatexTable(caption, ref, new String[] { "Dataset", String.format("\\multicolumn{%d}{|c|}{%s %s [\\%%]}", nModels, measure, sampleOrigin) }, String.format("|%s", StringUtils.repeat("l|", nModels + 1)), values);
				}
				break;
			}
			report.append(table);

			//
			// Results - First parametric tests, then non-parametric (2
			// iterations)
			// Print results for alls non-parametric tests except McNemar.
			// McNemar is not based on the same performance measures but on a
			// contingency matrix, which is
			// printed in a separate section.
			for (String testType : new String[] { "Parametric", "Non-Parametric" }) {
				report.append(String.format("\\subsubsection{%s Testing}", testType));

				Pair<String, AbstractTestResult> result = null;
				if (testType.equals("Parametric")) {
					result = evalResults.getParametricTestResults().get(measure);
				} else {
					result = evalResults.getNonParametricTestResults().get(measure);
				}

				// Use pretty-print method descriptor if specified
				String method = result.getKey();
				if (StatsConfigConstants.PRETTY_PRINT_METHODS.containsKey(method)) {
					method = StatsConfigConstants.PRETTY_PRINT_METHODS.get(method);
				}
				methodsSummary.put(testType, method);

				TestResult r = (TestResult) result.getValue();
				report.append(String.format("The system compared the %d models using the \\emph{%s}. ", nModels, method));

				if (r != null && !Double.isNaN(r.getpValue())) {

					// A priori test: assumptions
					boolean assumptionViolated = false;
					Iterator<String> it = r.getAssumptions().keySet().iterator();
					while (it.hasNext()) {
						String assumption = it.next();

						TestResult at = (TestResult) r.getAssumptions().get(assumption);
						if (at == null) {
							report.append(String.format("Testing for %s failed. ", assumption));
							assumptionViolated = true;
							continue;
						}
						if (Double.isNaN(at.getpValue())) {
							report.append(String.format("Testing for %s using %s failed. ", assumption, at.getMethod()));
							assumptionViolated = true;
							continue;
						}
						double ap = at.getpValue();

						if (ap <= this.significance_low) {
							assumptionViolated = true;
						}

						// Verbalize result according to p value
						Pair<String, Double> verbalizedP = verbalizeP(ap, true);

						String testResultRepresentation = getTestResultRepresentation(at, verbalizedP.getValue());
						report.append(String.format("%s %s violation of %s (%s). ", at.getMethod(), verbalizedP.getKey(), assumption, testResultRepresentation));

					}

					// Create QQ-Normal diagram to support the analysis of a
					// normality assumption
					if (result.getKey().equals("DependentT") && samples.length == 2) {
						filename = String.format("qqNormPlot%s", measure.replaceAll("\\s", ""));
						path = String.format("%s%s%s", outputFolderPath, File.separator, filename);
						pathR = this.fixSlashes(path);
						figRef = String.format("fig:qqNormPlot%s", measure.replaceAll("\\s", ""));
						caption = String.format("QQ-Normal plot of pairwise differences between %s samples.", measure);
						double[] differences = new double[samples[0].length];
						for (int j = 0; j < samples[0].length; j++) {
							differences[j] = samples[0][j] - samples[1][j];
						}
						successful = stats.plotQQNorm(differences, "M0-M1", measure, pathR);
						if (successful) {
							figures.add(new String[] { figRef, caption, filename });
							report.append(String.format("See Fig. \\ref{%s} for a QQ-Normal plot of the samples. ", figRef));
						}
					}

					if (assumptionViolated) {
						report.append("Given that the assumptions are violated, the following test may be corrupted. ");
					}

					// A Priori test results
					// Verbalize result according to p value
					Pair<String, Double> verbalizedP = verbalizeP(r.getpValue(), false);
					String testResultRepresentation = getTestResultRepresentation(r, verbalizedP.getValue());
					report.append(String.format("The %s %s differences between the performances of the models (%s).\\\\ \n\n ", method, verbalizedP.getKey(), testResultRepresentation));

					// Store result for summary
					if (testSummary.get(testType).containsKey(verbalizedP.getKey())) {
						testSummary.get(testType).get(verbalizedP.getKey()).add(measure);
					} else {
						ArrayList<String> list = new ArrayList<String>();
						list.add(measure);
						testSummary.get(testType).put(verbalizedP.getKey(), list);
					}

					// Post-hoc test for >2 models (pairwise comparisons)
					if (evalResults.getSampleData().getModelMetadata().size() > 2) {

						Pair<String, AbstractTestResult> postHocResult = null;
						if (testType.equals("Parametric")) {
							postHocResult = evalResults.getParametricPostHocTestResults().get(measure);
						} else {
							postHocResult = evalResults.getNonParametricPostHocTestResults().get(measure);
						}
						method = postHocResult.getKey();
						if (StatsConfigConstants.PRETTY_PRINT_METHODS.containsKey(method)) {
							method = StatsConfigConstants.PRETTY_PRINT_METHODS.get(method);
						}
						methodsSummary.put(String.format("%sPostHoc", testType), method);

						PairwiseTestResult rPostHoc = (PairwiseTestResult) postHocResult.getValue();
						report.append(String.format("The system performed the \\emph{%s} post-hoc. ", method));

						if (rPostHoc == null) {
							report.append("The test failed. ");
							continue;
						}

						// Assumptions
						boolean assumptionsViolated = false;
						it = rPostHoc.getAssumptions().keySet().iterator();
						while (it.hasNext()) {
							String assumption = it.next();
							PairwiseTestResult at = (PairwiseTestResult) rPostHoc.getAssumptions().get(assumption);
							if (at == null) {
								report.append(String.format("Testing for %s failed. ", assumption));
								assumptionsViolated = true;
								continue;
							}

							// Create table with pairwise p-values for
							// assumption testing
							double[][] ap = at.getpValue();
							Pair<String[], String[][]> tableData = getPValueStringArray(ap, isBaselineEvaluation); // first
																													// element
																													// is
																													// header,
																													// second
																													// are
																													// values
							caption = String.format("P-values from the %s for %s", at.getMethod(), measure);
							ref = String.format("tbl:%s%s", at.getMethod().replaceAll("\\s", ""), measure.replaceAll("\\s", ""));
							table = createLatexTable(caption, ref, tableData.getKey(), String.format("|%s", StringUtils.repeat("l|", nModels)), tableData.getValue());

							double max = getMax(ap);
							double min = getMin(ap);
							verbalizedP = verbalizeP(min, true);
							if ((max > significance_low && min <= significance_low) || (max > significance_medium && min <= significance_medium) || (max > significance_high && min <= significance_high)) {
								// partly significant to degree as specified by
								// verbalized p-value
								report.append(String.format("%s partly %s violation of %s ($\\alpha=%.2f$, Tbl. \\ref{%s}).\n", at.getMethod(), verbalizedP.getKey(), assumption, verbalizedP.getValue(), ref));
							} else {
								report.append(String.format("%s %s violation of %s ($\\alpha=%.2f$, Tbl. \\ref{%s}).\n", at.getMethod(), verbalizedP.getKey(), assumption, verbalizedP.getValue(), ref));
							}
							report.append(table);

							if (min <= this.significance_low) {
								assumptionsViolated = true;
							}

						}

						if (assumptionViolated) {
							report.append("Given that the assumptions are violated, the following test may be corrupted. ");
						}

						// Result
						double[][] ap = rPostHoc.getpValue();
						Pair<String[], String[][]> tableData = getPValueStringArray(ap, isBaselineEvaluation); // first
																												// element
																												// is
																												// header,
																												// second
																												// are
																												// values
						caption = String.format("P-values from the %s for %s", method, measure);
						ref = String.format("tbl:%s%s", method.replaceAll("\\s", ""), measure.replaceAll("\\s", ""));
						String formatting = null;
						if (!isBaselineEvaluation) {
							formatting = String.format("|%s", StringUtils.repeat("l|", nModels));
						} else {
							formatting = String.format("|l|l|");
						}
						String tablePNonAdjusted = createLatexTable(caption, ref, tableData.getKey(), formatting, tableData.getValue());

						// Already fetch pairwise adjustments here in order to
						// determine choice of words
						double max = getMax(ap);
						double min = getMin(ap);
						verbalizedP = verbalizeP(min, false);
						ArrayList<String> adjustments = new ArrayList<String>(rPostHoc.getpValueCorrections().keySet());
						String adjustWord = "";
						if (adjustments.size() > 0) {
							adjustWord = " for non-adjusted p-values";
						}
						if ((max > significance_low && min <= significance_low) || (max > significance_medium && min <= significance_medium) || (max > significance_high && min <= significance_high)) {
							// partly significant to degree as specified by
							// verbalized p-value
							report.append(String.format("The %s partly %s differences between the performances of the models%s ($\\alpha=%.2f$, Tbl. \\ref{%s}). ", method, verbalizedP.getKey(), adjustWord, verbalizedP.getValue(), ref));
						} else {
							report.append(String.format("The %s %s differences between the performances of the models%s ($\\alpha=%.2f$, Tbl. \\ref{%s}). ", method, verbalizedP.getKey(), adjustWord, verbalizedP.getValue(), ref));
						}

						// Determine ordering of models
						HashMap<Integer, TreeSet<Integer>> postHocOrdering = null;
						int[][] orderingEdgeList = null;
						if (testType.equals("Parametric")) {
							postHocOrdering = evalResults.getParameticPostHocOrdering().get(measure);
							orderingEdgeList = evalResults.getParameticPostHocEdgelist().get(measure);
						} else {
							postHocOrdering = evalResults.getNonParameticPostHocOrdering().get(measure);
							orderingEdgeList = evalResults.getNonParameticPostHocEdgelist().get(measure);
						}
						String ordering = getModelOrderingRepresentation(postHocOrdering);
						report.append(ordering);

						// Print graphs of ordering for the current measure and
						// add the figure to the appendix
						filename = String.format("graphOrdering%s%s", measure.replaceAll("\\s", ""), testType);
						path = String.format("%s%s%s", outputFolderPath, File.separator, filename);
						pathR = this.fixSlashes(path);
						figRef = String.format("fig:graphOrdering%s%s", measure.replaceAll("\\s", ""), testType);
						caption = String.format("Directed graph of significant differences between the models and %s as determined by the %s post-hoc test.", measure, testType);
						// int nodes[] = new int[nModels];
						// for(int j=0; j<nModels;j++){nodes[j]=j;};
						successful = stats.plotGraph(orderingEdgeList, nModels, pathR);
						if (successful) {
							figures.add(new String[] { figRef, caption, filename });
							report.append(String.format("The ordering is visualized in Fig. \\ref{%s}. ", figRef));
						}

						// Pairwise adjustments
						String tablePAdjusted = null;
						if (adjustments.size() > 0) {
							String[] subcaption = new String[adjustments.size()];
							String[] header = null;
							String[][][] overallValues = new String[adjustments.size()][][];
							double[] minAdjustments = new double[adjustments.size()];
							double[] maxAdjustments = new double[adjustments.size()];
							for (int j = 0; j < adjustments.size(); j++) {
								String adjustmentMethod = adjustments.get(j);
								subcaption[j] = adjustmentMethod;
								double[][] correctedP = rPostHoc.getpValueCorrections().get(adjustmentMethod);
								if (StatsConfigConstants.PRETTY_PRINT_METHODS.containsKey(adjustmentMethod)) {
									subcaption[j] = StatsConfigConstants.PRETTY_PRINT_METHODS.get(adjustmentMethod);
								}
								tableData = getPValueStringArray(correctedP, isBaselineEvaluation);
								header = tableData.getKey();
								overallValues[j] = tableData.getValue();
								minAdjustments[j] = getMin(correctedP);
								maxAdjustments[j] = getMax(correctedP);
							}

							caption = String.format("Adjusted p-values from the %s for %s", method, measure);
							ref = String.format("tbl:%s%sAdjusted", method.replaceAll("\\s", ""), measure.replaceAll("\\s", ""));
							formatting = null;
							if (!isBaselineEvaluation) {
								formatting = String.format("|%s", StringUtils.repeat("l|", nModels));
							} else {
								formatting = String.format("|l|l|");
							}
							tablePAdjusted = createLatexSubTable(caption, subcaption, ref, header, formatting, overallValues);

							min = getMin(minAdjustments);
							max = getMax(maxAdjustments);
							verbalizedP = verbalizeP(min, false);

							if ((max > significance_low && min <= significance_low) || (max > significance_medium && min <= significance_medium) || (max > significance_high && min <= significance_high)) {
								// partly significant to degree as specified by
								// verbalized p-value
								report.append(String.format("It partly %s differences for adjusted p-values ($\\alpha=%.2f$, Tbl. \\ref{%s}).\n\n ", verbalizedP.getKey(), verbalizedP.getValue(), ref));
							} else {
								report.append(String.format("It %s differences for adjusted p-values ($\\alpha=%.2f$, Tbl. \\ref{%s}).\n\n ", verbalizedP.getKey(), verbalizedP.getValue(), ref));
							}
						}

						report.append(tablePNonAdjusted);
						if (tablePAdjusted != null) {
							report.append(tablePAdjusted);
						}

					}
				} else {
					report.append(String.format("The %s failed.", method));
				}
			}

		}

		//
		// Contingency table and McNemar results if this test was performed
		//
		if (evalResults.getNonParametricTest().equals("McNemar")) {
			String measure = "Contingency Table";
			String testType = "Non-Parametric";
			report.append("\\FloatBarrier\n");
			report.append("\\subsection{Contingency Table}\n");

			String caption = String.format("Contingency table with correctly and incorrectly classified folds for %s", measure);
			if (evalResults.getSampleData().getPipelineType() == ReportTypes.MULTIPLE_CV) {
				report.append(String.format("The contingency table drawn from the %s and the %d models is listed in Tbl. \\ref{%s}. The correctly and incorrectly classified instances per fold were averaged over all repetitions. \n",
						pipelineDescription, nModels, ref));
				caption = String.format("Averaged contingency table with correctly and incorrectly classified folds for %s", measure);
			} else {
				report.append(String.format("The contingency table drawn from the %s and the %d models is listed in Tbl. \\ref{%s}.\n", pipelineDescription, nModels, ref));
			}

			int[][] contingencyMatrix = evalResults.getSampleData().getContingencyMatrix();
			ref = "tbl:ContingencyMatrix";
			values = new String[][] { { "Wrong", "", "" }, { "Correct", "", "" } };
			values[0][1] = String.valueOf(contingencyMatrix[0][0]);
			values[0][2] = String.valueOf(contingencyMatrix[0][1]);
			values[1][1] = String.valueOf(contingencyMatrix[1][0]);
			values[1][2] = String.valueOf(contingencyMatrix[1][1]);

			table = createLatexTable(caption, ref, new String[] { "M0/M1", "Wrong", "Correct" }, "|l|l|l|", values);
			report.append(table);

			// Test results
			report.append(String.format("\\subsubsection{%s Testing}", testType));
			report.append(String.format("The system compared the %d models using the \\emph{McNemar test}. ", nModels));
			Pair<String, AbstractTestResult> result = evalResults.getNonParametricTestResults().get(measure);

			// Use pretty-print method descriptor if specified
			String method = result.getKey();
			if (StatsConfigConstants.PRETTY_PRINT_METHODS.containsKey(method)) {
				method = StatsConfigConstants.PRETTY_PRINT_METHODS.get(method);
			}
			methodsSummary.put(testType, method);

			TestResult r = (TestResult) result.getValue();
			if (r != null && !Double.isNaN(r.getpValue())) {
				StringBuilder parameters = new StringBuilder();
				Iterator<String> it = r.getParameter().keySet().iterator();
				while (it.hasNext()) {
					String parameter = it.next();
					double value = r.getParameter().get(parameter);
					parameters.append(String.format("%s=%.3f, ", parameter, value));
				}

				// Verbalize result according to p value
				Pair<String, Double> verbalizedP = verbalizeP(r.getpValue(), false);
				report.append(String.format("The test %s differences between the performances of the models ($%sp=%.3f, \\alpha=%.2f$).\\\\ \n", verbalizedP.getKey(), parameters.toString(), r.getpValue(), verbalizedP.getValue()));
				// Store result for summary
				if (testSummary.get(testType).containsKey(verbalizedP.getKey())) {
					testSummary.get(testType).get(verbalizedP.getKey()).add(measure);
				} else {
					ArrayList<String> list = new ArrayList<String>();
					list.add(measure);
					testSummary.get(testType).put(verbalizedP.getKey(), list);
				}

			} else {
				report.append("The test failed.\\\\ \n");
			}
		}

		//
		// Summary of results
		//
		report.append("\\FloatBarrier\n");
		report.append("\\section{Summary}\n");
		for (String testType : new String[] { "Parametric", "Non-Parametric" }) {
			String prefix = "";

			if (nModels == 2) {
				report.append(String.format("The system performed %s testing of the %d models using a %s. The test ", testType.toLowerCase(), nModels, methodsSummary.get(testType)));
				prefix = "It";
			} else {
				String postHocTesting = String.format("%sPostHoc", testType);
				report.append(String.format("The system performed %s testing of the %d models using a %s and a %s post-hoc. The tests ", testType.toLowerCase(), nModels, methodsSummary.get(testType), methodsSummary.get(postHocTesting)));
				prefix = "They";
			}

			// If all tests failed, there're no results to summarize.
			HashMap<String, List<String>> summary = testSummary.get(testType);
			if (summary.keySet().size() == 0) {
				report.append("failed. ");
				continue;
			}

			Iterator<String> it = summary.keySet().iterator();
			boolean usePrefix = false;
			while (it.hasNext()) {
				String pVerbalization = it.next();
				List<String> affectedMeasures = summary.get(pVerbalization);
				if (!usePrefix) {
					report.append(String.format("%s differences in performance for the %s. ", pVerbalization, createEnumeration(affectedMeasures)));
				} else {
					report.append(String.format("%s %s differences in performance for the %s. ", prefix, pVerbalization, createEnumeration(affectedMeasures)));
				}
				usePrefix = true;
			}
			report.append("\\\\ \n\n");

		}

		//
		// Appendix
		//
		// Add all figures
		report.append("\\FloatBarrier\n");
		report.append("\\section{Appendix}\n");
		for (int i = 0; i < figures.size(); i++) {
			ref = figures.get(i)[0];
			String caption = figures.get(i)[1];
			String filename = figures.get(i)[2];
			report.append("\\begin{figure}\n");
			report.append("\\centering\n");
			report.append(String.format("\\includegraphics[width=1\\linewidth]{%s}\n", filename));
			report.append(String.format("\\caption{%s}\n", caption));
			report.append(String.format("\\label{%s}\n", ref));
			report.append("\\end{figure}\n\n");
		}

		// Close document
		report.append("\\end{document}");
		return report.toString();

	}

	public String createPlainReport() {
		// Set locale to English globally to make reports independent of the
		// machine thei're created on, e.g. use "." as decimal points on any
		// machine
		Locale.setDefault(Locale.ENGLISH);

		StringBuilder report = new StringBuilder();

		//
		// Evaluation Overview
		//
		report.append("###\n");
		report.append("Evaluation Overview\n");
		report.append("###\n\n");

		int nModels = evalResults.getSampleData().getModelMetadata().size();
		ArrayList<String> measures = evalResults.getMeasures();
		String ref = "tbl:models";

		// Separate training/testing datasets
		List<String> trainingDataList = new ArrayList<String>();
		List<String> testingDataList = new ArrayList<String>();
		List<Pair<String, String>> datasets = evalResults.getSampleData().getDatasetNames();
		Iterator<Pair<String, String>> itp = datasets.iterator();
		while (itp.hasNext()) {
			Pair<String, String> trainTest = itp.next();
			trainingDataList.add(trainTest.getKey());
			if (trainTest.getValue() != null) {
				testingDataList.add(trainTest.getValue());
			}
		}
		Set<String> trainingDataSet = new HashSet<String>(trainingDataList);
		Set<String> testingDataSet = new HashSet<String>(testingDataList);

		String pipelineDescription = null;
		String sampleOrigin = "per CV";

		ReportTypes pipelineType = this.evalResults.getSampleData().getPipelineType();
		switch (pipelineType) {
		// One-domain n-fold CV (ReportData=per Fold)
		case CV:
			pipelineDescription = String.format("%d-fold cross validation", evalResults.getSampleData().getnFolds());
			sampleOrigin = "per fold ";
			break;
		case MULTIPLE_CV:
			pipelineDescription = String.format("%dx%s repeated cross validation", evalResults.getSampleData().getnRepetitions(), evalResults.getSampleData().getnFolds());
			break;
		case CV_DATASET_LVL:
			pipelineDescription = String.format("%d-fold cross validation over %d datasets", evalResults.getSampleData().getnFolds(), trainingDataSet.size());
			break;
		case MULTIPLE_CV_DATASET_LVL:
			pipelineDescription = String.format("%dx%s repeated cross validation over %d datasets", evalResults.getSampleData().getnRepetitions(), evalResults.getSampleData().getnFolds(), trainingDataSet.size());
			sampleOrigin = "per dataset";
			break;
		case TRAIN_TEST_DATASET_LVL:
			// In the train/test scenario, the number of datasets only includes
			// distinct ones
			Set<String> allDataSets = new HashSet<String>(testingDataSet);
			allDataSets.addAll(trainingDataSet);
			pipelineDescription = String.format("Train/Test over %d datasets", allDataSets.size());
			sampleOrigin = "per dataset";
			break;
		default:
			pipelineDescription = "!unknown pipeline type!";
			sampleOrigin = "!unknown pipeline type!";
			break;
		}

		boolean isBaselineEvaluation = evalResults.isBaselineEvaluation();
		report.append(String.format("The system performed a %s for the following %d models. \n", pipelineDescription, nModels));
		if (isBaselineEvaluation) {
			report.append(String.format("The models were compared against the first baseline model. \n", pipelineDescription, nModels));
		} else {
			report.append(String.format("The models were compared against each other. \n", pipelineDescription, nModels));
		}

		ArrayList<Pair<String, String>> modelMetadata = evalResults.getSampleData().getModelMetadata();
		for (int modelIndex = 0; modelIndex < modelMetadata.size(); modelIndex++) {
			String modelAlgorithm = modelMetadata.get(modelIndex).getKey();
			String modelFeatureSet = modelMetadata.get(modelIndex).getValue();
			report.append(String.format("M%d: %s; %s\n", modelIndex, modelAlgorithm, modelFeatureSet));
		}

		// List test/training datasets. Consider the case when these sets are
		// different.
		if (testingDataSet.isEmpty()) {
			if (trainingDataSet.size() == 1) {
				report.append(String.format("\nThe models were evaluated on the dataset %s. ", trainingDataList.get(0)));
			} else {
				report.append(String.format("\nThe models were evaluated on the datasets %s. ", this.createEnumeration(trainingDataList)));
			}
		} else {
			if (trainingDataSet.size() == 1 && testingDataSet.size() == 1) {
				report.append(String.format("\nThe models were trained on the dataset %s and tested on the dataset %s. ", trainingDataList.get(0), testingDataList.get(0)));
			} else if (trainingDataSet.size() > 1 && testingDataSet.size() == 1) {
				report.append(String.format("\nThe models were trained on the datasets %s and tested on the dataset %s. ", this.createEnumeration(new ArrayList<String>(trainingDataSet)), testingDataList.get(0)));
			} else if (trainingDataSet.size() == 1 && testingDataSet.size() > 1) {
				report.append(String.format("\nThe models were trained on the dataset %s and tested on the datasets %s. ", trainingDataList.get(0), this.createEnumeration(new ArrayList<String>(testingDataSet))));
			} else {
				report.append(String.format("\nThe models were trained on the datasets %s and tested on the datasets %s. ", this.createEnumeration(new ArrayList<String>(trainingDataSet)), this.createEnumeration(new ArrayList<String>(testingDataSet))));
			}
		}
		report.append(String.format("Their performance was assessed with the %s", createEnumeration(measures)));

		//
		// Results (for each measure separately)
		//
		report.append("\n\n###\n"); // All previous floats must be placed before
									// this point
		report.append("Results\n");
		report.append("###");

		for (int i = 0; i < measures.size(); i++) {

			// Continue for McNemar contingency matrix
			String measure = measures.get(i);
			if (!evalResults.getSampleData().getSamples().containsKey(measure)) {
				continue;
			}

			// Samples
			report.append("\n\n#\n");
			report.append(String.format("Evaluation for %s. \n", measure));
			report.append("#\n\n");

			report.append("Samples: \n");

			ArrayList<ArrayList<Double>> models = evalResults.getSampleData().getSamples().get(measure);
			for (int modelId = 0; i < models.size(); i++) {
				ArrayList<Double> samples = models.get(modelId);
				report.append(String.format("C%d: ", modelId));
				for (int j = 0; j < samples.size(); j++) {
					report.append(String.format("%.3f;", samples.get(j)));
				}
				report.append("\n");
			}
			report.append("\n");

			// Test results
			for (String testType : new String[] { "Parametric", "Non-Parametric" }) {
				report.append(String.format("%s Testing\n", testType));

				Pair<String, AbstractTestResult> result = null;
				if (testType.equals("Parametric")) {
					result = evalResults.getParametricTestResults().get(measure);
				} else {
					result = evalResults.getNonParametricTestResults().get(measure);
				}

				// Use pretty-print method descriptor if specified
				String method = result.getKey();
				if (StatsConfigConstants.PRETTY_PRINT_METHODS.containsKey(method)) {
					method = StatsConfigConstants.PRETTY_PRINT_METHODS.get(method);
				}

				TestResult r = (TestResult) result.getValue();
				report.append(String.format("The system compared the %d models using the %s. ", nModels, method));

				if (r != null && !Double.isNaN(r.getpValue())) {

					// A priori test: assumptions
					boolean assumptionViolated = false;
					Iterator<String> it = r.getAssumptions().keySet().iterator();
					while (it.hasNext()) {
						String assumption = it.next();
						TestResult at = (TestResult) r.getAssumptions().get(assumption);
						if (at == null) {
							report.append(String.format("Testing for %s failed. ", assumption));
							assumptionViolated = true;
							continue;
						}
						if (Double.isNaN(at.getpValue())) {
							report.append(String.format("Testing for %s using %s failed. ", assumption, at.getMethod()));
							assumptionViolated = true;
							continue;
						}
						double ap = at.getpValue();

						if (ap <= this.significance_low) {
							assumptionViolated = true;
						}

						// Verbalize result according to p value
						Pair<String, Double> verbalizedP = verbalizeP(ap, true);

						report.append(String.format("%s %s violation of %s (p=%f, alpha=%f). ", at.getMethod(), verbalizedP.getKey(), assumption, ap, verbalizedP.getValue()));

					}

					if (assumptionViolated) {
						report.append("Given that the assumptions are violated, the following test may be corrupted. ");
					}

					// A Priori test results
					Pair<String, Double> verbalizedP = verbalizeP(r.getpValue(), false);
					report.append(String.format("The %s %s differences between the performances of the models (p=%f, alpha=%f).\n\n", method, verbalizedP.getKey(), r.getpValue(), verbalizedP.getValue()));

					// Post-hoc test for >2 models (pairwise comparisons)
					if (evalResults.getSampleData().getModelMetadata().size() > 2) {

						Pair<String, AbstractTestResult> postHocResult = null;
						HashMap<Integer, TreeSet<Integer>> postHocOrdering = null;
						if (testType.equals("Parametric")) {
							postHocResult = evalResults.getParametricPostHocTestResults().get(measure);
							postHocOrdering = evalResults.getParameticPostHocOrdering().get(measure);
						} else {
							postHocResult = evalResults.getNonParametricPostHocTestResults().get(measure);
							postHocOrdering = evalResults.getNonParameticPostHocOrdering().get(measure);
						}
						method = postHocResult.getKey();
						if (StatsConfigConstants.PRETTY_PRINT_METHODS.containsKey(method)) {
							method = StatsConfigConstants.PRETTY_PRINT_METHODS.get(method);
						}

						PairwiseTestResult rPostHoc = (PairwiseTestResult) postHocResult.getValue();
						report.append(String.format("The system performed the %s post-hoc. ", method));

						if (rPostHoc == null) {
							report.append("The test failed. ");
							continue;
						}

						// Assumptions
						boolean assumptionsViolated = false;
						it = rPostHoc.getAssumptions().keySet().iterator();
						while (it.hasNext()) {
							String assumption = it.next();
							PairwiseTestResult at = (PairwiseTestResult) rPostHoc.getAssumptions().get(assumption);
							if (at == null) {
								report.append(String.format("Testing for %s failed. ", assumption));
								assumptionsViolated = true;
								continue;
							}

							report.append(String.format("\nTesting for %s using %s returned p-values:\n%s", assumption, at.getMethod(), this.pairwiseResultsToString(at.getpValue())));

							// Create table with pairwise p-values for
							// assumption testing
							double[][] ap = at.getpValue();
							double max = getMax(ap);
							double min = getMin(ap);
							verbalizedP = verbalizeP(min, true);
							if ((max > significance_low && min <= significance_low) || (max > significance_medium && min <= significance_medium) || (max > significance_high && min <= significance_high)) {
								// partly significant to degree as specified by
								// verbalized p-value
								report.append(String.format("%s partly %s violation of %s (alpha=%.2f).\n", at.getMethod(), verbalizedP.getKey(), assumption, verbalizedP.getValue()));
							} else {
								report.append(String.format("%s %s violation of %s (alpha=%.2f).\n", at.getMethod(), verbalizedP.getKey(), assumption, verbalizedP.getValue()));
							}

							if (min <= this.significance_low) {
								assumptionsViolated = true;
							}

						}

						if (assumptionViolated) {
							report.append("Given that the assumptions are violated, the following test may be corrupted. ");
						}

						// Result
						double[][] ap = rPostHoc.getpValue();
						report.append(String.format("P-values:\n%s", this.pairwiseResultsToString(rPostHoc.getpValue())));

						// Already fetch pairwise adjustments here in order to
						// determine choice of words
						double max = getMax(ap);
						double min = getMin(ap);
						verbalizedP = verbalizeP(min, false);
						ArrayList<String> adjustments = new ArrayList<String>(rPostHoc.getpValueCorrections().keySet());
						String adjustWord = "";
						if (adjustments.size() > 0) {
							adjustWord = " for non-adjusted p-values";
						}
						if ((max > significance_low && min <= significance_low) || (max > significance_medium && min <= significance_medium) || (max > significance_high && min <= significance_high)) {
							// partly significant to degree as specified by
							// verbalized p-value
							report.append(String.format("The %s partly %s differences between the performances of the models%s ($\\alpha=%.2f$, Tbl. \\ref{%s}). ", method, verbalizedP.getKey(), adjustWord, verbalizedP.getValue(), ref));
						} else {
							report.append(String.format("The %s %s differences between the performances of the models%s ($\\alpha=%.2f$, Tbl. \\ref{%s}). ", method, verbalizedP.getKey(), adjustWord, verbalizedP.getValue(), ref));
						}

						// Determine ordering of models
						String ordering = getModelOrderingRepresentation(postHocOrdering);
						report.append(ordering);
						report.append("\n\n");

						// Pairwise adjustments
						if (adjustments.size() > 0) {
							double[] minAdjustments = new double[adjustments.size()];
							double[] maxAdjustments = new double[adjustments.size()];
							for (int j = 0; j < adjustments.size(); j++) {
								String adjustmentMethod = adjustments.get(j);
								double[][] correctedP = rPostHoc.getpValueCorrections().get(adjustmentMethod);
								if (StatsConfigConstants.PRETTY_PRINT_METHODS.containsKey(adjustmentMethod)) {
									adjustmentMethod = StatsConfigConstants.PRETTY_PRINT_METHODS.get(adjustmentMethod);
								}
								report.append(String.format("\nAdjusted p-values according to %s:\n%s", adjustmentMethod, this.pairwiseResultsToString(correctedP)));

								minAdjustments[j] = getMin(correctedP);
								maxAdjustments[j] = getMax(correctedP);
							}

							min = getMin(minAdjustments);
							max = getMax(maxAdjustments);
							verbalizedP = verbalizeP(min, false);

							if ((max > significance_low && min <= significance_low) || (max > significance_medium && min <= significance_medium) || (max > significance_high && min <= significance_high)) {
								// partly significant to degree as specified by
								// verbalized p-value
								report.append(String.format("It partly %s differences for adjusted p-values (alpha=%.2f$).\n\n ", verbalizedP.getKey(), verbalizedP.getValue(), ref));
							} else {
								report.append(String.format("It %s differences for adjusted p-values (alpha=%.2f$).\n\n ", verbalizedP.getKey(), verbalizedP.getValue(), ref));
							}
						}
					}
				} else {
					report.append(String.format("The %s failed.", method));
				}
			}
		}

		//
		// Contingency table and McNemar results if this test was performed
		//
		if (evalResults.getNonParametricTest().equals("McNemar")) {
			String measure = "Contingency Table";
			String testType = "Non-Parametric";
			report.append("\n\n#\n");
			report.append("Evaluation for Contingency Table\n");
			report.append("#\n\n");

			int[][] contingencyMatrix = evalResults.getSampleData().getContingencyMatrix();
			if (evalResults.getSampleData().getPipelineType() == ReportTypes.MULTIPLE_CV) {
				report.append(String.format("Contingency table drawn from the %s and the %d models. The correctly and incorrectly classified instances per fold were averaged over all repetitions:\n%s\n", pipelineDescription, nModels,
						this.contingencyMatrixToString(contingencyMatrix)));
			} else {
				report.append(String.format("Contingency table drawn from the %s and the %d models:\n%s\n", pipelineDescription, nModels, this.contingencyMatrixToString(contingencyMatrix)));
			}

			// Test results
			report.append(String.format("%s Testing\n", testType));
			report.append(String.format("The system compared the %d models using the McNemar test. ", nModels));
			Pair<String, AbstractTestResult> result = evalResults.getNonParametricTestResults().get(measure);

			TestResult r = (TestResult) result.getValue();
			if (r != null && !Double.isNaN(r.getpValue())) {
				StringBuilder parameters = new StringBuilder();
				Iterator<String> it = r.getParameter().keySet().iterator();
				while (it.hasNext()) {
					String parameter = it.next();
					double value = r.getParameter().get(parameter);
					parameters.append(String.format("%s=%.3f, ", parameter, value));
				}

				// Verbalize result according to p value
				Pair<String, Double> verbalizedP = verbalizeP(r.getpValue(), false);
				report.append(String.format("The test %s differences between the performances of the models (%sp=%.3f, alpha=%.2f).\\\\ \n", verbalizedP.getKey(), parameters.toString(), r.getpValue(), verbalizedP.getValue()));

			} else {
				report.append("The test failed.\n");
			}
		}

		return report.toString();
	}

	/**
	 * Create a written enumeration of several items by adding dashes/"and"
	 * inbetween
	 * 
	 * @param items
	 *            the items to be enumerated as a list of Strings
	 * @return a String representing the enumeration
	 */
	private String createEnumeration(List<String> items) {
		if (items.size() < 1) {
			return "";
		} else if (items.size() == 1) {
			return items.get(0);
		} else {
			StringBuilder enumeration = new StringBuilder();
			String separator = "";
			for (int i = 0; i < items.size() - 1; i++) {
				String item = items.get(i);
				item = escapeLatexCharacters(item);
				enumeration.append(String.format("%s%s", separator, item));
				separator = ", ";
			}
			enumeration.append(String.format(" and %s", items.get(items.size() - 1)));
			return enumeration.toString();
		}
	}

	/**
	 * Creates an array of p-values with assigned signficance indicators and
	 * headers
	 * 
	 * @param ap
	 *            a double-array of p-values
	 * @return A Pair with the table header and table values as String-arrays
	 */
	private Pair<String[], String[][]> getPValueStringArray(double[][] ap, boolean isBaselineEvaluation) {

		// Create table with pairwise p-values for assumption testing

		int nCols = ap.length;
		int nRows = ap.length;
		if (isBaselineEvaluation) {
			nCols = 1;
		}

		// Create header first
		String[] header = new String[nCols + 1];
		header[0] = "";
		for (int j = 0; j < nCols; j++) {
			header[j + 1] = String.format("M%d", j);
		}

		// Create value array
		String[][] values = new String[nRows][];
		for (int j = 0; j < nRows; j++) {
			values[j] = new String[nCols + 1];
			values[j][0] = String.format("M%d", j + 1);
			for (int k = 0; k < nCols; k++) {
				if (Double.isNaN(ap[j][k])) {
					values[j][k + 1] = "-";
				} else {
					String significance = "";
					if (ap[j][k] <= significance_high) {
						significance = "{\\footnotesize ***}";
					} else if (ap[j][k] <= significance_medium) {
						significance = "{\\footnotesize **}";
					} else if (ap[j][k] <= significance_low) {
						significance = "{\\footnotesize *}";
					}
					values[j][k + 1] = String.format("%.3f%s", ap[j][k], significance);
				}
			}
		}

		return Pair.of(header, values);
	}

	private String contingencyMatrixToString(int[][] array) {

		StringBuilder str = new StringBuilder();
		if (array == null) {
			return "";
		}
		if (array.length != 2 || array[0].length != 2) {
			return "";
		}

		// Header
		str.append(String.format("            M1 Wrong  M1 Correct\n"));

		// Values
		for (int i = 0; i < array.length; i++) {
			int[] subarray = array[i];
			if (i == 0) {
				str.append("M0 Wrong  ");
			} else {
				str.append("M0 Correct");
			}
			for (int p : subarray) {
				str.append(String.format("%10d ", p));
			}
			str.append("\n");
		}
		return str.toString();
	}

	private String pairwiseResultsToString(double[][] array) {

		StringBuilder str = new StringBuilder();
		if (array == null) {
			return "";
		}

		// Header
		str.append(String.format("    "));
		for (int i = 0; i < array.length; i++) {
			str.append(String.format("M%d%4s", i, " "));
		}
		str.append("\n");

		// Values
		for (int i = 0; i < array.length; i++) {
			double[] subarray = array[i];
			str.append(String.format("M%d  ", i + 1));
			for (double p : subarray) {
				if (Double.isNaN(p)) {
					str.append(String.format("%f%3s", p, " "));
					// str.append(String.format("%f  ",p));
				} else {
					str.append(String.format("%.3f ", p));
				}
			}
			str.append("\n");
		}
		return str.toString();
	}

	/**
	 * Get the largest double value in a two-dimensional double array
	 * 
	 * @param twodArray
	 *            a two-dimensional double array
	 * @return The largest double value in the array
	 */
	private double getMax(double[][] twodArray) {
		return getMax(flattenCubicArray(twodArray));
	}

	/**
	 * Get the smalles double value in a two-dimensional double array
	 * 
	 * @param twodArray
	 *            a two-dimensional double array
	 * @return The smalles double value in the array
	 */
	private double getMin(double[][] twodArray) {
		return getMin(flattenCubicArray(twodArray));
	}

	/**
	 * Get the largest double value in a double array
	 * 
	 * @param array
	 *            a double array
	 * @return The largest double value in the array
	 */
	private double getMax(double[] array) {
		double maxValue = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > maxValue) {
				maxValue = array[i];
			}
		}
		return maxValue;
	}

	/**
	 * Get the smalles double value in a double array
	 * 
	 * @param array
	 *            a double array
	 * @return The smalles double value in the array
	 */
	private double getMin(double[] array) {
		double minValue = Double.POSITIVE_INFINITY;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < minValue) {
				minValue = array[i];
			}
		}
		return minValue;
	}

	/**
	 * Flattens a cubic array (dimensions equal) to a one-dimensional array
	 * 
	 * @param array
	 *            the two-dimensional, cubic array to be flattened
	 * @return the flattened, one-dimensional array
	 */
	private double[] flattenCubicArray(double[][] array) {
		double[] flattened = new double[array.length * array.length];
		int i = 0;
		for (double[] subarray : array) {
			for (double d : subarray) {
				flattened[i++] = d;
			}
		}
		return flattened;
	}

	/**
	 * Verbalizes a p-value according to different significance levels (defined
	 * globally) and use-cases
	 * 
	 * @param p
	 *            the p-value to be verbalized
	 * @param isAssumption
	 *            boolean to signfiy whether the p-values belongs to a test
	 *            assumption or the test itself. The verbalizations will be
	 *            different.
	 * @return a verbalization of the p-value according to the different
	 *         significance levels
	 */
	private Pair<String, Double> verbalizeP(double p, boolean isAssumption) {
		Pair<String, Double> test = null;
		Pair<String, Double> assumption = null;
		if (p > significance_low) {
			if (isAssumption) {
				assumption = Pair.of("did not show a", significance_low);
			} else {
				test = Pair.of("did not show significant", significance_low);
			}
		} else if (p <= significance_low && p > significance_medium) {
			if (isAssumption) {
				assumption = Pair.of("indicated a weak", significance_low);
			} else {
				test = Pair.of("showed weak significant", significance_low);
			}
		} else if (p <= significance_medium && p > significance_high) {
			if (isAssumption) {
				assumption = Pair.of("indicated a medium", significance_medium);
			} else {
				test = Pair.of("showed medium significant", significance_medium);
			}
		} else {
			if (isAssumption) {
				assumption = Pair.of("indicated a strong", significance_high);
			} else {
				test = Pair.of("showed strong significant", significance_high);
			}
		}

		if (isAssumption) {
			return assumption;
		}
		return test;

	}

	private String fixSlashes(String filename) {
		if (File.separator.equals("\\")) {
			return filename.replace("\\", "\\\\");
		} else {
			return filename.replace("/", "//");
		}
	}

}
