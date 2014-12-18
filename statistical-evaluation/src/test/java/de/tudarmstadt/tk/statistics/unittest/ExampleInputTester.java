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

import org.junit.Test;

import de.tudarmstadt.tk.statistics.config.StatsConfig;
import de.tudarmstadt.tk.statistics.config.StatsConfigConstants;
import de.tudarmstadt.tk.statistics.test.StatsProcessor;

public class ExampleInputTester {
	
	
	/*
	 * The file contains performance samples for three feature sets and a fixed classification algorithm. One model is defined as a baseline to compare the others against.
	 */
	//Works
	@Test
	public void testCVFeaturesBaseline(){
		
		StatsConfig config = StatsConfig.getInstance();

		String csvPath = "src/main/resources/examples/CVFeaturesBaseline.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateCV(config, csvPath, outputPath, ';');	
		
	}
	

	/*
	 * The file contains performance samples for four different classifiers and a fixed feature set. One model is defined as a baseline to compare the others against.
	 */
	//Works
	@Test
	public void testCVClassifierBaseline(){
		
		StatsConfig config = StatsConfig.getInstance();
		config.setFixIndependentVariable(StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.Classifier);

		String csvPath = "src/main/resources/examples/CVClassifierBaseline.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateCV(config, csvPath, outputPath, ';');	
		
	}
	
	/*
	 * The file contains performance samples for four classification algorithms and two feature sets, resulting in 8 different models.
	 * The dataset is split according to the "fixIndependentVariable" setting in the configuration.
	 */
	//Works
	@Test
	public void testCVTwoIV(){
		
		StatsConfig config = StatsConfig.getInstance();
		config.setFixIndependentVariable(StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.FeatureSet);

		String csvPath = "src/main/resources/examples/CV2IV.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateCV(config, csvPath, outputPath, ';');	
		
	}
	
	
	/*
	 * The file contains performance samples for two different classification algorithms and three different feature sets, resulting in 6 different models.
	 * The dataset is split differently depending on the "fixIndependentVariable" setting in the configuration.
	 */
	@Test
	public void testTrainTestTwoIV(){
		
		StatsConfig config = StatsConfig.getInstance();
		config.setFixIndependentVariable(StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.Classifier);

		String csvPath = "src/main/resources/examples/TrainTest2IV.csv";
		String outputPath = "src/main/resources/examples/";
		StatsProcessor.evaluateTrainTest(config, csvPath, outputPath, ';');	
		
	}

}
