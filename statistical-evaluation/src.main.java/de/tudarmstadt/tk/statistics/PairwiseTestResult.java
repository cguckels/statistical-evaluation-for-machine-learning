package de.tudarmstadt.tk.statistics;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Representation of a statistic test result involving pairwise comparisons,
 * allowing to print the results or use them further
 * 
 * @author Guckelsberger, Schulz
 */
public class PairwiseTestResult extends AbstractTestResult {

	private HashMap<String, double[][]> pValueCorrections;

	public HashMap<String, double[][]> getpValueCorrections() {
		return pValueCorrections;
	}

	private double[][] pValue;
	private double[][] statistic;
	private String correctionMethod;
	private boolean requiresPValueCorrection;

	// Used to determine whether the p-values in this TestResult emerge from
	// evaluating n items pairwise (NxN) or all items against one control item
	// (Control)

	public PairwiseTestResult(String method, String correctionMethod, HashMap<String, Double> parameter, double[][] pValue, double[][] statistic) {
		super(method, parameter);
		this.pValue = pValue;
		this.statistic = statistic;
		this.correctionMethod = correctionMethod;
		pValueCorrections = new HashMap<String, double[][]>();
		requiresPValueCorrection = false;
	}

	public PairwiseTestResult(PairwiseTestResult r) {
		super(r.method, new HashMap<String, Double>(r.parameter));
		this.pValue = r.pValue.clone();
		if (r.statistic != null) {
			this.statistic = r.statistic.clone();
		}
		if (r.assumptions != null) {
			this.assumptions = (HashMap<String, AbstractTestResult>) r.assumptions.clone();
		}
		if (r.pValueCorrections != null) {
			this.pValueCorrections = (HashMap<String, double[][]>) r.pValueCorrections.clone();
		}
		this.correctionMethod = r.correctionMethod;
		this.statisticType = r.getStatisticType();
	}

	/**
	 * P-values for pairwise comparisons are stored in a 2-dimensional array in
	 * lower triangular form
	 * 
	 * @return A 2-dimensional array in which an entry pValue[1][2] corresponds
	 *         to the p-Value resulting from the pairwise comparison of the
	 *         entities with indices 1 and 2
	 */
	public double[][] getpValue() {
		return pValue;
	}

	/**
	 * P-values for pairwise comparisons are stored in a 2-dimensional array in
	 * lower triangular form
	 */
	public void setpValue(double[][] pValue) {
		this.pValue = pValue;
	}

	/**
	 * The statistics values for pairwise comparisons are stored in a
	 * 2-dimensional array in lower triangular form
	 * 
	 * @return A 2-dimensional array in which an entry statistic[1][2]
	 *         corresponds to the statistic value resulting from the pairwise
	 *         comparison of the entities with indices 1 and 2
	 */
	public double[][] getStatistic() {
		return statistic;
	}

	public String getCorrectionMethod() {
		return correctionMethod;
	}

	public void addPValueCorrections(String method, double[][] pValues) {
		this.pValueCorrections.put(method, pValues);
	}

	public boolean getRequiresPValueCorrection() {
		return requiresPValueCorrection;
	}

	public void setRequiresPValueCorrection(boolean requiresPValueCorrection) {
		this.requiresPValueCorrection = requiresPValueCorrection;
	}

	/**
	 * Prints the pairwise test results in a human-friendly way
	 */
	public String toString() {

		String params = "";
		Iterator<Entry<String, Double>> it = this.parameter.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Double> pair = it.next();
			params += String.format("%s=%g\n", pair.getKey(), pair.getValue());
		}

		String pv = toString(this.pValue);
		String st = toString(this.statistic);

		String str;
		if (this.parameter.entrySet().size() != 0) {
			str = String.format("Parameters:\n%s\nStatistic:\n%s\nP-Value:\n%s\n", params, st, pv);
		} else {
			str = String.format("Statistic:\n%s\nP-Value:\n%s\n", st, pv);
		}

		// String str =
		// String.format("Method: %s\n\nParameters:\n%s\nStatistic:\n%s\nP-Value:\n%s",
		// this.method, params, st, pv);
		return str;

	}

	private String toString(double[][] array) {

		String str = "";
		if (array == null) {
			return str;
		}

		for (double[] subarray : array) {
			for (double p : subarray) {
				str += String.format("%g ", p);
			}
			str += "\n";
		}
		return str;
	}

	/*
	 * private String toString(double[][] array){
	 * 
	 * String str=""; if(array==null){ return str; }
	 * 
	 * //NUMBER CONVERSION ERROR str+="i "; for(int i=1; i<array.length; i++){
	 * str+=String.format("%g ",i); } str+="\n"; for(int i=0; i<array.length;
	 * i++){ double[] subarray = array[i]; str+=String.format("%g ",i+2);
	 * for(double p:subarray){ str+=String.format("%g ",p); } str+="\n"; }
	 * return str; }
	 */

}
