package de.tudarmstadt.tk.statistics.config;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Comprises allowed values (e.g. available performance measures, statistical
 * tests, etc.) for the stats setup config file
 * 
 * @author Guckelsberger, Schulz
 */

public abstract class StatsConfigConstants {

	public static enum TEST_CLASSES{
		TwoSamplesNonParametricContingency,
		TwoSamplesParametric,
		TwoSamplesNonParametric,
		MultipleSamplesParametric,
		MultipleSamplesNonParametric,
		MultipleSamplesParametricPosthoc,
		MultipleSamplesNonParametricPostHoc,
		MultipleSamplesParametricPosthocBaseline,
		MultipleSamplesNonParametricPostHocBaseline
		};
		
	public static enum INDEPENDENT_VARIABLES_VALUES{Classifier, FeatureSet};
	
	public static enum CORRECTION_VALUES{bonferroni, holm, hochberg, hommel, BH, BY};
	
	public static enum SIGNIFICANCE_LEVEL_VALUES{low, medium, high};
	
	public static final ArrayList<String> TWO_SAMPLES_NONPARAMETRIC_CONTINGENCY_TABLE_VALUES = new ArrayList<String>(){{add("McNemar");}};
	public static final ArrayList<String> TWO_SAMPLES_PARAMETRIC_VALUES = new ArrayList<String>(){{add("DependentT");}};
	public static final ArrayList<String> TWO_SAMPLES_NONPARAMETRIC_VALUES = new ArrayList<String>(){{add("WilcoxonSignedRank");}};
	public static final ArrayList<String> MULTIPLE_SAMPLES_PARAMETRIC_VALUES = new ArrayList<String>(){{add("RepeatedMeasuresOneWayANOVA");}};
	public static final ArrayList<String> MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC_VALUES = new ArrayList<String>(){{add("PairwiseDependentT"); add("Tukey");}};
	public static final ArrayList<String> MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC_BASELINE_VALUES = new ArrayList<String>(){{add("Dunett");}};
	public static final ArrayList<String> MULTIPLE_SAMPLES_NONPARAMETRIC_VALUES = new ArrayList<String>(){{add("Friedman");}};
	public static final ArrayList<String> MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC_VALUES = new ArrayList<String>(){{add("Nemenyi");}};
	public static final ArrayList<String> MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC_BASELINE_VALUES = new ArrayList<String>(){{add("PairwiseWilcoxonSignedRank");}};
	
	public static final HashMap<TEST_CLASSES,ArrayList<String>> TESTS = new HashMap<TEST_CLASSES,ArrayList<String>>(){{
		put(TEST_CLASSES.TwoSamplesNonParametricContingency,TWO_SAMPLES_NONPARAMETRIC_CONTINGENCY_TABLE_VALUES);
		put(TEST_CLASSES.TwoSamplesParametric,TWO_SAMPLES_PARAMETRIC_VALUES);
		put(TEST_CLASSES.TwoSamplesNonParametric,TWO_SAMPLES_NONPARAMETRIC_VALUES);
		put(TEST_CLASSES.MultipleSamplesParametric,MULTIPLE_SAMPLES_PARAMETRIC_VALUES);
		put(TEST_CLASSES.MultipleSamplesNonParametric,MULTIPLE_SAMPLES_NONPARAMETRIC_VALUES);
		put(TEST_CLASSES.MultipleSamplesParametricPosthoc,MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC_VALUES);
		put(TEST_CLASSES.MultipleSamplesNonParametricPostHoc,MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC_VALUES);
		put(TEST_CLASSES.MultipleSamplesParametricPosthocBaseline,MULTIPLE_SAMPLES_PARAMETRIC_POSTHOC_BASELINE_VALUES);
		put(TEST_CLASSES.MultipleSamplesNonParametricPostHocBaseline,MULTIPLE_SAMPLES_NONPARAMETRIC_POSTHOC_BASELINE_VALUES);};
	};

	public static final HashMap<String,String> PRETTY_PRINT_METHODS = new HashMap<String,String>(){
						{put("WilcoxonSignedRank","Wilcoxon signed-rank test"); 
						put("DependentT","dependent t-test"); 
						put("Friedman","Friedman test");
						put("Nemenyi","Nemenyi test");
						put("RepeatedMeasuresOneWayANOVA","repeated-measures one-way ANOVA"); 
						put("PairwiseDependentT","pairwise dependent T-test");
						put("McNemar","McNemar test");
						put("bonferroni","Bonferroni");
						put("holm","Holm");
						put("hochberg","Hochberg");
						put("hommel","Hommel");
						put("BH","Benjamini-Hochberg");
						put("BY","Benjamini-Yekutieli");
						put("Tukey","Tukey's test");
						put("Dunett","Dunett's test");
						put("PairwiseWilcoxonSignedRank","Pairwise Wilcoxon signed-rank test");}
					};
	
	}
						