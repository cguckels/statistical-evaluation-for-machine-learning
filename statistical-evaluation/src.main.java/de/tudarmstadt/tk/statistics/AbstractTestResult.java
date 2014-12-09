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

/**
 * Abstract class for the result of a statistics test, comprising fields both
 * present for tests on two or more entities
 * 
 * @author Guckelsberger, Schulz
 */
public abstract class AbstractTestResult {

	protected String method;
	protected String statisticType;
	protected HashMap<String, Double> parameter;
	protected HashMap<String, AbstractTestResult> assumptions;

	public AbstractTestResult(String method, HashMap<String, Double> parameter) {
		this.method = method;
		this.parameter = parameter;
		assumptions = new HashMap<String, AbstractTestResult>();
	}

	public String getMethod() {
		return method;
	}

	public HashMap<String, Double> getParameter() {
		return parameter;
	}

	public HashMap<String, AbstractTestResult> getAssumptions() {
		return this.assumptions;
	}

	public String getStatisticType() {
		return statisticType;
	}

	public void setStatisticType(String statisticType) {
		this.statisticType = statisticType;
	}
}
