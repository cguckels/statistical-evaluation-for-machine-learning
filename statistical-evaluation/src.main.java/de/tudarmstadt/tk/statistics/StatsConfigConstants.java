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

import java.util.HashMap;

/**
 * Comprises allowed values (e.g. available performance measures, statistical
 * tests, etc.) for the stats_setup config file
 * 
 * @author Guckelsberger, Schulz
 */
public abstract class StatsConfigConstants {

	public static final String MEASURES = "measures";
	public static final String MEASURE = "measure";
	public static final String TESTS = "tests";
	public static final String CORRECTIONS = "corrections";
	public static final String CORRECTION = "correction";
	public static final String TWO_SAMPLES_NONPARAMETRIC_CONTINGENCY_TABLE = "two samples, non-parametric with contingency table";
	public static final String TWO_SAMPLES_NONPARAMETRIC = "two samples, non-parametric";
	public static final String TWO_SAMPLES_PARAMETRIC = "two samples, parametric";
	public static final String MULTIPLE_SAMPLES_PARAMETRIC = "multiple samples, parametric";
	public static final String MULTIPLE_SAMPLES_NONPARAMETRIC = "multiple samples, non-parametric";
	public static final String MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC = "multiple samples, parametric, post-hoc";
	public static final String MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC = "multiple samples, non-parametric, post-hoc";
	public static final String MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC_BASELINE = "multiple samples, parametric, post-hoc baseline";
	public static final String MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC_BASELINE = "multiple samples, non-parametric, post-hoc baseline";
	public static final String SIGNIFICANCE_LOW = "low significance";
	public static final String SIGNIFICANCE_MEDIUM = "medium significance";
	public static final String SIGNIFICANCE_HIGH = "high significance";
	public static final String SELECT_BEST_N = "select best n";
	public static final String SELECT_BEST_N_BY_MEASURE = "select best n by measure";

	public static final String[] CORRECTION_VALUES = new String[] { "bonferroni", "holm", "hochberg", "hommel", "BH", "BY" };
	// public static final String[] MEASURE_VALUES = new
	// String[]{ReportConstants.PRECISION,ReportConstants.RECALL,
	// ReportConstants.FMEASURE,ReportConstants.WGT_PRECISION,ReportConstants.WGT_RECALL,
	// ReportConstants.WGT_FMEASURE};
	public static final String[] MEASURE_VALUES = new String[] { ReportConstants.WGT_PRECISION, ReportConstants.WGT_RECALL, ReportConstants.WGT_FMEASURE };
	public static final String[] TWO_SAMPLES_NONPARAMETRIC_CONTINGENCY_TABLE_VALUES = new String[] { "McNemar" };
	public static final String[] TWO_SAMPLES_PARAMETRIC_VALUES = new String[] { "DependentT" };
	public static final String[] TWO_SAMPLES_NONPARAMETRIC_VALUES = new String[] { "WilcoxonSignedRank" };
	public static final String[] MULTIPLE_SAMPLES_PARAMETRIC_VALUES = new String[] { "RepeatedMeasuresOneWayANOVA" };
	public static final String[] MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC_VALUES = new String[] { "PairwiseDependentT", "Tukey" };
	public static final String[] MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC_BASELINE_VALUES = new String[] { "Dunett" };
	public static final String[] MULTIPLE_SAMPLES_NONPARAMETRIC_VALUES = new String[] { "Friedman" };
	public static final String[] MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC_VALUES = new String[] { "Nemenyi" };
	public static final String[] MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC_BASELINE_VALUES = new String[] { "PairwiseWilcoxonSignedRank" };
	public static final HashMap<String, String> PRETTY_PRINT_METHODS = new HashMap<String, String>() {
		{
			put("WilcoxonSignedRank", "Wilcoxon signed-rank test");
			put("DependentT", "dependent t-test");
			put("Friedman", "Friedman test");
			put("Nemenyi", "Nemenyi test");
			put("RepeatedMeasuresOneWayANOVA", "repeated-measures one-way ANOVA");
			put("PairwiseDependentT", "pairwise dependent T-test");
			put("McNemar", "McNemar test");
			put("bonferroni", "Bonferroni");
			put("holm", "Holm");
			put("hochberg", "Hochberg");
			put("hommel", "Hommel");
			put("BH", "Benjamini-Hochberg");
			put("BY", "Benjamini-Yekutieli");
			put("Tukey", "Tukey's test");
			put("Dunett", "Dunett's test");
			put("PairwiseWilcoxonSignedRank", "Pairwise Wilcoxon signed-rank test");
		}
	};
}