package de.tudarmstadt.tk.statistics.unittest;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import de.tudarmstadt.tk.statistics.config.StatsConfig;
import de.tudarmstadt.tk.statistics.config.StatsConfigConstants;
import de.tudarmstadt.tk.statistics.test.StatsProcessor;

/**
 * Several methods reading different sorts of example data and demonstrating the different approaches of configuring STATSREP-ML 
 * @author Guckelsberger, Schulz
 *
 */
public class ExampleInputTester {
	
	/*
	 * The file contains performance samples for three classification algorithms and a fixed feature set.
	 */
	@Test
	public void testCV(){
        
        //Programmatic configuration, setting everything manually
        HashMap<StatsConfigConstants.TEST_CLASSES,String> requiredTests = new HashMap<StatsConfigConstants.TEST_CLASSES,String>();
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.TwoSamplesNonParametricContingency, "McNemar");
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.TwoSamplesParametric, "DependentT");
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.TwoSamplesNonParametric, "WilcoxonSignedRank");
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametric, "RepeatedMeasuresOneWayANOVA");
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametric, "Friedman");
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametricPosthoc, "Tukey");
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametricPostHoc, "Nemenyi");
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesParametricPosthocBaseline, "Dunett");
        requiredTests.put(StatsConfigConstants.TEST_CLASSES.MultipleSamplesNonParametricPostHocBaseline, "PairwiseWilcoxonSignedRank");
        
        List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections = new ArrayList<StatsConfigConstants.CORRECTION_VALUES>();
        requiredCorrections.add(StatsConfigConstants.CORRECTION_VALUES.bonferroni);
        requiredCorrections.add(StatsConfigConstants.CORRECTION_VALUES.hochberg);
        requiredCorrections.add(StatsConfigConstants.CORRECTION_VALUES.holm);
        
        HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES, Double> significanceLevels = new HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES, Double>();
        significanceLevels.put(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.low, 0.1);
        significanceLevels.put(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.medium, 0.05);
        significanceLevels.put(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.high, 0.01);

        StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable = StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.FeatureSet;

        int selectBestN = 10;
        String selectByMeasure = "Weighted F-Measure";
        
        StatsConfig config = StatsConfig.getInstance(requiredTests, requiredCorrections, significanceLevels, selectBestN, selectByMeasure, fixIndependentVariable);

		String csvPath = "src/main/resources/examples/CV.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateCV(config, csvPath, outputPath, ';');	
		
	}
	
	/*
	 * The file contains performance samples for three feature sets and a fixed classification algorithm. One model is defined as a baseline to compare the others against.
	 */
	@Test
	public void testCVFeaturesBaseline(){
		
        //Programmatic configuration, using default values
		StatsConfig config = StatsConfig.getInstance();

		String csvPath = "src/main/resources/examples/CVFeaturesBaseline.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateCV(config, csvPath, outputPath, ';');	
		
	}

	/*
	 * The file contains performance samples for four different classifiers and a fixed feature set. One model is defined as a baseline to compare the others against.
	 */
	@Test
	public void testCVClassifierBaseline(){
		
		//Read configuration from file and alter it slightly
        StatsConfig config = StatsConfig.getInstance("config.xml");
		config.setFixIndependentVariable(StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.Classifier);

		String csvPath = "src/main/resources/examples/CVClassifierBaseline.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateCV(config, csvPath, outputPath, ';');	
		
	}
	
	/*
	 * The file contains performance samples for four classification algorithms and two feature sets, resulting in 8 different models.
	 * The dataset is split according to the "fixIndependentVariable" setting in the configuration.
	 */
	@Test
	public void testCVTwoIV(){
		
		StatsConfig config = StatsConfig.getInstance();
		config.setFixIndependentVariable(StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.FeatureSet);

		String csvPath = "src/main/resources/examples/CV2IV.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateCV(config, csvPath, outputPath, ';');	
		
	}

	/*
	 * The file contains performance samples from training and testing one classifier with 10 different feature sets on 10 datasets.
	 * One feature set represents a baselien which the others extend and are compared against.
	 */
	@Test
	public void testTrainTestFeaturesBaseline(){
		
		StatsConfig config = StatsConfig.getInstance();

		String csvPath = "src/main/resources/examples/TrainTestFeaturesBaseline.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateTrainTest(config, csvPath, outputPath, ';');	
		
	}

}
