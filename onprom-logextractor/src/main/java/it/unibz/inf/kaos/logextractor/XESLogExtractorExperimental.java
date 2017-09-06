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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;


import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.QueryParserRegistry;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.rdf.rdfxml.parser.TripleLogger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueriesV2;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;

import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgEvent;
import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgListener;
import it.unibz.inf.kaos.logextractor.exception.XESLogExtractionFailureException;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.kaos.logextractor.model.EBDAModelWithOptimizedXAttributesEncoding;
import it.unibz.inf.kaos.logextractor.model.XAtt;
import it.unibz.inf.kaos.logextractor.model.impl.EBDAModelImpl2;
import it.unibz.inf.kaos.logextractor.model.impl.EBDAModelImpl3;
import it.unibz.inf.kaos.logextractor.model.impl.EBDAModelNaiveImpl;
import it.unibz.inf.kaos.logextractor.model.impl.LEObjectFactory;
import it.unibz.inf.kaos.logextractor.model.impl.XAttributeOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XEventOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XEventOnPromEfficient;
import it.unibz.inf.kaos.logextractor.model.impl.XEventTimeStampClassifier;
import it.unibz.inf.kaos.logextractor.model.impl.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.model.impl.XLogOnProm;
import it.unibz.inf.kaos.logextractor.reasoner.EBDAReasonerImpl;
import it.unibz.inf.kaos.logextractor.reasoner.EBDAReasonerImplExperiment;
import it.unibz.inf.kaos.logextractor.reasoner.EBDAReasonerImplWithParallelProcessing;
import it.unibz.inf.kaos.logextractor.reasoner.EBDAReasonerImplWithXAttributesOptimization;
import it.unibz.inf.kaos.logextractor.util.EfficientHashMap;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorOWL2QL;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.Quest;
import it.unibz.inf.ontop.owlrefplatform.core.QuestQueryProcessor;
import it.unibz.inf.ontop.owlrefplatform.core.QuestStatement;
import it.unibz.inf.ontop.owlrefplatform.core.QuestUnfolder;
import it.unibz.inf.ontop.owlrefplatform.core.translator.MappingVocabularyRepair;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.parser.SQLQueryDeepParser;
import it.unibz.inf.ontop.parser.TableNameVisitor;

/*
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//LOG EXTRACTOR
//========================================================================================================================
//(Typical) Inputs:
// 1. Domain Ontology T
// 2. OBDA mapping M (i.e., mapping from Database Schema R to Domain Ontology T)
// 3. Annotation A (i.e., Some Sparql queries over T describing how to construct XES event log)
//========================================================================================================================
//(Typical) Working Step:
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
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
*/

