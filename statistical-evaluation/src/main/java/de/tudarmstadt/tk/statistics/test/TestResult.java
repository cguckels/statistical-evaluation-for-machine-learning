package de.tudarmstadt.tk.statistics.test;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Representation of a statistic test allowing to print the results or use them
 * further
 * 
 * @author Guckelsberger, Schulz
 */
public class TestResult extends AbstractTestResult {

	private double pValue;
	private double statistic;

	public TestResult(String method, HashMap<String, Double> parameter, double pValue, double statistic) {
		super(method, parameter);
		this.pValue = pValue;
		this.statistic = statistic;
	}

	public TestResult(TestResult r) {
		super(r.method, new HashMap<String, Double>(r.parameter));
		this.pValue = r.pValue;
		this.statistic = r.statistic;
		if (r.assumptions != null) {
			this.assumptions = (HashMap<String, AbstractTestResult>) r.assumptions.clone();
		}
		this.statisticType = r.getStatisticType();

	}

	public double getpValue() {
		return pValue;
	}

	public double getStatistic() {
		return statistic;
	}

	/**
	 * Prints the test results in a human-friendly way
	 */
	public String toString() {

		String params = "";
		Iterator<Entry<String, Double>> it = this.parameter.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Double> pair = it.next();
			params += String.format("%s=%g\n", pair.getKey(), pair.getValue());
		}

		String str;
		if (this.parameter.entrySet().size() != 0) {
			str = String.format("Parameters:\n%s\nStatistic:\n%g\n\nP-Value:\n%g\n", params, this.statistic, this.pValue);
		} else {
			str = String.format("Statistic:\n%g\n\nP-Value:\n%g\n", this.statistic, this.pValue);
		}
		return str;

	}

}