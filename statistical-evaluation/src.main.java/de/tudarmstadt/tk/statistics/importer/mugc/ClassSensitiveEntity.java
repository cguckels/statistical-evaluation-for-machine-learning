package de.tudarmstadt.tk.statistics.importer.mugc;

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
 * interface used for stratifying dataset differing in their class labels
 * 
 * @author Karolus, Schulz 
 *
 */
public interface ClassSensitiveEntity {

	/**
	 * identifies the class labels of this entity
	 * 
	 * @return String representation of the class label
	 */
	public String getClassLabel();

	/**
	 * state whether your class label is nominal or not.<br>
	 * Stratification can only be done on nominal attributes
	 * 
	 * @return whether class label is nominal
	 */
	public boolean isNominal();

	/**
	 * identifies the id of this entity, within its dataset
	 * 
	 * @return id of this entity
	 */
	public long getId();

	/**
	 * format this entity in its original csv format for rewriting after e.g.
	 * stratification
	 * 
	 * @return String representation (for csv) of this entity
	 */
	public String toCsvFile();

	/**
	 * if your dataset requires a functioning header, supply it here
	 * 
	 * @return header for your csv file (add linebreak at the end); otherwise
	 *         empty string
	 */
	public String getCsvHeader();
}