/**
 * This class provides the functionalities for extracting XES Log based on the OnProm Approach
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class XESLogExtractorExperimental implements ExecutionMsgListener{
	
	//private static final Logger logger = Logger.getLogger(LEConstants.LOGGER_NAME);
	private static final Logger logger = (Logger) LoggerFactory.getLogger(LEConstants.LOGGER_NAME);
	//private static final Logger logger = (Logger) LoggerFactory.getLogger(XESLogExtractor.class);

	private StringBuilder execMsg;
	private int initExecMsgLength;
	private boolean allowToSnapshotMemory = true;

	public XESLogExtractorExperimental(){
		
		//logger related initialization
		this.setVerboseMode(false);
		this.disableAllOntopLogger();
		this.turnOffMemorySnapshot();
		
		this.execMsg = new StringBuilder(
				"\n\n==========================================\n "
				+ "Summary of Execution Note:"
				+ "\n==========================================\n");

		this.initExecMsgLength = this.execMsg.length();
		//END OF logger related initialization
	}
	
		
	/////////////////////////////////////////////////////////////////////////////////////////////
	//EBDA MODEL CREATOR
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
		public EBDAModelNaiveImpl createEBDAModelNaiveImpl(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
				throws InvalidDataSourcesNumberException, InvalidAnnotationException, OWLException, XESLogExtractionFailureException, OBDAException, MalformedQueryException{
	
			List<OBDADataSource> odsList = obdaModel.getSources();
			if(odsList.size() > 1)
				throw new InvalidDataSourcesNumberException(odsList.size());
	
			//Construct EBDA Model
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start constucting an EBDA Model"));
			LEObjectFactory leFact = LEObjectFactory.getInstance();
			EBDAModelNaiveImpl ebdaModel = leFact.createEBDAModelNaiveImpl();
			
			//add data source information to the EBDA Model
			ebdaModel.addSource(odsList.get(0));
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Construct the mappings for the EBDA Model"));
			//add new mapping based on the annotation information
			ebdaModel.addMapping(domainOntology, obdaModel, annotation);
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish constucting an EBDA Model"));
	
			return ebdaModel;	
		}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//END OF EBDA MODEL CREATOR
	/////////////////////////////////////////////////////////////////////////////////////////////	

	
	
	///////////////////////////////////////////////////////////////////////////////////
	// SOME UTILITY METHODS
	///////////////////////////////////////////////////////////////////////////////////
	
		private XLogOnProm addDefaultGlobalAttributesAndClassifiers(XLogOnProm xlog){
	
			//Adding Default Trace/Event Global Attribute information and also classifier
			try {
			
				//create trace concept:name global attribute
				XAttribute traceConceptNameAtt = XFactoryOnProm.getInstance().createGlobalConceptNameAttribute();
	
				//create event concept:name global attribute
				XAttribute eventConceptNameAtt = XFactoryOnProm.getInstance().createGlobalConceptNameAttribute();
				
				//create event time:timestamp global attribute
				XAttribute timeStampAtt = XFactoryOnProm.getInstance().createGlobalTimeStampAttribute();
	
				//create event lifecycle:transition global attribute
				XAttribute lifecycleAtt = XFactoryOnProm.getInstance().createGlobalLifecycleTransitionAttribute();
				
				//adding event global attribute 'time:timestamp' and time:timestamp classifier
				if(timeStampAtt != null){
					xlog.addGlobalEventAttribute(timeStampAtt);
					xlog.addEventClassifier(new XEventTimeStampClassifier());
				}
				
				//adding event global attribute 'lifecycle:transition' and lifecycle:transition classifier
				if(lifecycleAtt != null) {
					xlog.addGlobalEventAttribute(lifecycleAtt);
					xlog.addEventClassifier(new XEventLifeTransClassifier());
				}
				
				//adding event global attribute 'concept:name' and concept:name classifier
				if(eventConceptNameAtt != null) {
					xlog.addGlobalEventAttribute(eventConceptNameAtt);
					xlog.addEventClassifier(new XEventNameClassifier());
				}
				
				//adding trace global attribute 'concept:name' and concept:name classifier
				if(traceConceptNameAtt != null) {
					xlog.addGlobalTraceAttribute(traceConceptNameAtt);
				}
				
			} catch (UnsupportedAttributeTypeException e) {
				e.printStackTrace();
			}
			//END OF Adding Default Trace/Event Global Attribute information and also classifier
	
	
			return xlog;
		}
		
	///////////////////////////////////////////////////////////////////////////////////
	// END OF SOME UTILITY METHODS
	///////////////////////////////////////////////////////////////////////////////////

	
	
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
			((Logger) LoggerFactory.getLogger(EBDAReasonerImpl.class)).setLevel(level);;
			((Logger) LoggerFactory.getLogger(EBDAReasonerImplExperiment.class)).setLevel(level);;
		}
	
		public void printExecutionNote(){
			
			StringBuilder endExecMsg = new StringBuilder(
					"\n\n==========================================\n "
					+ "END OF Summary of Execution Note:"
					+ "\n==========================================\n");
				
			// if there are some execution message to be printed
			if(this.execMsg.length() > this.initExecMsgLength){
				
				this.execMsg.append(endExecMsg);
	
			}else{// if there is no important execution message to be printed
				this.execMsg.append("\t No important execution note");
				this.execMsg.append(endExecMsg);			
			}
			
			logger.info(this.execMsg.toString());
		}
		
		public String getExecutionNote(){
			
			return this.execMsg.toString();
		}
		
		@Override
		public void addNewExecutionMsg(ExecutionMsgEvent log){
			this.execMsg.append(log.getLog());
		}
	
		public void disableAllOntopLogger(){
			
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestStatement.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Quest.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestQueryProcessor.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SparqlAlgebraToDatalogTranslator.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestOWL.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestUnfolder.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SQLQueryDeepParser.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OBDAModelImpl.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TripleLogger.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OWLAPITranslatorUtility.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OWLAPITranslatorOWL2QL.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(MappingVocabularyRepair.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QueryParserRegistry.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(DatalogUnfolder.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TableNameVisitor.class)).setLevel(ch.qos.logback.classic.Level.OFF);
		}
	
		public void enableAllOntopLogger(){
			
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestStatement.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Quest.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestQueryProcessor.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SparqlAlgebraToDatalogTranslator.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestOWL.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestUnfolder.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SQLQueryDeepParser.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OBDAModelImpl.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TripleLogger.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OWLAPITranslatorUtility.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OWLAPITranslatorOWL2QL.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(MappingVocabularyRepair.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QueryParserRegistry.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(DatalogUnfolder.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TableNameVisitor.class)).setLevel(ch.qos.logback.classic.Level.ALL);
		}
	
		public void turnOnMemorySnapshot(){
			this.allowToSnapshotMemory = true;
		}
	
		public void turnOffMemorySnapshot(){
			this.allowToSnapshotMemory = true;
		}
	
		private void snapshotMemory(){
			
			if(allowToSnapshotMemory){
				DecimalFormat f = new DecimalFormat("###,###.###");
				logger.info(String.format("maxMemory: \t %15s", f.format(Runtime.getRuntime().maxMemory())));
				logger.info(String.format("freeMemory: \t %15s", f.format(Runtime.getRuntime().freeMemory())));
				logger.info(String.format("totalMemory: \t %15s", f.format(Runtime.getRuntime().totalMemory())));
				logger.info(String.format("UsedMemory: \t %15s", f.format((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()))));
			}
		}
		
	///////////////////////////////////////////////////////////////////////////////////
	// END OF LOGGER RELATED STUFF
	///////////////////////////////////////////////////////////////////////////////////

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//EBDA MODEL CREATOR
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
		public EBDAModelImpl2 createEBDAModelImpl2(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
				throws InvalidDataSourcesNumberException, InvalidAnnotationException, OWLException, XESLogExtractionFailureException, OBDAException, MalformedQueryException{
	
			List<OBDADataSource> odsList = obdaModel.getSources();
			if(odsList.size() > 1)
				throw new InvalidDataSourcesNumberException(odsList.size());
	
			//Construct EBDA Model
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start constucting an EBDA Model"));
			LEObjectFactory leFact = LEObjectFactory.getInstance();
			EBDAModelImpl2 ebdaModel = leFact.createEBDAModelImpl2();
			
			//add data source information to the EBDA Model
			ebdaModel.addSource(odsList.get(0));
			
			//add new mapping based on the annotation information
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Construct the mappings for the EBDA Model"));
			ebdaModel.addMapping(domainOntology, obdaModel, annotation);
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish constucting an EBDA Model"));
	
			return ebdaModel;	
		}
	
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
		public EBDAModelImpl3 createEBDAModelImpl3(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
				throws InvalidDataSourcesNumberException, InvalidAnnotationException, OWLException, XESLogExtractionFailureException, OBDAException, MalformedQueryException{
	
			List<OBDADataSource> odsList = obdaModel.getSources();
			if(odsList.size() > 1)
				throw new InvalidDataSourcesNumberException(odsList.size());
	
			//Construct EBDA Model
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start constucting an EBDA Model"));
			LEObjectFactory leFact = LEObjectFactory.getInstance();
			EBDAModelImpl3 ebdaModel = leFact.createEBDAModelImpl3();
			
			//add data source information to the EBDA Model
			ebdaModel.addSource(odsList.get(0));
			
			//add new mapping based on the annotation information
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Construct the mappings for the EBDA Model"));
			ebdaModel.addMapping(domainOntology, obdaModel, annotation);
			
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish constucting an EBDA Model"));
			
			return ebdaModel;	
		}
	
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
			
			return ebdaModel;	
		}

	/////////////////////////////////////////////////////////////////////////////////////////////
	//END OF EBDA MODEL CREATOR
	/////////////////////////////////////////////////////////////////////////////////////////////	

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//EXPERIMENTAL LOG EXTRACTOR
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
		public XLog getXESLog5(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
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
			
			EBDAModel ebdaModel = createEBDAModelNaiveImpl(domainOntology, obdaModel, annotation);
			if(ebdaModel == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_EBDA_CONSTRUCTION_FAILURE);
	
			XLog xlog = getXESLog5(ebdaModel);
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
		public XLog getXESLog5(EBDAModel ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImpl ebdaR = new EBDAReasonerImpl(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
			
				//extract all attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES attributes information"));
				HashMap<String,XAttribute> xatts = ebdaR.getXAttributes();
				if(xatts == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_ATTRIBUTES_RETRIEVAL_FAILURE);
		
				//extract all events and associate each event with their attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
				HashMap<String, XEvent> xevents = ebdaR.getXEventsWithEventMandatoryAttributesCheckInQuery(xatts);
				if(xevents == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_EVENTS_RETRIEVAL_FAILURE);
		
				//extract all traces and associate each trace with their events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
				HashMap<String, XTrace> xtraces = ebdaR.getXTracesWithEventMandatoryAttributesCheckInQuery(xevents, xatts);
				if(xtraces == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_TRACES_RETRIEVAL_FAILURE);
				
				//add the traces into the log
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
				
				//Create XLogOnProm 
				XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm();
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
		
				//Add traces to the log
				xlog.addAll(xtraces.values());
		
				//Add default Trace/Event Global Attribute information and also classifier
				xlog = addDefaultGlobalAttributesAndClassifiers(xlog);
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				//return the log
				return xlog;
			
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}
	
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
		public XLog getXESLog6(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
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
			
			EBDAModel ebdaModel = createEBDAModelNaiveImpl(domainOntology, obdaModel, annotation);
			if(ebdaModel == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_EBDA_CONSTRUCTION_FAILURE);
	
			XLog xlog = getXESLog6(ebdaModel);
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
		public XLog getXESLog6(EBDAModel ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImpl ebdaR = new EBDAReasonerImpl(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
			
				//extract all attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES attributes information"));
				HashMap<String,XAttribute> xatts = ebdaR.getXAttributes();
				if(xatts == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_ATTRIBUTES_RETRIEVAL_FAILURE);
		
				//extract all events and associate each event with their attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
				HashMap<String, XEvent> xevents = ebdaR.getXEventsUsingAtomicQuery(xatts);
				if(xevents == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_EVENTS_RETRIEVAL_FAILURE);
		
				//extract all traces and associate each trace with their events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
				HashMap<String, XTrace> xtraces = ebdaR.getXTracesUsingAtomicQueries(xevents, xatts);
				if(xtraces == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_TRACES_RETRIEVAL_FAILURE);
				
				//add the traces into the log
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
				
				//Create XLogOnProm 
				XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm();
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
		
				//Add traces to the log
				xlog.addAll(xtraces.values());
		
				//Add default Trace/Event Global Attribute information and also classifier
				xlog = addDefaultGlobalAttributesAndClassifiers(xlog);
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				//return the log
				return xlog;
			
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}
	
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
		public XLog getXESLogRW(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
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
			
			EBDAModel ebdaModel = createEBDAModelImpl2(domainOntology, obdaModel, annotation);
			if(ebdaModel == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_EBDA_CONSTRUCTION_FAILURE);
	
			XLog xlog = getXESLogRW(ebdaModel);
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
		public XLog getXESLogRW(EBDAModel ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImpl ebdaR = new EBDAReasonerImpl(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
			
				//extract all attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES attributes information"));
				HashMap<String,XAttribute> xatts = ebdaR.getXAttributes();
				if(xatts == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_ATTRIBUTES_RETRIEVAL_FAILURE);
		
				//extract all events and associate each event with their attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
				HashMap<String, XEvent> xevents = ebdaR.getXEventsUsingAtomicQuery(xatts);
				if(xevents == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_EVENTS_RETRIEVAL_FAILURE);
		
				//extract all traces and associate each trace with their events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
				HashMap<String, XTrace> xtraces = ebdaR.getXTracesUsingAtomicQueries(xevents, xatts);
				if(xtraces == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_TRACES_RETRIEVAL_FAILURE);
				
				//add the traces into the log
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
				
				//Create XLogOnProm 
				XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm();
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
		
				//Add traces to the log
				xlog.addAll(xtraces.values());
		
				//Add default Trace/Event Global Attribute information and also classifier
				xlog = addDefaultGlobalAttributesAndClassifiers(xlog);
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				//return the log
				return xlog;
			
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}
		
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
		public XLog getXESLog10(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
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
		
			EBDAModel ebdaModel = createEBDAModelNaiveImpl(domainOntology, obdaModel, annotation);
			if(ebdaModel == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_EBDA_CONSTRUCTION_FAILURE);
	
			XLog xlog = getXESLog10(ebdaModel);
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
		 */
		public XLog getXESLog10(EBDAModel ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImpl ebdaR = new EBDAReasonerImpl(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
				
				//extract all events and associate each event with their attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
				HashMap<String, XEvent> xevents = ebdaR.getXEventsUsingMediumSizeQuery();
				if(xevents == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_EVENTS_RETRIEVAL_FAILURE);
		
				//extract all traces and associate each trace with their events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
				HashMap<String, XTrace> xtraces = ebdaR.getXTracesUsingMediumSizeQueries(xevents);
				if(xtraces == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_TRACES_RETRIEVAL_FAILURE);
						
				//add the traces into the log
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
		
				//Create XLogOnProm 
				XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm();
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
		
				//Add traces to the log
				xlog.addAll(xtraces.values());
		
				//Add default Trace/Event Global Attribute information and also classifier
				xlog = addDefaultGlobalAttributesAndClassifiers(xlog);
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				//return the log
				return xlog;
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}
	
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
		public XLog getXESLog12(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
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
			EBDAModel ebdaModel = createEBDAModelImpl3(domainOntology, obdaModel, annotation);
			if(ebdaModel == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_EBDA_CONSTRUCTION_FAILURE);
	
			XLog xlog = getXESLog12(ebdaModel);
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
		public XLog getXESLog12(EBDAModel ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImpl ebdaR = new EBDAReasonerImpl(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
			
				//extract all attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES attributes information"));
				HashMap<String,XAttributeOnProm> xatts = ebdaR.getXAttributesWithSplitQuery();
				if(xatts == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_ATTRIBUTES_RETRIEVAL_FAILURE);
		
				//extract all events and associate each event with their attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
				HashMap<String, XEventOnProm> xevents = ebdaR.getXEventsOnPromUsingAtomicQuery(xatts);
				if(xevents == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_EVENTS_RETRIEVAL_FAILURE);
		
				//extract all traces and associate each trace with their events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
				HashMap<String, XTrace> xtraces = ebdaR.getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck(xevents, xatts);
				if(xtraces == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_TRACES_RETRIEVAL_FAILURE);
				
				//add the traces into the log
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
				
				//Create XLogOnProm 
				XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm();
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
		
				//Add traces to the log
				xlog.addAll(xtraces.values());
		
				//Add default Trace/Event Global Attribute information and also classifier
				xlog = addDefaultGlobalAttributesAndClassifiers(xlog);
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				//return the log
				return xlog;
			
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}
	
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
		public XLog getXESLog15(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
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
	
			XLog xlog = getXESLog15(ebdaModel);
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
		public XLog getXESLog15(EBDAModelWithOptimizedXAttributesEncoding ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImplWithXAttributesOptimization ebdaR = new EBDAReasonerImplWithXAttributesOptimization(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
				
				HashMap<String,XAttribute> attributes = new HashMap<String,XAttribute>();
				
				//extract all events and associate each event with their attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
				HashMap<String, XEventOnProm> xevents = ebdaR.getXEventsAndXAttributes(attributes);
				if(xevents == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_EVENTS_RETRIEVAL_FAILURE);
				
				//extract all traces and associate each trace with their events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
				HashMap<String, XTrace> xtraces = ebdaR.getXTracesAndXAttributes(attributes);
				if(xtraces == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_TRACES_RETRIEVAL_FAILURE);
	
				//combine extracted traces and events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Combining XES traces and events"));
				xtraces = ebdaR.mergeXTracesXEvents(xtraces, xevents);
				if(xtraces == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_TRACES_RETRIEVAL_FAILURE);
	
				
				//add the traces into the log
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
				
				//Create XLogOnProm 
				XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm();
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
		
				//Add traces to the log
				xlog.addAll(xtraces.values());
		
				//Add default Trace/Event Global Attribute information and also classifier
				xlog = addDefaultGlobalAttributesAndClassifiers(xlog);
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				//return the log
				return xlog;
			
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}
	
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
		public XLog getXESLog17(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
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
	
			XLog xlog = getXESLog17(ebdaModel);
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
		public XLog getXESLog17(EBDAModelWithOptimizedXAttributesEncoding ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImplWithParallelProcessing ebdaR = new EBDAReasonerImplWithParallelProcessing(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
				//Create XLogOnProm 
				XLogOnProm xlog = ebdaR.extractXLog();
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				//return the log
				return xlog;
			
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}

		


	
	//##################################################################################################################	
	//##################################################################################################################
	// Memory Optimization work in June 2017
	//##################################################################################################################
	//##################################################################################################################
		
		//-------------------------------------------------------------------------------------------
		// Log Extractor with only simple atomic queries
		//	- It splits the query for retrieving XAttributes information
		//	- It can use either EBDAModelNaiveImpl, EBDAModelImpl2 or EBDAModelImpl3
		// OPTIMIZED?
		//-------------------------------------------------------------------------------------------
	
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
		public XLog extractOnPromXESLogUsingOnlyAtomicQueriesAndEBDAModelImpl3Efficiently3(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
				throws InvalidDataSourcesNumberException, InvalidAnnotationException, OWLException, XESLogExtractionFailureException, OBDAException, MalformedQueryException{
	
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start XES Log extractor"));
	
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start creating an EBDA Model"));
			
			EBDAModelImpl3 ebdaModel = createEBDAModelImpl3(domainOntology, obdaModel, annotation);
	
			if(ebdaModel == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_EBDA_CONSTRUCTION_FAILURE);
	
			XLog xlog = extractOnPromXESLogUsingOnlyAtomicQueriesEfficiently3(ebdaModel);
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
		public XLog extractOnPromXESLogUsingOnlyAtomicQueriesEfficiently3(EBDAModel ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImplExperiment ebdaR = new EBDAReasonerImplExperiment(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
			
				//extract all attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES attributes information"));
				
				snapshotMemory();
				
				ImmutableMap<String,XAtt> xatts = ebdaR.getXAttributesWithSplitQuery3();
				
				logger.info("result: "+ xatts.values().size()+" attributes are extracted.");
	
				snapshotMemory();
		
				//extract all events and associate each event with their attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
				
				snapshotMemory();
	
				ImmutableMap<String, XEventOnPromEfficient> xevents = ebdaR.getXEventsOnPromUsingAtomicQuery3(xatts);
	
				logger.info("result: "+ xevents.values().size()+" events are extracted.");
	
				snapshotMemory();
		
				//extract all traces and associate each trace with their events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
				
				snapshotMemory();
	
				ImmutableMap<String, XTrace> xtraces = ebdaR.getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck3(xevents, xatts);
	
				logger.info("result: "+ xtraces.values().size()+" traces are extracted.");
	
				snapshotMemory();
	
				xatts = null;
				xevents = null;
				ebdaR.dispose();
				
				//add the traces into the log
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
				
				//Create XLogOnProm 
				XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm();
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
		
				//Add traces to the log
				xlog.addAll(xtraces.values());
		
				//Add default Trace/Event Global Attribute information and also classifier
				xlog = addDefaultGlobalAttributesAndClassifiers(xlog);
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				xtraces = null;
	
				//return the log
				return xlog;
			
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}
	
		//-------------------------------------------------------------------------------------------
		// END OF Log Extractor with only simple atomic queries
		//-------------------------------------------------------------------------------------------
		
		//-------------------------------------------------------------------------------------------
		// Log Extractor with only simple atomic queries
		//	- It splits the query for retrieving XAttributes information
		//	- It can use either EBDAModelNaiveImpl, EBDAModelImpl2 or EBDAModelImpl3
		// OPTIMIZED?
		//-------------------------------------------------------------------------------------------
	
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
		public XLog extractOnPromXESLogUsingOnlyAtomicQueriesAndEBDAModelImpl3Efficiently5(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueriesV2 annotation) 
				throws InvalidDataSourcesNumberException, InvalidAnnotationException, OWLException, XESLogExtractionFailureException, OBDAException, MalformedQueryException{
	
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start XES Log extractor"));
	
			logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start creating an EBDA Model"));
			
			EBDAModelImpl3 ebdaModel = createEBDAModelImpl3(domainOntology, obdaModel, annotation);
	
			if(ebdaModel == null) 
				throw new XESLogExtractionFailureException(LEConstants.MSG_EBDA_CONSTRUCTION_FAILURE);
	
			XLog xlog = extractOnPromXESLogUsingOnlyAtomicQueriesEfficiently5(ebdaModel);
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
		public XLog extractOnPromXESLogUsingOnlyAtomicQueriesEfficiently5(EBDAModel ebdaModel) throws XESLogExtractionFailureException{
			
			logger.info(String.format(
					LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Model"));
			
			EBDAReasonerImplExperiment ebdaR = new EBDAReasonerImplExperiment(ebdaModel);
			ebdaR.setExecutionLogListener(this);
			
			try{
			
				//extract all attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES attributes information"));
				
				snapshotMemory();
				
				EfficientHashMap<XAtt> xatts = ebdaR.getXAttributesWithSplitQuery5();
				
				logger.info("result: "+ xatts.values().size()+" attributes are extracted.");
	
				snapshotMemory();
	
				//extract all events and associate each event with their attributes
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
				
				snapshotMemory();
	
				EfficientHashMap<XEventOnPromEfficient> xevents = ebdaR.getXEventsOnPromUsingAtomicQuery5(xatts);
	
				logger.info("result: "+ xevents.values().size()+" events are extracted.");
	
				snapshotMemory();
	
				//extract all traces and associate each trace with their events
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
				
				snapshotMemory();
	
				EfficientHashMap<XTrace> xtraces = ebdaR.getXTracesUsingAtomicQueriesAndWithEventMandatoryAttributesCheck5A(xevents, xatts);
	
				logger.info("result: "+ xtraces.values().size()+" traces are extracted.");
	
				snapshotMemory();
	
				xatts = null;
				xevents = null;
				ebdaR.dispose();
				
				//add the traces into the log
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
				
				//Create XLogOnProm 
				XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm(true);
				if(xlog == null) 
					throw new XESLogExtractionFailureException(LEConstants.MSG_LOG_CREATION_FAILURE);
		
				//Add traces to the log
				xlog.addAll(xtraces.values());
				
				logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
		
				xtraces = null;
	
				//return the log
				return xlog;
			
			} catch (OWLException e) {
				e.printStackTrace();
				throw new XESLogExtractionFailureException();
			}
		}
	
		//-------------------------------------------------------------------------------------------
		// END OF Log Extractor with only simple atomic queries
		//-------------------------------------------------------------------------------------------
	
	//##################################################################################################################	
	//##################################################################################################################
	// END OF Memory Optimization work in June 2017
	//##################################################################################################################
	//##################################################################################################################
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//END OF EXPERIMENTAL EXTRACTOR
	/////////////////////////////////////////////////////////////////////////////////////////////	

}





