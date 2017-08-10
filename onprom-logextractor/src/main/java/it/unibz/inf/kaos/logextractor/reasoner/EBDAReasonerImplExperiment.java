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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import ch.qos.logback.classic.Logger;
import gnu.trove.set.hash.TIntHashSet;
import it.unibz.inf.kaos.logextractor.XESLogExtractorExperimentParalel.EventExtractor16;
import it.unibz.inf.kaos.logextractor.XESLogExtractorExperimentParalel.TraceExtractor16;
import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeException;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
//import it.unibz.inf.kaos.logextractor.local.util.SimpleHashMap;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.kaos.logextractor.model.XAtt;
import it.unibz.inf.kaos.logextractor.model.impl.XAttributeOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XAttributeOnProm2;
import it.unibz.inf.kaos.logextractor.model.impl.XEventOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XEventOnPromEfficient;
import it.unibz.inf.kaos.logextractor.model.impl.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.util.EfficientHashMap;
import it.unibz.inf.kaos.logextractor.util.ExecutionMsgEvent;
import it.unibz.inf.kaos.logextractor.util.ExecutionMsgListener;
import it.unibz.inf.kaos.logextractor.util.Print;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConnection;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;

/**
 * This class provide some functionalities to do some particular reasoning over EBDA Model. 
 * Typically, this class contains experimental implementation before the experiment can be considered
 * mature enough to be integrated to the EBDAReasoner. Most of the experiment is about optimization.
 * Some of the experiment might not be promising and they never been moved to the EBDAReasoner. 
 * However, I just keep those implementation here for history. They can also be a base for 
 * further (optimization) experiment, or at least to prevent someone else from repeating the same thing 
 * (both repeating the mistake and the success). 
 * 
 * Well, don't re-invent the wheel when it is already exists ;)
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 * @since 2017
 */
public class EBDAReasonerImplExperiment  extends EBDAReasonerAbstract{

	private static final Logger logger = (Logger) LoggerFactory.getLogger("EBDAReasoner");

	private QuestOWL questReasoner;
	private boolean allowToDisposeQuestReasoner = true;
	private XFactoryOnProm xfact;
	
	/**
	 * Initializes EBDA Reasoner based on the given EBDA Model.
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param ebdaModel
	 */
	public EBDAReasonerImplExperiment(EBDAModel ebdaModel){
		
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
			
//		this.disableLocalLogging();
	}

	//////////////////////////////////////////////////////////////////////
	//Some local utility methods
	//////////////////////////////////////////////////////////////////////	
	
	public boolean isAllowToDisposeQuestReasoner() {
		return allowToDisposeQuestReasoner;
	}

	public void setAllowToDisposeQuestReasoner(boolean allowToDisposeQuestReasoner) {
		this.allowToDisposeQuestReasoner = allowToDisposeQuestReasoner;
	}
	
	public void dispose(){
		if(this.questReasoner != null)
			this.questReasoner.dispose();
	}

	public void disableLocalLogging(){
		((ch.qos.logback.classic.Logger) logger).setLevel(ch.qos.logback.classic.Level.OFF);
	}
	
	//////////////////////////////////////////////////////////////////////
	//END OF Some local utility methods
	//////////////////////////////////////////////////////////////////////	

	
	
/////////////////////////////////////////////////////////////////////////////////////////////
// OPTIMIZATION EXPERIMENT - JUNE 2017
/////////////////////////////////////////////////////////////////////////////////////////////	

	//////////////////////////////////////////////////////////////////////
	// Attributes retriever methods
	//////////////////////////////////////////////////////////////////////
	
	//Masalah, karena datanya gak bener2 URI, jadi ini gak guna
	public ImmutableMap<URI,XAtt> getXAttributesWithSplitQuery2() throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXAttributesWithSplitQuery():\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<URI,XAtt> attributes = new HashMap<URI,XAtt>(10000);
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        
        try {	

        	OWLObject attObj;
        	OWLLiteral keyObj;
        	OWLLiteral typeObj;
        	OWLLiteral valObj;

        	String newAttStr;
        	URI newAtt;
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
				newAttStr = (attObj == null? null: attObj.toString().intern()); 
				if(newAttStr == null) continue;//if the attribute is null, then skip the rest and move on
				newAtt = new URI(newAttStr.substring(1, newAttStr.length()-2));
				
				//if 'attributes' already has an attribute 'att', then fetch the next row
				//Note: if we encounter this case, then there is a chance that an attribute has either multiple
				//key, value or type because we use distinct in the query.
				if(attributes.containsKey(newAtt))
					continue;

				typeObj = rs2.getOWLLiteral(XESEOConstants.qAttTypeAnsVarAttType); 
				type = (typeObj == null? null: typeObj.getLiteral().intern()); 
				if(type == null) continue;//if the attribute type is null, then skip the rest and move on

				try {
					xatt = this.xfact.createXAtt(type, newAtt.toString());
					
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
				newAttStr = (attObj == null? null: attObj.toString().intern()); 
				if(newAttStr == null) continue;//if the attribute is null, then skip the rest and move on
				newAtt = new URI(newAttStr.substring(1, newAttStr.length()-2));
				
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
						attributes.get(newAtt).setExtension(xext);;
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
				newAttStr = (attObj == null? null: attObj.toString().intern()); 
				if(newAttStr == null) continue;//if the attribute is null, then skip the rest and move on
				newAtt = new URI(newAttStr.substring(1, newAttStr.length()-2));

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

		} catch (URISyntaxException e1) {
			e1.printStackTrace();
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
//        logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
//        	new StringBuilder("XAttributes Extraction Results:\n\n").append(getStringOfXAttributes(attributes))));
        
		return ImmutableMap.copyOf(attributes);	
	}	

	public ImmutableMap<String,XAtt> getXAttributesWithSplitQuery3() throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXAttributesWithSplitQuery():\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XAtt> attributes = new HashMap<String,XAtt>(10000);
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        
        
        //for collision check
        HashSet<Integer> hashKey = new HashSet<Integer>();
        int collision = 0;
        //END OF for collision check        
        
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
				
				//check for collision
				int hashCode = newAtt.hashCode();
				
				if(hashKey.contains(hashCode))
					collision++;
				else
					hashKey.add(hashCode);
				//END OF check for collision
				
			}
			if(rs2 != null) rs2.close(); rs2 = null;
			
