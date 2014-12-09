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