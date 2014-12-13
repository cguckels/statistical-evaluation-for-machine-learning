package de.tudarmstadt.tk.statistics.examples;

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

import org.junit.Test;

import de.tudarmstadt.tk.statistics.config.StatsConfig;
import de.tudarmstadt.tk.statistics.config.StatsConfigConstants;
import de.tudarmstadt.tk.statistics.importer.ExternalResultsReader;

public class Example4ClassesCV {
	
	@Test
	public void testCVInput(){

		//Configuration via file
		/*
		StatsConfig config = StatsConfig.getInstance("config.xml");
		*/
		
		//Configuration programmatically
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
		
		String csvPath = "src/main/resources/examples/4ClassesCVExample.csv";
		String outputPath = "src/main/resources/examples/";
		ExternalResultsReader.evaluateCV(config, csvPath, outputPath, ';');		
		
	}

}
