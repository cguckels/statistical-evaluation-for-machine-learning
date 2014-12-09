package de.tudarmstadt.tk.statistics.config;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import de.tudarmstadt.tk.statistics.test.RBridge;


/**
 * Singleton to encapsulate the configuration for the statistical evaluation
 * Parameters can be set both via config file or programmatically 
 * @author Guckelsberger, Schulz
 *
 */
public class StatsConfig {

	// Singleton: Only allow for one instance of RStatistics
	private static volatile StatsConfig instance = null;
	
    private static final Logger logger = LogManager.getLogger("Statistics");
    private static String SCHEMA_PATH = "config.xsd";
	
	private	HashMap<String,String> requiredTests = null;
	private	List<String> requiredCorrections = null;
	private HashMap<String, Double> significanceLevels = null;
	private int selectBestN;
	private String selectByMeasure;
	
	public static StatsConfig getInstance(String filePath) {
		if (instance == null) {
			synchronized (RBridge.class) {
				if (instance == null) {
					instance = new StatsConfig(filePath);
				}
			}
		}
		return instance;
	}
	
	private StatsConfig(String pathToConfigFile) {

		this.parseXML(pathToConfigFile);
		
	}
	
	/**
	 * Validate and parse XML config file. Also check if file contains legal values for tests, p-value corrections and signficiance levels.
	 * @param pathToConfigFile 
	 */
	private void parseXML(String pathToConfigFile){

		 //Validate
        XMLStreamReader reader;
		try {
			reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(pathToConfigFile));

	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema(new File(SCHEMA_PATH));

	        Validator validator = schema.newValidator();
	        validator.validate(new StAXSource(reader));
	        
		}catch(IllegalArgumentException e){
			logger.log(Level.ERROR, "Statistics config file doesn't validate!");
			System.err.println("Statistics config file doesn't validate!");
			System.exit(1);
		}catch (XMLStreamException	| FactoryConfigurationError | SAXException | IOException e1) {
			logger.log(Level.ERROR, "Error while validating statistics config file.");
			System.err.println("Error while validating statistics config file.");
			System.exit(1);
		}

		
		//Parse
		requiredTests = new HashMap<String,String>();
		requiredCorrections = new ArrayList<String>();
		significanceLevels = new HashMap<String, Double>();
		
	    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	    InputStream in;
	    try {
	    	in = new FileInputStream("config.xml");
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
			   
		      while (eventReader.hasNext()) {
		          XMLEvent event = eventReader.nextEvent();

		          if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("test")) {
		        	  String c = null;
		        	  String n = null;
		        	  while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("test"))){
		        		  event = eventReader.nextEvent();
				          if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("class")) {
				        	  event = eventReader.nextEvent();
				        	  c = event.asCharacters().getData();
				          } else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("name")) {
				        	  event = eventReader.nextEvent();
				        	  n = event.asCharacters().getData();
				          }
		        	  }
		        	  if(StatsConfigConstants.TESTS.containsKey(c)){
		        		  if(StatsConfigConstants.TESTS.get(c).contains(n)){
				        	  requiredTests.put(c, n);
				        	  continue;
		        		  }
		        	  }
		        	  throw new IllegalArgumentException(c + ", " + n); 
		          }
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("significanceLevel")) {
		        	  String l = null;
		        	  double v = 1; 
		        	  while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("significanceLevel"))){
		        		  event = eventReader.nextEvent();
				          if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("level")) {
				        	  event = eventReader.nextEvent();
				        	  l = event.asCharacters().getData();
				          }else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("value")) {
				        	  event = eventReader.nextEvent();
				        	  v = Double.parseDouble(event.asCharacters().getData());
				          }
		        	  }
		        	  if(StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.contains(l)){
			        	  significanceLevels.put(l, v);
			        	  continue;
		        	  }
		        	  throw new IllegalArgumentException(l); 
		          }
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("pCorrection")) {
		        	  event = eventReader.nextEvent();
		        	  String pC = event.asCharacters().getData();
		        	  
		        	  if(StatsConfigConstants.CORRECTION_VALUES.contains(pC)){
			        	  requiredCorrections.add(pC);
			        	  continue;
		        	  }
		        	  throw new IllegalArgumentException(pC); 
		          }
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("selectBest")) {
		        	  while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("selectBest"))){
		        		  event = eventReader.nextEvent();
				          if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("count")) {
				        	  event = eventReader.nextEvent();
				        	  selectBestN = Integer.parseInt(event.asCharacters().getData());
				          }else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("measure")) {
				        	  event = eventReader.nextEvent();
				        	  selectByMeasure = event.asCharacters().getData();
				          }
		        	  }
		          }
		      }	
		      
	    }catch(IllegalArgumentException e){
	    	logger.log(Level.ERROR, "Illegal argument in config XML: " + e.getMessage());
			System.err.println("Illegal argument in config XML: " + e.getMessage());
			System.exit(1);
	    } catch (FileNotFoundException e) {
		 	logger.log(Level.ERROR, "Statistics config file not found.");
			System.err.println("Statistics config file not found.");
			System.exit(1);
		} catch (XMLStreamException e) {
		 	logger.log(Level.ERROR, "Error while parsing statistics config file.");
			System.err.println("Error while parsing statistics config file.");
			System.exit(1);
		}
	}

	public HashMap<String, String> getRequiredTests() {
		return requiredTests;
	}

	public List<String> getRequiredCorrections() {
		return requiredCorrections;
	}

	public int getSelectBestN() {
		return selectBestN;
	}

	public String getSelectByMeasure() {
		return selectByMeasure;
	}

	public HashMap<String, Double> getSignificanceLevels() {
		return significanceLevels;
	}
	
}
