package de.tudarmstadt.tk.statistics.config;

import java.io.Serializable;
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


/**
 * An enum of possible data collection methods
 * @author Guckelsberger, Schulz
 */
public enum ReportTypes implements Serializable{
	
	
	/**
	 * a normal n-fold CrossValidation.<br>
	 * For List&lt;List&lt;ReportData&gt;&gt; in PipelineReportPerDataset:<br>
	 * Outer list: results of each FOLD of the CV (so there are n entries).<br>
	 * Inner list: Different models (feature sets) for each FOLD of the pipeline.<br>
	 * For List of datasetNames:<br>
	 * # of entries == # of FOLDS -> they share the datasetName, so all entries are equivalent.<br>
	 * This is a first inner level report!<br>
	 */
	CV,
	
	/**
	 * an n-fold CrossValidation that gets repeated k times (aka runs).<br>
	 * For List&lt;List&lt;ReportData&gt;&gt; in PipelineReportPerDataset:<br>
	 * Outer list: results of each RUN of the CV (so there are k entries).<br>
	 * Inner list: Different models (feature sets) for each RUN of the Multiple CV.<br>
	 * For List of datasetNames:<br>
	 * # of entries == # of RUNS -> they share the datasetName, so all entries are equivalent.<br>
	 * This is a second inner level report! (It contains a {@link PipelineTypes}.CV as first inner level report)
	 */
	MULTIPLE_CV,
	
	/**
	 * The dataset lvl of a normal n-fold CrossValidation.<br>
	 * For List&lt;List&lt;ReportData&gt;&gt; in PipelineReportPerDataset:<br>
	 * Outer list: results of each input dataset of the CV (so there are as many entries as specified input files).<br>
	 * Inner list: Different models (feature sets) for each CV of the pipeline.<br>
	 * For List of datasetNames:<br>
	 * # of entries == # of input datasets<br>
	 * This is a outer level report! (It contains a CV as inner level report)<br>
	 */
	CV_DATASET_LVL,
	
	/**
	 * The dataset lvl of a n-fold CrossValidation that gets repeated n times (aka runs).<br>
	 * For List&lt;List&lt;ReportData&gt;&gt; in PipelineReportPerDataset:<br>
	 * Outer list: results of each input dataset of the MultipleCV (so there are as many entries as specified input files).<br>
	 * Inner list: Different models (feature sets) for each MultipleCV of the pipeline.<br>
	 * For List of datasetNames:<br>
	 * # of entries == # of input datasets<br>
	 * This is a outer level report! (It contains a {@link PipelineTypes}.CV as first inner level report and a {@link PipelineTypes}.MULTIPLE_CV as second inner level).
	 */
	MULTIPLE_CV_DATASET_LVL,
	
	/**
	 * The dataset lvl of a normal Train/Test pipeline.<br>
	 * For List&lt;List&lt;ReportData&gt;&gt; in PipelineReportPerDataset:<br>
	 * Outer list: results of each combination of Train/Test input files (this results #trainsets * #testsets many entries)<br>
	 * Inner list: Different models (feature sets) of the Train/Test pipeline.<br>
	 * For List of datasetNames:<br>
	 * # of entries == # of combinations of Train/Test input files. The name will contain both Train and Test dataset in the following manner: "TRAIN_{name}-TEST_{name}"<br>
	 * This is a outer level report! (It contains a {@link PipelineTypes}.TRAIN_TEST as inner lvl report)
	 */
	TRAIN_TEST_DATASET_LVL,
	
	/**
	 * not used for Evaluation purposes
	 */
	CACHE_FILL;

}
	
