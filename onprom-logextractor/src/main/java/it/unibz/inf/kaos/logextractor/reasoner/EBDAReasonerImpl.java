/*
 * Copyright (C) 2017 Free University of Bozen-Bolzano
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.logextractor.reasoner;

import ch.qos.logback.classic.Logger;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
import it.unibz.inf.kaos.logextractor.model.*;
import it.unibz.inf.kaos.logextractor.util.EfficientHashMap;
import it.unibz.inf.kaos.logextractor.util.Print;
import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgEvent;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

/**
 * This class provide some functionalities to do some particular reasoning over EBDA Model.
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 * 
 */
public class EBDAReasonerImpl extends EBDAReasonerAbstract{

//	private static final Logger logger = Logger.getLogger(LEConstants.LOGGER_NAME);
//	private static final Logger logger = (Logger) LoggerFactory.getLogger(LEConstants.LOGGER_NAME);
//	private static final Logger logger = (Logger) LoggerFactory.getLogger(EBDAReasonerImpl.class);
	private static final Logger logger = (Logger) LoggerFactory.getLogger("EBDAReasoner");

	private QuestOWL questReasoner;
	private XFactoryOnProm xfact;
	private boolean verbose = false;
	private boolean allowToDisposeQuestReasoner = true;

	/**
	 * Initializes EBDA Reasoner based on the given EBDA Model.
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param ebdaModel
	 */
	public EBDAReasonerImpl(EBDAModel ebdaModel){
		
		init(ebdaModel);
	}

	/**
	 * Initializes EBDA Reasoner based on the given EBDA Mapping.
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param ebdaModel
	 */
	public EBDAReasonerImpl(EBDAMapping ebdaMapping){
		
		init(ebdaMapping);
	}

	/**
	 * Initializes EBDA Reasoner based on the given OBDA Model.
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param ebdaModel
	 */
	private void init(OBDAModel ebdaModel){
		
		this.xfact = XFactoryOnProm.getInstance();
		super.setExecutionLogListener(null);
		
		//Load the Event Ontology
			//System.out.println("\n--------------------------------------------------------");
			//System.out.println("DEBUGA: loading event ontology");
					
			OWLOntologyManager eventOntoMan = OWLManager.createOWLOntologyManager();
			URL eventOntoURL = this.getClass().getResource(XESEOConstants.eventOntoPath);
	
			//System.out.println("loading event ontology from: "+ this.eventOntoURL.getPath());
			
			OWLOntology eventOnto = null;
			
			try {
				eventOnto = eventOntoMan.loadOntologyFromOntologyDocument(eventOntoURL.openStream());
			} catch (OWLOntologyCreationException | IOException e) {
				e.printStackTrace();
			}
	
			//System.out.println("DEBUGA: END OF loading event ontology");
			//System.out.println("\n--------------------------------------------------------");
		//END OF Loading the Event Ontology
	
    	//Create an instance of Quest OWL reasoner.
			QuestOWLFactory factory = new QuestOWLFactory();
			QuestPreferences preferences = new QuestPreferences();
			preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);
	
	        Builder builder = QuestOWLConfiguration.builder();
			builder.obdaModel(ebdaModel);
			builder.preferences(preferences);
			
			QuestOWLConfiguration config = builder.build();
			
			if(eventOnto != null)
				questReasoner = factory.createReasoner(eventOnto, config);
    	//END OF Creating an instance of Quest OWL reasoner.
			
