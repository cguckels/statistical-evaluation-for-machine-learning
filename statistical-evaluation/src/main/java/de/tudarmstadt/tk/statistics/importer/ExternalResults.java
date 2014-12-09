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

/**
 * Used to read a .csv with the following columns: training dataset name, testing dataset name, classifier parameters, feature set name, recall, fMeasure, precision and accuracy
 * @author Guckelsberger, Schulz
 */
public class ExternalResults {

	public String trainSetName;
	public String testSetName;
	public String featureSetName;
	public String classifierParameters;
	public double recall = 0;
	public double fMeasure = 0;
	public double precision = 0;
	public double accuracy = 0;
}
