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

package it.unibz.inf.kaos.logextractor.model.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
//import java.util.logging.Logger;

import org.openrdf.query.MalformedQueryException;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueriesV2;
import it.unibz.inf.kaos.data.query.old.V2.CaseAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventLifecycleAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventResourceAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventTimestampAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.EventTraceAnnotationQueryV2;
import it.unibz.inf.kaos.data.query.old.V2.ResourceAnnotationQueryV2;
import it.unibz.inf.kaos.logextractor.XESLogExtractor;

//import it.unibz.inf.kaos.data.query2.AnnotationQueries;
//import it.unibz.inf.kaos.data.query2.EventAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.EventLifecycleAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.EventResourceAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.EventTimestampAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.EventTraceAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.ResourceAnnotationQuery;
//import it.unibz.inf.kaos.data.query2.TraceAnnotationQuery;

import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.kaos.obdamapper.reasoner.QuestOWLReasonerExt;
import it.unibz.inf.kaos.obdamapper.ontopext.SQLWithVarMap;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;
import it.unibz.inf.ontop.parser.TargetQueryParserException;
import it.unibz.inf.ontop.parser.TurtleOBDASyntaxParser;

/**
 * This class represents a particular OBDA Model that connects data source to OnProm Event Ontology.
 * It also provides some functionalities for retrieving some information that is usefull 
 * for constructing XES Event Log<br/><br/>
 * 
 * Note: this implementation does not distinguish an object that can become two different events.
 * For instance, when we have concepts A and B, where A is a subclass of B. Moreover, A and B 
 * are annotated as two different events E1 and E2, where each event can have their own attributes 
 * (e.g., their own name). In this case, each instance 'a' of the class A essentially can be considered 
 * as two different events. However, this EBDA Model that doesn't distinguish the instance 'a' as being 
 * either event E1 or E2. Hence, in this case, the instance 'a' will have two name attributes.
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class EBDAModelNaiveImpl extends EBDAModelAbstractImpl implements EBDAModel{

	private static final long serialVersionUID = 40062215694643163L;
//	private static final Logger logger = Logger.getLogger(LEConstants.LOGGER_NAME);
	private static final Logger logger = (Logger) LoggerFactory.getLogger(LEConstants.LOGGER_NAME);
	private TurtleOBDASyntaxParser turtleParser;
	
	//Some String Templates
		private final String uriTemplate = " <%s> "; //arg: A String of URI Template (i.e., contains place holder)
		private final String placeHolderTemplate = "{%s}"; //arg: A variable name
		private final String literalStringTemplate = "{%s}^^xsd:string"; //arg: A variable name
		private final String literalIntTemplate = "{%s}^^xsd:int"; //arg: A variable name
		
		private final String simpleTripleTemplate = " %s %s %s "; //<[Object/Class]> [ObjectProperty] <[Object/Class/Value]>
		private final String tripleObjPropTemplate = " <%s> %s <%s> "; //<[Object/Class]> [ObjectProperty] <[Object/Class]>
		private final String tripleDataPropTemplate = " <%s> %s %s "; //<[Object/Class]> [ObjectProperty] [Value]
		private final String tripleAddDataPropTemplate = " ; %s %s "; //; [DataProperty] [Value]
	
		private final String eoTripleObjPropTemplate = String.format(this.tripleObjPropTemplate, 
						XESEOConstants.eventOntoPrefix+"%s", 	//Object
						"%s",											//Object Properties 
						XESEOConstants.eventOntoPrefix+"%s");	//Object
		private final String constantTemplate = "\"%s\"";	//Object
	//END OF Some String Templates

	
	//URI template for Event Ontology Objects
	
		//URI Template for Trace Object, it needs 1 argument (1st: URI template)
		private final String traceURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"trace/%s"); 
	
		//URI Template for Event Object, it needs 1 argument (1st: URI template)
		private final String eventURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"event/%s"); 
	
		//URI Template for Trace's Name Attribute, it needs 2 arguments 
		//(1st: URI template, 2nd: URI template/place holder)
		private final String attTraceNameURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"att/trace/name/%s/%s"); 
	
		//URI Template for Event's Name Attribute, it needs 2 arguments 
		//(1st: URI template, 2nd: URI template/place holder)
		private final String attEventNameURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"att/event/name/%s/%s"); 
		
		//URI Template for Event's Timestamp Attribute, it needs 2 arguments 
		//(1st: URI template, 2nd: URI template/place holder)
		private final String attEventTimestampURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"att/event/timestamp/%s/%s"); 
		
		//URI Template for Event's Lifecycle Attribute, it needs 2 arguments 
		//(1st: URI template, 2nd: URI template/place holder)
		private final String attEventLifecycleURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"att/event/lifecycle/%s/%s"); 
		
		//URI Template for Event's Lifecycle Attribute, it needs 2 arguments 
		//(1st: URI template, 2nd: URI template/place holder)
		private final String attEventResourceURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"att/event/resource/%s/%s"); 
		
	//END OF URI template for Event Ontology Objects 

	//XES attribute stuff
		private final String xesAttTypeLiteral = "\"literal\"^^xsd:string";
		private final String xesAttTypeTimestamp = "\"timestamp\"^^xsd:string";
		
		private final String xesAttKeyConceptName = "\""+LEConstants.XES_ATT_KEY_CONCEPT_NAME+"\"^^xsd:string"; //based on XES Concept extension
		private final String xesAttKeyTimeTimestamp = "\""+LEConstants.XES_ATT_KEY_TIME_TIMESTAMP+"\"^^xsd:string"; //based on XES Time extension
		private final String xesAttKeyLifecycleTransition = "\""+LEConstants.XES_ATT_KEY_LIFECYCLE_TRANSITION+"\"^^xsd:string"; //based on XES Lifecycle extension
		private final String xesAttKeyOrgResource = "\""+LEConstants.XES_ATT_KEY_ORG_RESOURCE+"\"^^xsd:string"; //based on XES Organization extension
	//END OF XES attribute stuff
	
	//Target Query Template
	
		//the following String template needs four arguments 
		//(1st: Attribute Object, 2nd: Attribute Type, 3rd: Attribute Key, 4th: Attribute Value)
		private final String attDataPropertiesTargetQueryTemplate  = 
						"%s "+ //Attribute Object
						XESEOConstants.ATT_TYPE_ATT+" %s ; "+ 	//Attribute Type
						XESEOConstants.ATT_KEY_ATT +" %s ; "+ 	//Attribute Key 
						XESEOConstants.ATT_VAL_ATT +" %s^^xsd:string  . "; 	//Attribute Value
		
		//the following String template needs two arguments (1st: EventObject, 2nd: Attribute Object)
		private final String eventAttTargetQueryTemplate  = String.format(this.simpleTripleTemplate , 
				"%s ", XESEOConstants.E_CONTAINS_A_ROLE, " %s .");	
		
		//the following String template needs two arguments (1st: Trace Object, 2nd: Event Object)
		private final String eventTraceTargetQueryTemplate  = String.format(this.simpleTripleTemplate , 
				"%s ", XESEOConstants.T_CONTAINS_E_ROLE, " %s .");	

		//the following String template needs two arguments (1st: Trace Object, 2nd: Attribute Object)
		private final String traceAttTargetQueryTemplate  = String.format(this.simpleTripleTemplate , 
				"%s ", XESEOConstants.T_CONTAINS_A_ROLE, " %s .");	

	//END OF Target Query Template

	
	/*
	 * Note: the access modifier for this constructor is set to 'package' in order 
	 * to force that this class can be instantiated only by a class in this package.
	 * To obtain the instance of this class in another package, one need to use the LEModelFactory.
	 */
	EBDAModelNaiveImpl(){
		super();
		this.turtleParser = new TurtleOBDASyntaxParser(this.getPrefixManager());

		//add Event Ontology Prefix
		//addEventOntologyPrefix();
		
		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "An EBDA Model is initialized"));
	}
	
	public void addMapping(OWLOntology ontology, OBDAModel obdaModel, AnnotationQueriesV2 annoQ) 
			throws InvalidAnnotationException, InvalidDataSourcesNumberException {
		
    	//Create an instance of Extended Quest OWL Reasoner.
			QuestOWLConfiguration config = createDefaultQuestOWLConfiguration(obdaModel);
			QuestOWLReasonerExt questReasoner = new QuestOWLReasonerExt(ontology, config);
    	//END OF Creating an instance of Extended Quest OWL Reasoner.

		try {
			//Process the trace annotation
				//TraceAnnotationQuery taq = annoQ.getTrace();
				CaseAnnotationQueryV2 taq = annoQ.getQuery(CaseAnnotationQueryV2.class);

				if(taq == null) 
					throw new InvalidAnnotationException("No Trace Annotation");
			
				this.addMapping(taq, questReasoner);
			//END OF processing the trace annotation
		
			//Process the event annotation
				//Set<EventAnnotationQuery> eaq =  annoQ.getEvents();
				Set<EventAnnotationQueryV2> eaq =  annoQ.getQueries(EventAnnotationQueryV2.class);
				
				if(eaq == null)
					throw new InvalidAnnotationException("No Event Annotation");
		
				this.addMapping(eaq, questReasoner);
			//END OF processing the event annotation
		} catch (OBDAException | MalformedQueryException | OWLException e) {
			e.printStackTrace();
		}finally{
			try {
				questReasoner.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//Process the resource annotation
		//Set<ResourceAnnotationQuery> raq =  annoQ.getResources();
		//TODO: What to do here? XD Why do we need this?
		//END OF Processing the resource annotation

	}	
	
	/////////////////////////////////////////////////////////
	//Methods for adding some mapping based on annotation query
	/////////////////////////////////////////////////////////
	
	private void addMapping(CaseAnnotationQueryV2 taq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{

		//TODO: make a check if reformulateSPARQL2 ada masalah, lanjut ke query berikutnya, jangan mati
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(taq.getQuery());
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			//TODO: need to check whether taq.getTraceAnsVariable() is null or not before calling varMap.get
			//TODO: need to check whether taq.getNameAnsVariable() is null or not before calling varMap.get
			addTraceMapping(sqlWithVarMap.getSQL(), 
						cleanURI(varMap.get(taq.getTraceAnsVariable())),
						cleanURI(varMap.get(taq.getNameAnsVariable()))
				);
		}
	}

	private void addMapping(Set<EventAnnotationQueryV2> eaqSet, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		Iterator<EventAnnotationQueryV2> it = eaqSet.iterator();
		while(it.hasNext()){
			EventAnnotationQueryV2 eaq = it.next();
			
			EventTraceAnnotationQueryV2 etaq = eaq.getEventTrace();
			EventTimestampAnnotationQueryV2 etsaq = eaq.getEventTimestamp();
			EventResourceAnnotationQueryV2 eraq = eaq.getEventResource();
			EventLifecycleAnnotationQueryV2 elaq = eaq.getEventLifecycle();
			
			this.addMapping(eaq, reasoner);

			if(etaq != null) this.addMapping(etaq, reasoner);
			if(etsaq != null) this.addMapping(etsaq, reasoner);
			if(eraq != null) this.addMapping(eraq, reasoner);
			if(elaq != null) this.addMapping(elaq, reasoner);
		}
	}
	
	private void addMapping(EventAnnotationQueryV2 eaq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(eaq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			//TODO: need to check whether eaq.getEventAnsVariable() is null or not before calling varMap.get
			//TODO: need to check whether eaq.getNameAnsVariable() is null or not before calling varMap.get
			addEventMapping(sqlWithVarMap.getSQL(), 
					cleanURI(varMap.get(eaq.getEventAnsVariable())),
					cleanURI(varMap.get(eaq.getNameAnsVariable()))
				);
		}
	}

	private void addMapping(EventTraceAnnotationQueryV2 etaq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{

		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(etaq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();

			//TODO: need to check whether etaq.getTraceAnsVariable() is null or not before calling varMap.get
			//TODO: need to check whether etaq.getEventAnsVariable() is null or not before calling varMap.get
			addEventTraceMapping(sqlWithVarMap.getSQL(), 
						cleanURI(varMap.get(etaq.getTraceAnsVariable())), 
						cleanURI(varMap.get(etaq.getEventAnsVariable()))
				);

		}
	}
	
	private void addMapping(EventTimestampAnnotationQueryV2 etaq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(etaq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			addEventTimestampMapping(sqlWithVarMap.getSQL(), 
						cleanURI(varMap.get(etaq.getEventAnsVariable())),
						cleanURI(varMap.get(etaq.getTimestampAnsVariable()))
				);
		}
	}

	private void addMapping(EventResourceAnnotationQueryV2 eraq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(eraq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			addEventResourceMapping(
					sqlWithVarMap.getSQL(), 
					cleanURI(varMap.get(eraq.getEventAnsVariable())),
					cleanURI(varMap.get(eraq.getResourceAnsVariable()))
				);
		}

	}
	
	private void addMapping(EventLifecycleAnnotationQueryV2 elaq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(elaq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			addEventLifecycleMapping(sqlWithVarMap.getSQL(), 
					cleanURI(varMap.get(elaq.getEventAnsVariable())),
					cleanURI(varMap.get(elaq.getLifecycleAnsVariable()))
				);
		}

	}

	/////////////////////////////////////////////////////////
	//END OF Methods for adding some mapping based on annotation query
	/////////////////////////////////////////////////////////
	
	
	
	/////////////////////////////////////////////////////////	
	//mapping generator
	/////////////////////////////////////////////////////////
	
	private void addTraceMapping(String sqlQuery, String traceVariable, String nameVariable){

		String targetQuery1 = 
			String.format(this.traceAttTargetQueryTemplate, 
				String.format(this.traceURITemplate, traceVariable),
				String.format(this.attTraceNameURITemplate, traceVariable, nameVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);
		
		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attTraceNameURITemplate, traceVariable, nameVariable), 				//att obj
				this.xesAttTypeLiteral, 																//type
				this.xesAttKeyConceptName, 																//key
				(isConstant(nameVariable)? String.format(constantTemplate, nameVariable): nameVariable)	//value
			);
		
		this.addMapping(sqlQuery, targetQuery2);
	}

	private void addEventMapping(String sqlQuery, String eventVariable, String nameVariable){
		
		String targetQuery1 = 
			String.format(this.eventAttTargetQueryTemplate, 
				String.format(this.eventURITemplate, eventVariable),
				String.format(this.attEventNameURITemplate, eventVariable, nameVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);
		
		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attEventNameURITemplate, eventVariable, nameVariable), 				//att obj
				this.xesAttTypeLiteral, 																//type
				this.xesAttKeyConceptName, 																	//key
				(isConstant(nameVariable)? String.format(constantTemplate, nameVariable): nameVariable)	//value
			);

		this.addMapping(sqlQuery, targetQuery2);
	}

	private void addEventTraceMapping(String sqlQuery, String traceVariable, String eventVariable){
				
		//String targetQuery = this.createTraceEventTargetQuery("trace/{"+traceVariable+"}", "event/{"+eventVariable+"}");

		String targetQuery = 
				String.format(this.eventTraceTargetQueryTemplate, 
						String.format(this.traceURITemplate, traceVariable),
						String.format(this.eventURITemplate, eventVariable)
				);

		this.addMapping(sqlQuery, targetQuery);
	}

	private void addEventTimestampMapping(String sqlQuery, String eventVariable, String timestampVariable) {

		String targetQuery1 = 
			String.format(this.eventAttTargetQueryTemplate, 
				String.format(this.eventURITemplate, eventVariable),
				String.format(this.attEventTimestampURITemplate, eventVariable, timestampVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);

		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attEventTimestampURITemplate, eventVariable, timestampVariable), 					//att obj
				this.xesAttTypeTimestamp, 																				//type
				this.xesAttKeyTimeTimestamp, 																				//key
				(isConstant(timestampVariable)? String.format(constantTemplate, timestampVariable): timestampVariable)	//value
			);

		this.addMapping(sqlQuery, targetQuery2);
	}

	private void addEventResourceMapping(String sqlQuery, String eventVariable, String resourceVariable) {
		
		String targetQuery1 = 
			String.format(this.eventAttTargetQueryTemplate, 
				String.format(this.eventURITemplate, eventVariable),
				String.format(this.attEventResourceURITemplate, eventVariable, resourceVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);
		
		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attEventResourceURITemplate, eventVariable, resourceVariable), 					//att obj
				this.xesAttTypeLiteral, 																			//type
				this.xesAttKeyOrgResource, 																			//key
				(isConstant(resourceVariable)? String.format(constantTemplate, resourceVariable): resourceVariable)	//value
			);

		this.addMapping(sqlQuery, targetQuery2);
	}

	private void addEventLifecycleMapping(String sqlQuery, String eventVariable, String lifecycleVariable) {

		String targetQuery1 = 
			String.format(this.eventAttTargetQueryTemplate, 
				String.format(this.eventURITemplate, eventVariable),
				String.format(this.attEventLifecycleURITemplate, eventVariable, lifecycleVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);
		
		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attEventLifecycleURITemplate, eventVariable, lifecycleVariable), 					//att obj
				this.xesAttTypeLiteral, 																				//type
				this.xesAttKeyLifecycleTransition, 																		//key
				(isConstant(lifecycleVariable)? String.format(constantTemplate, lifecycleVariable): lifecycleVariable) 	//value
			);
		
		this.addMapping(sqlQuery, targetQuery2);
	}
	
	/////////////////////////////////////////////////////////
	//END OF mapping generator
	/////////////////////////////////////////////////////////
	
}