			typeObj = null; type = null;

			//============================================================================
			//END OF Handling Attribute Types
			//============================================================================

			System.out.println("Collision: "+collision);
			
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
						attributes.get(newAtt).setExtension(xext);;
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
//        logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
//        	new StringBuilder("XAttributes Extraction Results:\n\n").append(getStringOfXAttributes(attributes))));
        
        ImmutableMap<String,XAtt> results = ImmutableMap.copyOf(attributes);
        attributes.clear();
        attributes = null;
        
		return results;
	}	

	public EfficientHashMap<XAtt> getXAttributesWithSplitQuery5() throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXAttributesWithSplitQuery():\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		EfficientHashMap<XAtt> attributes = new EfficientHashMap<XAtt>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        
        
//        //for collision check
//        TIntHashSet hashKey = new TIntHashSet();
//        int collision = 0;
//        //END OF for collision check        
        
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
				
//				//check for collision
//				int hashCode = newAtt.hashCode();
//				
//				if(hashKey.contains(hashCode))
//					collision++;
//				else
//					hashKey.add(hashCode);
//				//END OF check for collision
				
			}
			if(rs2 != null) rs2.close(); rs2 = null;
			
			typeObj = null; type = null;

			//============================================================================
			//END OF Handling Attribute Types
			//============================================================================

//			//check for collision
//			System.out.println("Collision: "+collision);
//			//END OF check for collision
			
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
						attributes.get(newAtt).setExtension(xext);;
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
//        logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
//        	new StringBuilder("XAttributes Extraction Results:\n\n").append(getStringOfXAttributes(attributes))));
        
		return attributes;
	}	

	//////////////////////////////////////////////////////////////////////
	// END OF Attributes retriever methods
	//////////////////////////////////////////////////////////////////////

	
	
	//////////////////////////////////////////////////////////////////////
	// Events retriever methods
	//////////////////////////////////////////////////////////////////////
	
	//Masalah, karena datanya gak bener2 URI, jadi ini gak guna
	public ImmutableMap<URI,XEventOnProm> getXEventsOnPromUsingAtomicQuery2(ImmutableMap<URI,XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXEventsOnPromUsingAtomicQuery(HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<URI,XEventOnProm> events = new HashMap<URI,XEventOnProm>(1060000);
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        QuestOWLResultSet rs = null;
        
		int iterationCounter = 0;
		
        try {	
	
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
				"Querying the relations between events and their attributes"));

			rs = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
				"Finish querying the relations between events and their attributes"));
			
			OWLObject evtObj; OWLObject attObj;
			String newEvtStr, newAttStr;
			URI newEvt, newAtt;
			XAtt xatt;
			XEventOnProm xevt;
			
			while (rs.nextRow()) {
				
				iterationCounter++;
				
				//============================================================================
				//Reading the Query results
				//============================================================================

					evtObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent);
					newEvtStr = (evtObj == null? null: evtObj.toString().intern()); 
					evtObj = null;
					
					//if evt is null, then skip the rest and move on
					if(newEvtStr == null) continue;
					
					newEvt = new URI(newEvtStr.substring(1, newEvtStr.length()-2));
					newEvtStr = null;		
					
					attObj = rs.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt);
					newAttStr = (attObj == null? null: attObj.toString().intern());
					attObj = null;

					newAtt = new URI(newAttStr.substring(1, newAttStr.length()-2));
					newAttStr = null;
					
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
						xevt = this.xfact.createXEventOnProm(newEvt.toString());			
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
			
        	System.out.println("iterationCounter (i.e., # event attributes): "+iterationCounter);

