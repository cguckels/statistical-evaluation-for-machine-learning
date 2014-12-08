package de.tudarmstadt.tk.statistics.importer.mugc;

import java.io.Serializable;


/**
 * a model class for a given Tweet.<br>
 * Note that the tweet may not yet been annotated (preprocessed). Therefore only use it in conjunction
 * with the pipeline, as it provides a config file.<br>
 * 
 * @author Jakob
 *
 */
public class Tweet implements Serializable, ClassSensitiveEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public static final String NO_LABEL_ASSIGNED = "NO_LABEL";
	
	
	private long id;
	
	/**
	 * the class label associated with this tweet (NO, crash, fire, shooting)
	 */
	private String label;
	
	/**
	 * the tweeted text
	 */
	private String text;
	
	
	/**
	 * the preprocessed text, if available
	 */
	private String cleanedText;

	
	
	/**
	 * @param id
	 * @param label
	 * @param text
	 */
	public Tweet(long id, String text, String label) {
		super();
		this.id = id;
		this.label = label;
		this.text = text;
		this.cleanedText = "";
	}


	public Tweet() {
	}


	public long getId() {
		return id;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}


	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label)
	{
		this.label = label;
	}

	
	/**
	 * 
	 * @return the original tweet text. This one is NOT preprocessed
	 */
	public String getText() {
		return text;
	}


	public String getCleanedText() {
		return cleanedText;
	}


	public void setCleanedText(String cleanedText) {
		this.cleanedText = cleanedText;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cleanedText == null) ? 0 : cleanedText.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tweet other = (Tweet) obj;
		if (cleanedText == null) {
			if (other.cleanedText != null)
				return false;
		} else if (!cleanedText.equals(other.cleanedText))
			return false;
		if (id != other.id)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}


	@Override
	public String getClassLabel() {
		return getLabel();
	}


	@Override
	public boolean isNominal() {
		return true;
	}


	@Override
	public String toCsvFile() {
		return id + ";" + text + ";" + label;
	}


	@Override
	public String getCsvHeader() {
		return "";
	}

}
