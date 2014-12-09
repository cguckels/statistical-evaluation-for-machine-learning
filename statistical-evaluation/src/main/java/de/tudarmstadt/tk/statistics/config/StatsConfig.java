package de.tudarmstadt.tk.statistics.config;
/**
 * Copyright 2014
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import de.tudarmstadt.tk.statistics.test.RBridge;


/**
 * Singleton to encapsulate the configuration for the statistical evaluation
 * Parameters can be set both via config file or programmatically 
 * @author Guckelsberger, Schulz
 *
 */
public class StatsConfig {

	// Singleton: Only allow for one instance of RStatistics
	private static volatile StatsConfig instance = null;
	
    private static final Logger logger = LogManager.getLogger("Statistics");
	
	private	HashMap<String,String> requiredTests = null;
	private	List<String> requiredCorrections = null;
	private double significance_low=1;
	private double significance_medium=1;
	private double significance_high=1;
	private int selectBestN;
	private String selectByMeasure;
	
	public static StatsConfig getInstance(String filePath) {
		if (instance == null) {
			synchronized (RBridge.class) {
				if (instance == null) {
					instance = new StatsConfig(filePath);
				}
			}
		}
		return instance;
	}
	
	private StatsConfig(String pathToConfigFile) {

		HashMap<String, Object> parameters;
		try {
			parameters = readParametersFromConfig(pathToConfigFile);
			requiredTests = (HashMap<String, String>) parameters.get(StatsConfigConstants.TESTS);
			requiredCorrections = (List<String>) parameters.get(StatsConfigConstants.CORRECTIONS);
			significance_low = (double) parameters.get(StatsConfigConstants.SIGNIFICANCE_LOW);
			significance_medium = (double) parameters.get(StatsConfigConstants.SIGNIFICANCE_MEDIUM);
			significance_high = (double) parameters.get(StatsConfigConstants.SIGNIFICANCE_HIGH);
			selectBestN = (int)parameters.get(StatsConfigConstants.SELECT_BEST_N);
			selectByMeasure = (String)parameters.get(StatsConfigConstants.SELECT_BEST_N_BY_MEASURE);
		} catch (FileNotFoundException e) {
			logger.log(Level.ERROR, "Statistics config file not found.");
			System.err.println("Statistics config file not found.");
			e.printStackTrace();
		} catch (JSONException e) {
			logger.log(Level.ERROR, "Error while reading statistics config file.");
			System.err.println("Error while reading statistics config file.");
			e.printStackTrace();
		}
		
	}

	/**
	 * Read statistics evaluation parameter from the (JSON) config file
	 * 
	 * @param pathToConfigFile
	 *            path to the config file to use (including suffix .cfg)
	 * @return a HashMap with the parameters from the config file
	 * @throws JSONException
	 * @throws FileNotFoundException
	 * @see StatsConfigConstants
	 */
	public static HashMap<String, Object> readParametersFromConfig(String pathToConfigFile) throws FileNotFoundException, JSONException {

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		JSONObject config = new JSONObject(new JSONTokener(new FileReader(pathToConfigFile)));
		List<String> measures = getListFromConfigFile(config.getJSONArray(StatsConfigConstants.MEASURES), StatsConfigConstants.MEASURE, Arrays.asList(StatsConfigConstants.MEASURE_VALUES));
		// List<String> measures =
		// getMeasuresFromConfigFile(config.getJSONArray(StatsConfigConstants.MEASURES));
		List<String> corrections = getListFromConfigFile(config.getJSONArray(StatsConfigConstants.CORRECTIONS), StatsConfigConstants.CORRECTION, Arrays.asList(StatsConfigConstants.CORRECTION_VALUES));
		HashMap<String, String> tests = getTestsFromConfigFile(config.getJSONArray(StatsConfigConstants.TESTS));

		parameters.put(StatsConfigConstants.SIGNIFICANCE_LOW, config.getDouble(StatsConfigConstants.SIGNIFICANCE_LOW));
		parameters.put(StatsConfigConstants.SIGNIFICANCE_MEDIUM, config.getDouble(StatsConfigConstants.SIGNIFICANCE_MEDIUM));
		parameters.put(StatsConfigConstants.SIGNIFICANCE_HIGH, config.getDouble(StatsConfigConstants.SIGNIFICANCE_HIGH));

		parameters.put(StatsConfigConstants.MEASURES, measures);
		parameters.put(StatsConfigConstants.CORRECTIONS, corrections);
		parameters.put(StatsConfigConstants.TESTS, tests);

		parameters.put(StatsConfigConstants.SELECT_BEST_N, config.getInt(StatsConfigConstants.SELECT_BEST_N));
		parameters.put(StatsConfigConstants.SELECT_BEST_N_BY_MEASURE, config.getString(StatsConfigConstants.SELECT_BEST_N_BY_MEASURE));

		// parameters.put(StatsConfigConstants.PARAMETRIC_AND_NONPARAMETRIC,
		// config.get(StatsConfigConstants.PARAMETRIC_AND_NONPARAMETRIC));
		return parameters;

	}

	/**
	 * Fetches a list of values from a JSON array and returns them as an array
	 * of type String
	 * 
	 * @param jsonArray
	 *            The JSON array with the required entries
	 * @param identifier
	 *            The name of the JSON array with the requires entries
	 * @param conformity
	 *            A list of allowed values to check whether the JSON array
	 *            entries conform
	 * @return a List of type String containing the keys from the JSON array,
	 *         conforming with the allowed values
	 * @see StatsConfigConstants
	 */
	private static List<String> getListFromConfigFile(JSONArray jsonArray, String identifier, List<String> conformity) {
		List<String> l = new ArrayList<String>();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonO;
			try {
				jsonO = jsonArray.getJSONObject(i);
				String name = jsonO.getString(identifier);
				if (conformity.contains(name)) {
					l.add(name);
				} else {
					String error = String.format("%s '%s' from statistics config file not allowed!", identifier, name);
					System.err.println(error);
					logger.log(Level.ERROR, error);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return l;
	}

	/**
	 * Retrieves the statistical tests defined in a JSON array as part of the
	 * statistics config file
	 * 
	 * @param testArray
	 *            The JSON array containing the test keys and valus
	 * @return a HashMap with String-keys representing the test category (e.g.
	 *         MULTIPLE_SAMPLES_SINGLE_DOMAIN_NONPARAMETRIC) and the respective
	 *         test as String-value
	 * @see StatsConfigConstants
	 */
	private static HashMap<String, String> getTestsFromConfigFile(JSONArray testArray) {

		HashMap<String, String> tests = new HashMap<String, String>();

		for (int i = 0; i < testArray.length(); i++) {
			try {
				JSONObject entry = testArray.getJSONObject(i);
				String key = JSONObject.getNames(entry)[0];
				tests.put(key, entry.optString(key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return tests;
	}

	public HashMap<String, String> getRequiredTests() {
		return requiredTests;
	}

	public List<String> getRequiredCorrections() {
		return requiredCorrections;
	}

	public double getSignificance_low() {
		return significance_low;
	}

	public double getSignificance_medium() {
		return significance_medium;
	}

	public double getSignificance_high() {
		return significance_high;
	}

	public int getSelectBestN() {
		return selectBestN;
	}

	public String getSelectByMeasure() {
		return selectByMeasure;
	}
	
}
