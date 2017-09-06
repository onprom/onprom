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

package it.unibz.inf.kaos.logextractor;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.openrdf.query.MalformedQueryException;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.LoggerFactory;

import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueriesV2;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;

//import it.unibz.inf.kaos.data.query2.AnnotationQueries;
//import it.unibz.inf.kaos.data.query2.EventAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.EventLifecycleAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.EventResourceAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.EventTimestampAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.EventTraceAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.ResourceAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.TraceAnnotationQuery;

import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgEvent;
import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgListener;
import it.unibz.inf.kaos.logextractor.exception.XESLogExtractionFailureException;
import it.unibz.inf.kaos.logextractor.model.EBDAModelWithOptimizedXAttributesEncoding;
import it.unibz.inf.kaos.logextractor.model.impl.LEObjectFactory;
import it.unibz.inf.kaos.logextractor.model.impl.XEventOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XLogOnProm;
import it.unibz.inf.kaos.logextractor.reasoner.EBDAReasonerImplExperiment;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;



/**
 * This class provides the functionalities for extracting XES Log based on the OnProm Event Ontology
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class XESLogExtractorExperimentParalel implements ExecutionMsgListener{
	
	private static final Logger logger = Logger.getLogger(LEConstants.LOGGER_NAME);
	private XFactoryOnProm xfact = XFactoryOnProm.getInstance();
	private StringBuilder execMsg;
	private StringBuilder noExecMsg;
	private StringBuilder endExecMsg;
	private int initExecMsgLength;

	public XESLogExtractorExperimentParalel(){
		
		XESLogExtractorExperimentParalel.logger.setLevel(Level.ALL);
		this.execMsg = new StringBuilder(
				"\n\n==========================================\n "
				+ "Summary of Execution Note:"
				+ "\n==========================================\n");

		this.noExecMsg = new StringBuilder("\t No important execution note");

		this.endExecMsg = new StringBuilder(
				"\n\n==========================================\n "
				+ "END OF Summary of Execution Note:"
				+ "\n==========================================\n");

		this.initExecMsgLength = this.execMsg.length();
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////
	// LOGGER RELATED STUFF
	///////////////////////////////////////////////////////////////////////////////////
	
	public void setVerboseMode(boolean verbose){
		if(verbose){
			
			logger.setLevel(Level.ALL);
			((Logger) LoggerFactory.getLogger("EBDAReasoner")).setLevel(Level.ALL);
			
		}else{
			
			logger.setLevel(Level.OFF);
			((Logger) LoggerFactory.getLogger("EBDAReasoner")).setLevel(Level.OFF);
		}
	}

	public void setVerboseMode(Level level){
		logger.setLevel(level);
	}

	public void printExecutionNote(){
		//if(executionLogger != null)
			//logger.info(initExecutionNote.append(executionLogger.getExecNote()).toString());

		// if there are some execution message to be printed
		if(this.execMsg.length() > this.initExecMsgLength){
			
			this.execMsg.append(this.endExecMsg);

		}else{// if there is no important execution message to be printed
			this.execMsg.append(this.noExecMsg);
			this.execMsg.append(this.endExecMsg);			
		}
		
		logger.info(this.execMsg.toString());
	}
	
	@Override
	public void addNewExecutionMsg(ExecutionMsgEvent log){
		this.execMsg.append(log.getLog());
	}

	///////////////////////////////////////////////////////////////////////////////////
	// END OF LOGGER RELATED STUFF
	///////////////////////////////////////////////////////////////////////////////////

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//CREATE EBDA MODEL
	/////////////////////////////////////////////////////////////////////////////////////////////	

	/**
	 * Generates EBDA Model based on the given inputs, namely: 
	 * Domain Ontology, OBDA Model, and annotation information
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param domainOntology
	 * @param obdaModel
	 * @param annotation
	 * @return EBDA Model
	 * @throws InvalidDataSourcesNumberException 
	 * @throws OWLException 
	 * @throws InvalidAnnotationException 
	 * @throws XESLogExtractionFailureException 
	 */
	public EBDAModelWithOptimizedXAttributesEncoding createEBDAModelWithOptimizedXAttributesEncoding(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
			throws InvalidDataSourcesNumberException, InvalidAnnotationException, OWLException, XESLogExtractionFailureException, OBDAException, MalformedQueryException{

		List<OBDADataSource> odsList = obdaModel.getSources();
		if(odsList.size() > 1)
			throw new InvalidDataSourcesNumberException(odsList.size());

		//Construct EBDA Model
		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start constucting an EBDA Model"));
		LEObjectFactory leFact = LEObjectFactory.getInstance();
		EBDAModelWithOptimizedXAttributesEncoding ebdaModel = leFact.createEBDAModelWithOptimizedXAttributesEncoding();
		
		//add data source information to the EBDA Model
		ebdaModel.addSource(odsList.get(0));
		
		//add new mapping based on the annotation information
		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Construct the mappings for the EBDA Model"));
		ebdaModel.addMapping(domainOntology, obdaModel, annotation);
		
		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish constucting an EBDA Model"));
		
		//Just for testing: saving EBDA Model to a file
		//writeEBDAModelToFile(ebdaModel, "src/main/resources/RW17.ebda");
		//END OF for testing: saving EBDA Model to a file

		return ebdaModel;	
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	//END OF CREATE EBDA MODEL
	/////////////////////////////////////////////////////////////////////////////////////////////	

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//LOG EXTRACTOR
	/////////////////////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * Generates XES log based on the given inputs, namely: 
	 * Domain Ontology, OBDA Model, and annotation information
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param domainOntology
	 * @param obdaModel
	 * @param annotation
	 * @return event log - XLog
	 * @throws InvalidDataSourcesNumberException 
	 * @throws OWLException 
	 * @throws InvalidAnnotationException 
	 * @throws XESLogExtractionFailureException 
	 */
	public XLog extractXESLog16(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
			throws InvalidDataSourcesNumberException, InvalidAnnotationException, OWLException, XESLogExtractionFailureException, OBDAException, MalformedQueryException{

	//========================================================================================================================
	//Input:
	// 1. Domain Ontology T
	// 2. OBDA mapping M (i.e., mapping from Database Schema R to Domain Ontology T)
	// 3. Annotation A (i.e., Some Sparql queries over T describing how to construct XES event log)
	//========================================================================================================================
	//Step:
	// 1. Process the annotation - create the mapping between data source to event ontology
	//		INPUT Step 1: - all the input above
	//    	(this step essentially creates the second OBDA model that connects the Database and the event ontology)
	// 		- For each annotation, reformulate (rewrite + unfold) it into SQL query 
	// 		- For each SQL query obtained from the step 1, create a mapping to the corresponding concept/role on XES ontology
	//		OUTPUT Step 1: - an OBDA model that connects the Database and the event ontology
	// 2. Generate XES log
	//		INPUT Step 2: - OBDA model that connects the Database and the event ontology
	//		- retrieve all log information and generate XLog
	//========================================================================================================================
		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start XES Log extractor"));

		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start creating an EBDA Model"));
		EBDAModelWithOptimizedXAttributesEncoding ebdaModel = createEBDAModelWithOptimizedXAttributesEncoding(domainOntology, obdaModel, annotation);
		if(ebdaModel == null) 
			throw new XESLogExtractionFailureException(LEConstants.MSG_EBDA_CONSTRUCTION_FAILURE);

		XLog xlog = extractXESLog16(ebdaModel);
		return xlog;	
	}

	/**
	 * 
	 * Generates XES log based on the given particular OBDA model that connects a 
	 * Database to the Event Ontology (i.e., EBDA).
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param ebdaModel - an Event OBDA model (i.e., a particular OBDA model) that 
	 * 						connects a Database to the Event Ontology
	 * @return event log - XLog
	 * @throws OWLException 
	 */
	public XLog extractXESLog16(EBDAModelWithOptimizedXAttributesEncoding ebdaModel) throws XESLogExtractionFailureException{
		
		logger.info(String.format(
				LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
		
		Hashtable<String,XAttribute> attributes = new Hashtable<String,XAttribute>();
		HashMap<String, XTrace> xtraces = null;

		try{
			
			Object lock = new Object();
			
			EBDAReasonerImplExperiment ebdaR1 = new EBDAReasonerImplExperiment(ebdaModel);
			ebdaR1.setExecutionLogListener(this);
			EventExtractor16 eventExtractor = new EventExtractor16(attributes, ebdaR1, lock);
			eventExtractor.start();

			EBDAReasonerImplExperiment ebdaR2 = new EBDAReasonerImplExperiment(ebdaModel);
			ebdaR2.setExecutionLogListener(this);
			TraceExtractor16 traceExtractor = new TraceExtractor16(attributes, ebdaR2, lock);
			traceExtractor.start();
			
			//Create XLogOnProm 
			XLogOnProm xlog = xfact.createXLogOnProm(true);
			if(xlog == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);

			//combine extracted traces and events
			EBDAReasonerImplExperiment ebdaR3 = new EBDAReasonerImplExperiment(ebdaModel);
			ebdaR3.setExecutionLogListener(this);			
			xtraces = ebdaR3.combineTracesEventsExtractor(eventExtractor, traceExtractor, lock);
			if(xtraces == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_TRACES_RETRIEVAL_FAILURE);
			
			//add the traces into the log
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
			
			//Add traces to the log
			xlog.addAll(xtraces.values());
							
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
	
			//return the log
			return xlog;
		
		} catch (OWLException e) {
			e.printStackTrace();
			throw new XESLogExtractionFailureException();
		}
	}

	public class EventExtractor16 extends Thread{

		private Hashtable<String,XAttribute> attributes;
		private HashMap<String, XEventOnProm> xevents;
		private EBDAReasonerImplExperiment ebdaR;
		private Object lock;
		private boolean finished;

		public EventExtractor16(Hashtable<String,XAttribute> attributes, EBDAReasonerImplExperiment ebdaR, Object lock){

			this.attributes = attributes;
			this.ebdaR = ebdaR;
			this.setPriority(Thread.MAX_PRIORITY);
			this.lock = lock;
		}
		
		@Override
		public void run() {
			this.finished = false;

			//extract all events and associate each event with their attributes
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
			try {
				xevents = ebdaR.retrieveXEvents16(attributes);
				
			} catch (OWLException e) {
				e.printStackTrace();
			}
			this.finished = true;
			synchronized(lock){
				lock.notifyAll();
			}
		}

		public HashMap<String, XEventOnProm> getResults(){
			
			return this.xevents;
		}

		public boolean isFinished(){
			return this.finished;
		}
	}

	public class TraceExtractor16 extends Thread{

		private Hashtable<String,XAttribute> attributes;
		private HashMap<String, XTrace> xtraces;
		private EBDAReasonerImplExperiment ebdaR;
		private Object lock;
		private boolean finished;
		
		public TraceExtractor16(Hashtable<String,XAttribute> attributes, EBDAReasonerImplExperiment ebdaR, Object lock){

			this.attributes = attributes;
			this.ebdaR = ebdaR;
			this.setPriority(Thread.MAX_PRIORITY);
			this.lock = lock;
		}
		
		@Override
		public void run() {
			
			this.finished = false;
			
			//extract all traces and associate each trace with their events
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
			try {
				xtraces = ebdaR.retrieveXTraces16(attributes);
			} catch (OWLException e) {
				e.printStackTrace();
			}
			this.finished = true;
			synchronized(lock){
				lock.notifyAll();
			}
		}
	
		public HashMap<String, XTrace> getResults(){
			
			return this.xtraces;
		}
		public boolean isFinished(){
			return this.finished;
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	//END OF LOG EXTRACTOR
	/////////////////////////////////////////////////////////////////////////////////////////////	

}





