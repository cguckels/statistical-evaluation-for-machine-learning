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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
	
	private	HashMap<StatsConfigConstants.TEST_CLASSES,String> requiredTests = null;
	private	List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections = null;
	private HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES,Double> significanceLevels = null;
	private int selectBestN;
	private String selectByMeasure;
	private StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable;
	
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
	
	public static StatsConfig getInstance(HashMap<StatsConfigConstants.TEST_CLASSES,String> requiredTests, List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections, HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES,Double> significanceLevels, int selectBestN, String selectByMeasure, StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable) {
		if (instance == null) {
			synchronized (RBridge.class) {
				if (instance == null) {
					
					//Validate arguments
					//Tests
					if(requiredTests.size()!=StatsConfigConstants.TEST_CLASSES.values().length){
						throw new IllegalArgumentException("Number of test classes specified does not match requirements!");
					}
					Iterator<StatsConfigConstants.TEST_CLASSES> itT = requiredTests.keySet().iterator();
					while(itT.hasNext()){
						StatsConfigConstants.TEST_CLASSES testClass = itT.next();
						String testName = requiredTests.get(testClass);
						
						if(!StatsConfigConstants.TESTS.get(testClass).contains(testName)){
							throw new IllegalArgumentException(testName + " is not a valid test for test class " + testClass+"!");
						}
					}
					
					//Correction methods
					if(requiredCorrections.size()==0){
						throw new IllegalArgumentException("At least one p-value correction method must be specified!");	
					}
					
					//Significance levels
					if(requiredCorrections.size()!=StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.values().length){
						throw new IllegalArgumentException("Number of significance levels specified does not match requirements!");
					}
					Iterator<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES> it = significanceLevels.keySet().iterator();
					while(it.hasNext()){
						double significanceValue = significanceLevels.get(it.next());
						if(significanceValue<0 || significanceValue>1){
							throw new IllegalArgumentException(significanceValue + " is not a valid significance value (must be between 0 and 1)!");
						}
					}
					
					instance = new StatsConfig(requiredTests, requiredCorrections, significanceLevels, selectBestN, selectByMeasure, fixIndependentVariable); 
					
				}
			}
		}
		return instance;
	}
	
	private StatsConfig(String pathToConfigFile) {

		this.parseXML(pathToConfigFile);
		
	}
	
	private StatsConfig(HashMap<StatsConfigConstants.TEST_CLASSES,String> requiredTests, List<StatsConfigConstants.CORRECTION_VALUES> requiredCorrections, HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES,Double> significanceLevels, int selectBestN, String selectByMeasure, StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES fixIndependentVariable){

		this.requiredTests = requiredTests;
		this.requiredCorrections = requiredCorrections;
		this.significanceLevels = significanceLevels;
		this.selectBestN = selectBestN;
		this.selectByMeasure = selectByMeasure;
		this.fixIndependentVariable = fixIndependentVariable;
		
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
		requiredTests = new HashMap<StatsConfigConstants.TEST_CLASSES,String>();
		requiredCorrections = new ArrayList<StatsConfigConstants.CORRECTION_VALUES>();
		significanceLevels = new HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES,Double>();
		
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
		        	  
		        	  boolean illegal = true;
		        	  for (StatsConfigConstants.TEST_CLASSES tc : StatsConfigConstants.TEST_CLASSES.values()) {
		        	        if (tc.name().equals(c)) {
		        	        	 if(StatsConfigConstants.TESTS.get(tc).contains(n)){
		        	        		 requiredTests.put(tc, n);
				        	         illegal = false;
						        	 break;
				        		  }
		        	        }
		        	  }
		        	  
		        	  if(illegal){
		        		  throw new IllegalArgumentException(c + ", " + n); 
		        	  }
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
		        	  
		        	  boolean illegal = true;
		        	  for (StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES s : StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES.values()) {
		        	        if (s.name().equals(l)) {
		        	        	significanceLevels.put(s, v);
		        	        	illegal = false;
					        	break;		        	        
					        }
		        	  }
		        	  
		        	  if(illegal){
		        		  throw new IllegalArgumentException(l); 
		        	  }
		          }
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("pCorrection")) {
		        	  event = eventReader.nextEvent();
		        	  String pC = event.asCharacters().getData();
		        	  
		        	  boolean illegal = true;
		        	  for (StatsConfigConstants.CORRECTION_VALUES c : StatsConfigConstants.CORRECTION_VALUES.values()) {
		        	        if (c.name().equals(pC)) {
		        	        	requiredCorrections.add(c);
		        	        	illegal = false;
					        	break;		        	        
					        }
		        	  }
		        	  
		        	  if(illegal){
		        		  throw new IllegalArgumentException(pC); 
		        	  }
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
		          else if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("fixIndependentVariable")) {
		        	  event = eventReader.nextEvent();
		        	  String f = event.asCharacters().getData();
		        	  
		        	  boolean illegal = true;
		        	  for (StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES i : StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES.values()) {
		        	        if (i.name().equals(f)) {
		        	        	fixIndependentVariable=i;
		        	        	illegal=false;
		        	        	break;		        	        
					        }
		        	  }
		        	  
		        	  if(illegal){
		        		  throw new IllegalArgumentException(f); 
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

	public HashMap<StatsConfigConstants.TEST_CLASSES, String> getRequiredTests() {
		return requiredTests;
	}

	public List<StatsConfigConstants.CORRECTION_VALUES> getRequiredCorrections() {
		return requiredCorrections;
	}

	public int getSelectBestN() {
		return selectBestN;
	}

	public String getSelectByMeasure() {
		return selectByMeasure;
	}

	public HashMap<StatsConfigConstants.SIGNIFICANCE_LEVEL_VALUES, Double> getSignificanceLevels() {
		return significanceLevels;
	}

	public StatsConfigConstants.INDEPENDENT_VARIABLES_VALUES getFixIndependentVariable() {
		return fixIndependentVariable;
	}

}
