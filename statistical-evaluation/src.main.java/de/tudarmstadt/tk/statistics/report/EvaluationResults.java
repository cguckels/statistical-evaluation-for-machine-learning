package de.tudarmstadt.tk.statistics.report;

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
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;

import de.tudarmstadt.tk.statistics.AbstractTestResult;
import de.tudarmstadt.tk.statistics.SampleData;

/**
 * Container to store the results of a statistical evaluation, i.e. the p-values, parameters and statistics of the individual tests, the names of the data sets, the metadata of the evaluated models etc.
 * @author Guckelsberger, Schulz
 */
public class EvaluationResults implements java.io.Serializable{
	
	private static final long serialVersionUID = -8891642496921094578L;
	
	//Pre-defined significance levels. Changes will affect what is deemed high/medium/low/not significant in the reports
	private double significance_low = 1;
	private double significance_medium = 1;
	private double significance_high = 1;
	
	//Entire sample information for a particular pipeline run
	private SampleData sampleData;

	//(Post-hoc) test results
	private HashMap<String,Pair<String,AbstractTestResult>> parametricTestResults;
	private HashMap<String,Pair<String,AbstractTestResult>> parametricPostHocTestResults;
	private HashMap<String,Pair<String,AbstractTestResult>> nonParametricTestResults;
	private HashMap<String,Pair<String,AbstractTestResult>> nonParametricPostHocTestResults;
	
	//(Post-hoc) n x m comparisons ordering
	private HashMap<String,HashMap<Integer,TreeSet<Integer>>> parameticPostHocOrdering;
	private HashMap<String,int[][]> parameticPostHocEdgelist;
	private HashMap<String,HashMap<Integer,TreeSet<Integer>>> nonParameticPostHocOrdering;
	private HashMap<String,int[][]> nonParameticPostHocEdgelist;

	//Applied tests
	private String parametricTest = null;
	private String parametricPostHocTest = null;
	private String nonParametricTest = null;
	private String nonParametricPostHocTest = null;
	
	//Evaluated performance measures
	private HashSet<String> measures = null;
	
	//Indicates if this is a nxn or 1:n baseline evaluation
	private boolean isBaselineEvaluation = false;

	public EvaluationResults(){
		parametricTestResults = new HashMap<String,Pair<String,AbstractTestResult>>();
		nonParametricTestResults = new HashMap<String,Pair<String,AbstractTestResult>>();
		parametricPostHocTestResults = new HashMap<String,Pair<String,AbstractTestResult>>();
		nonParametricPostHocTestResults = new HashMap<String,Pair<String,AbstractTestResult>>();
		parameticPostHocOrdering = new HashMap<String,HashMap<Integer,TreeSet<Integer>>>();
		nonParameticPostHocOrdering = new HashMap<String,HashMap<Integer,TreeSet<Integer>>>();
		parameticPostHocEdgelist = new HashMap<String,int[][]>();
		nonParameticPostHocEdgelist = new HashMap<String,int[][]>();
		measures = new HashSet<String>();
	}
	
	public boolean isBaselineEvaluation() {
		return isBaselineEvaluation;
	}

	public void setIsBaselineEvaluation(boolean isBaselineEvaluation) {
		this.isBaselineEvaluation = isBaselineEvaluation;
	}

	public void addParametricTestResult(Pair<String,AbstractTestResult> result, String measure){
		this.parametricTestResults.put(measure,result);
	}

	public void addNonParametricTestResult(Pair<String, AbstractTestResult> nonParametricTestResult, String measure) {
		this.nonParametricTestResults.put(measure,nonParametricTestResult);
	}	
	
	public void addParametricPostHocTestResult(Pair<String,AbstractTestResult> result, String measure){
		this.parametricPostHocTestResults.put(measure,result);
	}

	public void addNonParametricPostHocTestResult(Pair<String, AbstractTestResult> nonParametricTestResult, String measure) {
		this.nonParametricPostHocTestResults.put(measure,nonParametricTestResult);
	}
	
	public HashMap<String, Pair<String, AbstractTestResult>> getParametricTestResults() {
		return parametricTestResults;
	}

	public HashMap<String, Pair<String, AbstractTestResult>> getParametricPostHocTestResults() {
		return parametricPostHocTestResults;
	}

	public HashMap<String, Pair<String, AbstractTestResult>> getNonParametricTestResults() {
		return nonParametricTestResults;
	}

	public HashMap<String, Pair<String, AbstractTestResult>> getNonParametricPostHocTestResults() {
		return nonParametricPostHocTestResults;
	}
	
	public void setSignificanceLevel(double low, double medium, double high){
		this.significance_low=low;
		this.significance_medium=medium;
		this.significance_high=high;
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
	
	public SampleData getSampleData() {
		return sampleData;
	}

	public void setSampleData(SampleData sampleData) {
		this.sampleData = sampleData;
	}
	
	public String getParametricTest() {
		return parametricTest;
	}

	public void setParametricTest(String parametricTest) {
		this.parametricTest = parametricTest;
	}

	public String getParametricPostHocTest() {
		return parametricPostHocTest;
	}

	public void setParametricPostHocTest(String parametricPostHocTest) {
		this.parametricPostHocTest = parametricPostHocTest;
	}

	public String getNonParametricTest() {
		return nonParametricTest;
	}

	public void setNonParametricTest(String nonParametricTest) {
		this.nonParametricTest = nonParametricTest;
	}

	public String getNonParametricPostHocTest() {
		return nonParametricPostHocTest;
	}

	public void setNonParametricPostHocTest(String nonParametricPostHocTest) {
		this.nonParametricPostHocTest = nonParametricPostHocTest;
	}
	
	public ArrayList<String> getMeasures() {
		return new ArrayList<String>(measures);
	}
	
	public void addMeasure(String measure){
		this.measures.add(measure);
	}

	public HashMap<String, HashMap<Integer, TreeSet<Integer>>> getParameticPostHocOrdering() {
		return parameticPostHocOrdering;
	}

	public void setParameticPostHocOrdering(
			HashMap<String, HashMap<Integer, TreeSet<Integer>>> parameticPostHocOrdering) {
		this.parameticPostHocOrdering = parameticPostHocOrdering;
	}

	public HashMap<String, HashMap<Integer, TreeSet<Integer>>> getNonParameticPostHocOrdering() {
		return nonParameticPostHocOrdering;
	}

	public void setNonParameticPostHocOrdering(
			HashMap<String, HashMap<Integer, TreeSet<Integer>>> nonParameticPostHocOrdering) {
		this.nonParameticPostHocOrdering = nonParameticPostHocOrdering;
	}

	public HashMap<String, int[][]> getParameticPostHocEdgelist() {
		return parameticPostHocEdgelist;
	}

	public void setParameticPostHocEdgelist(
			HashMap<String, int[][]> parameticPostHocEdgelist) {
		this.parameticPostHocEdgelist = parameticPostHocEdgelist;
	}

	public HashMap<String, int[][]> getNonParameticPostHocEdgelist() {
		return nonParameticPostHocEdgelist;
	}

	public void setNonParameticPostHocEdgelist(
			HashMap<String, int[][]> nonParameticPostHocEdgelist) {
		this.nonParameticPostHocEdgelist = nonParameticPostHocEdgelist;
	}
	
}
