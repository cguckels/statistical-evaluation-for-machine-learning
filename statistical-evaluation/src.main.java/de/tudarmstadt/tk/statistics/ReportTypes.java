package de.tudarmstadt.tk.statistics;

import java.io.Serializable;

/**
 * This enum lists all possible {@link ReportTypes} that can be obtained via {@link AbstractPipelineReport}.<br>
 * Detailed information is available per type below. For clarification the levels of reports see the following:<br><br>
 * 
 * <b>Report levels and Pipeline types:</b><br>
 * <pre>
 * 				Inner Levels						Outer Level (Dataset level)
 * 				First			Second				
 * isCV				CV(1)			None				CV_DATASET_LVL
 * isCV && is_multiple_CV		CV(2)			MULTIPLE_CV(3)			MULTIPLE_CV_DATASET_LVL
 * !isCV				None			None				TRAIN_TEST_DATASET_LVL
 *</pre><br>
 * All this information is also found in the MUGC documentation.<br><br>
 * 
 * What kind of report you will get depends where exactly you add the report in the code! (See BatchTaskPipelineReport as an example)<br>
 * For Dataset lvl: BatchTaskPipeline.init(). This will forward ALL DATASET_LVL Report to your report class.<br>
 * For Inner lvls:<br>
 * CV -> CVPipeline.defineBatchTask(..) -> This will only grant access to a normal CV (not the CVs of a MultipleCV, see (1)).<br>
 * MultipleCV -> MutipleCVPipeline.defineBatchTask(..) -> Grants access to the second lvl MULTIPLE_CV report (3).
 * Consider BatchTaskMultipleCV.init() if you want to get access to the single CVs (2) within a multiple CV.<br><br>
 * 
 * Note that you will only get reports for levels you did specify (where you added your report in the code). In other words adding a report on a 
 * dataset lvl will not grant you access to the inner level reports and vice versa.<br>
 * Where to add you report highly depends on what kind of information you need!
 * 
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
	