//			iterationCounter = -5;
	
			if(rs != null) rs.close(); rs = null;
			
        }catch(OutOfMemoryError oome){
        	
			if(rs != null) rs.close(); rs = null;
			if (st != null && !st.isClosed()) st.close(); st = null;
			if (conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();

        	System.out.println("iterationCounter: "+iterationCounter);
        	
			logger.info("maxMemory:" +Runtime.getRuntime().maxMemory());
			logger.info("freeMemory:" +Runtime.getRuntime().freeMemory());
			logger.info("totalMemory:" +Runtime.getRuntime().totalMemory());

        	oome.printStackTrace();
        	
        	System.exit(1);
        } catch (URISyntaxException e) {
			e.printStackTrace();
		}
        finally{
			if (st != null && !st.isClosed()) st.close(); st = null;
			if (conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }
        
        //Enable the line below to debug the event extraction results
        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXEvents(events)));
        
		//return events;	
		return ImmutableMap.copyOf(events);	
	}	

	public ImmutableMap<String,XEventOnPromEfficient> getXEventsOnPromUsingAtomicQuery3(ImmutableMap<String,XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXEventsOnPromUsingAtomicQuery(HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XEventOnPromEfficient> events = new HashMap<String,XEventOnPromEfficient>(100000);
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        QuestOWLResultSet rs = null;
        
		int iterationCounter = 0;
		
        //for collision check
        HashSet<Integer> hashKey = new HashSet<Integer>();
        int collision = 0;
        //END OF for collision check        

        try {	
	
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
				"Querying the relations between events and their attributes"));

			rs = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
				"Finish querying the relations between events and their attributes"));
			
			OWLObject evtObj; OWLObject attObj;
			String newEvt, newAtt;
			XAtt xatt;
			XEventOnPromEfficient xevt;
			
			while (rs.nextRow()) {
				
				iterationCounter++;
				
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
						xevt = this.xfact.createXEventOnPromNoURI();			
						if(xevt != null){

							if(newAtt != null && xatt != null)
								xevt.addXAttribute(xatt);

							events.put(newEvt, xevt);
							
							//check for collision
							int hashCode = newEvt.hashCode();
							
							if(hashKey.contains(hashCode))
								collision++;
							else
								hashKey.add(hashCode);
							//END OF check for collision

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
			
        	System.out.println("iterationCounter (i.e., # event attributes): "+iterationCounter);
			System.out.println("Collision: "+collision);

//			iterationCounter = -5;
	
			if(rs != null) rs.close(); rs = null;
			
        }catch(OutOfMemoryError oome){
        	
			if(rs != null) rs.close(); rs = null;
			if (st != null && !st.isClosed()) st.close(); st = null;
			if (conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();

        	System.out.println("iterationCounter: "+iterationCounter);
        	
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
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }
        
        //Enable the line below to debug the event extraction results
        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXEvents(events)));
        
		//return events;	

        
        ImmutableMap<String,XEventOnPromEfficient> results = ImmutableMap.copyOf(events);
        events.clear();
        events = null;

        return results;	
	}	
	
	public EfficientHashMap<XEventOnPromEfficient> getXEventsOnPromUsingAtomicQuery5(EfficientHashMap<XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXEventsOnPromUsingAtomicQuery(HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		EfficientHashMap<XEventOnPromEfficient> events = new EfficientHashMap<XEventOnPromEfficient>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();
        QuestOWLResultSet rs = null;
        
		int iterationCounter = 0;
		
//        //for collision check
//		TIntHashSet hashKey = new TIntHashSet();
//        int collision = 0;
//        //END OF for collision check        

        try {	
	
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
				"Querying the relations between events and their attributes"));

			rs = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, 
				"Finish querying the relations between events and their attributes"));
			
			OWLObject evtObj; OWLObject attObj;
			String newEvt, newAtt;
			XAtt xatt;
			XEventOnPromEfficient xevt;
			
			while (rs.nextRow()) {
				
				iterationCounter++;
				
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
						xevt = this.xfact.createXEventOnPromNoURI();			
						if(xevt != null){

							if(newAtt != null && xatt != null)
								xevt.addXAttribute(xatt);

							events.put(newEvt, xevt);
							
//							//check for collision
//							int hashCode = newEvt.hashCode();
//							
//							if(hashKey.contains(hashCode))
//								collision++;
//							else
//								hashKey.add(hashCode);
//							//END OF check for collision

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
			
        	System.out.println("iterationCounter (i.e., # event attributes): "+iterationCounter);

//			//check for collision
//			System.out.println("Collision: "+collision);
//			//END OF check for collision

//			iterationCounter = -5;
	
			if(rs != null) rs.close(); rs = null;
			
        }catch(OutOfMemoryError oome){
        	
			if(rs != null) rs.close(); rs = null;
			if (st != null && !st.isClosed()) st.close(); st = null;
			if (conn != null && !conn.isClosed()) conn.close(); conn = null;
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();

        	System.out.println("iterationCounter: "+iterationCounter);
        	
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
        if (currentExecutionNote.length() > currExecNoteInitLength){
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        }
        
        //Enable the line below to debug the event extraction results
        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXEvents(events)));
        
        return events;	
	}	

	//////////////////////////////////////////////////////////////////////
	// END OF Events retriever methods
	//////////////////////////////////////////////////////////////////////
	

	
	//////////////////////////////////////////////////////////////////////
	// Traces retriever methods
	//////////////////////////////////////////////////////////////////////

	//Masalah, karena datanya gak bener2 URI, jadi ini gak guna
	public ImmutableMap<URI,XTrace> getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck2(ImmutableMap<URI,XEventOnProm> xevtmap, ImmutableMap<URI,XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck(HashMap<String,XEventOnProm> xevtmap, HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message

		HashMap<URI,XTrace> traces = new HashMap<URI,XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			//====================================================================================================
			//Handling traces events
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces events"));

				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_Simple);
		
				String newTraceStr, newEventStr, newAttStr;
				URI newTrace, newEvent, newAtt;
				OWLObject traceObj;
				
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						traceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace); 
						newTraceStr = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTraceStr == null) continue;
						
						newTrace = new URI(newTraceStr.substring(1, newTraceStr.length()-2));
						
						OWLObject eventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent); 
						newEventStr = (eventObj == null? null : eventObj.toString().intern());
						
						newEvent = new URI(newEventStr.substring(1, newEventStr.length()-2));

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
							//else xevtmap.remove(newEvent);
							
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
			
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//		
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null)
//									xtrace.setAttributes(attsOfATrace);
			
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
						traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						newTraceStr = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTraceStr == null) continue;
						
						newTrace = new URI(newTraceStr.substring(1, newTraceStr.length()-2));
						
						OWLObject attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						newAttStr = (attObj == null? null : attObj.toString().intern()); 

						newAtt = new URI(newAttStr.substring(1, newAttStr.length()-2));
					
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAtt xatt = null;
						
						if(xattmap.containsKey(newAtt)){
//							XAttributeOnProm2 xattOnProm = xattmap.get(newAtt);
							xatt = xattmap.get(newAtt);
							if(!xatt.hasCompleteInfo())
								xatt = null;
//							try {
//								xatt = xattOnProm.toXESXAttribute();
//							} catch (UnsupportedAttributeTypeException e) {
//								if(xattOnProm != null)
//									currentExecutionNote.append(String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE2, 
//										xattOnProm.getType(), xattOnProm.getKey(), xattOnProm.getValue()));
//							}

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

//							if(traces.get(newTrace).getAttributes() != null){
								
								if(newAtt != null && xatt != null)
									traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
								
//							}else{
//								
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null){
//									
//									if(newAtt != null && xatt != null)
//										attsOfATrace.put(xatt.getKey(), xatt);
//									
//									traces.get(newTrace).setAttributes(attsOfATrace);
//								}
//							}
														
						}else {
							//handling the traces that are not captured in the previous step 
							//(e.g., traces that have no events)
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
							
							if(xtrace != null){
//			
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//		
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null){
//									
//									if(newAtt != null && xatt != null)
//										attsOfATrace.put(xatt.getKey(), xatt);
//									
//									xtrace.setAttributes(attsOfATrace);
//								}
			
								xtrace.getAttributes().put(xatt.getKey(), xatt);

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
					traceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					newTraceStr = (traceObj == null? null : traceObj.toString().intern()); 
					
					//if the current trace is null, then skip the rest and move on 
					if(newTraceStr == null) continue;
					
					newTrace = new URI(newTraceStr.substring(1, newTraceStr.length()-2));
					
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

        } catch (URISyntaxException e) {
			e.printStackTrace();
		}finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //Enable the line below to debug the trace extraction results
        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXTraces(traces)));
        		
		//return traces;	
		return ImmutableMap.copyOf(traces);	
	}	
	
	public ImmutableMap<String,XTrace> getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck3(ImmutableMap<String,XEventOnPromEfficient> xevtmap, ImmutableMap<String,XAtt> xattmap) throws OWLException{
		
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
		
				String newTrace, newEvent, newAtt;
				OWLObject traceObj;
				
		        //for collision check
		        HashSet<Integer> hashKey = new HashSet<Integer>();
		        int collision = 0;
		        //END OF for collision check        

				
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						traceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace); 
						newTrace = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
												
						OWLObject eventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent); 
						newEvent = (eventObj == null? null : eventObj.toString().intern());
						
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current event that is being read
					//============================================================================
						XEvent xevt = null;
						
						//if the given XEvent map xevtmap doesn't contain the information about event att
						if(xevtmap.containsKey(newEvent)){
							XEventOnPromEfficient xevtOnProm = xevtmap.get(newEvent);
							if(xevtOnProm.hasAllMandatoryAttributes())
								xevt = xevtOnProm;
							//else xevtmap.remove(newEvent);
							
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
			
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//		
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null)
//									xtrace.setAttributes(attsOfATrace);
			
								if(xevt != null)
									xtrace.insertOrdered(xevt);

								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
			
								//check for collision
								int hashCode = newTrace.hashCode();
								
								if(hashKey.contains(hashCode))
									collision++;
								else
									hashKey.add(hashCode);
								//END OF check for collision

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
						traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						newTrace = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						newAtt = (attObj == null? null : attObj.toString().intern()); 

					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAtt xatt = null;
						
						if(xattmap.containsKey(newAtt)){
//							XAttributeOnProm2 xattOnProm = xattmap.get(newAtt);
							xatt = xattmap.get(newAtt);
							if(!xatt.hasCompleteInfo())
								xatt = null;
//							try {
//								xatt = xattOnProm.toXESXAttribute();
//							} catch (UnsupportedAttributeTypeException e) {
//								if(xattOnProm != null)
//									currentExecutionNote.append(String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE2, 
//										xattOnProm.getType(), xattOnProm.getKey(), xattOnProm.getValue()));
//							}

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

//							if(traces.get(newTrace).getAttributes() != null){
								
								if(newAtt != null && xatt != null)
									traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
								
//							}else{
//								
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null){
//									
//									if(newAtt != null && xatt != null)
//										attsOfATrace.put(xatt.getKey(), xatt);
//									
//									traces.get(newTrace).setAttributes(attsOfATrace);
//								}
//							}
														
						}else {
							//handling the traces that are not captured in the previous step 
							//(e.g., traces that have no events)
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
							
							if(xtrace != null){
//			
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//		
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null){
//									
//									if(newAtt != null && xatt != null)
//										attsOfATrace.put(xatt.getKey(), xatt);
//									
//									xtrace.setAttributes(attsOfATrace);
//								}
			
								xtrace.getAttributes().put(xatt.getKey(), xatt);

								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
								
								//check for collision
								int hashCode = newTrace.hashCode();
								
								if(hashKey.contains(hashCode))
									collision++;
								else
									hashKey.add(hashCode);
								//END OF check for collision

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
					traceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					newTrace = (traceObj == null? null : traceObj.toString().intern()); 
					
					//if the current trace is null, then skip the rest and move on 
					if(newTrace == null) continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					if(!traces.containsKey(newTrace)){
						XTrace xtrace= this.xfact.createXTraceNaiveImpl();
						
						if(xtrace != null){
							
							traces.put(newTrace, xtrace);
						
							//check for collision
							int hashCode = newTrace.hashCode();
							
							if(hashKey.contains(hashCode))
								collision++;
							else
								hashKey.add(hashCode);
							//END OF check for collision
						}
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
					}
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling Traces that have neither events nor attributes
			//====================================================================================================

			System.out.println("Collision: "+collision);

		}finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //Enable the line below to debug the trace extraction results
        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXTraces(traces)));
        		
		//return traces;	
        

        ImmutableMap<String,XTrace> results = ImmutableMap.copyOf(traces);
        traces.clear();
        traces = null;

        return results;	

	}	

	public EfficientHashMap<XTrace> getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck5(EfficientHashMap<XEventOnPromEfficient> xevtmap, EfficientHashMap<XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck(HashMap<String,XEventOnProm> xevtmap, HashMap<String,XAttributeOnProm> xattmap):\n");
		int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message

		EfficientHashMap<XTrace> traces = new EfficientHashMap<XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			//====================================================================================================
			//Handling traces events
	        //====================================================================================================
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces events"));

				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_Simple);
		
				String newTrace, newEvent, newAtt;
				OWLObject traceObj;
				
