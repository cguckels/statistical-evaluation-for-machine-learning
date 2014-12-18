package de.tudarmstadt.tk.statistics.helper;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tudarmstadt.tk.statistics.test.SampleData;

/**
 * @author Guckelsberger, Schulz
 */
public class Helpers {

	private static final Logger logger = LogManager.getLogger("Statistics");

	/**
	 * Remove models with the worst performance measures from the sample data if
	 * the no. of models exceeds a certain threshold. If a baseline evaluation
	 * is performed, the baseline model is retained. If the number of models
	 * does not exceed the specified threshold, nothing happens.
	 * 
	 * @param sampleData
	 *            An object of type {@link StatisticalEvaluationData}
	 * @param selectBestN
	 *            Maximum number of models
	 * @param selectByMeasure
	 *            Measure by which to select the models
	 * @return the truncated SampleData object
	 */
	public static SampleData truncateData(SampleData sampleData, int selectBestN, String selectByMeasure) {

		int nModels = sampleData.getModelMetadata().size();

		// Only select a subset of the data if there're more models than the
		// specified maximum
		if (nModels > selectBestN && nModels > 1) {

			// Get sample averages
			ArrayList<Double> sampleAverages = sampleData.getSamplesAverage().get(selectByMeasure);
			if (sampleAverages == null) {
				sampleAverages = sampleData.getSamplesAverage().get("Averaged " + selectByMeasure);
			}

			if (sampleAverages == null) {
				logger.log(Level.ERROR, "Measure for model selection not available in sample data! No selection.");
				return sampleData;
			}

			// Sort them
			ArrayList<Pair<Integer, Double>> sortedAvgs = new ArrayList<Pair<Integer, Double>>();
			for (int i = 0; i < sampleAverages.size(); i++) {
				sortedAvgs.add(Pair.of(i, sampleAverages.get(i)));
			}

			Collections.sort(sortedAvgs, new PairValueComparator());

			ArrayList<Integer> obsolete = new ArrayList<Integer>();
			for (int i = 0; i < sortedAvgs.size() - selectBestN; i++) {
				int toBeRemoved = sortedAvgs.get(i).getKey();
				// If this is a baseline evaluation, do not remove the baseline
				// model
				if (sampleData.isBaselineEvaluation() && toBeRemoved == 0) {
					continue;
				}
				obsolete.add(toBeRemoved);
			}

			Collections.sort(obsolete);
			Collections.reverse(obsolete);

			// Remove obsolete samples
			Iterator<String> itm = sampleData.getSamplesAverage().keySet().iterator();
			while (itm.hasNext()) {
				String measure = itm.next();
				for (int i = 0; i < obsolete.size(); i++) {
					sampleData.getSamplesAverage().get(measure).remove((int) obsolete.get(i));
					sampleData.getSamples().get(measure).remove((int) obsolete.get(i));
				}
			}
			// Remove obsolete model metadata
			for (int i = 0; i < obsolete.size(); i++) {
				sampleData.getModelMetadata().remove((int) obsolete.get(i));
			}

		}

		return sampleData;
	}
	
	

	public static class LexicographicArrayComparator implements Comparator<String[]> {
		@Override
		public int compare(String[] a, String[] b) {
			if (a[0].equals(b[0])) {
				return a[1].compareToIgnoreCase(b[1]);
			} else {
				return a[0].compareToIgnoreCase(b[0]);
			}
		}
	}

	static class LexicographicPairComparator implements Comparator<Pair<String, String>> {
		@Override
		public int compare(Pair<String, String> a, Pair<String, String> b) {
			if (a.getLeft().equals(b.getLeft())) {
				return a.getRight().compareToIgnoreCase(b.getRight());
			} else {
				return a.getLeft().compareToIgnoreCase(b.getLeft());
			}
		}
	}

	static class PairValueComparator implements Comparator<Pair<Integer, Double>> {
		@Override
		public int compare(Pair<Integer, Double> a, Pair<Integer, Double> b) {
			return a.getRight().compareTo(b.getRight());
		}
	}

}
