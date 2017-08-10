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
import java.util.Iterator;
import java.util.Map.Entry;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeException;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
import it.unibz.inf.kaos.logextractor.exception.XESLogExtractionFailureException;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.kaos.logextractor.model.EBDAModelWithOptimizedXAttributesEncoding;
import it.unibz.inf.kaos.logextractor.model.impl.XEventOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XLogOnProm;
import it.unibz.inf.kaos.logextractor.util.ExecutionMsgEvent;
import it.unibz.inf.kaos.logextractor.util.ExecutionMsgListener;
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
 * In addition, it tries to run some processes in paralel.
 * 
 * Note: this implementation might not yet mature enough. 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class EBDAReasonerImplWithParallelProcessing extends EBDAReasonerAbstract{

	private static final Logger logger = (Logger) LoggerFactory.getLogger("EBDAReasoner");

	private OWLOntology eventOnto;
	private XFactoryOnProm xfact;
	private QuestOWLConfiguration questOWLDefaultConfig;
	private QuestOWLFactory questFactory;

	/**
	 * Initializes EBDA Reasoner based on the given EBDA Model.
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param ebdaModel
	 */
	public EBDAReasonerImplWithParallelProcessing(EBDAModelWithOptimizedXAttributesEncoding ebdaModel){
	
		this.xfact = XFactoryOnProm.getInstance();
		super.setExecutionLogListener(null);
		this.questFactory = new QuestOWLFactory();

		//Load the Event Ontology
			//System.out.println("\n--------------------------------------------------------");
			//System.out.println("DEBUGA: loading event ontology");
					
			OWLOntologyManager eventOntoMan = OWLManager.createOWLOntologyManager();
			URL eventOntoURL = this.getClass().getResource(XESEOConstants.eventOntoPath);
	
			//System.out.println("loading event ontology from: "+ this.eventOntoURL.getPath());
			
			try {
				eventOnto = eventOntoMan.loadOntologyFromOntologyDocument(eventOntoURL.openStream());
			} catch (OWLOntologyCreationException | IOException e) {
				e.printStackTrace();
			}
	
			//System.out.println("DEBUGA: END OF loading event ontology");
			//System.out.println("\n--------------------------------------------------------");
		//END OF Loading the Event Ontology

		this.questOWLDefaultConfig = this.createQuestOWLDefaultConfig(ebdaModel);
		this.disableLocalLogging();
	}
	
	//////////////////////////////////////////////////////////////////////
	//Some Local utility methods
	//////////////////////////////////////////////////////////////////////	

	public void disableLocalLogging(){
		((ch.qos.logback.classic.Logger) logger).setLevel(ch.qos.logback.classic.Level.OFF);
	}

	private QuestOWLConfiguration createQuestOWLDefaultConfig(EBDAModel ebdaModel){
		
		QuestPreferences preferences = new QuestPreferences();
		preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

        Builder builder = QuestOWLConfiguration.builder();
		builder.obdaModel(ebdaModel);
		builder.preferences(preferences);
		
		QuestOWLConfiguration config = builder.build();
		
		return config;
	}
	
	private QuestOWL getNewQuestReasoner(){
		
		QuestOWLFactory factory = new QuestOWLFactory();
		return factory.createReasoner(this.eventOnto, this.questOWLDefaultConfig);
	}
	
	public void dispose(){
		//nothing really can/need to be done
	}

	//////////////////////////////////////////////////////////////////////
	//END OF Some Local utility methods
	//////////////////////////////////////////////////////////////////////	
	
	
	
	//////////////////////////////////////////////////////////////////////
	// Some methods for retrieving XES Log information - 17th version
	// 
	// Important notes:
	//	- second implementation that exploits paralel processing (should be a cleaner/better one)
	//	- this implementation doesn't share the attributes information among the event and trace extractor
	//		- i.e., specifically tuned for the data that has no shared attributes between events and traces
	//		- but anyway, it should be able to handle normal XES log extraction 
	//		- (it's just not optimized for that situation)
	//
	//////////////////////////////////////////////////////////////////////	
	
	public XLogOnProm extractXLog() throws XESLogExtractionFailureException, OWLException{

		//run the event information extractor
		EventExtractor17 eventExtractor = new EventExtractor17(this.eventOnto, this.questOWLDefaultConfig, this);
		eventExtractor.setPriority(Thread.MAX_PRIORITY);
		eventExtractor.start();

		//run the trace information extractor
		TraceExtractor17 traceExtractor = new TraceExtractor17(this.eventOnto, this.questOWLDefaultConfig, this);
		traceExtractor.setPriority(Thread.MAX_PRIORITY);
		traceExtractor.start();

		//----------------------------------------------------------------------------------------------
		//Waiting for the event and trace information extractor until ONE OF THEM finish their job
    	//----------------------------------------------------------------------------------------------

			synchronized(this){
				while(!eventExtractor.isFinished() && !traceExtractor.isFinished()){
					try {
						this.notify();
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

    	//----------------------------------------------------------------------------------------------
		//END OF Waiting for the event and trace information extractor until ONE OF THEM finish their job
    	//----------------------------------------------------------------------------------------------

//		//----------------------------------------------------------------------------------------------
//		//Waiting for the event and trace information extractor until BOTH OF THEM finish their job
//    	//----------------------------------------------------------------------------------------------
//
//		synchronized(this){
//			while(!eventExtractor.isFinished() || !traceExtractor.isFinished()){
//				try {
//					this.notify();
//					this.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		//----------------------------------------------------------------------------------------------
//		//END OF Waiting for the event and trace information extractor until BOTH OF THEM  finish their job
//    	//----------------------------------------------------------------------------------------------

		//init execution logging message
			StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:retrieveXLog17():\n");
			int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
		//END OF init execution logging message
	
		//Create XLogOnProm 
		XLogOnProm xlog = xfact.createXLogOnProm(true);
		if(xlog == null) 
			throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
	

		//prepare to combine the results of event and trace information extractor
		QuestOWL questReasoner = questFactory.createReasoner(this.eventOnto, this.questOWLDefaultConfig);
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        
        try{
			//====================================================================================================
			//Handling Trace's Events
	        //====================================================================================================
        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "combineTracesEvents17: start querying traces events"));
	        	//Note: we must use a query in which the results is ordered by the trace and event timestamp
				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qTraceEvt_OrderByTrace);
				
        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "combineTracesEvents17: END OF querying traces events"));

        		//----------------------------------------------------------------------------------------------
        		//Waiting for the event and trace information extractor until BOTH OF THEM finish their job
            	//----------------------------------------------------------------------------------------------

    			synchronized(this){
    				while(!eventExtractor.isFinished() || !traceExtractor.isFinished()){
    					try {
    						this.notify();
    						this.wait();
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
    				}
    			}
        		
        		//----------------------------------------------------------------------------------------------
        		//END OF Waiting for the event and trace information extractor until BOTH OF THEM finish their job
            	//----------------------------------------------------------------------------------------------

    			//Get the results from the event and trace information extractor
	    			HashMap<String, XEventOnProm> xevtmap = eventExtractor.getResults();
	    			HashMap<String, XTrace> xtracesmap = traceExtractor.getResults();
	
	    			if(xevtmap == null || xtracesmap == null) 
	    				throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
    			//END OF Getting the results from the event and trace information extractor

				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Combining XES traces and events"));

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
						//System.out.println("trace: "+ newTrace + "\n" + "event: "+ newEvent);
						
					//============================================================================
					//END OF Reading the Query results
					//============================================================================
									
					//============================================================================
					//Handling the current event that is being read
					//============================================================================

						XEventOnProm currXEvent = null;
						
						//we only need to do something in case newEvent != null
						if(newEvent != null)
							currXEvent = xevtmap.get(newEvent);//if the given XEvent map (i.e., xevtmap) contain the information about currEvent
						
						if(currXEvent == null){
							//if the given XEvent map (i.e., xevtmap) doesn't contain 
							//the information about currEvent
							currentExecutionNote.append(
								String.format(LEConstants.TRACE_MISS_EVENT, newTrace, newEvent)); 
							continue;
						}

					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================

					//============================================================================
					//Handling the current trace that is currently being read
					//============================================================================

						if(!currTrace.equals(newTrace)){//handle newly read trace 

							XTrace newXTrace = xtracesmap.get(newTrace);
							
							//if there is a problem in the retrieval of the XTrace object, just skip the rest and move on
							if(newXTrace == null) {
								currentExecutionNote.append(
										String.format(LEConstants.TRACE_MISSING, newTrace)); 
								continue;
							}
							
							if(!currTrace.equals("") && currXTrace != null){
								xlog.add(currXTrace);
							}

							currXTrace = newXTrace;
							currTrace = newTrace;
							
							if(currXEvent != null && currXEvent.hasAllMandatoryAttributes()){
								currXTrace.insertOrdered(currXEvent);
							}
			
						}else{//the case where the current trace is the same with the previously read trace
		
							//just add the newly read event
							if(currXEvent != null && currXEvent.hasAllMandatoryAttributes()){
								currXTrace.insertOrdered(currXEvent);
							}
						}
					//============================================================================
					//END OF Handling the current trace that is being read
					//============================================================================
				}
		
				//for the last trace that we read (This is needed because in the loop above, 
				//we add a 'trace' into 'traces' when we start processing the next trace)
				if(!currTrace.equals("") && currXTrace != null){
					xlog.add(currXTrace);
				}
				
				if(rs != null) rs.close();
			//====================================================================================================
			//END OF Handling Trace's Events
			//====================================================================================================
				
		}finally {
			if(st != null && !st.isClosed()) st.close();
			if(conn != null && !conn.isClosed()) conn.close();
			questReasoner.dispose();
		}
		
		//if there is any additional execution note within this method, 
        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
        if (currentExecutionNote.length() > currExecNoteInitLength)
        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
        
		return xlog;	
	}
	
	private class EventExtractor17 extends Thread{

		private HashMap<String, XEventOnProm> xevents;
		private boolean finished;
		
		private OWLOntology eventOnto;
		private QuestOWLConfiguration questOWLConfig;
		private XFactoryOnProm xfact;
		private Object caller;
		
		public EventExtractor17(OWLOntology eventOnto, QuestOWLConfiguration questOWLConfig, Object caller){

			this.eventOnto = eventOnto;
			this.questOWLConfig = questOWLConfig;
			this.caller = caller;
			this.xfact = XFactoryOnProm.getInstance();
			this.setPriority(Thread.MAX_PRIORITY);
		}
		
		@Override
		public void run() {
			this.finished = false;
			this.setPriority(Thread.MAX_PRIORITY);
			
			//extract all events and associate each event with their attributes
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
			try {
				xevents = retrieveXEvents17();
				
			} catch (OWLException e) {
				e.printStackTrace();
			}
			
			this.finished = true;
			
			synchronized(caller){
				caller.notify();
			}
		}

		/**
		 * It retrieves events and their attributes. 
		 * 
		 * @return HashMap<String,XEvent>
		 * @throws OWLException
		 */
		public HashMap<String,XEventOnProm> retrieveXEvents17() throws OWLException{
			
			//init execution logging message
				StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:retrieveXEvents17():\n");
				int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
			//END OF init execution logging message
			
			HashMap<String,XEventOnProm> events = new HashMap<String,XEventOnProm>();
			HashMap<String,XAttribute> attributes = new HashMap<String,XAttribute>();

			QuestOWLFactory questFactory = new QuestOWLFactory();
			QuestOWL questReasoner = questFactory.createReasoner(this.eventOnto, this.questOWLConfig);
			QuestOWLConnection conn = questReasoner.getConnection();
	        QuestOWLStatement st = conn.createStatement();

	        try{
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents17: start querying events and attributes"));
	        	//execute the query that retrieves the events and their attributes
		        //Note: we must use a query in which the results is ordered by the event
				QuestOWLResultSet rs = st.executeTuple(XESEOConstants.qEvtAtt_WithOrderByEvent);
				
				String currEvt = "";
				XEventOnProm currXEvt = null;
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents17: END OF querying events and attributes"));
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents17: start processing the query of event attributes association"));

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

							if(newAtt != null && xatt != null)
								currXEvt.addXAttribute(xatt);
							
							//update the pointer of the previously read event
							currEvt = newEvt; //prevXEvt = xevt;
		
						}else{//handle previously read event

							//just add the newly read attribute
							//Note: only add the newly read attribute in case it is not null
							if(newAtt != null && xatt != null)
								currXEvt.addXAttribute(xatt);
						}
					//============================================================================
					//END OF Handling the current event that is being read
					//============================================================================
				}
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents17: END OF processing the query of event attributes association"));

				//for the last read event (This is needed because in the loop above, 
				//we add and 'event' into 'events' when we start processing the next event)
				if(!currEvt.equals("") &&  currXEvt != null)
					events.put(currEvt, currXEvt);
		
				if(rs != null) rs.close();

			}finally {
				if (st != null && !st.isClosed()) st.close();
				if (conn != null && !conn.isClosed()) conn.close();
				questReasoner.dispose();
			}
			
			//if there is any additional execution note within this method, 
	        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
	        if (currentExecutionNote.length() > currExecNoteInitLength)
	        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));
	        
	        //Enable the line below to debug the event extraction results
	        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXEvents(events)));
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXEvents17: END OF this method"));

			return events;	
		}	

		public HashMap<String, XEventOnProm> getResults(){
			
			return this.xevents;
		}

		public boolean isFinished(){
			return this.finished;
		}
	}

	private class TraceExtractor17 extends Thread{

		private HashMap<String, XTrace> xtraces;
		private boolean finished;
		
		private OWLOntology eventOnto;
		private QuestOWLConfiguration questOWLConfig;
		private XFactoryOnProm xfact;
		private Object caller;
		
		public TraceExtractor17(OWLOntology eventOnto, QuestOWLConfiguration questOWLConfig, Object caller){

			this.eventOnto = eventOnto;
			this.questOWLConfig = questOWLConfig;
			this.caller = caller;
			this.xfact = XFactoryOnProm.getInstance();
			this.setPriority(Thread.MAX_PRIORITY);
		}
		
		@Override
		public void run() {
			
			this.finished = false;
			
			//extract all traces and associate each trace with their events
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
			try {
				xtraces = retrieveXTraces17();
			} catch (OWLException e) {
				e.printStackTrace();
			}
			
			this.finished = true;
			
			synchronized(caller){
				caller.notify();
			}
		}
	
		/**
		 * It retrieves traces (as well as their attributes) and adds the events to the correponding traces 
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
		public HashMap<String,XTrace> retrieveXTraces17() throws OWLException{
			
			//init execution logging message
				StringBuilder currentExecutionNote = new StringBuilder("EBDAReasoner:retrieveXTraces17():\n");
				int currExecNoteInitLength = currentExecutionNote.length();//to check if there is any additional execution note
			//END OF init execution logging message
			
			HashMap<String,XTrace> traces = new HashMap<String,XTrace>();
			HashMap<String,XAttribute> attributes = new HashMap<String,XAttribute>();

			QuestOWLFactory questFactory = new QuestOWLFactory();
			QuestOWL questReasoner = questFactory.createReasoner(this.eventOnto, this.questOWLConfig);
			QuestOWLConnection conn = questReasoner.getConnection();
	        QuestOWLStatement st = conn.createStatement();

	        try{

				//====================================================================================================
				//Handling All Traces
				//====================================================================================================

	        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: start querying all traces"));
					QuestOWLResultSet rs3 = st.executeTuple(XESEOConstants.qTrace_Simple);
					
					XTrace currXTrace = null;

					logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: END OF querying all traces"));

					logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: start processing the query of traces results"));

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

					logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: END OF start processing the query of traces results"));

				//====================================================================================================
				//END OF Handling All Traces
				//====================================================================================================

				//====================================================================================================
				//Handling Trace's Attributes
				//====================================================================================================
	        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: start querying all traces attributes"));
					QuestOWLResultSet rs2 = st.executeTuple(XESEOConstants.qTraceAtt_OrderByTrace);
	        		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: END OF querying all traces attributes"));
			
					logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: start processing the results trace attributes query"));

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
			
					logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: END OF processing the results trace attributes query"));

					if(rs2 != null) rs2.close();
				//====================================================================================================
				//END OF Handling Trace's Attributes
				//====================================================================================================
				
					
			}finally {
				if(st != null && !st.isClosed()) st.close();
				if(conn != null && !conn.isClosed()) conn.close();
				questReasoner.dispose();
			}
			
			//if there is any additional execution note within this method, 
	        //then send an ExecutionLogEvent to corresponding the ExecutionLogListener
	        if (currentExecutionNote.length() > currExecNoteInitLength)
	        	addNewExecutionMsg(new ExecutionMsgEvent(this, currentExecutionNote));

	        //Enable the line below to debug the trace extraction results
	        //logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, getStringOfXTraces(traces)));

			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "retrieveXTraces17: END OF this method"));

			return traces;	
		}

		public HashMap<String, XTrace> getResults(){
			
			return this.xtraces;
		}
		
		public boolean isFinished(){
			return this.finished;
		}
	}

	//////////////////////////////////////////////////////////////////////
	// END OF Some methods for retrieving XES Log information - 17th version
	//////////////////////////////////////////////////////////////////////	

}