		//this.disableLocalLogging();
	}

	//////////////////////////////////////////////////////////////////////
	// Some utility methods
	//////////////////////////////////////////////////////////////////////
	
	public boolean isAllowToDisposeQuestReasoner() {
		return allowToDisposeQuestReasoner;
	}

	public void setAllowToDisposeQuestReasoner(boolean allowToDisposeQuestReasoner) {
		this.allowToDisposeQuestReasoner = allowToDisposeQuestReasoner;
	}

	public void disableLocalLogging(){
		logger.setLevel(ch.qos.logback.classic.Level.OFF);
	}

	public void enableLocalLogging(){
		logger.setLevel(ch.qos.logback.classic.Level.ALL);
	}
	
	public void dispose(){
		if(this.questReasoner != null)
			this.questReasoner.dispose();
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	//////////////////////////////////////////////////////////////////////
	// END OF Some utility methods
	//////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////
	// XAttributes retriever methods
	//////////////////////////////////////////////////////////////////////
	
	/**
	 * It retrieves attributes by using the following query 
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?att ?attType ?attKey ?attValue 
	 * WHERE {
	 * 		?att a :Attribute; :typeA ?attType; :keyA ?attKey; :valueA ?attVal.
	 * }
	 * </code>
	 * </pre>
	 * 
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It exhaustively retrieves all possible attributes 
	 * 			(even the one that might not belong to either log, trace or event).
	 * 		</li>
	 * 		<li> 
	 * 			It doesn't accesses the query result using the name of the answer 
	 * 			variables (hence, it's unsafe).
	 * 		</li>
	 * </ul>
	 * 
	 * @return HashMap<String,XAttribute> - A hashMap between an attribute URI and the corresponding XAttribute object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XAttribute> getXAttributesSimpleImpl() throws OWLException{
		
		HashMap<String,XAttribute> attributes = new HashMap<String,XAttribute>();
		
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qAttTypeKeyVal_Simple);

			while (rs.nextRow()) {
				String att = rs.getOWLObject(1).toString(); 
				String type = rs.getOWLLiteral(2).getLiteral(); 
				String key = rs.getOWLLiteral(3).getLiteral(); 
				String val = rs.getOWLLiteral(4).getLiteral(); 

				XExtension xext = this.xfact.createPredefinedXExtension(key);
				XAttribute xatt = this.xfact.createXAttribute(type, key, val, xext);
				
				if(!attributes.containsKey(att))//this check handles duplicates
					attributes.put(att, xatt);
				
				//System.out.println("------------------------------------------------------------");
				//System.out.println("DEBUGA:");
				//System.out.println("\t att uri: "+ att);
				//System.out.println("\t XAttribute obj: "+ xatt);
				//System.out.println("\t type: "+ type + "; key: "+ key +"; val: "+ val);
			}
			
			rs.close();
			
		}catch (UnsupportedAttributeTypeException e) {
			e.printStackTrace();
		}finally {
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
		}
		
        //Logging the information about the extracted XAttributes information
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.logXAttributesExtraction(attributes)));

		return attributes;	
	}	
	
	/**
	 * It retrieves attributes by using the following query 
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?att ?attType ?attKey ?attValue 
	 * WHERE {
	 * 		?att a :Attribute; :typeA ?attType; :keyA ?attKey; :valueA ?attVal.
	 * }
	 * </code>
	 * </pre>
	 * 
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It exhaustively retrieves all possible attributes 
	 * 			(even the one that might not belong to either log, trace or event).
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables (hence, quite robust).
	 * 		</li>
	 * </ul>
	 * 
	 * @return HashMap<String,XAttribute> - A hashMap between an attribute URI and the corresponding XAttribute object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XAttribute> getXAttributes() throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXAttributes():\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XAttribute> attributes = new HashMap<String,XAttribute>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qAttTypeKeyVal_Simple);

			while (rs.nextRow()) {
				OWLObject attObj = rs.getOWLObject(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAtt);
				String newAtt = (attObj == null? null: attObj.toString()); 
				if(newAtt == null) continue;//if the attribute is null, then skip the rest and move on
				
				OWLLiteral typeObj = rs.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttType); 
				String type = (typeObj == null? null: typeObj.getLiteral()); 
				if(type == null) continue;//if the attribute type is null, then skip the rest and move on
				
				OWLLiteral keyObj = rs.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttKey); 
				String key = (keyObj == null? null: keyObj.getLiteral()); 
				if(key == null) continue;

				OWLLiteral valObj = rs.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttVal); 
				String val = (valObj == null? null: valObj.getLiteral());
				if(val == null) continue;

				//if 'attributes' already has an attribute 'att', then fetch the next row
				//Note: if we encounter this case, then there is a chance that an attribute has either multiple
				//key, value or type because we use distint in the query.
				if(attributes.containsKey(newAtt)){
					
					XAttribute xatt = attributes.get(newAtt);
					
					if(xatt != null)
						currentExecutionNote.append(String.format(LEConstants.ATT_MULTIPLE_PROPERTIES, 
								newAtt, xatt.getKey(), xatt.getClass(), xatt, key, type, val));

					continue;
				}

				XExtension xext = this.xfact.getPredefinedXExtension(key);
				XAttribute xatt = null;
				
				try {
					xatt = this.xfact.createXAttribute(type, key, val, xext);
				} catch (UnsupportedAttributeTypeException e) {
					currentExecutionNote.append(
							String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE2, type, key, val));
					continue;
				}
				
				//just in case there is a failure in the attribute creation
				if(xatt == null){
					currentExecutionNote.append(String.format(LEConstants.ATT_CREATION_FAILURE2, type, key, val));
					continue;
				}

				attributes.put(newAtt, xatt);
				
				//System.out.println("------------------------------------------------------------");
				//System.out.println("DEBUGA:");
				//System.out.println("\t att uri: "+ att);
				//System.out.println("\t XAttribute obj: "+ xatt);
				//System.out.println("\t type: "+ type + "; key: "+ key +"; val: "+ val);
			}
			
			if(rs != null) rs.close();
			
		}finally {
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }

        //debug the attributes extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXAttributes(attributes)));
        
		return attributes;	
	}	

	/**
	 * It retrieves attributes, and uses the following query to retrieve the attributes:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?att ?attType
	 * WHERE {
	 * 		?att :typeA ?attType.
	 * }
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?att ?attKey 
	 * WHERE {
	 * 		?att :keyA ?attKey.
	 * }
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?att ?attValue 
	 * WHERE {
	 * 		?att :valueA ?attVal.
	 * }
	 * </code>
	 * </pre>
	 * 
	 * I.e., it splits the query for retrieving attributes information
	 * 
	 * <br/><br/>
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It exhaustively retrieves all possible attributes 
	 * 			(even the one that might not belong to either log, trace or event).
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables (hence, quite robust).
	 * 		</li>
	 * </ul>
	 * 
	 * @return HashMap<String,XAttributeOnProm> - A hashMap between an attribute URI and the corresponding XAttributeOnProm object
	 * @throws OWLException
	 */
	public HashMap<String,XAttributeOnProm> getXAttributesWithSplitQuery() throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXAttributesWithSplitQuery():\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XAttributeOnProm> attributes = new HashMap<String,XAttributeOnProm>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        
        try {	

			//============================================================================
			//Handling Attribute Keys and Extension
			//============================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling XAttribute Keys"));

        	QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qAttKey);

        	OWLObject attObj;
        	OWLLiteral keyObj;
        	OWLLiteral typeObj;
        	OWLLiteral valObj;

        	String newAtt;
        	String key;
        	String type;
        	String val;
        	
        	XAttributeOnProm xatt;
        	XExtension xext;
        	
			while (rs.nextRow()) {
				attObj = rs.getOWLObject(XESEOConstants.qAttKeyAnsVarAtt);
				newAtt = (attObj == null? null: attObj.toString()); 
				if(newAtt == null) continue;//if the attribute is null, then skip the rest and move on
				
				keyObj = rs.getOWLLiteral(XESEOConstants.qAttKeyAnsVarAttKey); 
				key = (keyObj == null? null: keyObj.getLiteral()); 
				if(key == null) continue;//if the attribute key is null, then skip the rest and move on

				//if 'attributes' already has an attribute 'att', then fetch the next row
				//Note: if we encounter this case, then there is a chance that an attribute has either multiple
				//key, value or type because we use distinct in the query.
				if(attributes.containsKey(newAtt))
					continue;

				xatt = this.xfact.createXAttributeOnProm(newAtt);
				
				//just in case there is a failure in the attribute creation
				if(xatt == null){
					currentExecutionNote.append(String.format(LEConstants.ATT_CREATION_FAILURE, newAtt));
					continue;
				}

				//add extension and key information of the attribute
				xext = this.xfact.getPredefinedXExtension(key);
				xatt.setKey(key);
				xatt.setXext(xext);

				attributes.put(newAtt, xatt);
			}
			if(rs != null) rs.close(); rs = null;

			xatt = null; keyObj = null; key = null;
			
			//============================================================================
			//END OF Handling Attribute Keys and Extension
			//============================================================================

			//============================================================================
			//Handling Attribute Types
			//============================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling XAttribute Types"));

        	QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qAttType);
        	
			while (rs2.nextRow()) {
				attObj = rs2.getOWLObject(XESEOConstants.qAttTypeAnsVarAtt);
				newAtt = (attObj == null? null: attObj.toString()); 
				if(newAtt == null) continue;//if the attribute is null, then skip the rest and move on
				
				if(!attributes.containsKey(newAtt))
					continue;

				typeObj = rs2.getOWLLiteral(XESEOConstants.qAttTypeAnsVarAttType); 
				type = (typeObj == null? null: typeObj.getLiteral()); 
				if(type == null) continue;//if the attribute type is null, then skip the rest and move on

				if(attributes.containsKey(newAtt))
					attributes.get(newAtt).setType(type);
			}
			if(rs2 != null) rs2.close(); rs2 = null;
			
			typeObj = null; type = null;

			//============================================================================
			//END OF Handling Attribute Types
			//============================================================================
			
			//============================================================================
			//Handling Attribute Values
			//============================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling XAttribute Values"));

        	QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qAttValue);

			while (rs3.nextRow()) {
				attObj = rs3.getOWLObject(XESEOConstants.qAttValAnsVarAtt);
				newAtt = (attObj == null? null: attObj.toString()); 
				if(newAtt == null) continue;//if the attribute is null, then skip the rest and move on

				if(!attributes.containsKey(newAtt))
					continue;

				valObj = rs3.getOWLLiteral(XESEOConstants.qAttValAnsVarAttVal); 
				val = (valObj == null? null: valObj.getLiteral()); 
				if(val == null) continue;//if the attribute type is null, then skip the rest and move on

				if(attributes.containsKey(newAtt))
					attributes.get(newAtt).setValue(val);
			}
			if(rs3 != null) rs3.close(); rs3 = null;

			valObj = null; val = null;
			
			//============================================================================
			//END OF Handling Attribute Values
			//============================================================================

		}finally {
			if(st != null && !st.isClosed()) st.close(); st = null;
			if(conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }

        //debug the attributes extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
        			new StringBuilder("XAttributes Extraction Results:\n\n")
        				.append(Print.getStringOfXAttributeOnProm(attributes))));
        
		return attributes;	
	}	
	
	/**
	 * It retrieves attributes, and uses the following query to retrieve the attributes:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?att ?attType
	 * WHERE {
	 * 		?att :typeA ?attType.
	 * }
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?att ?attKey 
	 * WHERE {
	 * 		?att :keyA ?attKey.
	 * }
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?att ?attValue 
	 * WHERE {
	 * 		?att :valueA ?attVal.
	 * }
	 * </code>
	 * </pre>
	 * 
	 * I.e., it splits the query for retrieving attributes information.
	 * 
	 * <br/><br/>
	 * 
	 * Main features:
	 * <ul>
	 * 		<li>
	 * 			It is optimized for minimizing the memory usage 
	 * 		</li>
	 * </ul>
	 * 
	 * <br/><br/>
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It exhaustively retrieves all possible attributes 
	 * 			(even the one that might not belong to either log, trace or event).
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables (hence, quite robust).
	 * 		</li>
	 * </ul>
	 * 
	 * @return EfficientHashMap<XAtt> - A hashMap between an attribute URI and the corresponding XAtt
	 * @throws OWLException
	 * @since June 2017
	 */
	public EfficientHashMap<XAtt> getXAttributesWithSplitQueryMO() throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXAttributesWithSplitQueryMO():\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		EfficientHashMap<XAtt> attributes = new EfficientHashMap<XAtt>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        
        try {	

        	OWLObject attObj;
        	OWLLiteral keyObj;
        	OWLLiteral typeObj;
        	OWLLiteral valObj;

        	String newAtt;
        	String key;
        	String type;
        	String val;
        	
        	XAtt xatt = null;
        	XExtension xext;

			//============================================================================
			//Handling Attribute Types
			//============================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling XAttribute Types"));

        	QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qAttType);
        	
			while (rs2.nextRow()) {
				attObj = rs2.getOWLObject(XESEOConstants.qAttTypeAnsVarAtt);
				newAtt = (attObj == null? null: attObj.toString().intern()); 
				if(newAtt == null) continue;//if the attribute is null, then skip the rest and move on
				
				//if 'attributes' already has an attribute 'att', then fetch the next row
				//Note: if we encounter this case, then there is a chance that an attribute has either multiple
				//key, value or type because we use distinct in the query.
				if(attributes.containsKey(newAtt))
					continue;

				typeObj = rs2.getOWLLiteral(XESEOConstants.qAttTypeAnsVarAttType); 
				type = (typeObj == null? null: typeObj.getLiteral().intern()); 
				if(type == null) continue;//if the attribute type is null, then skip the rest and move on

				try {
					xatt = this.xfact.createXAttNoURI(type);
					
				} catch (UnsupportedAttributeTypeException e) {
					
					currentExecutionNote.append(
							String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE, type));
					continue;
				}
				
				//just in case there is a failure in the attribute creation
				if(xatt == null){
					currentExecutionNote.append(String.format(LEConstants.ATT_CREATION_FAILURE, newAtt));
					continue;
				}

				attributes.put(newAtt, xatt);
				
			}
			if(rs2 != null) rs2.close(); rs2 = null;
			
			typeObj = null; type = null;

			//============================================================================
			//END OF Handling Attribute Types
			//============================================================================
			
			//============================================================================
			//Handling Attribute Keys and Extension
			//============================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling XAttribute Keys"));

        	QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qAttKey);
        	
			while (rs.nextRow()) {
				attObj = rs.getOWLObject(XESEOConstants.qAttKeyAnsVarAtt);
				newAtt = (attObj == null? null: attObj.toString().intern()); 
				if(newAtt == null) continue;//if the attribute is null, then skip the rest and move on
				
				if(!attributes.containsKey(newAtt))
					continue;

				keyObj = rs.getOWLLiteral(XESEOConstants.qAttKeyAnsVarAttKey); 
				key = (keyObj == null? null: keyObj.getLiteral()); 
				if(key == null) continue;//if the attribute key is null, then skip the rest and move on

				//add extension and key information of the attribute
				xext = this.xfact.getPredefinedXExtension(key);
				
				if(attributes.containsKey(newAtt)){
					attributes.get(newAtt).setKey(key);
					if(xext != null)
						attributes.get(newAtt).setExtension(xext);
				}
			}
			if(rs != null) rs.close(); rs = null;

			xatt = null; keyObj = null; key = null;

			//============================================================================
			//END OF Handling Attribute Keys and Extension
			//============================================================================
			
			//============================================================================
			//Handling Attribute Values
			//============================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling XAttribute Values"));

        	QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qAttValue);

			while (rs3.nextRow()) {
				attObj = rs3.getOWLObject(XESEOConstants.qAttValAnsVarAtt);
				newAtt = (attObj == null? null: attObj.toString().intern()); 
				if(newAtt == null) continue;//if the attribute is null, then skip the rest and move on

				if(!attributes.containsKey(newAtt))
					continue;

				valObj = rs3.getOWLLiteral(XESEOConstants.qAttValAnsVarAttVal); 
				val = (valObj == null? null: valObj.getLiteral()); 
				if(val == null) continue;//if the attribute type is null, then skip the rest and move on

				if(attributes.containsKey(newAtt))
					attributes.get(newAtt).setVal(val);					
			}
			if(rs3 != null) rs3.close(); rs3 = null;

			valObj = null; val = null;
			
			//============================================================================
			//END OF Handling Attribute Values
			//============================================================================

		}finally {
			if(st != null && !st.isClosed()) st.close(); st = null;
			if(conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }

        //Enable the line below to debug the attributes extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
        			new StringBuilder("XAttributes Extraction Results:\n\n").append(Print.getStringOfXAttributes(attributes))));
        
		return attributes;
	}	

	//////////////////////////////////////////////////////////////////////
	// END OF XAttributes retriever methods
	//////////////////////////////////////////////////////////////////////

	
	
	//////////////////////////////////////////////////////////////////////
	// XEvents retriever methods
	//////////////////////////////////////////////////////////////////////

	/**
	 * It retrieves all association between events and their attributes by using the following query:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?event ?att 
	 * WHERE {
	 * 		?event :EcontainsA ?att . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * From the query results, it creates a hashmap of XEvents where the keys are the Event URIs. 
	 * Each XEvent within the created hashmap of XEvents is filled with the corresponding 
	 * XAttribute objects that are taken from the given hashmap of XAttributes.
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			There is no checks on event's mandatory attributes (name, lifecycle, timestamp)
	 * 		</li>
	 * 		<li>
	 * 			Only retrieve the events that has at least one attributes.
	 * 		</li>
	 * 		<li>
	 * 			It exhaustively retrieves all possible events
	 * 			(even the one that might not belong to a trace).
	 * 		</li>
	 * 		<li> 
	 * 			It doesn't accesses the query result using the name of the answer 
	 * 			variables (hence, it's unsafe).
	 * 		</li>
	 * </ul>
	 * 
	 * @param xattmap - a hashMap between an attribute URI and the corresponding XAttribute object
	 * @return HashMap<String,XEvent> - A hashMap between an event URI and the corresponding XEvent object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * 
	 */
	public HashMap<String,XEvent> getXEventsSimpleImpl(HashMap<String,XAttribute> xattmap) throws OWLException{
		
		HashMap<String,XEvent> events = new HashMap<String,XEvent>();
		
		StringBuilder currentExecutionNote = new StringBuilder(
				"EBDAReasoner:getXEventsSimpleImpl(HashMap<String,XAttribute>):\n");
		int currExecNoteInitLength = currentExecutionNote.length();
		
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
			
			while (rs.nextRow()) {
				String evt = rs.getOWLObject(1).toString(); 
				String att = rs.getOWLObject(2).toString(); 
				
				//System.out.println("------------------------------");
				//System.out.println("evt: "+ evt+ "\n" +"att: "+ att);
				
				//if the given XAttribute map xattmap doesn't contain the information about attribute att
				if(!xattmap.containsKey(att)){
										
					currentExecutionNote.append(String.format(
						"\tThe event '%s' has the attribute '%s', \n\t\tbut it misses the information about this attribute.\n", evt, att)); 
					continue;
				}
				
				if(events.containsKey(evt)){
					
					//check for attribute duplication
					if(!events.get(evt).getAttributes().containsKey(att))
						events.get(evt).getAttributes().put(att, xattmap.get(att));
					
				}else{
					
					XEvent xevt = this.xfact.createEvent();			
					XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
					XAttribute xatt = xattmap.get(att);

					//System.out.println("--------------------");
					//System.out.println("XAttributeMap: "+attsOfEvent);
					//System.out.println("xattmapp contain keys: "+att+" -> "+xattmap.containsKey(att));
					//System.out.println("xatt: "+xatt);
					//System.out.println("xatt: "+xatt.getKey());

					attsOfEvent.put(att, xatt);
					xevt.setAttributes(attsOfEvent);
					events.put(evt, xevt);
				}
			}

			rs.close();
			
		}finally {
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //Logging the information about the extracted XEvents information
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.logXEventsExtraction(events)));

		return events;	
	}	
	
	/**
	 * It retrieves all association between events and their attributes by using the following query:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?event ?att 
	 * WHERE {
	 * 		?trace :TcontainsE ?event . 
	 * 		?event :EcontainsA ?att . 
	 * 		?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string . 
	 * 		?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
	 * 		?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * From the query results, it creates a hashmap of XEvents where the keys are the Event URIs. 
	 * Each XEvent within the created hashmap of XEvents is filled with the corresponding 
	 * XAttribute objects that are taken from the given hashmap of XAttributes.
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It checks event's mandatory attributes (name, lifecycle, timestamp) in the query
	 * 		</li>
	 * 		<li>
	 * 			Only retrieve the events that has the mandatory attributes and belong to a trace
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param xattmap - a hashMap between an attribute URI and the corresponding XAttribute object
	 * @return HashMap<String,XEvent> - A hashMap between an event URI and the corresponding XEvent object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XEvent> getXEventsWithEventMandatoryAttributesCheckInQuery(
			HashMap<String,XAttribute> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXEventsWithEventMandatoryAttributesCheckInQuery(HashMap<String,XAttribute> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XEvent> events = new HashMap<String,XEvent>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	
	
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheck);
			
			while (rs.nextRow()) {
				
				//============================================================================
				//Reading the Query results
				//============================================================================

					OWLObject evtObj = rs.getOWLObject(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheckAnsVarEvent);
					String newEvt = (evtObj == null? null: evtObj.toString()); 
					
					//if evt is null, then skip the rest and move on
					if(newEvt == null) continue;
					
					OWLObject attObj = rs.getOWLObject(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheckAnsVarAtt);
					String newAtt = (attObj == null? null: attObj.toString()); 
					
					//System.out.println("------------------------------");
					//System.out.println("evt: "+ evt);
					//System.out.println("att: "+ att);
					
				//============================================================================
				//END OF Reading the Query results
				//============================================================================

				//============================================================================
				//Handling the current attribute that is being read
				//============================================================================

					XAttribute xatt = null;
					
					//if the given XAttribute map xattmap contains the information about attribute newAtt
					if(xattmap.containsKey(newAtt))
						xatt = xattmap.get(newAtt);
					else //if the given XAttribute map xattmap doesn't contain the information about attribute newAtt
						currentExecutionNote.append(String.format(LEConstants.EVENT_MISS_ATT, newEvt, newAtt)); 
				
				//============================================================================
				//END OF Handling the current attribute that is being read
				//============================================================================

				//============================================================================
				//Handling the current event that is being read
				//============================================================================
					if(events.containsKey(newEvt)){
						
						if(events.get(newEvt).getAttributes() != null){
							
							if(newAtt != null && xatt != null)
								events.get(newEvt).getAttributes().put(xatt.getKey(), xatt);
						}else{
							
							XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
							
							if(attsOfEvent != null){

								if(newAtt != null && xatt != null)
									attsOfEvent.put(xatt.getKey(), xatt);
								
								events.get(newEvt).setAttributes(attsOfEvent);
							}
						}
						
					}else{
						XEvent xevt = this.xfact.createEvent();			
						if(xevt != null){
							XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
							
							if(attsOfEvent != null){

								if(newAtt != null && xatt != null)
									attsOfEvent.put(xatt.getKey(), xatt);
								
								xevt.setAttributes(attsOfEvent);
							}

							events.put(newEvt, xevt);
						}else{
							currentExecutionNote.append(
									String.format(LEConstants.EVENT_CREATION_FAILURE, newEvt)); 
						}
					}
				//============================================================================
				//END OF Handling the current event that is being read
				//============================================================================
			}
	
			if(rs != null) rs.close();
			
        }finally{
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //debug the event extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXEvents(events)));
        
		return events;	
	}	
	
	/**
	 * It retrieves all association between events and their attributes by using the following query:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?event ?att 
	 * WHERE {
	 * 		?event :EcontainsA ?att . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * From the query results, it creates a hashmap of XEvents where the keys are the Event URIs. 
	 * Each XEvent within the created hashmap of XEvents is filled with the corresponding 
	 * XAttribute objects that are taken from the given hashmap of XAttributes.
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It doesn't check event's mandatory attributes (name, lifecycle, timestamp)
	 * 		</li>
	 * 		<li>
	 * 			Only retrieve the events that has at least one attributes.
	 * 		</li>
	 * 		<li>
	 * 			It exhaustively retrieves all possible events
	 * 			(even the one that might not belong to a trace).
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param xattmap - a hashMap between an attribute URI and the corresponding XAttribute object
	 * @return HashMap<String,XEvent> - A hashMap between an event URI and the corresponding XEvent object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XEvent> getXEventsUsingAtomicQuery(HashMap<String,XAttribute> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXEventsUsingAtomicQuery(HashMap<String,XAttribute> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XEvent> events = new HashMap<String,XEvent>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	
	
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
			
			while (rs.nextRow()) {
				
				//============================================================================
				//Reading the Query results
				//============================================================================

					OWLObject evtObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent);
					String newEvt = (evtObj == null? null: evtObj.toString()); 
					
					//if evt is null, then skip the rest and move on
					if(newEvt == null) continue;
					
					OWLObject attObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt);
					String newAtt = (attObj == null? null: attObj.toString()); 
					
					//System.out.println("------------------------------");
					//System.out.println("evt: "+ evt);
					//System.out.println("att: "+ att);
					
				//============================================================================
				//END OF Reading the Query results
				//============================================================================

				//============================================================================
				//Handling the current attribute that is being read
				//============================================================================

					XAttribute xatt = null;
					
					//if the given XAttribute map xattmap contains the information about attribute newAtt
					if(xattmap.containsKey(newAtt))
						xatt = xattmap.get(newAtt);
					else //if the given XAttribute map xattmap doesn't contain the information about attribute newAtt
						currentExecutionNote.append(String.format(LEConstants.EVENT_MISS_ATT, newEvt, newAtt)); 
				
				//============================================================================
				//END OF Handling the current attribute that is being read
				//============================================================================

				//============================================================================
				//Handling the current event that is being read
				//============================================================================
					if(events.containsKey(newEvt)){
						
						if(events.get(newEvt).getAttributes() != null){
							
							if(newAtt != null && xatt != null)
								events.get(newEvt).getAttributes().put(xatt.getKey(), xatt);
						}else{
							
							XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
							
							if(attsOfEvent != null){

								if(newAtt != null && xatt != null)
									attsOfEvent.put(xatt.getKey(), xatt);
								
								events.get(newEvt).setAttributes(attsOfEvent);
							}
						}
						
					}else{
						XEvent xevt = this.xfact.createEvent();			
						if(xevt != null){
							XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
							
							if(attsOfEvent != null){

								if(newAtt != null && xatt != null)
									attsOfEvent.put(xatt.getKey(), xatt);
								
								xevt.setAttributes(attsOfEvent);
							}

							events.put(newEvt, xevt);
						}else{
							currentExecutionNote.append(
									String.format(LEConstants.EVENT_CREATION_FAILURE, newEvt)); 
						}
					}
				//============================================================================
				//END OF Handling the current event that is being read
				//============================================================================
			}
	
			if(rs != null) rs.close();
			
        }finally{
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }
        
        //debug the event extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXEvents(events)));
        
		return events;	
	}	

	/**
	 * It retrieves all association between events and their attributes by using the following query:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?event ?att ?attKey ?attType ?attVal
	 * WHERE {
	 * 		?trace :TcontainsE ?event . 
	 * 		?event :EcontainsA ?att . ?att :keyA ?attKey; :typeA ?attType; :valueA ?attVal. 
	 * 		?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string . 
	 * 		?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
	 * 		?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
	 * }
	 * ORDER BY ?event
	 * </code>
	 * </pre>
	 *
	 * The results  of this query are ordered by event.
	 * 
	 * From the query results, it creates a hashmap of XEvents where the keys are the Event URIs. 
	 * Each XEvent within the created hashmap of XEvents is filled with the corresponding 
	 * XAttribute objects that are created based on the results of the query.
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It checks event's mandatory attributes (name, lifecycle, timestamp)
	 * 		</li>
	 * 		<li>
	 * 			Only retrieve the events that has the mandatory attributes and belong to a trace
	 * 		</li>
	 * 		<li> 
	 * 	 		It exploits a query that order the results by event URI. Hence it minimizes the hashing
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @return HashMap<String,XEvent> - A hashMap between an event URI and the corresponding XEvent object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XEvent> getXEventsUsingMediumSizeQuery() throws OWLException{
		
		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXEvents():\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XEvent> events = new HashMap<String,XEvent>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
        	//execute the query that retrieves the events and their attributes
	        //Note: we must use a query in which the results is ordered by the event
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEvent);
			
			String prevEvt = "";
			XEvent prevXEvt = null;
			
			while (rs.nextRow()) {
				
				//============================================================================
				//Reading the Query results
				//============================================================================

					OWLObject evtObj = rs.getOWLObject(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarEvent);
					//The following 'null checks' are for preventing 'null pointer exception' when getting the string
					//Actually, it might not be needed since the query doesn't use optional
					String currEvt = (evtObj == null? null: evtObj.toString()); 
	
					//if the current event is null, just ignore the rest and move forward
					if(currEvt == null) continue;
					
					OWLObject attObj = rs.getOWLObject(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarAtt);
					OWLLiteral attTypeObj = rs.getOWLLiteral(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarAttType);
					OWLLiteral attKeyObj = rs.getOWLLiteral(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarAttKey);
					OWLLiteral attValObj = rs.getOWLLiteral(XESEOConstants.qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarAttVal);

					String currAtt = (attObj == null? null: attObj.toString()); 
					String attType = (attTypeObj == null? null: attTypeObj.getLiteral());
					String attKey = (attKeyObj == null? null: attKeyObj.getLiteral()); 
					String attVal = (attValObj == null? null: attValObj.getLiteral());
					
					//System.out.println("------------------------------");
					//System.out.println("currEvt: "+ currEvt);
					//System.out.println("currAtt: "+ currAtt);
					//System.out.println("attType: "+ attType);
					//System.out.println("attKey: "+ attKey);
					//System.out.println("attVal: "+ attVal);
				
				//============================================================================
				//END OF Reading the Query results
				//============================================================================

				//============================================================================
				//Handling the current attribute that is being read
				//============================================================================
					XExtension xext = this.xfact.getPredefinedXExtension(attKey);
					XAttribute xatt = null;
					try {
						xatt = this.xfact.createXAttribute(attType, attKey, attVal, xext);
		
					} catch (UnsupportedAttributeTypeException e) {
						currentExecutionNote.append(String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE_EVENT_ATT, 
							attType, currEvt, attKey, attVal));
						xatt = null;
					}
					
					//just in case there is a failure in the attribute creation
					if(xatt == null)
						currentExecutionNote.append(String.format(
							LEConstants.ATT_CREATION_FAILURE_EVENT_ATT, currEvt, attType, attKey, attVal));
				//============================================================================
				//END OF Handling current attribute that is being read
				//============================================================================
				
				//============================================================================
				//Handling the current event that is being read
				//============================================================================
					if(!prevEvt.equals(currEvt)){//handle newly read event
	
						//just add the previously processed event since we start to process the next one
						if(!prevEvt.equals("") && prevXEvt != null)
							events.put(prevEvt, prevXEvt);
	
						//create the XEvent
						//XEvent xevt = this.xfact.createEvent();		
						prevXEvt = this.xfact.createEvent();		
						
						//if there is a problem with the creation of this event, then just skip the rest and move on
						if(prevXEvt == null) {
							currentExecutionNote.append(String.format(LEConstants.EVENT_CREATION_FAILURE, currEvt)); 
							prevEvt = "";
							continue;
						}

						//Note: from now on 'xevt' must not be null, hence no null check is needed.
						XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
						if(attsOfEvent != null){
							
							if(currAtt != null && xatt != null)
								attsOfEvent.put(xatt.getKey(), xatt);
						
							prevXEvt.setAttributes(attsOfEvent);
						}
						else{
							currentExecutionNote.append(String.format(LEConstants.EVENT_XATTMAP_CREATION_FAILURE, currEvt)); 
							continue;
						}
						
						//update the pointer of the previously read event
						prevEvt = currEvt; //prevXEvt = xevt;
	
					}else{//handle previously read event
						
						if(prevXEvt.getAttributes() == null){
							XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
							prevXEvt.setAttributes(attsOfEvent);
						}
						
						//just add the newly read attribute
						//Note: only add the newly read attribute in case it is not null
						if(prevXEvt.getAttributes() != null && currAtt != null && xatt != null)
							prevXEvt.getAttributes().put(xatt.getKey(), xatt);
					}
				//============================================================================
				//END OF Handling the current event that is being read
				//============================================================================
			}
			
			//for the last read event (This is needed because in the loop above, 
			//we add and 'event' into 'events' when we start processing the next event)
			if(!prevEvt.equals("") &&  prevXEvt != null)
				events.put(prevEvt, prevXEvt);
	
			if(rs != null) rs.close();

		}finally {
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //debug the event extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXEvents(events)));

		return events;	
	}	
	
	/**
	 * It retrieves all association between events and their attributes by using the following query:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?event ?att 
	 * WHERE {
	 * 		?event :EcontainsA ?att . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * From the query results, it creates a hashmap of XEvents where the keys are the Event URIs. 
	 * Each XEvent within the created hashmap of XEvents is filled with the corresponding 
	 * XAttributeOnProm objects that are taken from the given hashmap of XAttributeOnProm.
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It doesn't check event's mandatory attributes (name, lifecycle, timestamp)
	 * 		</li>
	 * 		<li>
	 * 			Only retrieve the events that has at least one attributes.
	 * 		</li>
	 * 		<li>
	 * 			The results of this method is a hashmap between event URIs and XEventOnProm object. 
	 * 			The use of XEventOnProm object allows us to check the event mandatory attributes easily
	 * 		</li>
	 * 		<li>
	 * 			It exhaustively retrieves all possible events
	 * 			(even the one that might not belong to a trace and doesn't have mandatory attributes).
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables (hence, quite robust).
	 * 		</li>
	 * </ul>
	 * 
	 * @param HashMap<String,XAttributeOnProm> xattmap - a hashMap between an attribute URI and the corresponding XAttributeOnProm object
	 * @return HashMap<String,XEventOnProm> - A hashMap between an event URI and the corresponding XEventOnProm object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XEventOnProm> getXEventsOnPromUsingAtomicQuery(HashMap<String,XAttributeOnProm> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXEventsOnPromUsingAtomicQuery(HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XEventOnProm> events = new HashMap<String,XEventOnProm>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        QuestOWLResultSet rs = null;
        
		int iterationCounter = 0;
		
        try {	
	
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
				"Querying the relations between events and their attributes"));
			
//logger.info("maxMemory:" +Runtime.getRuntime().maxMemory());
//logger.info("freeMemory:" +Runtime.getRuntime().freeMemory());
//logger.info("totalMemory:" +Runtime.getRuntime().totalMemory());

			rs = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
				"Finish querying the relations between events and their attributes"));
			
			OWLObject evtObj; OWLObject attObj;
			String newEvt; String newAtt;
			XAttribute xatt;
			XAttributeOnProm xattOnProm;
			XEventOnProm xevt;
			
			while (rs.nextRow()) {
				
				iterationCounter++;
				
				//============================================================================
				//Reading the Query results
				//============================================================================

					evtObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent);
					newEvt = 
							(evtObj == null? 
							null: evtObj.toString()); 
					evtObj = null;
					
					//if evt is null, then skip the rest and move on
					if(newEvt == null) continue;
										
					attObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt);
					newAtt = 
							(attObj == null? 
							null: attObj.toString());
					attObj = null;
					
					//System.out.println("------------------------------");
					//System.out.println("evt: "+ evt);
					//System.out.println("att: "+ att);
					
				//============================================================================
				//END OF Reading the Query results
				//============================================================================

				//============================================================================
				//Handling the current attribute that is being read
				//============================================================================

					xatt = null;
					
					//if the given XAttribute map xattmap contains the information about attribute newAtt
					if(xattmap.containsKey(newAtt)){
						xattOnProm = xattmap.get(newAtt);
						try {
							xatt = xattOnProm.toXESXAttribute();
						} catch (UnsupportedAttributeTypeException e) {
							
							if(xattOnProm != null)
								currentExecutionNote.append(String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE2, 
									xattOnProm.getType(), xattOnProm.getKey(), xattOnProm.getValue()));
						}
						xattOnProm = null;
					}
					else //if the given XAttribute map xattmap doesn't contain the information about attribute newAtt
						currentExecutionNote.append(String.format(LEConstants.EVENT_MISS_ATT, newEvt, newAtt)); 
				
				//============================================================================
				//END OF Handling the current attribute that is being read
				//============================================================================

				//============================================================================
				//Handling the current event that is being read
				//============================================================================
					if(events.containsKey(newEvt)){
						
						//if(events.get(newEvt).getAttributes() != null){
							
							if(newAtt != null && xatt != null)
								events.get(newEvt).addXAttribute(xatt);
						/*
						}else{
							
							XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
							
							if(attsOfEvent != null){

								if(newAtt != null && xatt != null)
									attsOfEvent.put(xatt.getKey(), xatt);
								
								events.get(newEvt).setAttributes(attsOfEvent);
							}
						}
						*/
						
					}else{
						xevt = this.xfact.createXEventOnProm(newEvt);			
						if(xevt != null){

							if(newAtt != null && xatt != null)
								xevt.addXAttribute(xatt);

							events.put(newEvt, xevt);
						}else{
							currentExecutionNote.append(
									String.format(LEConstants.EVENT_CREATION_FAILURE, newEvt)); 
						}
						xevt = null;
					}
				//============================================================================
				//END OF Handling the current event that is being read
				//============================================================================
			}
			
			iterationCounter = -5;
	
			if(rs != null) rs.close(); rs = null;
			
        }catch(OutOfMemoryError oome){
        	
			if(rs != null) rs.close(); rs = null;
			if (st != null && !st.isClosed()) st.close(); st = null;
			if (conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();

        	System.out.println("iterationCounter: "+iterationCounter);
        	
			logger.info("maxMemory:" +Runtime.getRuntime().maxMemory());
			logger.info("freeMemory:" +Runtime.getRuntime().freeMemory());
			logger.info("totalMemory:" +Runtime.getRuntime().totalMemory());

        	oome.printStackTrace();
        	
        	System.exit(1);
        }
        finally{
			if (st != null && !st.isClosed()) st.close(); st = null;
			if (conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }
        
        //debug the event extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXEvents(events)));
        
		return events;	
	}	

	/**
	 * It retrieves all association between events and their attributes by using the following query:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?event ?att 
	 * WHERE {
	 * 		?event :EcontainsA ?att . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * From the query results, it creates a hashmap of XEvents where the keys are the Event URIs. 
	 * Each XEvent within the created hashmap of XEvents is filled with the corresponding 
	 * XAttribute objects that are taken from the given hashmap of XAttributes.
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It doesn't check event's mandatory attributes (name, lifecycle, timestamp)
	 * 		</li>
	 * 		<li>
	 * 			Only retrieve the events that has at least one attributes.
	 * 		</li>
	 * 		<li>
	 * 			The results of this method is a hashmap between event URIs and XEventOnProm object. 
	 * 			The use of XEventOnProm object allows us to check the event mandatory attributes easily
	 * 		</li>
	 * 		<li>
	 * 			It exhaustively retrieves all possible events
	 * 			(even the one that might not belong to a trace and doesn't have mandatory attributes).
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables (hence, quite robust).
	 * 		</li>
	 * </ul>
	 * 
	 * @param HashMap<String,XAttribute> xattmap - a hashMap between an attribute URI and the corresponding XAttribute object
	 * @return HashMap<String,XEventOnProm> - A hashMap between an event URI and the corresponding XEventOnProm object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XEventOnProm> getXEventsOnPromUsingAtomicQuery2(HashMap<String,XAttribute> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXEventsOnPromUsingAtomicQuery(HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XEventOnProm> events = new HashMap<String,XEventOnProm>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	
	
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
			
			while (rs.nextRow()) {
				
				//============================================================================
				//Reading the Query results
				//============================================================================

					OWLObject evtObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent);
					String newEvt = (evtObj == null? null: evtObj.toString()); 
					
					//if evt is null, then skip the rest and move on
					if(newEvt == null) continue;
					
					OWLObject attObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt);
					String newAtt = (attObj == null? null: attObj.toString()); 
					
					//System.out.println("------------------------------");
					//System.out.println("evt: "+ evt);
					//System.out.println("att: "+ att);
					
				//============================================================================
				//END OF Reading the Query results
				//============================================================================

				//============================================================================
				//Handling the current attribute that is being read
				//============================================================================

					XAttribute xatt = null;
					
					//if the given XAttribute map xattmap contains the information about attribute newAtt
					if(xattmap.containsKey(newAtt)){
						xatt = xattmap.get(newAtt);
					}
					else //if the given XAttribute map xattmap doesn't contain the information about attribute newAtt
						currentExecutionNote.append(String.format(LEConstants.EVENT_MISS_ATT, newEvt, newAtt)); 
				
				//============================================================================
				//END OF Handling the current attribute that is being read
				//============================================================================

				//============================================================================
				//Handling the current event that is being read
				//============================================================================
					if(events.containsKey(newEvt)){
						
						if(events.get(newEvt).getAttributes() != null){
							
							if(newAtt != null && xatt != null)
								events.get(newEvt).addXAttribute(xatt);
						}else{
							
							XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
							
							if(attsOfEvent != null){

								if(newAtt != null && xatt != null)
									attsOfEvent.put(xatt.getKey(), xatt);
								
								events.get(newEvt).setAttributes(attsOfEvent);
							}
						}
						
					}else{
						XEventOnProm xevt = this.xfact.createXEventOnProm(newEvt);			
						if(xevt != null){

							if(newAtt != null && xatt != null)
								xevt.addXAttribute(xatt);

							events.put(newEvt, xevt);
						}else{
							currentExecutionNote.append(
									String.format(LEConstants.EVENT_CREATION_FAILURE, newEvt)); 
						}
					}
				//============================================================================
				//END OF Handling the current event that is being read
				//============================================================================
			}
	
			if(rs != null) rs.close();
			
        }finally{
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }
        
        //debug the event extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXEvents(events)));
        
		return events;	
	}	

	/**
	 * It retrieves all association between events and their attributes by using the following query:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?event ?att 
	 * WHERE {
	 * 		?event :EcontainsA ?att . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * From the query results, it creates a hashmap of XEvents where the keys are the Event URIs. 
	 * Each XEvent within the created hashmap of XEvents is filled with the corresponding 
	 * XAttribute objects that are taken from the given hashmap of XAttributes.
	 * 
	 * <br/><br/>
	 * Main feature:
	 * <ul>
	 * 		<li>
	 * 			It is optimized for minimizing the memory usage 
	 * 		</li>
	 * </ul>
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It doesn't check event's mandatory attributes (name, lifecycle, timestamp)
	 * 		</li>
	 * 		<li>
	 * 			Only retrieve the events that has at least one attributes.
	 * 		</li>
	 * 		<li>
	 * 			The results of this method is a hashmap between event URIs and XEventOnPromNoURI object. 
	 * 			The use of XEventOnPromNoURI object allows us to check the event mandatory attributes easily
	 * 		</li>
	 * 		<li>
	 * 			It exhaustively retrieves all possible events
	 * 			(even the one that might not belong to a trace and doesn't have mandatory attributes).
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables (hence, quite robust).
	 * 		</li>
	 * </ul>
	 * 
	 * @param EfficientHashMap<XAtt> xattmap - a hashMap between an attribute URI and the corresponding XAtt
	 * @return EfficientHashMap<XEventOnPromNoURI> - A hashMap between an event URI and the corresponding XEventOnPromNoURI object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @since June 2017
	 */
	public EfficientHashMap<XEventOnPromEfficient> getXEventsOnPromUsingAtomicQueryMO(EfficientHashMap<XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXEventsOnPromUsingAtomicQueryMO(EfficientHashMap2<XAtt> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		EfficientHashMap<XEventOnPromEfficient> events = new EfficientHashMap<XEventOnPromEfficient>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        QuestOWLResultSet rs = null;
        
        try {	
	
			//logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
			//	"Querying the relations between events and their attributes"));

			rs = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
			
			//logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
			//	"Finish querying the relations between events and their attributes"));
			
			OWLObject evtObj; OWLObject attObj;
			String newEvt, newAtt;
			XAtt xatt;
			XEventOnPromEfficient xevt;
			
			while (rs.nextRow()) {
				
				//============================================================================
				//Reading the Query results
				//============================================================================

					evtObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent);
					newEvt = (evtObj == null? null: evtObj.toString().intern()); 
					evtObj = null;
					
					//if evt is null, then skip the rest and move on
					if(newEvt == null) continue;
										
					attObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt);
					newAtt = (attObj == null? null: attObj.toString().intern());
					attObj = null;

					//System.out.println("------------------------------");
					//System.out.println("evt: "+ evt);
					//System.out.println("att: "+ att);
					
				//============================================================================
				//END OF Reading the Query results
				//============================================================================

				//============================================================================
				//Handling the current attribute that is being read
				//============================================================================

					xatt = null;
										
					//if the given XAttribute map xattmap contains the information about attribute newAtt
					if(xattmap.containsKey(newAtt)){
						xatt = xattmap.get(newAtt);
						if(!xatt.hasCompleteInfo())
							xatt = null;
					}
					else //if the given XAttribute map xattmap doesn't contain the information about attribute newAtt
						currentExecutionNote.append(String.format(LEConstants.EVENT_MISS_ATT, newEvt, newAtt)); 
				
				//============================================================================
				//END OF Handling the current attribute that is being read
				//============================================================================

				//============================================================================
				//Handling the current event that is being read
				//============================================================================
					if(events.containsKey(newEvt)){
						
						if(newAtt != null && xatt != null)
							events.get(newEvt).addXAttribute(xatt);
						
					}else{
						xevt = this.xfact.createXEventOnPromNoURI();			
						if(xevt != null){

							if(newAtt != null && xatt != null)
								xevt.addXAttribute(xatt);

							events.put(newEvt, xevt);

						}else{
							currentExecutionNote.append(
									String.format(LEConstants.EVENT_CREATION_FAILURE, newEvt)); 
						}
						xevt = null;
					}
				//============================================================================
				//END OF Handling the current event that is being read
				//============================================================================
			}
	
			if(rs != null) rs.close(); rs = null;
			
        }catch(OutOfMemoryError oome){
        	
			if(rs != null) rs.close(); rs = null;
			if (st != null && !st.isClosed()) st.close(); st = null;
			if (conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();

			logger.info("maxMemory:" +Runtime.getRuntime().maxMemory());
			logger.info("freeMemory:" +Runtime.getRuntime().freeMemory());
			logger.info("totalMemory:" +Runtime.getRuntime().totalMemory());

        	oome.printStackTrace();
        	
        	System.exit(1);
		}finally{
			if (st != null && !st.isClosed()) st.close(); st = null;
			if (conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXEvents(events)));
        
        return events;	
	}	
	
	//////////////////////////////////////////////////////////////////////
	// END OF XEvents retriever methods
	//////////////////////////////////////////////////////////////////////
	

	
	//////////////////////////////////////////////////////////////////////
	// XTraces retriever methods
	//////////////////////////////////////////////////////////////////////

	/**
	 * It retrieves all association between traces and their events, as well as 
	 * the association between traces and their attributes.  
	 * Then it creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects) that are taken from the given hashmap of 
	 * XEvents (resp. XAttributes)
	 * 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event ?timestamp
	 * WHERE {
	 * 		?trace :TcontainsE ?event.  
	 * 		?event :EcontainsA ?timestamp . 
	 * 		 ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampValue. 
	 * }
	 * ORDER BY ?trace ASC(?timestampValue)
	 * </code>
	 * </pre>
	 *  
	 *  
	 * Then it creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects) that are taken from the given hashmap of 
	 * XEvents (resp. XAttributes).
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			There is no checks on event's mandatory attributes (name, lifecycle, timestamp)
	 * 		</li>
	 * 		<li>
	 * 			It only retrieves the traces that has at least one event or one attributes
	 * 			(i.e., traces that have neither events nor attributes will not be retrieved)
	 * 		</li>
	 * 		<li> 
	 * 			It doesn't accesses the query result using the name of the answer 
	 * 			variables (hence, it's unsafe).
	 * 		</li>
	 * </ul>
	 * 
	 * @param xattmap - A hashMap between an attribute URI and the corresponding XAttribute object
	 * @param xevtmap - A hashMap between an event URI and the corresponding XEvent object
	 * @return HashMap<String,XTrace> - A hashMap between a trace URI and the corresponding XTrace object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XTrace> getXTracesSimpleImpl(HashMap<String,XEvent> xevtmap, HashMap<String,XAttribute> xattmap) throws OWLException{
		
		HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
		
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesSimpleImpl(HashMap<String,XEvent> xevtmap, HashMap<String,XAttribute> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();

		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_WithEventOrderingByTimestamp);

			while (rs.nextRow()) {
				String trace = rs.getOWLObject(1).toString(); 
				String event = rs.getOWLObject(2).toString(); 
				
				//if the given XEvent map xevtmap doesn't contain the information about event att
				if(!xevtmap.containsKey(event)){
					currentExecutionNote.append(String.format(
						"\tThe trace '%s' has the event '%s', \n\t\tbut it misses the information about this event.\n", trace, event)); 
					continue;
				}
				
				if(traces.containsKey(trace)){
					traces.get(trace).insertOrdered(xevtmap.get(event));
					
				}else{
					XTrace xtrace = this.xfact.createXTraceNaiveImpl();
					xtrace.insertOrdered(xevtmap.get(event));
					xtrace.setAttributes(this.xfact.createAttributeMap());
					traces.put(trace, xtrace);
				}
			}
			
			rs.close();
			
			QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_Simple);

			while (rs2.nextRow()) {
				String trace = rs2.getOWLObject(1).toString(); 
				String att = rs2.getOWLObject(2).toString(); 
				
				if(!xattmap.containsKey(att))
					continue;
				
				if(traces.containsKey(trace))
					traces.get(trace).getAttributes().put(att, xattmap.get(att));
			}

			rs2.close();
			
		}finally {
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
			if (isAllowToDisposeQuestReasoner()) questReasoner.dispose();
		}
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	currentExecutionNote.insert(0, "EBDAReasoner:retrieveXEvents(HashMap<String,XAttribute>):\n");
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }
        
        //Logging the information about the extracted XEvents information
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.logXTracesExtraction(traces)));

		return traces;	
	}	
	
	/**
	 * It retrieves all association between traces and their events, as well as 
	 * the association between traces and their attributes.  
	 * Then it creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects) that are taken from the given hashmap of 
	 * XEvents (resp. XAttributes)
	 * 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event ?timestampValue
	 * WHERE {
	 * 		?trace :TcontainsE ?event . 
	 * 		?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampValue. 
	 * 		?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
	 * 		?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
	 * } 
	 * ORDER BY ?trace ASC(?timestampValue)
	 * </code>
	 * </pre>
	 * 
	 * 
	 * It uses the following query for retrieving the association between traces and attributes:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att 
	 * WHERE {
	 * 		?trace :TcontainsA ?att .  
	 * }
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving all traces:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It checks event's mandatory attributes (name, lifecycle, timestamp) and 
	 * 			only retrieves the events that has the mandatory attributes.
	 * 		</li>
	 * 		<li>
	 * 			It uses a query that sorts the events based on their timestamp
	 * 		</li>
	 * 		<li> 
	 * 			It retrieves all traces, even if they have neither events nor attributes.
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param xattmap - A hashMap between an attribute URI and the corresponding XAttribute object
	 * @param xevtmap - A hashMap between an event URI and the corresponding XEvent object
	 * @return HashMap<String,XTrace> - A hashMap between a trace URI and the corresponding XTrace object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XTrace> getXTracesWithEventMandatoryAttributesCheckInQuery(HashMap<String,XEvent> xevtmap, HashMap<String,XAttribute> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesWithEventMandatoryAttributesCheckInQuery(HashMap<String,XEvent> xevtmap, HashMap<String,XAttribute> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message

		HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			//====================================================================================================
			//Handling Trace's Events
	        //====================================================================================================

				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestamp);
		
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						OWLObject traceObj = rs.getOWLObject(
							XESEOConstants.qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestampAnsVarTrace); 
						String newTrace = (traceObj == null? null : traceObj.toString()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject eventObj = rs.getOWLObject(
							XESEOConstants.qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestampAnsVarEvent); 
						String newEvent = (eventObj == null? null : eventObj.toString()); 
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current event that is being read
					//============================================================================
						XEvent xevt = null;
						
						//if the given XEvent map xevtmap doesn't contain the information about event att
						if(xevtmap.containsKey(newEvent))
							xevt = xevtmap.get(newEvent);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_EVENT, newTrace, newEvent)); 
					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================
					
					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================
						if(traces.containsKey(newTrace)){//handle the trace that has been read previously
			
							if(xevt != null)
								traces.get(newTrace).insertOrdered(xevt);
							
						}else{//handle new trace
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
			
							if(xtrace != null){
			
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
		
								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null)
									xtrace.setAttributes(attsOfATrace);
			
								if(xevt != null)
									xtrace.insertOrdered(xevt);

								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
			
							}else {
								currentExecutionNote.append(
									String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
							}
						}
					//============================================================================
					//END OF Handling the current trace that is currently being read
					//============================================================================
				}
		
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
	        //====================================================================================================
			
			//====================================================================================================
			//Handling Trace's Attributes
	        //====================================================================================================

				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
		
				while (rs2.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						OWLObject traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						String newTrace = (traceObj == null? null : traceObj.toString()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						String newAtt = (attObj == null? null : attObj.toString()); 
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAttribute xatt = null;
						
						if(xattmap.containsKey(newAtt))
							xatt = xattmap.get(newAtt);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_ATT, newTrace, newAtt)); 
					
					//============================================================================
					//END OF Handling current attribute that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is being read
					//============================================================================
						if(traces.containsKey(newTrace)){
							//handling the traces that are captured in the previous step 

							if(traces.get(newTrace).getAttributes() != null){
								
								if(newAtt != null && xatt != null)
									traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
								
							}else{
								
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();

								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null){
									
									if(newAtt != null && xatt != null)
										attsOfATrace.put(xatt.getKey(), xatt);
									
									traces.get(newTrace).setAttributes(attsOfATrace);
								}
							}
														
						}else {
							//handling the traces that are not captured in the previous step 
							//(e.g., traces that have no events)
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
							
							if(xtrace != null){
			
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
		
								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null){
									
									if(newAtt != null && xatt != null)
										attsOfATrace.put(xatt.getKey(), xatt);
									
									xtrace.setAttributes(attsOfATrace);
								}
			
								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
							}else{
								currentExecutionNote.append(String.format(
										LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
							}
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
				
				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
	        //====================================================================================================
	
			//====================================================================================================
			//Handling Traces that have neither events nor attributes
			//====================================================================================================
				
				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				while (rs3.nextRow()) {
					OWLObject newTraceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					String newTrace = (newTraceObj == null? null: newTraceObj.toString()); 
	
					//if the newly read trace is null, then just skip the rest and move on
					if(newTrace == null)
						continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					if(!traces.containsKey(newTrace)){
						XTrace xtrace= this.xfact.createXTraceNaiveImpl();
						
						if(xtrace != null)
							traces.put(newTrace, xtrace);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
					}
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling Traces that have neither events nor attributes
			//====================================================================================================

        }finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //debug the trace extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));
        		
		return traces;	
	}	

	/**
	 * It retrieves all association between traces and their events, as well as 
	 * the association between traces and their attributes. 
	 * Then it creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects) that are taken from the given hashmap of 
	 * XEvents (resp. XAttributes)
	 * 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event 
	 * WHERE {
	 * 		?trace :TcontainsE ?event . 
	 * } 
	 * </code>
	 * </pre>
	 * 
	 * 
	 * It uses the following query for retrieving the association between traces and attributes:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att 
	 * WHERE {
	 * 		?trace :TcontainsA ?att .  
	 * }
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving all traces:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It doesn't checks event's mandatory attributes (name, lifecycle, timestamp)
	 * 		</li>
	 * 		<li> 
	 * 			It retrieves all traces, even if they have neither events nor attributes.
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param xattmap - A hashMap between an attribute URI and the corresponding XAttribute object
	 * @param xevtmap - A hashMap between an event URI and the corresponding XEvent object
	 * @return HashMap<String,XTrace> - A hashMap between a trace URI and the corresponding XTrace object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XTrace> getXTracesUsingAtomicQueries(HashMap<String,XEvent> xevtmap, HashMap<String,XAttribute> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesUsingAtomicQuery(HashMap<String,XEvent> xevtmap, HashMap<String,XAttribute> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message

		HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			//====================================================================================================
			//Handling Trace's Events
	        //====================================================================================================

				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_Simple);
		
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						OWLObject traceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace); 
						String newTrace = (traceObj == null? null : traceObj.toString()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject eventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent); 
						String newEvent = (eventObj == null? null : eventObj.toString()); 
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current event that is being read
					//============================================================================
						XEvent xevt = null;
						
						//if the given XEvent map xevtmap doesn't contain the information about event att
						if(xevtmap.containsKey(newEvent))
							xevt = xevtmap.get(newEvent);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_EVENT, newTrace, newEvent)); 
					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================
					
					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================
						if(traces.containsKey(newTrace)){//handle the trace that has been read previously
			
							if(xevt != null)
								traces.get(newTrace).insertOrdered(xevt);
							
						}else{//handle new trace
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
			
							if(xtrace != null){
			
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
		
								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null)
									xtrace.setAttributes(attsOfATrace);
			
								if(xevt != null)
									xtrace.insertOrdered(xevt);

								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
			
							}else {
								currentExecutionNote.append(
									String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
							}
						}
					//============================================================================
					//END OF Handling the current trace that is currently being read
					//============================================================================
				}
		
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
	        //====================================================================================================
			
			//====================================================================================================
			//Handling Trace's Attributes
	        //====================================================================================================

				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
		
				while (rs2.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						OWLObject traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						String newTrace = (traceObj == null? null : traceObj.toString()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						String newAtt = (attObj == null? null : attObj.toString()); 
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAttribute xatt = null;
						
						if(xattmap.containsKey(newAtt))
							xatt = xattmap.get(newAtt);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_ATT, newTrace, newAtt)); 
					
					//============================================================================
					//END OF Handling current attribute that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is being read
					//============================================================================
						if(traces.containsKey(newTrace)){
							//handling the traces that are captured in the previous step 

							if(traces.get(newTrace).getAttributes() != null){
								
								if(newAtt != null && xatt != null)
									traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
								
							}else{
								
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();

								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null){
									
									if(newAtt != null && xatt != null)
										attsOfATrace.put(xatt.getKey(), xatt);
									
									traces.get(newTrace).setAttributes(attsOfATrace);
								}
							}
														
						}else {
							//handling the traces that are not captured in the previous step 
							//(e.g., traces that have no events)
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
							
							if(xtrace != null){
			
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
		
								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null){
									
									if(newAtt != null && xatt != null)
										attsOfATrace.put(xatt.getKey(), xatt);
									
									xtrace.setAttributes(attsOfATrace);
								}
			
								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
							}else{
								currentExecutionNote.append(String.format(
										LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
							}
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
				
				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
	        //====================================================================================================
	
			//====================================================================================================
			//Handling Traces that have neither events nor attributes
			//====================================================================================================
				
				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				while (rs3.nextRow()) {
					OWLObject newTraceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					String newTrace = (newTraceObj == null? null: newTraceObj.toString()); 
	
					//if the newly read trace is null, then just skip the rest and move on
					if(newTrace == null)
						continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					if(!traces.containsKey(newTrace)){
						XTrace xtrace= this.xfact.createXTraceNaiveImpl();
						
						if(xtrace != null)
							traces.put(newTrace, xtrace);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
					}
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling Traces that have neither events nor attributes
			//====================================================================================================

        }finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //debug the trace extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));
        		
		return traces;	
	}	

	/**
	 * It retrieves all association between traces and their events, as well as 
	 * the association between traces and their attributes. Importantly, when it retrieves the 
	 * association between traces and their attributes, it also retrieves the types, keys and values
	 * of the corresponding attributes. 
	 *  
	 * It  also creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects). The XEvent objects are taken from the given hashmap of 
	 * XEvents, while the XAttributes objects are created based on the retrieval of the association 
	 * between traces and their attributes.
	 * 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event ?timestampValue
	 * WHERE {
	 *     ?trace :TcontainsE ?event . 
	 *     ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampValue. 
	 *     ?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
	 *     ?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
	 * }
	 * ORDER BY ?trace ASC(?timestampValue)
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query to retrieve the association between traces and attributes
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att ?attKey ?attType ?attVal
	 * WHERE {
	 * 		?trace :TcontainsA ?att . ?att :keyA ?attKey; :typeA ?attType; :valueA ?attVal. 
	 * }
	 * ORDER BY ?trace
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query to retrieve all traces:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It checks event's mandatory attributes (name, lifecycle, timestamp) and 
	 * 			only retrieves the events that has the mandatory attributes.
	 * 		</li>
	 * 		<li>
	 * 			It uses a query that sorts the events based on their timestamp
	 * 		</li>
	 * 		<li>
	 * 			when it retrieves the association between traces and their attributes, 
	 * 			it also retrieves the types, keys and values of the corresponding attributes.
	 * 		</li>
	 * 		<li> 
	 * 			It retrieves all traces, even if they have neither events nor attributes.
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param xevtmap - A hashMap between an event URI and the corresponding XEvent object
	 * @return HashMap<String,XTrace> - A hashMap between a trace URI and the corresponding XTrace object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XTrace> getXTracesUsingMediumSizeQueries(HashMap<String,XEvent> xevtmap) throws OWLException{
		
		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder(
					"EBDAReasoner:getXTraces(HashMap<String,XEvent> xevtmap):\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			//====================================================================================================
			//Handling Trace's Events
	        //====================================================================================================
	        	//Note: we must use a query in which the results is ordered by the trace and event timestamp
				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestamp);
				
				String currTrace = "";
				XTrace currXTrace = null;
				
				while (rs.nextRow()) {

					//============================================================================
					//Reading the Query results
					//============================================================================
	
						//The following 'null checks' are for preventing 'null pointer exception' 
						//when getting the string (Actually, it might not be needed since the query doesn't 
						//use optional, but at least it could save us from some unexpected errors on 
						//the external library)
						OWLObject newTraceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestampAnsVarTrace);
						String newTrace = (newTraceObj == null? null : newTraceObj.toString());
						
						//if the current trace is null, just ignore the rest and move on
						if(newTrace == null) continue;
						
						OWLObject newEventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestampAnsVarEvent);
						String newEvent = (newEventObj == null? null : newEventObj.toString()); 
						
						//System.out.println("------------------------------\n");
						//System.out.println("trace: "+ currTrace + "\n" + "event: "+ currEvent);
						
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
									
					//============================================================================
					//Handling the current event that is being read
					//============================================================================

						XEvent currXEvent = null;
						
						//we only need to do something in case currEvent != null
						if(newEvent != null){
							
							//if the given XEvent map (i.e., xevtmap) contain the information about currEvent
							if(xevtmap.containsKey(newEvent)){
								
								currXEvent = xevtmap.get(newEvent);
	
							}else{
								//if the given XEvent map (i.e., xevtmap) doesn't contain the information about currEvent
								currentExecutionNote.append(
									String.format(LEConstants.TRACE_MISS_EVENT,newTrace, newEvent)); 
							}
						}
						//Note: at this point "currXEvent is null" if "currEvent is null".

					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================

						if(!currTrace.equals(newTrace)){//handle newly read trace 
							
							if(!currTrace.equals("") && currXTrace != null)
								traces.put(currTrace, currXTrace);

							currXTrace = this.xfact.createXTraceNaiveImpl();			
			
							//if there is a problem in the creation of XTrace object, just skip the rest and move on
							if(currXTrace == null) {
								currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
								continue;
							}
			
							XAttributeMap xattmap = this.xfact.createAttributeMap();
							
							if(xattmap == null)
								currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 

							if(xattmap != null)
								currXTrace.setAttributes(xattmap);


							if(currXEvent != null)
								currXTrace.insertOrdered(currXEvent);
							
							currTrace = newTrace;
			
						}else{//the case where the current trace is the same with the previously read trace
		
							//just add the newly read event
							if(currXEvent != null)
								currXTrace.insertOrdered(currXEvent);
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
		
				//for the last trace that we read (This is needed because in the loop above, 
				//we add a 'trace' into 'traces' when we start processing the next trace)
				if(!currTrace.equals("") && currXTrace != null)
					traces.put(currTrace, currXTrace);
				
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
			//====================================================================================================
				
			//====================================================================================================
			//Handling Trace's Attributes
			//====================================================================================================
				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_WithAttTypeKeyValAndOrderByTrace);
		
				currTrace = "";
				currXTrace = null;
				
				while (rs2.nextRow()) {

					//============================================================================
					//Reading the Query results
					//============================================================================
	
						OWLObject newTraceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarTrace); 
						String newTrace = (newTraceObj == null? null : newTraceObj.toString()); 
						
						//if the current trace is null, just ignore the rest and move on
						if(newTrace == null) continue;

						//OWLObject attObj = rs2.getOWLObject(EOConstants.qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarAtt); 
						OWLLiteral attKeyObj = rs2.getOWLLiteral(XESEOConstants.qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarAttKey);
						OWLLiteral attTypeObj = rs2.getOWLLiteral(XESEOConstants.qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarAttType); 
						OWLLiteral attValObj = rs2.getOWLLiteral(XESEOConstants.qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarAttVal); 
						
						//String att = (attObj == null? null : attObj.toString()); 
						String attKey = (attKeyObj == null? null : attKeyObj.getLiteral());
						String attType = (attTypeObj == null? null : attTypeObj.getLiteral()); 
						String attVal = (attValObj == null? null : attValObj.getLiteral()); 
		
						//System.out.println("------------------------------");
						//System.out.println("trace: "+ currTrace);
						//System.out.println("att: "+ att);
						//System.out.println("attType: "+ attType);
						//System.out.println("attKey: "+ attKey);
						//System.out.println("attVal: "+ attVal);
					
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
									
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================
						XExtension xext = this.xfact.getPredefinedXExtension(attKey);
						XAttribute xatt = null;
						try {
							xatt = this.xfact.createXAttribute(attType, attKey, attVal, xext);
			
						} catch (UnsupportedAttributeTypeException e) {
							currentExecutionNote.append(String.format(
								LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE_TRACE_ATT, attType, newTrace, attKey, attVal));
							xatt = null;
						}
						
						//just in case there is a failure in the attribute creation
						if(xatt == null)
							currentExecutionNote.append(String.format(
								LEConstants.ATT_CREATION_FAILURE_TRACE_ATT,newTrace, attType, attKey, attVal));
						
					//============================================================================
					//END OF Handling current attribute that is being read
					//============================================================================
	
					//============================================================================
					//Handling the current trace that is being read
					//============================================================================
						if(!currTrace.equals(newTrace)){//handle newly read trace 
		
							if(traces.containsKey(newTrace)){
								currXTrace = traces.get(newTrace);
								
								if(currXTrace.getAttributes() == null){
									XAttributeMap xattmap = this.xfact.createAttributeMap();

									if(xattmap == null)
										currentExecutionNote.append(
												String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 

									if(xattmap != null)
										currXTrace.setAttributes(xattmap);
								}
									
								if(currXTrace.getAttributes() != null && xatt != null)
									currXTrace.getAttributes().put(xatt.getKey(), xatt);
								
							}else {
								//(possibly) a newly read trace that (possibly) has no event (since it is not
								//detected in the previous step).
								
								currXTrace = this.xfact.createXTraceNaiveImpl();			
								
								//if there is a problem in the creation of XTrace object, just skip the rest and move on
								if(currXTrace == null) {
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
									continue;
								}
				
								XAttributeMap xattmap = this.xfact.createAttributeMap();
								
								if(xattmap == null)
									currentExecutionNote.append(
											String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
								
								if(xattmap != null)
									currXTrace.setAttributes(xattmap);

								if(currXTrace.getAttributes() != null && xatt != null)
									currXTrace.getAttributes().put(xatt.getKey(), xatt);

							}
							
							currTrace = newTrace;
		
						}else{//the case where the current trace is the same with the previously read trace
							
							if(currXTrace.getAttributes() == null){
								XAttributeMap xattmap = this.xfact.createAttributeMap();
								
								if(xattmap == null)
									currentExecutionNote.append(
											String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 

								if(xattmap != null)
									currXTrace.setAttributes(xattmap);
							}
								
							//just add the newly read attributes
							if(currXTrace.getAttributes() != null && xatt != null)
								currXTrace.getAttributes().put(xatt.getKey(), xatt);
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
		
				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
			//====================================================================================================
			
			//====================================================================================================
			//Handling Traces that have neither events nor attributes
			//====================================================================================================

				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				currXTrace = null;
				
				while (rs3.nextRow()) {
					OWLObject newTraceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					String newTrace = (newTraceObj == null? null: newTraceObj.toString()); 
	
					//if the newly read trace is null, then just skip the rest and move on
					if(newTrace == null)
						continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					if(!traces.containsKey(newTrace)){
						currXTrace = this.xfact.createXTraceNaiveImpl();
						
						if(currXTrace != null)
							traces.put(newTrace, currXTrace);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
					}
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling Traces that have neither events nor attributes
			//====================================================================================================
				
		}finally {
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //debug the trace extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));

		return traces;	
	}

	/**
	 * It retrieves all association between traces and their events, as well as 
	 * the association between traces and their attributes. 
	 * Then it creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects) that are taken from the given hashmap of 
	 * XEvents (resp. XAttributes)
	 * 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event 
	 * WHERE {
	 * 		?trace :TcontainsE ?event . 
	 * } 
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving the association between traces and attributes:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att 
	 * WHERE {
	 * 		?trace :TcontainsA ?att .  
	 * }
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving all traces:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It checks event's mandatory attributes (name, lifecycle, timestamp) and 
	 * 			only retrieves the events that has the mandatory attributes. The checks are performed
	 * 			within the program.
	 * 		</li>
	 * 		<li> 
	 * 			It retrieves all traces, even if they have neither events nor attributes.
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param HashMap<String,XEventOnProm> xevtmap - A hashMap between an event URI and the corresponding XEvent object
	 * @param HashMap<String,XAttributeOnProm> xattmap - A hashMap between an attribute URI and the corresponding XAttribute object
	 * @return HashMap<String,XTrace> - A hashMap between a trace URI and the corresponding XTrace object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XTrace> getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck(HashMap<String,XEventOnProm> xevtmap, HashMap<String,XAttributeOnProm> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck(HashMap<String,XEventOnProm> xevtmap, HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message

		HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			//====================================================================================================
			//Handling traces events
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces events"));

				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_Simple);
		
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						OWLObject traceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace); 
						String newTrace = (traceObj == null? null : traceObj.toString()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject eventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent); 
						String newEvent = (eventObj == null? null : eventObj.toString()); 
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current event that is being read
					//============================================================================
						XEvent xevt = null;

						//if the given XEvent map xevtmap doesn't contain the information about event att
						if(xevtmap.containsKey(newEvent)){
							XEventOnProm xevtOnProm = xevtmap.get(newEvent);
							
							if(xevtOnProm.hasAllMandatoryAttributes())
								xevt = xevtOnProm;
							else xevtmap.remove(newEvent);
							
						}else{
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_EVENT, newTrace, newEvent)); 
						}
					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================
					
					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================
						if(traces.containsKey(newTrace)){//handle the trace that has been read previously
			
							if(xevt != null)
								traces.get(newTrace).insertOrdered(xevt);
							
						}else{//handle new trace
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
			
							if(xtrace != null){
			
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
		
								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null)
									xtrace.setAttributes(attsOfATrace);
			
								if(xevt != null)
									xtrace.insertOrdered(xevt);

								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
			
							}else {
								currentExecutionNote.append(
									String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
							}
						}
					//============================================================================
					//END OF Handling the current trace that is currently being read
					//============================================================================
				}
		
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
	        //====================================================================================================
			
			//====================================================================================================
			//Handling Traces Attributes
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces attributes"));

				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
		
				while (rs2.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						OWLObject traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						String newTrace = (traceObj == null? null : traceObj.toString()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						String newAtt = (attObj == null? null : attObj.toString()); 
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAttribute xatt = null;
						
						if(xattmap.containsKey(newAtt)){
							XAttributeOnProm xattOnProm = xattmap.get(newAtt);
							try {
								xatt = xattOnProm.toXESXAttribute();
							} catch (UnsupportedAttributeTypeException e) {
								if(xattOnProm != null)
									currentExecutionNote.append(String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE2, 
										xattOnProm.getType(), xattOnProm.getKey(), xattOnProm.getValue()));
							}

						}else
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_ATT, newTrace, newAtt)); 
					
					//============================================================================
					//END OF Handling current attribute that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is being read
					//============================================================================
						if(traces.containsKey(newTrace)){
							//handling the traces that are captured in the previous step 

							if(traces.get(newTrace).getAttributes() != null){
								
								if(newAtt != null && xatt != null)
									traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
								
							}else{
								
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();

								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null){
									
									if(newAtt != null && xatt != null)
										attsOfATrace.put(xatt.getKey(), xatt);
									
									traces.get(newTrace).setAttributes(attsOfATrace);
								}
							}
														
						}else {
							//handling the traces that are not captured in the previous step 
							//(e.g., traces that have no events)
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
							
							if(xtrace != null){
			
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
		
								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null){
									
									if(newAtt != null && xatt != null)
										attsOfATrace.put(xatt.getKey(), xatt);
									
									xtrace.setAttributes(attsOfATrace);
								}
			
								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
							}else{
								currentExecutionNote.append(String.format(
										LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
							}
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
				
				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
	        //====================================================================================================
	
			//====================================================================================================
			//Handling Traces that have neither events nor attributes
			//====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces that have neither events nor attributes"));
				
				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				while (rs3.nextRow()) {
					OWLObject newTraceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					String newTrace = (newTraceObj == null? null: newTraceObj.toString()); 
	
					//if the newly read trace is null, then just skip the rest and move on
					if(newTrace == null)
						continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					if(!traces.containsKey(newTrace)){
						XTrace xtrace= this.xfact.createXTraceNaiveImpl();
						
						if(xtrace != null)
							traces.put(newTrace, xtrace);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
					}
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling Traces that have neither events nor attributes
			//====================================================================================================

        }finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //debug the trace extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));
        		
		return traces;	
	}	

	/**
	 * It retrieves all association between traces and their events, as well as 
	 * the association between traces and their attributes. 
	 * Then it creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects) that are taken from the given hashmap of 
	 * XEvents (resp. XAttributes)
	 * 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event 
	 * WHERE {
	 * 		?trace :TcontainsE ?event . 
	 * } 
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving the association between traces and attributes:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att 
	 * WHERE {
	 * 		?trace :TcontainsA ?att .  
	 * }
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving all traces:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It checks event's mandatory attributes (name, lifecycle, timestamp) and 
	 * 			only retrieves the events that has the mandatory attributes. The checks are performed
	 * 			within the program.
	 * 		</li>
	 * 		<li> 
	 * 			It retrieves all traces, even if they have neither events nor attributes.
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param HashMap<String,XEventOnProm> xevtmap - A hashMap between an event URI and the corresponding XEvent object
	 * @param HashMap<String,XAttribute> xattmap - A hashMap between an attribute URI and the corresponding XAttribute object
	 * @return HashMap<String,XTrace> - A hashMap between a trace URI and the corresponding XTrace object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public HashMap<String,XTrace> getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck2(HashMap<String,XEventOnProm> xevtmap, HashMap<String,XAttribute> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck(HashMap<String,XEventOnProm> xevtmap, HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message

		HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			//====================================================================================================
			//Handling traces events
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces events"));

				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_Simple);
		
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						OWLObject traceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace); 
						String newTrace = (traceObj == null? null : traceObj.toString()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject eventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent); 
						String newEvent = (eventObj == null? null : eventObj.toString()); 
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current event that is being read
					//============================================================================
						XEvent xevt = null;
						
						//if the given XEvent map xevtmap doesn't contain the information about event att
						if(xevtmap.containsKey(newEvent)){
							XEventOnProm xevtOnProm = xevtmap.get(newEvent);
							if(xevtOnProm.hasAllMandatoryAttributes())
								xevt = xevtOnProm;
							else xevtmap.remove(newEvent);
							
						}else{
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_EVENT, newTrace, newEvent)); 
						}
					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================
					
					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================
						if(traces.containsKey(newTrace)){//handle the trace that has been read previously
			
							if(xevt != null)
								traces.get(newTrace).insertOrdered(xevt);
							
						}else{//handle new trace
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
			
							if(xtrace != null){
			
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
		
								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null)
									xtrace.setAttributes(attsOfATrace);
			
								if(xevt != null)
									xtrace.insertOrdered(xevt);

								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
			
							}else {
								currentExecutionNote.append(
									String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
							}
						}
					//============================================================================
					//END OF Handling the current trace that is currently being read
					//============================================================================
				}
		
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
	        //====================================================================================================
			
			//====================================================================================================
			//Handling Traces Attributes
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces attributes"));

				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
		
				while (rs2.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						OWLObject traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						String newTrace = (traceObj == null? null : traceObj.toString()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						String newAtt = (attObj == null? null : attObj.toString()); 
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAttribute xatt = null;
						
						if(xattmap.containsKey(newAtt)){
							xatt = xattmap.get(newAtt);

						}else
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_ATT, newTrace, newAtt)); 
					
					//============================================================================
					//END OF Handling current attribute that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is being read
					//============================================================================
						if(traces.containsKey(newTrace)){
							//handling the traces that are captured in the previous step 

							if(traces.get(newTrace).getAttributes() != null){
								
								if(newAtt != null && xatt != null)
									traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
								
							}else{
								
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();

								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null){
									
									if(newAtt != null && xatt != null)
										attsOfATrace.put(xatt.getKey(), xatt);
									
									traces.get(newTrace).setAttributes(attsOfATrace);
								}
							}
														
						}else {
							//handling the traces that are not captured in the previous step 
							//(e.g., traces that have no events)
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
							
							if(xtrace != null){
			
								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
		
								if(attsOfATrace == null)
									currentExecutionNote.append(
										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
		
								if(attsOfATrace != null){
									
									if(newAtt != null && xatt != null)
										attsOfATrace.put(xatt.getKey(), xatt);
									
									xtrace.setAttributes(attsOfATrace);
								}
			
								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
							}else{
								currentExecutionNote.append(String.format(
										LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
							}
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
				
				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
	        //====================================================================================================
	
			//====================================================================================================
			//Handling Traces that have neither events nor attributes
			//====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces that have neither events nor attributes"));
				
				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				while (rs3.nextRow()) {
					OWLObject newTraceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					String newTrace = (newTraceObj == null? null: newTraceObj.toString()); 
	
					//if the newly read trace is null, then just skip the rest and move on
					if(newTrace == null)
						continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					if(!traces.containsKey(newTrace)){
						XTrace xtrace= this.xfact.createXTraceNaiveImpl();
						
						if(xtrace != null)
							traces.put(newTrace, xtrace);
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
					}
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling Traces that have neither events nor attributes
			//====================================================================================================

        }finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(isAllowToDisposeQuestReasoner()) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //debug the trace extraction results
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));
        		
		return traces;	
	}	

	/**
	 * It retrieves all association between traces and their events, as well as 
	 * the association between traces and their attributes. 
	 * Then it creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects) that are taken from the given hashmap of 
	 * XEvents (resp. XAttributes)
	 * 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event 
	 * WHERE {
	 * 		?trace :TcontainsE ?event . 
	 * } 
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving the association between traces and attributes:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att 
	 * WHERE {
	 * 		?trace :TcontainsA ?att .  
	 * }
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving all traces:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * <br/><br/>
	 * Main feature:
	 * <ul>
	 * 		<li>
	 * 			It is optimized for minimizing the memory usage 
	 * 		</li>
	 * </ul>
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It checks event's mandatory attributes (name, lifecycle, timestamp) and 
	 * 			only retrieves the events that has the mandatory attributes. The checks are performed
	 * 			within the program.
	 * 		</li>
	 * 		<li> 
	 * 			It retrieves all traces, even if they have neither events nor attributes.
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param EfficientHashMap<XEventOnPromNoURI> xevtmap - A hashMap between an event URI and the corresponding XEventOnPromNoURI object
	 * @param EfficientHashMap<XAtt> xattmap - A hashMap between an attribute URI and the corresponding XAtt
	 * @return EfficientHashMap<XTrace> - A hashMap between a trace URI and the corresponding XTrace object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @since June 2017
	 */
	public EfficientHashMap<XTrace> getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheckMO(EfficientHashMap<XEventOnPromEfficient> xevtmap, EfficientHashMap<XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheckMO(EfficientHashMap<XEventOnPromNoURI> xevtmap, EfficientHashMap<XAtt> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message

		EfficientHashMap<XTrace> traces = new EfficientHashMap<XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
        	
			String newTrace, newEvent, newAtt;
			OWLObject traceObj, eventObj, attObj;
			XEvent xevt;
			XEventOnPromEfficient xevtOnProm;
			        	
			//====================================================================================================
			//Handling ALL Traces 
			//====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling ALL traces"));
				
				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				while (rs3.nextRow()) {
					traceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					newTrace = (traceObj == null? null : traceObj.toString().intern()); 
					
					//if the current trace is null, then skip the rest and move on 
					if(newTrace == null) continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					XTrace xtrace= this.xfact.createXTraceNaiveImpl();
					
					if(xtrace != null)
						traces.put(newTrace, xtrace);
					else
						currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling ALL Traces
			//====================================================================================================

			//====================================================================================================
			//Handling traces events
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces events"));

				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_Simple);				
				
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						traceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace); 
						newTrace = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
												
						eventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent); 
						newEvent = (eventObj == null? null : eventObj.toString().intern());
						
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current event that is being read
					//============================================================================
						xevt = null;
						
						//if the given XEvent map xevtmap doesn't contain the information about event att
						if(xevtmap.containsKey(newEvent)){
							xevtOnProm = xevtmap.get(newEvent);
							if(xevtOnProm.hasAllMandatoryAttributes())
								xevt = xevtOnProm;
							
						}else{
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_EVENT, newTrace, newEvent)); 
						}
					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================
					
					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================
						if(traces.containsKey(newTrace)){//handle the trace that has been read previously
			
							if(xevt != null)
								traces.get(newTrace).insertOrdered(xevt);
							
						}
					//============================================================================
					//END OF Handling the current trace that is currently being read
					//============================================================================
				}
		
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
	        //====================================================================================================
			
			//====================================================================================================
			//Handling Traces Attributes
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces attributes"));

				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
		
				while (rs2.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						newTrace = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						newAtt = (attObj == null? null : attObj.toString().intern()); 

					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAtt xatt = null;
						
						if(xattmap.containsKey(newAtt)){
							xatt = xattmap.get(newAtt);
							if(!xatt.hasCompleteInfo())
								xatt = null;

						}else
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_ATT, newTrace, newAtt)); 
					
					//============================================================================
					//END OF Handling current attribute that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is being read
					//============================================================================
						if(traces.containsKey(newTrace)){
							//handling the traces that are captured in the previous step 
							if(newAtt != null && xatt != null)
								traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
				
				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
	        //====================================================================================================

		}finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));
        		
		return traces;	
	}	

	/**
	 * It retrieves all association between traces and their events, as well as 
	 * the association between traces and their attributes. 
	 * Then it creates a hashmap of XTrace where the keys are the Trace URIs.
	 * Each XTrace within the created hashmap of XTraces is filled with the corresponding 
	 * XEvent objects (resp. XAttribute objects) that are taken from the given hashmap of 
	 * XEvents (resp. XAttributes)
	 * 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event 
	 * WHERE {
	 * 		?trace :TcontainsE ?event . 
	 * } 
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving the association between traces and attributes:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att 
	 * WHERE {
	 * 		?trace :TcontainsA ?att .  
	 * }
	 * </code>
	 * </pre>
	 * 
	 * It uses the following query for retrieving all traces:
	 * 
	 * <pre>
	 * <code>
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * <br/><br/>
	 * Main feature:
	 * <ul>
	 * 		<li>
	 * 			It is optimized for minimizing the memory usage 
	 * 		</li>
	 * </ul>
	 * 
	 * <br/><br/>
	 * Notes:
	 * <ul>
	 * 		<li>
	 * 			It checks event's mandatory attributes (name, lifecycle, timestamp) and 
	 * 			only retrieves the events that has the mandatory attributes. The checks are performed
	 * 			within the program.
	 * 		</li>
	 * 		<li> 
	 * 			It retrieves all traces, even if they have neither events nor attributes.
	 * 		</li>
	 * 		<li> 
	 * 			It accesses the query result using the name of the answer 
	 * 			variables.
	 * 		</li>
	 * </ul>
	 * 
	 * @param EfficientHashMap<XEventOnPromNoURI> xevtmap - A hashMap between an event URI and the corresponding XEventOnPromNoURI object
	 * @param EfficientHashMap<XAtt> xattmap - A hashMap between an attribute URI and the corresponding XAtt
	 * @return EfficientHashMap<XTrace> - A hashMap between a trace URI and the corresponding XTrace object
	 * @throws OWLException
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @since June 2017
	 */
	public EfficientHashMap<XTrace> getXTracesUsingAtomicQueriesAndWithoutEventMandatoryAttributesCheckMO(EfficientHashMap<XEventOnPromEfficient> xevtmap, EfficientHashMap<XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheckMO(EfficientHashMap<XEventOnPromNoURI> xevtmap, EfficientHashMap<XAtt> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message

		EfficientHashMap<XTrace> traces = new EfficientHashMap<XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
        	
			String newTrace, newEvent, newAtt;
			OWLObject traceObj, eventObj, attObj;
			XEvent xevt;
			XEventOnPromEfficient xevtOnProm;
			        	
			//====================================================================================================
			//Handling ALL Traces 
			//====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling ALL traces"));
				
				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				while (rs3.nextRow()) {
					traceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					newTrace = (traceObj == null? null : traceObj.toString().intern()); 
					
					//if the current trace is null, then skip the rest and move on 
					if(newTrace == null) continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					XTrace xtrace= this.xfact.createXTraceNaiveImpl();
					
					if(xtrace != null)
						traces.put(newTrace, xtrace);
					else
						currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling ALL Traces
			//====================================================================================================

			//====================================================================================================
			//Handling traces events
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces events"));

				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_Simple);				
				
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						traceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace); 
						newTrace = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
												
						eventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent); 
						newEvent = (eventObj == null? null : eventObj.toString().intern());
						
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current event that is being read
					//============================================================================
						xevt = null;
						
						//if the given XEvent map xevtmap doesn't contain the information about event att
						if(xevtmap.containsKey(newEvent)){
							xevtOnProm = xevtmap.get(newEvent);
							//if(xevtOnProm.hasAllMandatoryAttributes())
								xevt = xevtOnProm;
							
						}else{
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_EVENT, newTrace, newEvent)); 
						}
					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================
					
					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================
						if(traces.containsKey(newTrace)){//handle the trace that has been read previously

							if (xevt != null) {
								try {
									traces.get(newTrace).insertOrdered(xevt);
								} catch (Exception e) {
									
									logger.error("Error while inserting an event into the associated trace. "
											+ "One possible reason: there is a mismatch between the XES attribute type and some reserve XES attribute key. "
											+ "E.g., if the AnnotationQueries says that a certain attribute with the key='time:timestamp' has the type literal ");
									
									/*
									XAttribute timestamp = xevt.getAttributes().get("time:timestamp");
									if (!(timestamp instanceof XAttTimestampEfficient)) {
										logger.error(
												"Timestamp type mismatch-> Trace: " + newTrace + 
												" Name: " + xevt.getAttributes().get("concept:name") + 
												" TS Value: " + timestamp + " TS Class: " + timestamp.getClass());
									}
									logger.error(e.getMessage(), e);
									*/
								}
							}
						}
					//============================================================================
					//END OF Handling the current trace that is currently being read
					//============================================================================
				}
		
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
	        //====================================================================================================
			
			//====================================================================================================
			//Handling Traces Attributes
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces attributes"));

				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
		
				while (rs2.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						newTrace = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						newAtt = (attObj == null? null : attObj.toString().intern()); 

					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAtt xatt = null;
						
						if(xattmap.containsKey(newAtt)){
							xatt = xattmap.get(newAtt);
							if(!xatt.hasCompleteInfo())
								xatt = null;

						}else
							currentExecutionNote.append(String.format(LEConstants.TRACE_MISS_ATT, newTrace, newAtt)); 
					
					//============================================================================
					//END OF Handling current attribute that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is being read
					//============================================================================
						if(traces.containsKey(newTrace)){
							//handling the traces that are captured in the previous step 
							if(newAtt != null && xatt != null)
								traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
				
				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
	        //====================================================================================================

		}finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));
        		
		return traces;	
	}	

	//////////////////////////////////////////////////////////////////////
	// END OF XTraces retriever methods
	//////////////////////////////////////////////////////////////////////
	
}
