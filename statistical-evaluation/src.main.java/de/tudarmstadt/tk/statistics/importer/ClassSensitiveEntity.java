package de.tudarmstadt.tk.statistics.importer;


/**
 * interface used for stratifying dataset differing in their class labels
 * 
 * @author Karolus
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
	 * format this entity in its original csv format for rewriting after e.g. stratification
	 * 
	 * @return String representation (for csv) of this entity
	 */
	public String toCsvFile();

	/**
	 * if your dataset requires a functioning header, supply it here
	 * 
	 * @return header for your csv file (add linebreak at the end); otherwise empty string
	 */
	public String getCsvHeader();
}
