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
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.StringTokenizer;

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

import it.unibz.inf.kaos.logextractor.XESLogExtractorExperimentParalel.EventExtractor16;
import it.unibz.inf.kaos.logextractor.XESLogExtractorExperimentParalel.TraceExtractor16;
import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeException;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
import it.unibz.inf.kaos.logextractor.exception.XESLogExtractionFailureException;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.kaos.logextractor.model.EBDAModelWithOptimizedXAttributesEncoding;
import it.unibz.inf.kaos.logextractor.model.impl.EBDAModelImpl2;
import it.unibz.inf.kaos.logextractor.model.impl.EBDAModelImpl3;
import it.unibz.inf.kaos.logextractor.model.impl.EBDAModelNaiveImpl;
import it.unibz.inf.kaos.logextractor.model.impl.XAttributeOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XEventOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.util.Print;
import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgEvent;
import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgListener;
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
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 * 
 */
public class EBDAReasonerImplWithXAttributesOptimization  extends EBDAReasonerAbstract{

//	private static final Logger logger = Logger.getLogger(LEConstants.LOGGER_NAME);
	private static final Logger logger = (Logger) LoggerFactory.getLogger("EBDAReasoner");

	private QuestOWL questReasoner;
	private XFactoryOnProm xfact;
	private boolean allowToDisposeQuestReasoner = true;
	private boolean verbose = false;
	
	/**
	 * Initializes EBDA Reasoner based on the given EBDA Model.
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param ebdaModel
	 */
	public EBDAReasonerImplWithXAttributesOptimization(EBDAModelWithOptimizedXAttributesEncoding ebdaModel){
		
		this.xfact = XFactoryOnProm.getInstance();
		super.setExecutionLogListener(null);
		
		//Load the Event Ontology
			//System.out.println("\n--------------------------------------------------------");
			//System.out.println("DEBUGA: loading event ontology");
					
			OWLOntologyManager eventOntoMan = OWLManager.createOWLOntologyManager();
			URL eventOntoURL = this.getClass().getResource(XESEOConstants.eventOntoPath);
	
			//System.out.println("loading event ontology from: "+ eventOntoURL.getPath());
			
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

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	//////////////////////////////////////////////////////////////////////
	//END OF Some local utility methods
	//////////////////////////////////////////////////////////////////////	
	
	//////////////////////////////////////////////////////////////////////
	// Some methods for retrieving XES Log information - 15th version
	//
	// Important Notes:
	//	- It assumes that XAttribute URI contains the information about its key, type and value
	//		- the reasoner should be initialized with that special EBDAModel (e.g., EBDAModel5)
	//		- hence, we do not need to query attributes information (e.g., key, value, types)
	//		- just extract it from the URI	
	//	- it orders the query results for retrieving events and their attributes by events
	//	- it orders the query results for retrieving traces and their events by traces
	//	- this is an implementation towards the parallel extractor (in the next version)
	//
	// Notes: 
	//	- it uses the OBDA Model that removes the duplication of the data structures 
	//	  for maintaining the extracted elementary XAttributes
	//		- attributes having the same key, type and value are collapsed   
	//	- it orders the events in the program (instead in the query)
	//		- in order to reduce the complexity of the query, 
	//	- it checks the events mandatory attributes in the program (instead in the query)
	//		- in order to reduce the complexity of the query, 
	//
	// History:
	//	- renaming on 2017.05.05:
	//		- retrieveXEvents15		==> getXEventsAndXAttributes
	//		- retrieveXTraces15		==> getXTracesAndXAttributes
	//		- combineTracesEvents15 ==> combineXTracesXEvents
	//
	//////////////////////////////////////////////////////////////////////	

	/**
	 * It retrieves events and their attributes. 
	 * 
	 * @return HashMap<String,XEvent>
	 * @throws OWLException
	 */
	public HashMap<String,XEventOnProm> getXEventsAndXAttributes(HashMap<String,XAttribute> attributes) throws OWLException{
		
		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXEventsAndXAttributes(HashMap<String,XAttribute> attributes):\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XEventOnProm> events = new HashMap<String,XEventOnProm>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
        	//execute the query that retrieves the events and their attributes
	        //Note: we must use a query in which the results is ordered by the event
			QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qEvtAtt_WithOrderByEvent);
			
			String currEvt = "";
			XEventOnProm currXEvt = null;
			
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
        
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXEvents(events)));

		return events;	
	}	
	
	/**
	 * It retrieves all traces as well as their attributes. 
	 * 
	 * @return HashMap<String,XEvent>
	 * @throws OWLException
	 */
	public HashMap<String,XTrace> getXTracesAndXAttributes(HashMap<String,XAttribute> attributes) throws OWLException{
		
		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:getXTracesAndXAttributes(HashMap<String,XAttribute> attributes):\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{

			//====================================================================================================
			//Handling All Traces
			//====================================================================================================

				QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
				
				XTrace currXTrace = null;
				
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

			//====================================================================================================
			//END OF Handling All Traces
			//====================================================================================================

			//====================================================================================================
			//Handling Trace's Attributes
			//====================================================================================================
				QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_OrderByTrace);
		
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
						
						String att = (attObj == null? null : attObj.toString()); 
		
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

        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(traces)));

		return traces;	
	}

	/**
	 * It retrieves the association between traces and events
	 * 
	 * @return HashMap<String,XEvent>
	 * @throws OWLException
	 */
	public HashMap<String,XTrace> mergeXTracesXEvents(HashMap<String,XTrace> xtracesmap, HashMap<String,XEventOnProm> xevtmap) throws OWLException{
		
		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:mergeXTracesXEvents(HashMap<String,XTrace> xtracesmap, HashMap<String,XEventOnProm> xevtmap):\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
		
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try{
			//====================================================================================================
			//Handling Trace's Events
	        //====================================================================================================
	        	//Note: we must use a query in which the results is ordered by the trace and event timestamp
				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_OrderByTrace);
				
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
        
        if(verbose)
        	logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, Print.getStringOfXTraces(xtracesmap)));

		return xtracesmap;	
	}
	
	//////////////////////////////////////////////////////////////////////
	// END OF Some methods for retrieving XES Log information - 15th version 
	//////////////////////////////////////////////////////////////////////	

}
