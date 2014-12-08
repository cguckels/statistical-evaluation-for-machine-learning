package de.tudarmstadt.tk.statistics;

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

import org.apache.commons.lang3.tuple.Pair;

/**
 * Object to store the entire sample information for a particular pipeline run
 * @author Guckelsberger, Schulz
 *
 */
public class StatisticalEvaluationData {

	//Pipeline type
	private ReportTypes pipelineType;
	
	//Contingency matrix: Only!=null in case of two classifiers on a single domain
	private int[][] contingencyMatrix;
	
	//Samples: performance measure; model index; sample list
	//Example: F-Measure, ((M1, (43,543,43,21)),(M2, (343,23,23)))
	private HashMap<String, ArrayList<ArrayList<Double>>> samples;
	
	//Sum of samples: performance measure; model index; sum of samples
	private HashMap<String,ArrayList<Double>> samplesAverage;
	
	//Names of train/test datasets used in evaluation
	private List<Pair<String,String>> datasetNames;
	
	//Metadata of the models (classifier, feature set)
	private ArrayList<Pair<String,String>> modelMetadata;
	
	//Number of folds in case of a CV
	private int nFolds;
	
	//Number of repetitions in case of a repeated evaluation
	private int nRepetitions;
	
	//Indicates if this is a nxn or 1:n baseline evaluation
	private boolean isBaselineEvaluation;

	/**
	 * Creates an object he entire sample information for a particular pipeline run
	 * @param samples A 2-dimensional HashMap. 1st level key: performance measure type. 2nd level key: model index, value: sample values
	 * @param samplesAverage A HashMap comprising the average sample value per model (2nd level) and performance measure (1st level)
	 * @param datasetNames A List with the train/test dataset names
	 * @param modelMetadata A HashMap comprising the metadata for each model (value), identified by an index (key)
	 * @param isBaselineEvaluation A boolean expressing whether this test result came from a 1:n (baseline) or n:m evaluation
	 */
	public StatisticalEvaluationData(
			int[][] contingencyMatrix,
			HashMap<String, ArrayList<ArrayList<Double>>> samples,
			HashMap<String, ArrayList<Double>> samplesAverage,
			List<Pair<String, String>> datasetNames,
			ArrayList<Pair<String,String>> modelMetadata,
			ReportTypes pipelineType,
			int nFolds,
			int nRepetitions,
			boolean isBaselineEvaluation) {
		this.contingencyMatrix=contingencyMatrix;
		this.samples = samples;
		this.samplesAverage = samplesAverage;
		this.datasetNames = datasetNames;
		this.modelMetadata = modelMetadata;
		this.pipelineType = pipelineType;
		this.nFolds=nFolds;
		this.nRepetitions=nRepetitions;
		this.isBaselineEvaluation=isBaselineEvaluation;
	}

	public int[][] getContingencyMatrix() {
		return contingencyMatrix;
	}
	public HashMap<String, ArrayList<ArrayList<Double>>> getSamples() {
		return samples;
	}
	public HashMap<String, ArrayList<Double>> getSamplesAverage() {
		return samplesAverage;
	}
	public List<Pair<String, String>> getDatasetNames() {
		return datasetNames;
	}
	public ArrayList<Pair<String,String>> getModelMetadata() {
		return modelMetadata;
	}
	public ReportTypes getPipelineType() {
		return pipelineType;
	}
	public int getnFolds() {
		return nFolds;
	}
	public int getnRepetitions() {
		return nRepetitions;
	}
	public boolean isBaselineEvaluation() {
		return isBaselineEvaluation;
	}
}
