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
