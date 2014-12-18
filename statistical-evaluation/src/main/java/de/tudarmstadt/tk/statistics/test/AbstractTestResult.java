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
