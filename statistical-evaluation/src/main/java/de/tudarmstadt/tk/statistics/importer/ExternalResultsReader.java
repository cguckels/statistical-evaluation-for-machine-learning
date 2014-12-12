package de.tudarmstadt.tk.statistics.importer;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import de.tudarmstadt.tk.statistics.config.ReportTypes;
import de.tudarmstadt.tk.statistics.config.StatsConfig;
import de.tudarmstadt.tk.statistics.config.StatsConfigConstants;
import de.tudarmstadt.tk.statistics.helper.Helpers;
import de.tudarmstadt.tk.statistics.report.EvaluationResults;
import de.tudarmstadt.tk.statistics.report.EvaluationResultsWriter;
import de.tudarmstadt.tk.statistics.test.SampleData;
import de.tudarmstadt.tk.statistics.test.StatsProcessor;


/**
 * @author Guckelsberger, Schulz
 */
public class ExternalResultsReader{
	
    private static final Logger logger = LogManager.getLogger("Statistics");
	
	public static void readMUGCTrainTest(String filePath)
	{		
		String outFileName = "AggregatedTrainTest.csv";
		
		logger.log(Level.INFO, String.format("Importing data from directory %s.",filePath));

		// Method requires input directory. Check this condition.
		File directory = new File(filePath);
		if (directory.isDirectory()) {
			System.err.println("Please specify a file. Aborting.");
			return;
		}

		//Empty previous output file, if there was one
		File outputFile = new File(directory.getParentFile(),outFileName);
        if (outputFile.exists()){
        	outputFile.delete();
        }  
		try {
			String header = "Train;Test;Classifier;FeatureSet;Measure;Value";

			PrintWriter out = new PrintWriter(new FileWriter(outputFile, true));
			out.println(header);
			out.close();
		} catch (IOException e) {
			System.err.println("Error while writing aggregated Train-Test file.");
			e.printStackTrace();
		}
		
		ArrayList<String> outputRows = new ArrayList<String>();

		// iterate all rows
		List<String[]> inputRowsFirstFile = new ArrayList<>();
		inputRowsFirstFile = readAndCheckCSV(filePath,';');

		// first: order by train set
		ArrayList<ExternalResults> extResults = new ArrayList<>();

		for (int i = 0; i < inputRowsFirstFile.size(); i++) {
			ExternalResults results = new ExternalResults();
			
			// identify current train/test split
			String[] datasetNames = inputRowsFirstFile.get(i)[0].replace("TRAIN:", "").replace("TEST:","").split(",");
			results.trainSetName = datasetNames[0].replace(" ","");
			results.testSetName = datasetNames[1].replace(" ","");

			// set classifier name
			results.classifierParameters = inputRowsFirstFile.get(i)[1];
			
			// read feature set
			results.featureSetName = inputRowsFirstFile.get(i)[2];
			
			// read classification results
			results.recall = Double.parseDouble(inputRowsFirstFile.get(i)[3]);
			results.fMeasure= Double.parseDouble(inputRowsFirstFile.get(i)[4]);
			results.precision= Double.parseDouble(inputRowsFirstFile.get(i)[5]);
			results.accuracy= Double.parseDouble(inputRowsFirstFile.get(i)[10])/100;

			extResults.add(results);
		}
		
		HashMap<String,ArrayList<ExternalResults>> extResultsByTrainTestFeature = new HashMap<>();

		// order by test set
		for(ExternalResults result : extResults)
		{
			String IdKey = result.trainSetName + result.testSetName + result.featureSetName;
			
			if(extResultsByTrainTestFeature.containsKey(IdKey))
			{
				extResultsByTrainTestFeature.get(IdKey).add(result);
			}
			else
			{
				extResultsByTrainTestFeature.put(IdKey, new ArrayList<ExternalResults>());
				extResultsByTrainTestFeature.get(IdKey).add(result);
			}
		}
		
		ArrayList<ExternalResults> aggregatedResults = new ArrayList<>();
		
		// aggregate results or keep as are
		for(Entry<String,ArrayList<ExternalResults>> trainTestSplit : extResultsByTrainTestFeature.entrySet())
		{
			ExternalResults aggrResult = new ExternalResults();
			
			double recall = 0;
			double fMeasure = 0;
			double precision = 0;
			double accuracy = 0;
			int nrClassifiers = 0;
			
			// for all entries that are from the same train/test split and use the same feature set -> aggregate results
			for(ExternalResults result : trainTestSplit.getValue())
			{
				aggrResult.testSetName = result.testSetName;
				aggrResult.trainSetName = result.trainSetName;
				aggrResult.classifierParameters = result.classifierParameters;
				aggrResult.featureSetName = result.featureSetName;
				
				recall += result.recall;
				fMeasure += result.fMeasure;
				precision+= result.precision;
				accuracy+= result.accuracy;
				nrClassifiers++;
			}
			
			aggrResult.accuracy = (accuracy / nrClassifiers);
			aggrResult.fMeasure = (fMeasure / nrClassifiers);
			aggrResult.recall = (recall / nrClassifiers);
			aggrResult.precision = (precision / nrClassifiers);
			
			aggregatedResults.add(aggrResult);
		}
			
			// write values of measure
			for(ExternalResults result : aggregatedResults)
			{
				String outputRow = String.format("%s;%s;%s;%s;%s;%s", result.trainSetName, result.testSetName, "0", result.featureSetName, "Percent Correct", result.accuracy);
				outputRows.add(outputRow);
				
				outputRow = String.format("%s;%s;%s;%s;%s;%s", result.trainSetName, result.testSetName, "0", result.featureSetName, "Weighted Precision", result.precision);
				outputRows.add(outputRow);

				outputRow = String.format("%s;%s;%s;%s;%s;%s", result.trainSetName, result.testSetName, "0", result.featureSetName, "Weighted Recall", result.recall);
				outputRows.add(outputRow);

				outputRow = String.format("%s;%s;%s;%s;%s;%s", result.trainSetName, result.testSetName, "0", result.featureSetName, "Weighted F-Measure", result.fMeasure);
				outputRows.add(outputRow);

			}
			
		// Write aggregated data to a new file
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outputFile, true));
			for (String s : outputRows) {
				out.println(s);
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while writing aggregated Train-Test file.");
			e.printStackTrace();
		}
	
		logger.log(Level.INFO, String.format("Finished import. The aggregated data was written to %s.",outFileName));
	}

	public static void readMUGCCV(String filePath)
	{		
		String outFileName = "AggregatedTrainTest.csv";
		
		logger.log(Level.INFO, String.format("Importing data from directory %s.",filePath));

		// Method requires input directory. Check this condition.
		File directory = new File(filePath);
		if (directory.isDirectory()) {
			System.err.println("Please specify a file. Aborting.");
			return;
		}

		//Empty previous output file, if there was one
		File outputFile = new File(directory.getParentFile(),outFileName);
        if (outputFile.exists()){
        	outputFile.delete();
        }  
		try {
			String header = "Train;Test;Classifier;FeatureSet;Measure;Value";

			PrintWriter out = new PrintWriter(new FileWriter(outputFile, true));
			out.println(header);
			out.close();
		} catch (IOException e) {
			System.err.println("Error while writing aggregated Train-Test file.");
			e.printStackTrace();
		}
		
		ArrayList<String> outputRows = new ArrayList<String>();

		// iterate all rows
		List<String[]> inputRowsFirstFile = new ArrayList<>();
		inputRowsFirstFile = readAndCheckCSV(filePath,';');

		// first: order by train set
		ArrayList<ExternalResults> extResults = new ArrayList<>();

		for (int i = 0; i < inputRowsFirstFile.size(); i++) {
			ExternalResults results = new ExternalResults();
			
			// identify current train/test split
			String[] datasetNames = inputRowsFirstFile.get(i)[0].split(",");
			results.trainSetName = datasetNames[0].replace("CV: ","").replace(" ","");

			// set classifier name
			results.classifierParameters = inputRowsFirstFile.get(i)[1];
			
			// read feature set
			results.featureSetName = inputRowsFirstFile.get(i)[2];
			
			// read classification results
			results.recall = Double.parseDouble(inputRowsFirstFile.get(i)[3]);
			results.fMeasure= Double.parseDouble(inputRowsFirstFile.get(i)[4]);
			results.precision= Double.parseDouble(inputRowsFirstFile.get(i)[5]);
			results.accuracy= Double.parseDouble(inputRowsFirstFile.get(i)[10])/100;

			extResults.add(results);
		}
		
		HashMap<String,ArrayList<ExternalResults>> extResultsByTrainTestFeature = new HashMap<>();

		// order by test set
		for(ExternalResults result : extResults)
		{
			String IdKey = result.trainSetName + result.testSetName + result.featureSetName;
			
			if(extResultsByTrainTestFeature.containsKey(IdKey))
			{
				extResultsByTrainTestFeature.get(IdKey).add(result);
			}
			else
			{
				extResultsByTrainTestFeature.put(IdKey, new ArrayList<ExternalResults>());
				extResultsByTrainTestFeature.get(IdKey).add(result);
			}
		}
		
		ArrayList<ExternalResults> aggregatedResults = new ArrayList<>();
		
		// aggregate results or keep as are
		for(Entry<String,ArrayList<ExternalResults>> trainTestSplit : extResultsByTrainTestFeature.entrySet())
		{
			ExternalResults aggrResult = new ExternalResults();
			
			double recall = 0;
			double fMeasure = 0;
			double precision = 0;
			double accuracy = 0;
			int nrClassifiers = 0;
			
			// for all entries that are from the same train/test split and use the same feature set -> aggregate results
			for(ExternalResults result : trainTestSplit.getValue())
			{
				aggrResult.testSetName = result.testSetName;
				aggrResult.trainSetName = result.trainSetName;
				aggrResult.classifierParameters = result.classifierParameters;
				aggrResult.featureSetName = result.featureSetName;
				
				recall += result.recall;
				fMeasure += result.fMeasure;
				precision+= result.precision;
				accuracy+= result.accuracy;
				nrClassifiers++;
			}
			
			aggrResult.accuracy = (accuracy / nrClassifiers);
			aggrResult.fMeasure = (fMeasure / nrClassifiers);
			aggrResult.recall = (recall / nrClassifiers);
			aggrResult.precision = (precision / nrClassifiers);
			
			aggregatedResults.add(aggrResult);
		}
			
			// write values of measure
			for(ExternalResults result : aggregatedResults)
			{
				String outputRow = String.format("%s;%s;%s;%s;%s;%s", result.trainSetName, result.testSetName, "0", result.featureSetName, "Percent Correct", result.accuracy);
				outputRows.add(outputRow);
				
				outputRow = String.format("%s;%s;%s;%s;%s;%s", result.trainSetName, result.testSetName, "0", result.featureSetName, "Weighted Precision", result.precision);
				outputRows.add(outputRow);

				outputRow = String.format("%s;%s;%s;%s;%s;%s", result.trainSetName, result.testSetName, "0", result.featureSetName, "Weighted Recall", result.recall);
				outputRows.add(outputRow);

				outputRow = String.format("%s;%s;%s;%s;%s;%s", result.trainSetName, result.testSetName, "0", result.featureSetName, "Weighted F-Measure", result.fMeasure);
				outputRows.add(outputRow);

			}
			
		// Write aggregated data to a new file
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outputFile, true));
			for (String s : outputRows) {
				out.println(s);
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while writing aggregated Train-Test file.");
			e.printStackTrace();
		}
	
		logger.log(Level.INFO, String.format("Finished import. The aggregated data was written to %s.",outFileName));
	}
	
	public static void readLODPipelineTrainTest(String pathToDirectory) {
		Locale.setDefault(Locale.ENGLISH);
				
		String[] semanticFeatures = new String[] { "Baseline", "+ALL", "+LOC", "+TIME", "+LOD", "+LOC+TIME", "+LOC+LOD", "+TIME+LOD", "+TYPES", "+CAT" };
		String[] measures = new String[] { "Percent Correct", "Weighted Precision", "Weighted Recall", "Weighted F-Measure" };
		String outFileName = "AggregatedCVRandom.csv";
		
		logger.log(Level.INFO, String.format("Importing data from directory %s.",pathToDirectory));
        
		// Method requires input directory. Check this condition.
		File directory = new File(pathToDirectory);
		if (!directory.isDirectory()) {
			System.err.println("Please specify a directory with the source .csv files. Aborting.");
			return;
		}

		//Empty previous output file, if there was one
		File outputFile = new File(directory,outFileName);
        if (outputFile.exists()){
        	outputFile.delete();
        }  
		try {
			String header = "Train;Test;Classifier;FeatureSet;Measure;Value";

			PrintWriter out = new PrintWriter(new FileWriter(outputFile, true));
			out.println(header);
			out.close();
		} catch (IOException e) {
			System.err.println("Error while writing aggregated Train-Test file.");
			e.printStackTrace();
		}
		
		// prepare files lists
		HashMap<String, ArrayList<File>> filesMap = new HashMap<>();

		// read all subdirectories that match the city names
		File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

		//Iterate all subdirectories
		for (File subDirectory : subdirs) {

			// get train set name
			String trainSetName = subDirectory.getName();

			// iterate all files in directory
			File[] filesInDirectory = subDirectory.listFiles();
			List<File> fileList = Arrays.asList(filesInDirectory);

			for (File subDirFile : fileList) {
				// get name of test data set
				String[] filenameTokens = subDirFile.getName().split("To");
				//String testDataName = filenameTokens[1].substring(0, filenameTokens[1].length() - 11);

				
				String testDataName;
				
				// if only this string is left, then CV
				if (filenameTokens[1].equals("Results.csv"))
				{
					testDataName = trainSetName;
				}
				else
				{
					testDataName = filenameTokens[1].split("Results.csv")[0];
					testDataName = testDataName.split("2C.csv|4C.csv|.csv")[0];
				}
				
				// put current file to test data name -> this way all files
				// corresponding to the same test set are in one map
				if (filesMap.get(testDataName) != null) {
					// get existing list and add file
					ArrayList<File> currentFileList = filesMap.get(testDataName);
					currentFileList.add(subDirFile);
				} else {
					// create new list and add current file
					ArrayList<File> newFileList = new ArrayList<>();
					newFileList.add(subDirFile);
					filesMap.put(testDataName, newFileList);
				}
			}

			ArrayList<String> outputRows = new ArrayList<String>();
			int nrDifferentClassifiers = 0;

			// iterate all files of one map
			Iterator<Entry<String, ArrayList<File>>> it = filesMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String testSetName = (String) pairs.getKey();
				ArrayList<File> testFiles = (ArrayList<File>) pairs.getValue();

				nrDifferentClassifiers = testFiles.size();

				// initialize data store
				ArrayList<HashMap<String, Object>> values = new ArrayList<>();

				// get rows for first file to initialize store
				List<String[]> inputRowsFirstFile = readAndCheckCSV(testFiles.get(0).getAbsolutePath(), ';');

				for (int i = 0; i < inputRowsFirstFile.size(); i++) {
					HashMap<String, Object> currentRowValues = new HashMap<>();
					currentRowValues.put("semanticFeature", "");
					currentRowValues.put("classifierParameters", "");
					currentRowValues.put("aggregatedMeasureValues", new double[measures.length]);
					currentRowValues.put("nGrams", "");
					values.add(currentRowValues);
				}

				// get results from other files
				for (File testFile : testFiles) {
					// Only analyse files with .csv extension
					if (!FilenameUtils.getExtension(testFile.getName().toLowerCase()).equals("csv") || testFile.getName().equals("AggregatedTrainTest.csv")) {
						continue;
					}
					// check file for consistency
					List<String[]> inputRows = readAndCheckCSV(testFile.getAbsolutePath(), ';');

					// check if length matches first file
					if (!(inputRows.size() == values.size())) {
						// TODO error message
					} else {
						for (int i = 0; i < inputRows.size(); i++) {
							String[] inputCells = inputRows.get(i);

							// read current values and compare with entries
							String semanticFeature = semanticFeatures[i % semanticFeatures.length];

							if (values.get(i).get("semanticFeature") == "") {
								values.get(i).put("semanticFeature", semanticFeature);
							} else {
								if (values.get(i).get("semanticFeature").equals(semanticFeature) == false) {
									System.err.println("Semantic Features do not match.");
									System.exit(1);
								}
							}

							// needs rework as we do aggregation here
							// String classifierParameters = inputCells[0];
							//
							// if (values.get(i).get("classifierParameters") ==
							// "")
							// {
							// values.get(i).put("classifierParameters",
							// classifierParameters);
							// }
							// else
							// {
							// if
							// (values.get(i).get("classifierParameters").equals(classifierParameters)
							// == false)
							// {
							// System.err.println("Classifier parameters do not match.");
							// System.exit(1);
							// }
							// }

							String nGrams = inputCells[12];

							if (values.get(i).get("nGrams") == "") {
								values.get(i).put("nGrams", nGrams);
							} else {
								if (values.get(i).get("nGrams").equals(nGrams) == false) {
									System.err.println("N Gram Length does not match.");
									System.exit(1);
								}
							}

							// get and aggregate values
							for (int j = 0; j < measures.length; j++) {
								if (j == 0) {
									//double currentValue = ((double[]) values.get(i).get("aggregatedMeasureValues"))[j];
									double valueInFile = Double.parseDouble(inputCells[j + 16]) / 100;

									((double[]) values.get(i).get("aggregatedMeasureValues"))[j] += valueInFile;
								} else {
									//double currentValue = ((double[]) values.get(i).get("aggregatedMeasureValues"))[j];
									double valueInFile = Double.parseDouble(inputCells[j + 16]);
									((double[]) values.get(i).get("aggregatedMeasureValues"))[j] += valueInFile;
								}
							}
						}
					}
				}

				// write aggregated results to file
				for (HashMap<String, Object> currentValues : values) {
					String semFeature = (String) currentValues.get("semanticFeature");
					String nGrams = (String) currentValues.get("nGrams");
					String featureSet = String.format("%s, nGrams: %s", semFeature, nGrams);

					for (int j = 0; j < measures.length; j++) {
						String outputRow = String.format("%s;%s;%s;%s;%s;%f", trainSetName, testSetName, "0", featureSet, measures[j], ((double[]) currentValues.get("aggregatedMeasureValues"))[j] / nrDifferentClassifiers);
						outputRows.add(outputRow);
					}
				}

				// avoids a ConcurrentModificationException
				it.remove();
			}

			// Write aggregated data to a new file
			try {
				PrintWriter out = new PrintWriter(new FileWriter(outputFile, true));
				for (String s : outputRows) {
					out.println(s);
				}
				out.close();
			} catch (IOException e) {
				System.err.println("Error while writing aggregated Train-Test file.");
				e.printStackTrace();
			}
		}
		
		logger.log(Level.INFO, String.format("Finished import. The aggregated data was written to %s.",outFileName));

	}

	public static SampleData interpretCSV(StatsConfig config, List<String[]> rows, ReportTypes pipelineType, HashMap<String, Integer> pipelineMetadata) {

		HashMap<Integer, ArrayList<ArrayList<Double>>> samplesPerMeasure = new HashMap<Integer, ArrayList<ArrayList<Double>>>();

		rows.remove(0); // Remove header

		if (rows.size() > 1) {

			logger.log(Level.INFO, "Extracting samples and metadata from imported data.");
			int selectBestN = config.getSelectBestN();
			String selectByMeasure = config.getSelectByMeasure();
			
			// Preprocessing: Parse different models (classifier + feature set column) and measures
			ArrayList<String> measures = new ArrayList<String>();
			ArrayList<Pair<String, String>> datasets = new ArrayList<Pair<String, String>>();
			ArrayList<Pair<String, String>> models = new ArrayList<Pair<String, String>>();
			ArrayList<Pair<String, String>> baselineModels = new ArrayList<Pair<String,String>>();

			for (int i = 0; i < rows.size(); i++) {
				String[] columns = rows.get(i);
				String classifier = columns[2];
				if(classifier.equals("0")){
					classifier="Aggregated";
				}
				String featureSets = columns[3];
				Pair<String, String> model = Pair.of(classifier,featureSets);
				if (!models.contains(model)) {
					models.add(model);
					if(!baselineModels.contains(model) && Integer.parseInt(columns[6])==1){
						baselineModels.add(model);
					}
				}
				if (!measures.contains(columns[4])) {
					measures.add(columns[4]);
				}
			}

			// Now sort samples according to data
			Collections.sort(rows, new Helpers.LexicographicArrayComparator());
			for (int i = 0; i < rows.size(); i++) {
				String[] columns = rows.get(i);
				Pair<String, String> data = null;
				String trainData = columns[0].trim();
				trainData=trainData.split("\\.")[0];
				String testData = columns[1].trim();
				testData=testData.split("\\.")[0];
				if (trainData.equals(testData)) {
					data = Pair.of(trainData, null);
				} else {
					//columns[1] = columns[1].split(".")[0];
					data = Pair.of(trainData, testData);
				}
				if (!datasets.contains(data)) {
					datasets.add(data);
				}
			}

			// Preprocessing: Initialize sample container per measure/model
			for (int i = 0; i < measures.size(); i++) {
				ArrayList<ArrayList<Double>> samplesPerModel = new ArrayList<ArrayList<Double>>();
				for (int j = 0; j < models.size(); j++) {
					samplesPerModel.add(new ArrayList<Double>());
				}
				samplesPerMeasure.put(i, samplesPerModel);
			}

			// Assign samples to different models
			for (int i = 0; i < rows.size(); i++) {
				String[] columns = rows.get(i);
				String classifier = columns[2];
				if(classifier.equals("0")){
					classifier="Aggregated";
				}
				String featureSet = columns[3];
				String measure = columns[4];
				double value = Double.parseDouble(columns[5]);

				int measureIndex = measures.indexOf(measure);
				int modelIndex = models.indexOf(Pair.of(classifier, featureSet));

				ArrayList<ArrayList<Double>> sPMeasure = samplesPerMeasure.get(measureIndex);
				sPMeasure.get(modelIndex).add(value);
			}

			// Transform into data format required by the statistical evaluation
			HashMap<String, ArrayList<ArrayList<Double>>> indexedSamples = new HashMap<String, ArrayList<ArrayList<Double>>>();
			HashMap<String, ArrayList<Double>> indexedSamplesAverage = new HashMap<String, ArrayList<Double>>();

			Iterator<Integer> it = samplesPerMeasure.keySet().iterator();
			while (it.hasNext()) {
				int measureIndex = it.next();
				ArrayList<ArrayList<Double>> samplesPerModel = samplesPerMeasure.get(measureIndex);

				ArrayList<Double> sampleAverages = new ArrayList<Double>(models.size());
				for (int modelIndex = 0; modelIndex < models.size(); modelIndex++) {
					ArrayList<Double> sample = samplesPerModel.get(modelIndex);
					double average = 0;
					for (int j = 0; j < sample.size(); j++) {
						average += sample.get(j);
					}
					average /= sample.size();
					sampleAverages.add(average);
				}
				indexedSamplesAverage.put(measures.get(measureIndex), sampleAverages);
				indexedSamples.put(measures.get(measureIndex), samplesPerMeasure.get(measureIndex));
			}
			
			// Check if data fulfills general requirements: > 5 samples for each model, same number of samples per model
			it = samplesPerMeasure.keySet().iterator();
			while(it.hasNext()){
				ArrayList<ArrayList<Double>> samplesPerModel = samplesPerMeasure.get(it.next());
				int s = samplesPerModel.get(0).size();
				
				for(int i=1; i<samplesPerModel.size(); i++){
					if(samplesPerModel.get(i).size()<5){
						logger.log(Level.ERROR, "More than 5 samples are needed per model and measure. Aborting.");
						System.err.println("More than 5 samples are needed per model and measure. Aborting.");
						System.exit(1);
					}
					if(samplesPerModel.get(i).size()!=s){
						logger.log(Level.ERROR, "Different models are not represented by the same number of samples. Aborting.");
						System.err.println("Different models are not represented by the same number of samples. Aborting.");
						System.exit(1);
					}
				}
			}

			// Collect remaining data required for creating a SampleData object
			// Check if data fulfills requirements of the specific PipelineTypes
			int nFolds = 1;
			int nRepetitions = 1;
			switch (pipelineType) {
			case CV:
				if (datasets.size() > 1) {
					System.err.println("Input data corrupted. More than one dataset specified for Single-Domain Cross-Validation.");
					logger.log(Level.ERROR, "Input data corrupted. More than one dataset specified for Single-Domain Cross-Validation.");
					return null;
				} else if (datasets.get(0).getValue() != null) {
					System.err.println("Input data corrupted. Training and Test dataset must be same for Cross-Validation.");
					logger.log(Level.ERROR, "Input data corrupted. Training and Test dataset must be same for Cross-Validation.");
					return null;
				}
				nFolds = indexedSamples.get(measures.get(0)).get(0).size();
				nRepetitions = 1;
				break;
			case MULTIPLE_CV:
				if (datasets.size() > 1) {
					System.err.println("Input data corrupted. More than one dataset specified for Single-Domain Cross-Validation.");
					logger.log(Level.ERROR, "Input data corrupted. More than one dataset specified for Single-Domain Cross-Validation.");
					return null;
				} else if (datasets.get(0).getValue() != null) {
					System.err.println("Input data corrupted. Training and Test dataset must be same for Cross-Validation.");
					logger.log(Level.ERROR, "Input data corrupted. Training and Test dataset must be same for Cross-Validation.");
					return null;
				}
				nFolds = pipelineMetadata.get("nFolds");
				nRepetitions = indexedSamples.get(measures.get(0)).get(0).size();
				break;
			case CV_DATASET_LVL:
				nFolds = pipelineMetadata.get("nFolds");
				nRepetitions = 1;
				break;
			case MULTIPLE_CV_DATASET_LVL:
				nFolds = pipelineMetadata.get("nFolds");
				nRepetitions = pipelineMetadata.get("nRepetitions");
				break;
			case TRAIN_TEST_DATASET_LVL:
				nFolds = 1;
				nRepetitions = 1;
				break;
			default:
				System.err.println("Unknown PipelineType. Aborting.");
				logger.log(Level.ERROR, "Unknown PipelineType. Aborting.");
				return null;
			}	
			
			
			//Reorder data in case of a baseline evaluation (baseline first)
			if(baselineModels.size()==1){
				Pair<String,String> baselineModel = baselineModels.get(0);
				int modelIndex = models.indexOf(baselineModel);
				models.remove(modelIndex);
				models.add(0,baselineModel);
				for(String measure:indexedSamples.keySet()){
					ArrayList<Double> s = indexedSamples.get(measure).get(modelIndex);
					indexedSamples.get(measure).remove(modelIndex);
					indexedSamples.get(measure).add(0,s);
					double a = indexedSamplesAverage.get(measure).get(modelIndex);
					indexedSamplesAverage.get(measure).remove(modelIndex);
					indexedSamplesAverage.get(measure).add(0,a);
				}
			}
			
			SampleData sampleData = new SampleData(null,indexedSamples,indexedSamplesAverage,datasets,models,baselineModels,pipelineType,nFolds,nRepetitions);
			sampleData = Helpers.truncateData(sampleData, selectBestN, selectByMeasure);
			
			return sampleData;
		}
		return null;
	}
	
	public static List<SampleData> splitData(SampleData data, StatsConfig config){

		List<SampleData> splitted = new ArrayList<SampleData>();
		
		//Use lists instead of sets to maintain order of model metadata
		ArrayList<String> featureSets = new ArrayList<String>();
		ArrayList<String> classifiers = new ArrayList<String>();
		for(Pair<String,String> metadata:data.getModelMetadata()){
			if(!classifiers.contains(metadata.getLeft())){
				classifiers.add(metadata.getLeft());
			}
			if(!featureSets.contains(metadata.getRight())){
				featureSets.add(metadata.getRight());
			}
		}
		
		//Only separate data if there's more than one independent variable
		if(!(featureSets.size()>1 && classifiers.size()>1)){
			splitted.add(data);
			return splitted;
		}
		
		List<String> it = (config.getFixIndependentVariable()==StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.Classifier) ? classifiers : featureSets;
		for(String fixed: it){
			ArrayList<Pair<String,String>> modelMetadata = new ArrayList<Pair<String,String>>();
			HashMap<String,ArrayList<ArrayList<Double>>> samples = new HashMap<String,ArrayList<ArrayList<Double>>>();
			HashMap<String,ArrayList<Double>> sampleAverages = new HashMap<String,ArrayList<Double>>();
			for(int i=0; i<data.getModelMetadata().size(); i++){
				Pair<String,String> model = data.getModelMetadata().get(i);
				boolean eq = (config.getFixIndependentVariable()==StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.Classifier) ? model.getLeft().equals(fixed) : model.getRight().equals(fixed);
				if(eq){
					modelMetadata.add(model);
					for(String measure:data.getSamples().keySet()){
						if(!samples.containsKey(measure)){
							samples.put(measure, new ArrayList<ArrayList<Double>>());
							sampleAverages.put(measure, new ArrayList<Double>());
						}
						samples.get(measure).add(data.getSamples().get(measure).get(i));
						sampleAverages.get(measure).add(data.getSamplesAverage().get(measure).get(i));
					}
				}
			}
			ArrayList<Pair<String,String>> baselineModelData = new ArrayList<Pair<String,String>>();
			if(data.isBaselineEvaluation()){
				Pair<String,String> baselineModel = null;
				for(int i=0; i<data.getBaselineModelMetadata().size(); i++){
					boolean eq = (config.getFixIndependentVariable()==StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.Classifier) ? data.getBaselineModelMetadata().get(i).getLeft().equals(fixed) : data.getBaselineModelMetadata().get(i).getRight().equals(fixed);
					if(eq){
						baselineModel = data.getBaselineModelMetadata().get(i);
						break;
					}
				}
				if(baselineModel!=null){
					baselineModelData.add(baselineModel);
					int modelIndex = modelMetadata.indexOf(baselineModel);
					modelMetadata.remove(modelIndex);
					modelMetadata.add(0,baselineModel);
					for(String measure:data.getSamples().keySet()){
						ArrayList<Double> s = samples.get(measure).get(modelIndex);
						samples.get(measure).remove(modelIndex);
						samples.get(measure).add(0,s);
						double a = sampleAverages.get(measure).get(modelIndex);
						sampleAverages.get(measure).remove(modelIndex);
						sampleAverages.get(measure).add(0,a);
					}
				}else{
					logger.log(Level.ERROR, "Missing baseline model! Please check if baseline indicators are set correctly in the input file, and if they correspond correctly to the fixIndependentVariable property in the configuration. In case of both varying feature sets and classifiers, baseline indicators have to be set multiple times.");
					System.err.println("Missing baseline model! Please check if baseline indicators are set correctly in the input file, and if they correspond correctly to the fixIndependentVariable property in the configuration. In case of both varying feature sets and classifiers, baseline indicators have to be set multiple times.");
					System.exit(1);
				}
			}
			SampleData newData = new SampleData(null, samples, sampleAverages, data.getDatasetNames(), modelMetadata, baselineModelData, data.getPipelineType(), data.getnFolds(), data.getnRepetitions());	
			splitted.add(newData);
		}
		return splitted;
	}

	/**
	 * Triggers a statistical evaluation of external data and stores the report in the same folder. 
	 * Use this method if the data stems from an n-fold cross-validation. Each line should represent the model's performance for one fold of the CV.
	 * @param pathToCsvFile The path to the external data file.
	 * @param separator The character used to separate columns in the file.
	 */
	public static void evaluateCV(StatsConfig config, String pathToCsvFile, String outputPath, char separator) {
		logger.log(Level.INFO, "Starting evaluation of data from a simple cross-validation.");

		HashMap<String, Integer> pipelineMetadata = new HashMap<String, Integer>();
		evaluate(config, pathToCsvFile, outputPath, separator, ReportTypes.CV, pipelineMetadata);
	}

	/**
	 * Triggers a statistical evaluation of external data and stores the report in the same folder. 
	 * Use this method if the data stems from a repeated n-fold cross-validation. Each line should represent the model's performance for one CV averaged over all folds.
	 * @param pathToCsvFile The path to the external data file.
	 * @param separator The character used to separate columns in the file.
	 */
	public static void evaluateRepeatedCV(StatsConfig config, String pathToCsvFile, String outputPath, char separator, int nFolds) {
		logger.log(Level.INFO, "Starting evaluation of data from a repeated cross-validation.");
		
		HashMap<String, Integer> pipelineMetadata = new HashMap<String, Integer>();
		pipelineMetadata.put("nFolds", nFolds);
		evaluate(config, pathToCsvFile, outputPath, separator, ReportTypes.MULTIPLE_CV, pipelineMetadata);
	}

	/*
	// Multi-Domain CV: Each line=average performance per CV
	public static void evaluateMultiDomainCV(String pathToCsvFile, String separator, int nFolds, boolean againstBaseline) {
		HashMap<String, Integer> pipelineMetadata = new HashMap<String, Integer>();
		pipelineMetadata.put("nFolds", nFolds);
		evaluate(pathToCsvFile, separator, ReportTypes.CV_DATASET_LVL, againstBaseline, pipelineMetadata);
	}

	// Multi-Domain Repeated CV: Each line=average performance over all
	// repetitions
	public static void evaluateMultiDomainRepeatedCV(String pathToCsvFile, String separator, int nFolds, int nRepetitions, boolean againstBaseline) {
		HashMap<String, Integer> pipelineMetadata = new HashMap<String, Integer>();
		pipelineMetadata.put("nFolds", nFolds);
		pipelineMetadata.put("nRepetitions", nRepetitions);
		evaluate(pathToCsvFile, separator, ReportTypes.MULTIPLE_CV_DATASET_LVL, againstBaseline, pipelineMetadata);
	}*/

	/**
	 * Triggers a statistical evaluation of external data and stores the report in the same folder. 
	 * Use this method if the data stems from a Train-Test-Evaluation. Each line should represent the model's performance for one test.
	 * @param pathToCsvFile The path to the external data file.
	 * @param separator The character used to separate columns in the file.
	 */
	public static void evaluateTrainTest(StatsConfig config, String pathToCsvFile, String outputPath, char separator) {
		logger.log(Level.INFO, "Starting evaluation of data from a Train-Test scenario.");

		HashMap<String, Integer> pipelineMetadata = new HashMap<String, Integer>();
		evaluate(config, pathToCsvFile, outputPath, separator, ReportTypes.TRAIN_TEST_DATASET_LVL, pipelineMetadata);
	}

	public static void evaluate(StatsConfig config, String pathToCsvFile, String outputPath, char separator, ReportTypes pipelineType, HashMap<String, Integer> pipelineMetadata) {

		List<String[]> rows = readAndCheckCSV(pathToCsvFile, separator);
		SampleData sampleData = interpretCSV(config, rows, pipelineType, pipelineMetadata);
		List<SampleData> splittedSamples = splitData(sampleData, config);

		StatsProcessor stats = new StatsProcessor(config);
		//String outputPath = new File(pathToCsvFile).getParentFile().getAbsolutePath();

		for(int i=0; i<splittedSamples.size(); i++){
			
			SampleData samples = splittedSamples.get(i);
	
			EvaluationResults evalResults = stats.performStatisticalEvaluation(samples);
						
			if(evalResults==null){
				continue;
			}
			createEvaluationReport(outputPath, evalResults);
			
		}	
	}

	/**
	 * Read csv file, split each line by the specified separator and check
	 * whether each line can be split into the same number of columns
	 * 
	 * @param pathToCsvFile the path to the .csv file
	 * @param separator the separator to be used to split a line in separate cells, each relating to one column ArrayList<String[]> containing all lines split into tokens
	 */
	private static List<String[]> readAndCheckCSV(String pathToCsvFile, char separator) {
		List<String[]> rows = new ArrayList<String[]>();
		try {
		    CSVReader reader = new CSVReader(new FileReader(pathToCsvFile),separator);
		    rows = reader.readAll();
			reader.close();

			if(rows.size()>0){
				for(String[] row: rows){
					if(row.length!=rows.get(0).length){
						logger.log(Level.ERROR, ".csv file corrup: number of columns not same for each row.");
						System.err.println(".csv file corrup: number of columns not same for each row.");
						System.exit(1);
					}
				}
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.ERROR, "Input .csv file not found!");
			System.err.println("Input .csv file not found!");
			System.exit(1);		
		} catch (IOException e) {
				logger.log(Level.ERROR, "Exception while reading input data .csv!");
				System.err.println("Exception while reading input data .csv!");
				e.printStackTrace();
				System.exit(1);		
		}
		return rows;
	}

	/**
	 * Creates a latex and plain report of the evaluation results and writes it
	 * into the specified directory
	 * 
	 * @param pathToDirectory directory in which the reports should be written
	 * @param evalResults results of the statistical evaluation to be described in the reports
	 */
	private static void createEvaluationReport(String pathToDirectory, EvaluationResults evalResults) {

		try {
			File directory = new File(pathToDirectory);
			if (!directory.isDirectory()) {
				directory.getParent();
			}
			directory = new File(directory, "Report"+UUID.randomUUID());
			directory.mkdir();
			
			File latexFile = new File(directory, "statisticalReport.tex");
			File plainFile = new File(directory, "statisticalReport.txt");
			
			EvaluationResultsWriter resultsWriter = new EvaluationResultsWriter(evalResults);
			String latexReport = resultsWriter.createLatexReport(latexFile.getParentFile());
			String plainReport = resultsWriter.createPlainReport();

			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(latexFile)));
			out.write(latexReport);
			out.close();
			logger.log(Level.INFO, String.format("Wrote plain report of statistical evaluation to %s.",plainFile.toPath()));

			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plainFile)));
			out.write(plainReport);
			out.close();
			logger.log(Level.INFO, String.format("Wrote Latex report of statistical evaluation to %s.",latexFile.toPath()));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	


	
	

}