//		        //for collision check
//				TIntHashSet hashKey = new TIntHashSet();
//		        int collision = 0;
//		        //END OF for collision check        

				
				while (rs.nextRow()) {
					
					//============================================================================
					//Reading the Query results
					//============================================================================
						traceObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace); 
						newTrace = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
												
						OWLObject eventObj = rs.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent); 
						newEvent = (eventObj == null? null : eventObj.toString().intern());
						
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current event that is being read
					//============================================================================
						XEvent xevt = null;
						
						//if the given XEvent map xevtmap doesn't contain the information about event att
						if(xevtmap.containsKey(newEvent)){
							XEventOnPromEfficient xevtOnProm = xevtmap.get(newEvent);
							if(xevtOnProm.hasAllMandatoryAttributes())
								xevt = xevtOnProm;
							//else xevtmap.remove(newEvent);
							
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
			
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//		
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null)
//									xtrace.setAttributes(attsOfATrace);
			
								if(xevt != null)
									xtrace.insertOrdered(xevt);

								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
			
//								//check for collision
//								int hashCode = newTrace.hashCode();
//								
//								if(hashKey.contains(hashCode))
//									collision++;
//								else
//									hashKey.add(hashCode);
//								//END OF check for collision

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
						traceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace); 
						newTrace = (traceObj == null? null : traceObj.toString().intern()); 
						
						//if the current trace is null, then skip the rest and move on 
						if(newTrace == null) continue;
						
						OWLObject attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt); 
						newAtt = (attObj == null? null : attObj.toString().intern()); 

					//============================================================================
					//END OF Reading the Query results
					//============================================================================
					
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================

						XAtt xatt = null;
						
						if(xattmap.containsKey(newAtt)){
//							XAttributeOnProm2 xattOnProm = xattmap.get(newAtt);
							xatt = xattmap.get(newAtt);
							if(!xatt.hasCompleteInfo())
								xatt = null;
//							try {
//								xatt = xattOnProm.toXESXAttribute();
//							} catch (UnsupportedAttributeTypeException e) {
//								if(xattOnProm != null)
//									currentExecutionNote.append(String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE2, 
//										xattOnProm.getType(), xattOnProm.getKey(), xattOnProm.getValue()));
//							}

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

//							if(traces.get(newTrace).getAttributes() != null){
								
								if(newAtt != null && xatt != null)
									traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
								
//							}else{
//								
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null){
//									
//									if(newAtt != null && xatt != null)
//										attsOfATrace.put(xatt.getKey(), xatt);
//									
//									traces.get(newTrace).setAttributes(attsOfATrace);
//								}
//							}
														
						}else {
							//handling the traces that are not captured in the previous step 
							//(e.g., traces that have no events)
							
							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
							
							if(xtrace != null){
//			
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//		
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null){
//									
//									if(newAtt != null && xatt != null)
//										attsOfATrace.put(xatt.getKey(), xatt);
//									
//									xtrace.setAttributes(attsOfATrace);
//								}
			
								xtrace.getAttributes().put(xatt.getKey(), xatt);

								//Note: at this point 'newTrace' and 'xtrace' must not be null
								traces.put(newTrace, xtrace);
								
//								//check for collision
//								int hashCode = newTrace.hashCode();
//								
//								if(hashKey.contains(hashCode))
//									collision++;
//								else
//									hashKey.add(hashCode);
//								//END OF check for collision

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
					traceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					newTrace = (traceObj == null? null : traceObj.toString().intern()); 
					
					//if the current trace is null, then skip the rest and move on 
					if(newTrace == null) continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					if(!traces.containsKey(newTrace)){
						XTrace xtrace= this.xfact.createXTraceNaiveImpl();
						
						if(xtrace != null){
							
							traces.put(newTrace, xtrace);
						
//							//check for collision
//							int hashCode = newTrace.hashCode();
//							
//							if(hashKey.contains(hashCode))
//								collision++;
//							else
//								hashKey.add(hashCode);
//							//END OF check for collision
						}
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
					}
				}
		
				if(rs3 != null) rs3.close();

			//====================================================================================================
			//END OF Handling Traces that have neither events nor attributes
			//====================================================================================================

//			//check for collision
//			System.out.println("Collision: "+collision);
//			//END OF check for collision

		}finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //Enable the line below to debug the trace extraction results
        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXTraces(traces)));
        		
		return traces;	
	}	

	public EfficientHashMap<XTrace> getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck5A(EfficientHashMap<XEventOnPromEfficient> xevtmap, EfficientHashMap<XAtt> xattmap) throws OWLException{
		
		//init execution logging message
		StringBuilder currentExecutionNote = new StringBuilder(
			"EBDAReasoner:getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck(HashMap<String,XEventOnProm> xevtmap, HashMap<String,XAttributeOnProm> xattmap):\n");
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
			
//	        //for collision check
//			TIntHashSet hashKey = new TIntHashSet();
//	        int collision = 0;
//	        //END OF for collision check        

        	
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
									
//					if(!traces.containsKey(newTrace)){
						XTrace xtrace= this.xfact.createXTraceNaiveImpl();
						
						if(xtrace != null){
							
							traces.put(newTrace, xtrace);
						
//							//check for collision
//							int hashCode = newTrace.hashCode();
//							
//							if(hashKey.contains(hashCode))
//								collision++;
//							else
//								hashKey.add(hashCode);
//							//END OF check for collision
						}
						else
							currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
//					}
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
							//else xevtmap.remove(newEvent);
							
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
						
//						else{//handle new trace
//							
//							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
//			
//							if(xtrace != null){
//			
////								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
////		
////								if(attsOfATrace == null)
////									currentExecutionNote.append(
////										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
////		
////								if(attsOfATrace != null)
////									xtrace.setAttributes(attsOfATrace);
//			
//								if(xevt != null)
//									xtrace.insertOrdered(xevt);
//
//								//Note: at this point 'newTrace' and 'xtrace' must not be null
//								traces.put(newTrace, xtrace);
//			
////								//check for collision
////								int hashCode = newTrace.hashCode();
////								
////								if(hashKey.contains(hashCode))
////									collision++;
////								else
////									hashKey.add(hashCode);
////								//END OF check for collision
//
//							}else {
//								currentExecutionNote.append(
//									String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
//							}
//						}
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
//							XAttributeOnProm2 xattOnProm = xattmap.get(newAtt);
							xatt = xattmap.get(newAtt);
							if(!xatt.hasCompleteInfo())
								xatt = null;
//							try {
//								xatt = xattOnProm.toXESXAttribute();
//							} catch (UnsupportedAttributeTypeException e) {
//								if(xattOnProm != null)
//									currentExecutionNote.append(String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE2, 
//										xattOnProm.getType(), xattOnProm.getKey(), xattOnProm.getValue()));
//							}

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

//							if(traces.get(newTrace).getAttributes() != null){
								
								if(newAtt != null && xatt != null)
									traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
								
//							}else{
//								
//								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
//
//								if(attsOfATrace == null)
//									currentExecutionNote.append(
//										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
//		
//								if(attsOfATrace != null){
//									
//									if(newAtt != null && xatt != null)
//										attsOfATrace.put(xatt.getKey(), xatt);
//									
//									traces.get(newTrace).setAttributes(attsOfATrace);
//								}
//							}
														
						}
						
