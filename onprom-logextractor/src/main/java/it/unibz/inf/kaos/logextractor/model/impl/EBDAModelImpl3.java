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
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.data.query.EventAnnotationQuery;
import it.unibz.inf.kaos.data.query.EventLifecycleAnnotationQuery;
import it.unibz.inf.kaos.data.query.EventResourceAnnotationQuery;
import it.unibz.inf.kaos.data.query.EventTimestampAnnotationQuery;
import it.unibz.inf.kaos.data.query.EventTraceAnnotationQuery;
import it.unibz.inf.kaos.data.query.ResourceAnnotationQuery;
import it.unibz.inf.kaos.data.query.CaseAnnotationQuery;
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
import it.unibz.inf.kaos.logextractor.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.logextractor.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.kaos.logextractor.ontopext.SQLWithVarMap;
import it.unibz.inf.kaos.logextractor.reasoner.QuestOWLReasonerExt;
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
 * for constructing XES Event Log <br/><br/>
 * 
 * Features:
 * <ul>
 * 		<li>
 * 			In this version, we create a unique URI for each event annotation. 
 * 			It is needed to handle the case where an object might have two different time 
 * 			stamps and hence it belong to two different events.
 * 		</li>
 * 		<li>
 *  		In this version, we unify the attributes that has the same keys, values and types
 * 		</li>
 * </ul>
 *  
 * Note: this implementation only consider elementary attributes 
 * <ul>
 * 		<li>
 * 			i.e., no nested/composite attributes
 * 		</li>
 * 		<li>
 * 			Note that if we allow for nested attribute, then two attributes with the same key, value, and type 
 *    		can be considered different depending on the attributes that are belong to these attributes. Hence, 
 *    		if we want to handle nested/composite attributes, we might require additional information for creating 
 *    		the attribute URI, for instance 'event identifier'. By also using event identifier, an attribute that 
 *    		belongs to two different event can be distinguished.
 * 		</li>
 * </ul>
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class EBDAModelImpl3 extends EBDAModelAbstractImpl implements EBDAModel {

	private static final long serialVersionUID = 40062215694643168L;
//	private static final Logger logger = Logger.getLogger(LEConstants.LOGGER_NAME);
	private static final Logger logger = (Logger) LoggerFactory.getLogger(LEConstants.LOGGER_NAME);
//	private TurtleOBDASyntaxParser turtleParser;
	
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
				XESEOConstants.eventOntoPrefix+"event/%s/%s"); 
	
		//URI Template for Trace's Name Attribute, it needs 1 argument
		//(1st: URI template/place holder)
		private final String attTraceNameURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"elementaryAtt/name/%s"); 
	
		//URI Template for Event's Name Attribute, it needs 1 argument
		//(1st: URI template/place holder)
		private final String attEventNameURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"elementaryAtt/name/%s"); 
		
		//URI Template for Event's Timestamp Attribute, it needs 1 argument
		//(1st: URI template/place holder)
		private final String attEventTimestampURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"elementaryAtt/timestamp/%s"); 
		
		//URI Template for Event's Lifecycle Attribute, it needs 1 argument
		//(1st: URI template/place holder)
		private final String attEventLifecycleURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"elementaryAtt/lifecycle/%s"); 
		
		//URI Template for Event's Lifecycle Attribute, it needs 1 argument 
		//(1st: URI template/place holder)
		private final String attEventResourceURITemplate = String.format(this.uriTemplate,
				XESEOConstants.eventOntoPrefix+"elementaryAtt/resource/%s"); 
		
	//END OF URI template for Event Ontology Objects 

	//XES attribute stuff
		private final String xesAttTypeLiteral = "\"literal\"^^xsd:string";
		private final String xesAttTypeTimestamp = "\"timestamp\"^^xsd:string";
		
		private final String xesAttKeyConceptName = "\"concept:name\"^^xsd:string"; //based on XES Concept extension
		private final String xesAttKeyTimeTimestamp = "\"time:timestamp\"^^xsd:string"; //based on XES Time extension
		private final String xesAttKeyLifecycleTransition = "\"lifecycle:transition\"^^xsd:string"; //based on XES Lifecycle extension
		private final String xesAttKeyOrgResource = "\"org:resource\"^^xsd:string"; //based on XES Organization extension
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
	EBDAModelImpl3(){
		super();

		//add Event Ontology Prefix
		//addEventOntologyPrefix();
		
//		this.turtleParser = null;
		
		logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "An EBDA Model is initialized"));
	}

	public void addMapping(OWLOntology ontology, OBDAModel obdaModel, AnnotationQueries annoQ) 
			throws InvalidAnnotationException, InvalidDataSourcesNumberException{

//		if(this.turtleParser == null)
//			this.turtleParser = new TurtleOBDASyntaxParser(this.getPrefixManager());

		
    	//Create an instance of Extended Quest OWL Reasoner.
			QuestOWLConfiguration config = createDefaultQuestOWLConfiguration(obdaModel);
			QuestOWLReasonerExt questReasoner = new QuestOWLReasonerExt(ontology, config);
    	//END OF Creating an instance of Extended Quest OWL Reasoner.

		try{
			//Process the trace annotation
//			TraceAnnotationQuery taq = annoQ.getTrace();
			CaseAnnotationQuery taq = annoQ.getQuery(CaseAnnotationQuery.class);
			if(taq == null) 
				throw new InvalidAnnotationException("No Case Annotation");
			
			this.addMapping(taq, questReasoner);
			//END OF processing the trace annotation
			
			//Process the event annotation
//			Set<EventAnnotationQuery> eaq =  annoQ.getEvents();
			Set<EventAnnotationQuery> eaq =  annoQ.getQueries(EventAnnotationQuery.class);
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
			
//			this.turtleParser = null;
		}

		//Process the resource annotation
		//Set<ResourceAnnotationQuery> raq =  annoQ.getResources();
		//TODO: What to do here? XD Why do we need this?
		//END OF Processing the resource annotation

	}	
	
	
	/////////////////////////////////////////////////////////
	//Methods for adding some mapping based on annotation query
	/////////////////////////////////////////////////////////
	
	private void addMapping(CaseAnnotationQuery taq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{

		//TODO: make a check if reformulateSPARQL2 ada masalah, lanjut ke query berikutnya, jangan mati
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(taq.getQuery());
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			//TODO: need to check whether taq.getTraceAnsVariable() is null or not before calling varMap.get
			//TODO: need to check whether taq.getNameAnsVariable() is null or not before calling varMap.get
			addTraceNameMapping(sqlWithVarMap.getSQL(), 
						cleanURI(varMap.get(taq.getTraceAnsVariable())),
						cleanURI(varMap.get(taq.getNameAnsVariable()))
				);
		}
	}

	private void addMapping(Set<EventAnnotationQuery> eaqSet, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		Iterator<EventAnnotationQuery> it = eaqSet.iterator();
		
		int eventUID = 1;
		
		while(it.hasNext()){
			EventAnnotationQuery eaq = it.next();
			
			EventTraceAnnotationQuery etaq = eaq.getEventTrace();
			EventTimestampAnnotationQuery etsaq = eaq.getEventTimestamp();
			EventResourceAnnotationQuery eraq = eaq.getEventResource();
			EventLifecycleAnnotationQuery elaq = eaq.getEventLifecycle();
			
			this.addMapping(eventUID, eaq, reasoner);

			if(etaq != null) this.addMapping(eventUID, etaq, reasoner);
			if(etsaq != null) this.addMapping(eventUID, etsaq, reasoner);
			if(eraq != null) this.addMapping(eventUID, eraq, reasoner);
			if(elaq != null) this.addMapping(eventUID, elaq, reasoner);
			
			eventUID++;
		}
	}
	
	private void addMapping(int eventUID, EventAnnotationQuery eaq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(eaq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			//TODO: need to check whether eaq.getEventAnsVariable() is null or not before calling varMap.get
			//TODO: need to check whether eaq.getNameAnsVariable() is null or not before calling varMap.get
			addEventNameMapping(sqlWithVarMap.getSQL(), eventUID, 
					cleanURI(varMap.get(eaq.getEventAnsVariable())),
					cleanURI(varMap.get(eaq.getNameAnsVariable()))
				);
		}
	}

	private void addMapping(int eventUID, EventTraceAnnotationQuery etaq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{

		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(etaq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();

			//TODO: need to check whether etaq.getTraceAnsVariable() is null or not before calling varMap.get
			//TODO: need to check whether etaq.getEventAnsVariable() is null or not before calling varMap.get
			addEventTraceMapping(sqlWithVarMap.getSQL(), eventUID,
						cleanURI(varMap.get(etaq.getTraceAnsVariable())), 
						cleanURI(varMap.get(etaq.getEventAnsVariable()))
				);

		}
	}
	
	private void addMapping(int eventUID, EventTimestampAnnotationQuery etaq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(etaq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			addEventTimestampMapping(sqlWithVarMap.getSQL(), eventUID,
						cleanURI(varMap.get(etaq.getEventAnsVariable())),
						cleanURI(varMap.get(etaq.getTimestampAnsVariable()))
				);
		}
	}

	private void addMapping(int eventUID, EventResourceAnnotationQuery eraq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(eraq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			addEventResourceMapping(
					sqlWithVarMap.getSQL(), eventUID,
					cleanURI(varMap.get(eraq.getEventAnsVariable())),
					cleanURI(varMap.get(eraq.getResourceAnsVariable()))
				);
		}

	}
	
	private void addMapping(int eventUID, EventLifecycleAnnotationQuery elaq, QuestOWLReasonerExt reasoner) 
			throws OWLException, OBDAException, MalformedQueryException{
		
		List<SQLWithVarMap> unfoldedQuery = reasoner.reformulateSPARQL2(elaq.getQuery());
		
		Iterator<SQLWithVarMap> it = unfoldedQuery.iterator(); 
		
		while(it.hasNext()){

			SQLWithVarMap sqlWithVarMap = it.next();
			HashMap<String, String> varMap = sqlWithVarMap.getVariableMap();
			
			addEventLifecycleMapping(sqlWithVarMap.getSQL(), eventUID,
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
	
	private void addTraceNameMapping(String sqlQuery, String traceVariable, String nameVariable){

		String targetQuery1 = 
			String.format(this.traceAttTargetQueryTemplate, 
				String.format(this.traceURITemplate, traceVariable),
				String.format(this.attTraceNameURITemplate, nameVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);
		
		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 	
				String.format(this.attTraceNameURITemplate, nameVariable), 								//att obj
				this.xesAttTypeLiteral, 																//type
				this.xesAttKeyConceptName, 																//key
				(isConstant(nameVariable)? String.format(constantTemplate, nameVariable): nameVariable)	//value
			);
		
		this.addMapping(sqlQuery, targetQuery2);
	}

	private void addEventNameMapping(String sqlQuery, int eventUID, String eventVariable, String nameVariable){
		
		String targetQuery1 = 
			String.format(this.eventAttTargetQueryTemplate, 
				String.format(this.eventURITemplate, eventUID, eventVariable),
				String.format(this.attEventNameURITemplate, nameVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);
		
		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attEventNameURITemplate, nameVariable), 				//att obj
				this.xesAttTypeLiteral, 																//type
				this.xesAttKeyConceptName, 																	//key
				(isConstant(nameVariable)? String.format(constantTemplate, nameVariable): nameVariable)	//value
			);

		this.addMapping(sqlQuery, targetQuery2);
	}

	private void addEventTraceMapping(String sqlQuery, int eventUID, String traceVariable, String eventVariable){
				
		//String targetQuery = this.createTraceEventTargetQuery("trace/{"+traceVariable+"}", "event/{"+eventVariable+"}");

		String targetQuery = 
				String.format(this.eventTraceTargetQueryTemplate, 
						String.format(this.traceURITemplate, traceVariable),
						String.format(this.eventURITemplate, eventUID, eventVariable)
				);

		this.addMapping(sqlQuery, targetQuery);
	}

	private void addEventTimestampMapping(String sqlQuery, int eventUID, String eventVariable, String timestampVariable) {

		String targetQuery1 = 
			String.format(this.eventAttTargetQueryTemplate, 
				String.format(this.eventURITemplate, eventUID, eventVariable),
				String.format(this.attEventTimestampURITemplate, timestampVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);

		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attEventTimestampURITemplate, timestampVariable), 					//att obj
				this.xesAttTypeTimestamp, 																				//type
				this.xesAttKeyTimeTimestamp, 																				//key
				(isConstant(timestampVariable)? String.format(constantTemplate, timestampVariable): timestampVariable)	//value
			);

		this.addMapping(sqlQuery, targetQuery2);
	}

	private void addEventResourceMapping(String sqlQuery, int eventUID, String eventVariable, String resourceVariable) {
		
		String targetQuery1 = 
			String.format(this.eventAttTargetQueryTemplate, 
				String.format(this.eventURITemplate, eventUID, eventVariable),
				String.format(this.attEventResourceURITemplate, resourceVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);
		
		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attEventResourceURITemplate, resourceVariable), 					//att obj
				this.xesAttTypeLiteral, 																			//type
				this.xesAttKeyOrgResource, 																			//key
				(isConstant(resourceVariable)? String.format(constantTemplate, resourceVariable): resourceVariable)	//value
			);

		this.addMapping(sqlQuery, targetQuery2);
	}

	private void addEventLifecycleMapping(String sqlQuery, int eventUID, String eventVariable, String lifecycleVariable) {

		String targetQuery1 = 
			String.format(this.eventAttTargetQueryTemplate, 
				String.format(this.eventURITemplate, eventUID, eventVariable),
				String.format(this.attEventLifecycleURITemplate, lifecycleVariable)						
			);

		this.addMapping(sqlQuery, targetQuery1);
		
		String targetQuery2 = 
			String.format(this.attDataPropertiesTargetQueryTemplate, 
				String.format(this.attEventLifecycleURITemplate, lifecycleVariable), 					//att obj
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