//						else {
//							//handling the traces that are not captured in the previous step 
//							//(e.g., traces that have no events)
//							
//							XTrace xtrace = this.xfact.createXTraceNaiveImpl();
//							
//							if(xtrace != null){
////			
////								XAttributeMap attsOfATrace = this.xfact.createAttributeMap();
////		
////								if(attsOfATrace == null)
////									currentExecutionNote.append(
////										String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 
////		
////								if(attsOfATrace != null){
////									
////									if(newAtt != null && xatt != null)
////										attsOfATrace.put(xatt.getKey(), xatt);
////									
////									xtrace.setAttributes(attsOfATrace);
////								}
//			
//								xtrace.getAttributes().put(xatt.getKey(), xatt);
//
//								//Note: at this point 'newTrace' and 'xtrace' must not be null
//								traces.put(newTrace, xtrace);
//								
////								//check for collision
////								int hashCode = newTrace.hashCode();
////								
////								if(hashKey.contains(hashCode))
////									collision++;
////								else
////									hashKey.add(hashCode);
////								//END OF check for collision
//
//							}else{
//								currentExecutionNote.append(String.format(
//										LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
//							}
//						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
				
				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
	        //====================================================================================================
	

//			//check for collision
//			System.out.println("Collision: "+collision);
//			//END OF check for collision

		}finally{
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
        }
        
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //Enable the line below to debug the trace extraction results
        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXTraces(traces)));
        		
		return traces;	
	}	

	//////////////////////////////////////////////////////////////////////
	// END OF Traces retriever methods
	//////////////////////////////////////////////////////////////////////
	
	
	
/////////////////////////////////////////////////////////////////////////////////////////////
//END OF OPTIMIZATION EXPERIMENT - JUNE 2017
/////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// EXPERIMENT - PARALEL PROCESSING & OPTIMIZED XATTRIBUTES ENCODING
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////
	// Some methods for retrieving XES Log information - 16th version
	// Important Notes:
	//	- it exploits parallel computation (the first version for OnProm)
	//		- but the implementation is a bit messy
	//
	// Notes:
	//	- It assumes that XAttribute URI contains the information about its key, type and value
	//		- the reasoner should be initialized with that special EBDAModel (e.g., EBDAModel5)
	//		- hence, we do not need to query attributes information (e.g., key, value, types)
	//		- just extract it from the URI	
	//	- it orders the query results for retrieving events and their attributes by events
	//	- it orders the query results for retrieving traces and their events by traces
	//	- it uses the OBDA Model that removes the duplication of the data structures 
	//	  for maintaining the extracted elementary XAttributes
	//		- attributes having the same key, type and value are collapsed   
	//	- it orders the events in the program (instead in the query)
	//		- in order to reduce the complexity of the query, 
	//	- it checks the events mandatory attributes in the program (instead in the query)
	//		- in order to reduce the complexity of the query, 
	//	- it uses shared attributes information (the most important differences with version 17)
	//////////////////////////////////////////////////////////////////////	

	/**
	 * It retrieves events and their attributes. 
	 * It uses the following query to retrieve the association between events and attributes:
	 * 
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
	 * 
	 * @return HashMap<String,XEvent>
	 * @throws OWLException
	 */
	public HashMap<String,XEventOnProm> retrieveXEvents16(Hashtable<String,XAttribute> attributes) throws OWLException{
		
		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:retrieveXEvents16(Hashtable<String,XAttribute> attibutes):\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XEventOnProm> events = new HashMap<String,XEventOnProm>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents16: start querying events and attributes"));
        	//execute the query that retrieves the events and their attributes
	        //Note: we must use a query in which the results is ordered by the event
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qEvtAtt_WithOrderByEvent);
			
			String currEvt = "";
			XEventOnProm currXEvt = null;
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents16: END OF querying events and attributes"));
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents16: start processing the query of event attributes association"));

			while (rs.nextRow()) {
				
				//============================================================================
				//Reading the Query results
				//============================================================================

					OWLObject evtObj = rs.getOWLObject(XESEOConstants.qEvtAtt_WithOrderByEventAnsVarEvent);
					//The following 'null checks' are for preventing 'null pointer exception' when getting the string
					//Actually, it might not be needed since the query doesn't use optional
					String newEvt = (evtObj == null? null: evtObj.toString()); 
	
					//if the current event is null, just ignore the rest and move forward
					if(newEvt == null) continue;
					
					OWLObject attObj = rs.getOWLObject(XESEOConstants.qEvtAtt_WithOrderByEventAnsVarAtt);
					String newAtt = (attObj == null? null: attObj.toString()); 
					
					//System.out.println("------------------------------");
					//System.out.println("currEvt: "+ currEvt);
					//System.out.println("currAtt: "+ currAtt);

				//============================================================================
				//END OF Reading the Query results
				//============================================================================

				//============================================================================
				//Handling the current attribute that is being read
				//============================================================================

					//Note: the attribute URI is required to have the following form:
					//[ANY_STRINGS] SPECIAL_URI_DELIMETER [ATT_TYPE] SPECIAL_URI_DELIMETER [ATT_KEY] SPECIAL_URI_DELIMETER [ATT_VALUE]  
					
					XAttribute xatt = null;

					try {
						xatt = this.xfact.getXAttribute(attributes, newAtt);
		
					} catch (UnsupportedAttributeTypeException e) {
						currentExecutionNote.append(
							String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE_EVENT_ATT, 
							e.getAttType(), newEvt, e.getAttKey(), e.getAttValue()));
					} catch (UnsupportedAttributeException e) {
						currentExecutionNote.append(String.format(LEConstants.ATT_CREATION_FAILURE, e.getAttURI()));
					}
					
					//just in case there is a failure in the attribute creation
					if(xatt == null)
						currentExecutionNote.append(String.format(LEConstants.ATT_CREATION_FAILURE, newAtt));

				//============================================================================
				//END OF Handling current attribute that is being read
				//============================================================================
				
				//============================================================================
				//Handling the current event that is being read
				//============================================================================
					if(!currEvt.equals(newEvt)){//handle newly read event
	
						//just add the previously processed event since we start to process the next one
						//if there is previously processed event 
						if(!currEvt.equals("") && currXEvt != null)
							events.put(currEvt, currXEvt);
	
						//create the XEvent
						currXEvt = this.xfact.createXEventOnProm(currEvt);
						
						//if there is a problem with the creation of this event, then just skip the rest and move on
						if(currXEvt == null) {
							currentExecutionNote.append(String.format(LEConstants.EVENT_CREATION_FAILURE, newEvt)); 
							currEvt = "";
							continue;
						}

						//Note: from now on 'xevt' must not be null, hence no null check is needed.
//						XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
//						if(attsOfEvent != null){
							
							if(newAtt != null && xatt != null)
								currXEvt.addXAttribute(xatt);
//								attsOfEvent.put(xatt.getKey(), xatt);
//						
//							currXEvt.setAttributes(attsOfEvent);
//						}
//						else{
//							currentExecutionNote.append(String.format(LEConstants.EVENT_XATTMAP_CREATION_FAILURE, newEvt)); 
//							continue;
//						}
						
						//update the pointer of the previously read event
						currEvt = newEvt; //prevXEvt = xevt;
	
					}else{//handle previously read event
						
//						if(currXEvt.getAttributes() == null){
//							XAttributeMap attsOfEvent = this.xfact.createAttributeMap();
//							currXEvt.setAttributes(attsOfEvent);
//						}
						
						//just add the newly read attribute
						//Note: only add the newly read attribute in case it is not null
						if(newAtt != null && xatt != null)
							currXEvt.addXAttribute(xatt);

//						if(currXEvt.getAttributes() != null && newAtt != null && xatt != null)
//							currXEvt.addXAttribute(xatt);

						
//							currXEvt.getAttributes().put(xatt.getKey(), xatt);
					}
				//============================================================================
				//END OF Handling the current event that is being read
				//============================================================================
			}
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents16: END OF processing the query of event attributes association"));

			//for the last read event (This is needed because in the loop above, 
			//we add and 'event' into 'events' when we start processing the next event)
			if(!currEvt.equals("") &&  currXEvt != null)
				events.put(currEvt, currXEvt);
	
			if(rs != null) rs.close();

		}finally {
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
			if(allowToDisposeQuestReasoner) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //Enable the line below to debug the event extraction results
        logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXEvents(events)));
		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents16: END OF this method"));

		return events;	
	}	

	/**
	 * It retrieves traces (as well as their attributes) and adds the events to the corresponding traces 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event ?timestampValue
	 * WHERE {
	 *     ?trace :TcontainsE ?event . 
	 *     ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampValue. 
	 *     ?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
	 *     ?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
	 * }
	 * ORDER BY ?trace ASC(?timestampValue)
	 * 
	 * It uses the following query to retrieve the association between traces and attributes
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att ?attKey ?attType ?attVal
	 * WHERE {
	 * 		?trace :TcontainsA ?att . ?att :keyA ?attKey; :typeA ?attType; :valueA ?attVal. 
	 * }
	 * ORDER BY ?trace
	 * 
	 * It uses the following query to retrieve the traces:
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * 
	 * @return HashMap<String,XEvent>
	 * @throws OWLException
	 */
	public HashMap<String,XTrace> retrieveXTraces16(Hashtable<String,XAttribute> attributes) throws OWLException{
		
		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:retrieveXTraces16(Hashtable<String,XAttribute> attibutes):\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{

			//====================================================================================================
			//Handling All Traces
			//====================================================================================================

        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: start querying all traces"));
				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				XTrace currXTrace = null;

				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: END OF querying all traces"));

				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: start processing the query of traces results"));

				while (rs3.nextRow()) {
					OWLObject newTraceObj = rs3.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
					String newTrace = (newTraceObj == null? null: newTraceObj.toString()); 
	
					//if the newly read trace is null, then just skip the rest and move on
					if(newTrace == null)
						continue;
					
					//System.out.println("------------------------------");
					//System.out.println("trace: "+ currTrace);
									
					currXTrace = this.xfact.createXTraceNaiveImpl();
					
					if(currXTrace != null)
						traces.put(newTrace, currXTrace);
					else
						currentExecutionNote.append(String.format(LEConstants.TRACE_CREATION_FAILURE, newTrace)); 
				}
		
				if(rs3 != null) rs3.close();

				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: END OF start processing the query of traces results"));

			//====================================================================================================
			//END OF Handling All Traces
			//====================================================================================================

			//====================================================================================================
			//Handling Trace's Attributes
			//====================================================================================================
        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: start querying all traces attributes"));
				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_OrderByTrace);
        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: END OF querying all traces attributes"));
		
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: start processing the results trace attributes query"));

				String currTrace = "";
				currXTrace = null;
				
				while (rs2.nextRow()) {

					//============================================================================
					//Reading the Query results
					//============================================================================
	
						OWLObject newTraceObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_OrderByTraceAnsVarTrace); 
						String newTrace = (newTraceObj == null? null : newTraceObj.toString()); 
						
						//if the current trace is null, just ignore the rest and move on
						if(newTrace == null) continue;

						OWLObject attObj = rs2.getOWLObject(XESEOConstants.qTraceAtt_OrderByTraceAnsVarAtt); 
//						OWLLiteral attKeyObj = rs2.getOWLLiteral(EOConstants.qTraceAtt3AnsVarAttKey);
//						OWLLiteral attTypeObj = rs2.getOWLLiteral(EOConstants.qTraceAtt3AnsVarattType); 
//						OWLLiteral attValObj = rs2.getOWLLiteral(EOConstants.qTraceAtt3AnsVarAttVal); 
						
						String att = (attObj == null? null : attObj.toString()); 
//						String attKey = (attKeyObj == null? null : attKeyObj.getLiteral());
//						String attType = (attTypeObj == null? null : attTypeObj.getLiteral()); 
//						String attVal = (attValObj == null? null : attValObj.getLiteral()); 
		
						//System.out.println("------------------------------");
						//System.out.println("trace: "+ currTrace);
						//System.out.println("att: "+ att);
					
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
									
					//============================================================================
					//Handling the current attribute that is being read
					//============================================================================
						
						//Note: the attribute URI is required to have the following form:
						//[ANY_STRINGS] SPECIAL_URI_DELIMETER [ATT_TYPE] SPECIAL_URI_DELIMETER [ATT_KEY] SPECIAL_URI_DELIMETER [ATT_VALUE]  
						
						XAttribute xatt = null;

						try {
							xatt = this.xfact.getXAttribute(attributes, att);
			
						} catch (UnsupportedAttributeTypeException e) {
							currentExecutionNote.append(
								String.format(LEConstants.UNSUPPORTED_XATTRIBUTE_TYPE_TRACE_ATT, 
								e.getAttType(), newTrace, e.getAttKey(), e.getAttValue()));
						} catch (UnsupportedAttributeException e) {
							currentExecutionNote.append(String.format(LEConstants.ATT_CREATION_FAILURE, e.getAttURI()));
						}
						
						//just in case there is a failure in the attribute creation
						if(xatt == null)
							currentExecutionNote.append(String.format(LEConstants.ATT_CREATION_FAILURE, att));
						
					//============================================================================
					//END OF Handling current attribute that is being read
					//============================================================================
	
					//============================================================================
					//Handling the current trace that is being read
					//============================================================================
						if(!currTrace.equals(newTrace)){//handle newly read trace 
		
							//the following operation must be succeed, because previously we have retrieved all traces. 
							//If the following operation is not succeed, then probably there is an error either 
							//in ontop or in the event ontology
							currXTrace = traces.get(newTrace);
							
							if(currXTrace == null){
								currentExecutionNote.append("Missing trace "+newTrace+".\n"
										+ "Perhaps there is a bug either in ontop or in the event ontology");
								continue;
							}
							
							// just in case the trace doesn't have the attribute map yet
							if(currXTrace.getAttributes() == null){
								XAttributeMap xattmap = this.xfact.createAttributeMap();

								if(xattmap == null)
									currentExecutionNote.append(
											String.format(LEConstants.TRACE_XATTMAP_CREATION_FAILURE, newTrace)); 

								if(xattmap != null)
									currXTrace.setAttributes(xattmap);
							}
								
							//add the newly read attribute into the trace
							if(currXTrace.getAttributes() != null && xatt != null)
								currXTrace.getAttributes().put(xatt.getKey(), xatt);
															
							currTrace = newTrace;
		
						}else{//the case where the current trace is the same with the previously read trace
							
							// just in case the trace doesn't have the attribute map yet
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
		
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: END OF processing the results trace attributes query"));

				if(rs2 != null) rs2.close();
			//====================================================================================================
			//END OF Handling Trace's Attributes
			//====================================================================================================
			
				
		}finally {
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));

        //Enable the line below to debug the trace extraction results
        logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));

		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces16: END OF this method"));

		return traces;	
	}

	/**
	 * It retrieves traces (as well as their attributes) and adds the events to the corresponding traces 
	 * It uses the following query for retrieving the association between traces and events:
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT Distinct ?trace ?event ?timestampValue
	 * WHERE {
	 *     ?trace :TcontainsE ?event . 
	 *     ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampValue. 
	 *     ?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
	 *     ?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
	 * }
	 * ORDER BY ?trace ASC(?timestampValue)
	 * 
	 * It uses the following query to retrieve the association between traces and attributes
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace ?att ?attKey ?attType ?attVal
	 * WHERE {
	 * 		?trace :TcontainsA ?att . ?att :keyA ?attKey; :typeA ?attType; :valueA ?attVal. 
	 * }
	 * ORDER BY ?trace
	 * 
	 * It uses the following query to retrieve the traces:
	 * 
	 * PREFIX : <http://www.example.org/>
	 * SELECT distinct ?trace 
	 * WHERE {
	 * 		?trace a :Trace . 
	 * }
	 * 
	 * @return HashMap<String,XEvent>
	 * @throws OWLException
	 */
	public HashMap<String,XTrace> combineTracesEventsExtractor(EventExtractor16 eventExtractor, TraceExtractor16 traceExtractor, Object lock) throws OWLException{
		
		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:combineTracesEvents16(HashMap<String,XTrace> xtracesmap, HashMap<String,XEventOnProm> xevtmap):\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

		HashMap<String, XEventOnProm> xevtmap;
		HashMap<String, XTrace> xtracesmap;

        try{
			//====================================================================================================
			//Handling Trace's Events
	        //====================================================================================================
        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "combineTracesEvents16: start querying traces events"));
	        	//Note: we must use a query in which the results is ordered by the trace and event timestamp
				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_OrderByTrace);
				
        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "combineTracesEvents16: END OF querying traces events"));

        		String currTrace = "";
				XTrace currXTrace = null;
				
				synchronized(lock){
					while(!eventExtractor.isFinished() || !traceExtractor.isFinished()){
						try {
							lock.notifyAll();
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				xevtmap = eventExtractor.getResults();
				xtracesmap = traceExtractor.getResults();
				
				if(xevtmap == null || xtracesmap == null) 
					return null;

				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Combining XES traces and events"));

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

						XEventOnProm currXEvent = null;
						
						//we only need to do something in case newEvent != null
						if(newEvent != null){
							
							//if the given XEvent map (i.e., xevtmap) contain the information about currEvent
							currXEvent = xevtmap.get(newEvent);
	
							if(currXEvent == null){
								//if the given XEvent map (i.e., xevtmap) doesn't contain 
								//the information about currEvent
								currentExecutionNote.append(
									String.format(LEConstants.TRACE_MISS_EVENT, newTrace, newEvent)); 
								continue;
							}
						}

					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================

						if(!currTrace.equals(newTrace)){//handle newly read trace 
							
//							if(!currTrace.equals("") && currXTrace != null)
//								traces.put(currTrace, currXTrace);
//
//							currXTrace = this.xfact.createNewTrace();			

							currXTrace = xtracesmap.get(newTrace);
							
							//if there is a problem in the creation of XTrace object, just skip the rest and move on
							if(currXTrace == null) {
								currentExecutionNote.append(
										String.format(LEConstants.TRACE_MISSING, newTrace)); 
								currTrace = "";
								continue;
							}
			
							if(currXEvent != null && currXEvent.hasAllMandatoryAttributes())
								currXTrace.insertOrdered(currXEvent);
							
							currTrace = newTrace;
			
						}else{//the case where the current trace is the same with the previously read trace
		
							//just add the newly read event
							if(currXEvent != null && currXEvent.hasAllMandatoryAttributes())
								currXTrace.insertOrdered(currXEvent);
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
		
//				//for the last trace that we read (This is needed because in the loop above, 
//				//we add a 'trace' into 'traces' when we start processing the next trace)
//				if(!currTrace.equals("") && currXTrace != null)
//					traces.put(currTrace, currXTrace);
				
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
			//====================================================================================================
				
		}finally {
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			if(this.allowToDisposeQuestReasoner) questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
        //Enable the line below to debug the trace extraction results
        logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(xtracesmap)));

		return xtracesmap;	
	}

	//////////////////////////////////////////////////////////////////////
	// END OF Some methods for retrieving XES Log information - 16th version 
	//////////////////////////////////////////////////////////////////////	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//END OF EXPERIMENT - PARALEL PROCESSING & OPTIMIZED XATTRIBUTES ENCODING
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
}
