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

package it.unibz.inf.kaos.logextractor.constants;

import it.unibz.inf.kaos.logextractor.constants.XESOntology;

/**
 * This class provides some constants declaration related to the XES Event Ontology
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class XESEOConstants {

	private static XESOntology eo = new XESOntologyOnProm();
	//private static XESOntology eo = new XESOntologyLili();

	//Event Ontology location
		public static String eventOntoPath = eo.getEventOntoPath();
	
	//Prefix for event ontology vocabularies that are defined for OnProm
		public static String eventOntoPrefixAbbr = eo.getEventOntoPrefixAbbr();
		public static String eventOntoPrefix = eo.getEventOntoPrefix();
	
	//OnProm event ontology vocabularies
		//some vocabularies for concept names/classes
		public static String LOG_CONCEPT = eo.getLogConcept();
		public static String TRACE_CONCEPT = eo.getTraceConcept();
		public static String EVENT_CONCEPT = eo.getEventConcept();
		public static String ATTRIBUTE_CONCEPT = eo.getAttributeConcept();
	
		//some vocabularies for role names/object properties
		public static String T_CONTAINS_E_ROLE = eo.getTraceContainsEventRole();
		public static String T_CONTAINS_A_ROLE = eo.getTraceContainsAttributeRole();
		public static String E_CONTAINS_A_ROLE = eo.getEventContainsAttributeRole();
	
		//some vocabularies for attribute/data properties
		public static String ATT_TYPE_ATT = eo.getAttTypeAtt();
		public static String ATT_KEY_ATT = eo.getAttKeyAtt();
		public static String ATT_VAL_ATT = eo.getAttValAtt();
	//END OF OnProm event ontology vocabularies

		
	public static void setEventOntologie(XESOntology eventOnto){
		XESEOConstants.eo = eventOnto;

		//Event Ontology location
			XESEOConstants.eventOntoPath = eo.getEventOntoPath();
		
		//Prefix for event ontology vocabularies that are defined for OnProm
			XESEOConstants.eventOntoPrefixAbbr = eo.getEventOntoPrefixAbbr();
			XESEOConstants.eventOntoPrefix = eo.getEventOntoPrefix();
		
		//OnProm event ontology vocabularies
			//some vocabularies for concept names/classes
			XESEOConstants.LOG_CONCEPT = eo.getLogConcept();
			XESEOConstants.TRACE_CONCEPT = eo.getTraceConcept();
			XESEOConstants.EVENT_CONCEPT = eo.getEventConcept();
			XESEOConstants.ATTRIBUTE_CONCEPT = eo.getAttributeConcept();
		
			//some vocabularies for role names/object properties
			XESEOConstants.T_CONTAINS_E_ROLE = eo.getTraceContainsEventRole();
			XESEOConstants.T_CONTAINS_A_ROLE = eo.getTraceContainsAttributeRole();
			XESEOConstants.E_CONTAINS_A_ROLE = eo.getEventContainsAttributeRole();
		
			//some vocabularies for attribute/data properties
			XESEOConstants.ATT_TYPE_ATT = eo.getAttTypeAtt();
			XESEOConstants.ATT_KEY_ATT = eo.getAttKeyAtt();
			XESEOConstants.ATT_VAL_ATT = eo.getAttValAtt();
		//END OF OnProm event ontology vocabularies
	}
		
	
	//Some queries over the event ontology for retrieving the required information for generating XES log 

		//====================================================================================================
		//query for retrieving  the information about the association between a trace and its events
		//
		// PREFIX : <http://www.example.org/>
		// SELECT Distinct ?trace ?event ?timestamp
		// WHERE {
		//   ?trace :TcontainsE ?event.  
		//   ?event :EcontainsA ?timestamp . 
		//	 ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampValue. 
		// }
		// ORDER BY ?trace ASC(?timestampValue)
			public static final String qTraceEvt_WithEventOrderingByTimestamp = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?t ?e ?v \n"+
				"WHERE { "
						+ "?t "+ T_CONTAINS_E_ROLE+" ?e . "
						+ "?e "+ E_CONTAINS_A_ROLE+" ?a . "
						+ "?a "+ATT_TYPE_ATT+" \"timestamp\"^^xsd:string; "+ 
								ATT_VAL_ATT+" ?v}"+
				"ORDER BY ?t ASC(?v)";
		//====================================================================================================

		//====================================================================================================
		// Query for retrieving  the information about the association between a trace and its attributes.
		//
		// PREFIX : <http://www.example.org/>
		// SELECT distinct ?trace ?att 
		// WHERE {
		//   ?trace :TcontainsA ?att .  
		// }
			public static final String qTraceAtt_Simple = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace ?att \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_A_ROLE+" ?att . "
					+ "}";
	
			public static final String qTraceAtt_SimpleAnsVarTrace = "trace";	
			public static final String qTraceAtt_SimpleAnsVarAtt = "att";	
		//====================================================================================================

		
		//====================================================================================================
		//Query for retrieving  the information about the association between an event and its attribute
		//
		// PREFIX : <http://www.example.org/>
		// SELECT Distinct ?event ?att 
		// WHERE {
		//   ?event :EcontainsA ?att . 
		// }
			public static final String qEvtAtt_Simple = 	
					"PREFIX : <"+eventOntoPrefix+"> \n" +
					"SELECT ?event ?att \n"+
					"WHERE { ?event "+ E_CONTAINS_A_ROLE+" ?att }";
			
			public static final String qEvtAtt_SimpleAnsVarEvent = "event";	
			public static final String qEvtAtt_SimpleAnsVarAtt = "att";	
		//====================================================================================================
			
		//some queries for retrieving log attributes' related information		

			//====================================================================================================
			//query for retrieving attributes
			public static final String qAtt = 	
					"PREFIX : <"+eventOntoPrefix+"> \n" +
					"SELECT ?att \n"+
					"WHERE { ?att a "+ATTRIBUTE_CONCEPT+"}";
			//====================================================================================================

			//====================================================================================================
			// - Query for retrieving attributes together with their type, value and key.
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?att ?attType ?attKey ?attValue 
			// WHERE {
			//   ?att a :Attribute; :typeA ?attType; :keyA ?attKey; :valueA ?attValue.
			// }
			public static final String qAttTypeKeyVal_Simple = 	
					"PREFIX : <"+eventOntoPrefix+"> \n" +
					"SELECT distinct ?att ?attType ?attKey ?attValue \n"+
					"WHERE { ?att a "+ATTRIBUTE_CONCEPT+"; "+ 
									ATT_TYPE_ATT+" ?attType; "+ 
									ATT_KEY_ATT+" ?attKey; "+ 
									ATT_VAL_ATT+" ?attValue  }";
			
			public static final String qAttTypeKeyVal_SimpleAnsVarAtt = "att";	
			public static final String qAttTypeKeyVal_SimpleAnsVarAttType = "attType";	
			public static final String qAttTypeKeyVal_SimpleAnsVarAttKey = "attKey";	
			public static final String qAttTypeKeyVal_SimpleAnsVarAttVal = "attValue";	
			//====================================================================================================
	
			//====================================================================================================
			// Query for retrieving the information about the association between attribute and its type
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?att ?attType 
			// WHERE {
			//   ?att :typeA ?attType .
			// }
			public static final String qAttType = 	
					"PREFIX : <"+eventOntoPrefix+"> \n" +
					"SELECT DISTINCT ?att ?attType \n"+
					"WHERE { ?att "+ ATT_TYPE_ATT+" ?attType }";
			public static final String qAttTypeAnsVarAtt = "att";	
			public static final String qAttTypeAnsVarAttType = "attType";	
			//====================================================================================================
	
			//====================================================================================================
			// Query for retrieving the information about the association between attribute and its key
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?att ?attKey 
			// WHERE {
			//   ?att :keyA ?attKey .
			// }
			public static final String qAttKey = 	
					"PREFIX : <"+eventOntoPrefix+"> \n" +
					"SELECT DISTINCT ?att ?attKey \n"+
					"WHERE { ?att "+ ATT_KEY_ATT+" ?attKey }";
			public static final String qAttKeyAnsVarAtt = "att";	
			public static final String qAttKeyAnsVarAttKey = "attKey";	
			//====================================================================================================
	
			//====================================================================================================
			// Query for retrieving the information about the association between attribute and its value
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?att ?attType ?attKey ?attValue 
			// WHERE {
			//   ?att :valueA ?attValue.
			// }
			public static final String qAttValue = 	
					"PREFIX : <"+eventOntoPrefix+"> \n" +
					"SELECT DISTINCT ?att ?attValue \n"+
					"WHERE { ?att "+ ATT_VAL_ATT+" ?attValue }";
			public static final String qAttValAnsVarAtt = "att";	
			public static final String qAttValAnsVarAttVal= "attValue";	
			//====================================================================================================
			
		//END OF some queries for retrieving log attributes' related information	
			
	//END OF Some queries for retrieving information

			
			
	/////////////////////////////////////////////////////////////////////////////////////////////
	//JANGAN DISUBMIT KE KAOS
	/////////////////////////////////////////////////////////////////////////////////////////////	

		/////////////////////////////////////////////////////////////////////////////////////////////
		//QUERIES FOR MATERIALIZATION APPROACH
		/////////////////////////////////////////////////////////////////////////////////////////////

			//====================================================================================================
			// - Query for retrieving the information about the association between traces, events, 
			//   and attributes. 
			// - This query only retrieves the event that has the mandatory attributes, namely: 
			//   timestamp, name, and lifecycle. 
			// - The results is first ordered by trace and then ordered by the timestamp of the event. 
			// - Note: This query hasn't retrieve trace's attributes
			//		
			// PREFIX : <http://www.example.org/>
			// SELECT ?trace ?event ?timestampVal ?att ?attKey ?attVal ?attType
			// WHERE{
			//   ?trace :TcontainsE ?event .
			//   ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampVal . 
			//   ?event :EcontainsA ?att . ?att :keyA ?attKey; :valueA ?attVal; :typeA ?attType .
			// }
			// ORDER BY ?trace ASC(?timestampVal)
			public static final String qTraceEvtAtt_ALL = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace ?event ?timestampValue ?att ?attType ?attKey ?attVal ?timestamp ?name ?lifecycle \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_E_ROLE+" ?event . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?timestamp . "
						+ "?timestamp "	+ ATT_KEY_ATT+" \"time:timestamp\"^^xsd:string; "
										+ ATT_VAL_ATT+" ?timestampValue . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?name . "
						+ "?name "+ATT_KEY_ATT+" \"concept:name\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?lifecycle . "
						+ "?lifecycle "+ATT_KEY_ATT+" \"lifecycle:transition\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?att . "
						+ "?att "+ATT_TYPE_ATT+" ?attType; "
								 +ATT_KEY_ATT+" ?attKey; "
								 +ATT_VAL_ATT+" ?attVal . "
					+ "} "
					+ "ORDER BY ?trace ASC(?timestampValue)";
			//====================================================================================================
			
			
			//====================================================================================================			
			// - Query for retrieving  the information about the association between an event and its attribute.
			// - Query for retrieving  the information about the association between an event and its attribute.
			// - This query only retrieves the event that is associated to a trace.
			// - This query only retrieves the event that has the mandatory attributes, namely: timestamp, 
			//   name, and lifecycle. 
			// - This query doesn't retrieves the 'value', 'type' and 'key' of each attribute.
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?event ?att 
			// WHERE {
			//   ?trace :TcontainsE ?event . 
			//   ?event :EcontainsA ?att . 
			//   ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string . 
			//   ?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
			//   ?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
			// }
			public static final String qEvtAtt_WithEventMandatoryAttributesCheck = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?event ?att \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_E_ROLE+" ?event . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?att . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?timestamp . "
						+ "?timestamp "+ATT_KEY_ATT+" \"time:timestamp\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?name . "
						+ "?name "+ATT_KEY_ATT+" \"concept:name\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?lifecycle . "
						+ "?lifecycle "+ATT_KEY_ATT+" \"lifecycle:transition\"^^xsd:string . "
					+ "}";

			public static final String qEvtAtt_WithEventMandatoryAttributesCheckAnsVarEvent = "event";	
			public static final String qEvtAtt_WithEventMandatoryAttributesCheckAnsVarAtt = "att";	
			//====================================================================================================
			
			//====================================================================================================
			// - Query for retrieving  the information about the association between an event and its attribute.
			// - This query only retrieves the event that is associated to a trace.
			// - This query only retrieves the event that has the mandatory attributes, namely: timestamp, 
			//   name, and lifecycle. 
			// - This query also retrieves the 'value', 'type' and 'key' of each attribute.
			// - The results are ordered by events
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?event ?att ?attKey ?attType ?attVal
			// WHERE {
			//   ?trace :TcontainsE ?event . 
			//   ?event :EcontainsA ?att . ?att :keyA ?attKey; :typeA ?attType; :valueA ?attVal. 
			//   ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string . 
			//   ?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
			//   ?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
			// }
			// ORDER BY ?event
			public static final String qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEvent = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?event ?att ?attKey ?attType ?attVal \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_E_ROLE+" ?event . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?att . "
					+ "?att "+ATT_TYPE_ATT+" ?attType; "+ATT_KEY_ATT+" ?attKey; "+ATT_VAL_ATT+" ?attVal . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?timestamp . "
						+ "?timestamp "+ATT_KEY_ATT+" \"time:timestamp\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?name . "
						+ "?name "+ATT_KEY_ATT+" \"concept:name\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?lifecycle . "
						+ "?lifecycle "+ATT_KEY_ATT+" \"lifecycle:transition\"^^xsd:string . "
					+ "} ORDER BY ?event ";

			public static final String qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarEvent = "event";	
			public static final String qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarAtt = "att";	
			public static final String qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarAttKey = "attKey";	
			public static final String qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarAttType = "attType";	
			public static final String qEvtAtt_WithEventMandatoryAttributesCheckAndOrderByEventAnsVarAttVal = "attVal";	
			//====================================================================================================

			//====================================================================================================			
			// This query is similar to "qEvtAtt3" above, except that 
			// - several checks are omitted (e.g., checks for mandatory attributes)
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?event ?att 
			// WHERE {
			//   ?event :EcontainsA ?att . 
			// } ORDER BY ?event
			public static final String qEvtAtt_WithOrderByEvent = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?event ?att \n"+
				"WHERE { "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?att . "
					+ "} ORDER BY ?event ";

			public static final String qEvtAtt_WithOrderByEventAnsVarEvent = "event";	
			public static final String qEvtAtt_WithOrderByEventAnsVarAtt = "att";	
			//====================================================================================================

			//====================================================================================================
			// - Query for retrieving  the information about the association between a trace and its events.
			// - This query only retrieves the event that has the mandatory attributes, namely: timestamp, 
			//   name, and lifecycle. 
			// - The results are first ordered by trace and then ordered by the timestamp of the event. 
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?trace ?event ?timestampValue
			// WHERE {
			//     ?trace :TcontainsE ?event . 
			//     ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampValue. 
			//     ?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
			//     ?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
			// }
			// ORDER BY ?trace ASC(?timestampValue)
			public static final String qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestamp = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace ?event ?timestampValue \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_E_ROLE+" ?event . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?timestamp . "
						+ "?timestamp "	+ ATT_KEY_ATT+" \"time:timestamp\"^^xsd:string; "
										+ ATT_VAL_ATT+" ?timestampValue . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?name . ?name "+ATT_KEY_ATT+" \"concept:name\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?lifecycle . ?lifecycle "+ATT_KEY_ATT+" \"lifecycle:transition\"^^xsd:string . "
					+ "} ORDER BY ?trace ASC(?timestampValue)";

			public static final String qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestampAnsVarTrace = "trace";	
			public static final String qTraceEvt_WithEventMandatoryAttributesCheckAndEventOrderingByTimestampAnsVarEvent = "event";	
			//====================================================================================================

			
			//====================================================================================================
			// - Similar to "qTraceEvt2" except that we remove the mandatory attribute check for the events
			//   and also the sorting of the events
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?trace ?event 
			// WHERE {
			//     ?trace :TcontainsE ?event . 
			// }
			public static final String qTraceEvt_Simple = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace ?event \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_E_ROLE+" ?event . "
					+ "}";
			
			public static final String qTraceEvt_SimpleAnsVarTrace = "trace";	
			public static final String qTraceEvt_SimpleAnsVarEvent = "event";	
			//====================================================================================================

			//====================================================================================================
			// - Similar to "qTraceEvt2" except that we remove the mandatory attribute check for the events
			// - The results are ordered by traces
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?trace ?event 
			// WHERE {
			//     ?trace :TcontainsE ?event . 
			// } ORDER BY ?trace
			public static final String qTraceEvt_OrderByTrace = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace ?event \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_E_ROLE+" ?event . "
					+ "} ORDER BY ?trace ";
			
			public static final String qTraceEvt_OrderByTraceAnsVarTrace = "trace";	
			public static final String qTraceEvt_OrderByTraceCAnsVarEvent = "event";	
			//====================================================================================================

			
			//====================================================================================================
			// - Query for retrieving  the information about the association between a trace and its events.
			// - This query only retrieves the event that has the mandatory attributes, namely: timestamp, 
			//   name, and lifecycle. 
			// - It also retrieve the traces that have no event(s) (by using OPTIONAL).
			// - The results are first ordered by trace and then ordered by the timestamp of the event. 
			//
			// - Note: Better not to use this while we are still using Ontop 1.18, because their implementation
			//   for OPTIONAL is "buggy".
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?trace ?event ?timestampValue
			// WHERE {
			// ?trace a :Trace . 
			//   OPTIONAL { 
			//     ?trace :TcontainsE ?event . 
			//     ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string; :valueA ?timestampValue. 
			//     ?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
			//     ?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
			//   }
			// }
			// ORDER BY ?trace ASC(?timestampValue)
			public static final String qTraceEvt_WithOptionalEvent = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace ?event ?timestampValue \n"+
				"WHERE { "
					+ "?trace  a "+TRACE_CONCEPT+" . "
					+ "OPTIONAL { "
					+ "?trace "+ T_CONTAINS_E_ROLE+" ?event . ?event a "+EVENT_CONCEPT+" . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?timestamp . "
						+ "?timestamp "	+ ATT_KEY_ATT+" \"time:timestamp\"^^xsd:string; "
										+ ATT_VAL_ATT+" ?timestampValue . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?name . ?name "+ATT_KEY_ATT+" \"concept:name\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?lifecycle . ?lifecycle "+ATT_KEY_ATT+" \"lifecycle:transition\"^^xsd:string . "
					+ "}"
					+ "} ORDER BY ?trace ASC(?timestampValue)";

			public static final String qTraceEvt_WithOptionalEventAnsVarTrace = "trace";	
			public static final String qTraceEvt_WithOptionalEventAnsVarEvent = "event";	
			//====================================================================================================

			
			//====================================================================================================
			// - Query for retrieving  the information about the association between a trace and its attributes.
			// - This query also retrieves the 'key', 'type' and 'value' of each attribute.
			// - The results are ordered by the 'trace'. 
			//
			// PREFIX : <http://www.example.org/>
			// SELECT distinct ?trace ?att ?attKey ?attType ?attVal
			// WHERE {
			//   ?trace :TcontainsA ?att . ?att :keyA ?attKey; :typeA ?attType; :valueA ?attVal. 
			// }
			// ORDER BY ?trace
			public static final String qTraceAtt_WithAttTypeKeyValAndOrderByTrace = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace ?att ?attKey ?attType ?attVal \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_A_ROLE+" ?att . "
					+ "?att "+ATT_TYPE_ATT+" ?attType; "+ATT_KEY_ATT+" ?attKey; "+ATT_VAL_ATT+" ?attVal . "
					+ "} ORDER BY ?trace ";

			public static final String qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarTrace = "trace";	
			public static final String qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarAtt = "att";	
			public static final String qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarAttKey = "attKey";	
			public static final String qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarAttType = "attType";	
			public static final String qTraceAtt_WithAttTypeKeyValAndOrderByTraceAnsVarAttVal = "attVal";	
			//====================================================================================================

			//====================================================================================================
			// - Query for retrieving  the information about the association between a trace and its attributes.
			// - The results are ordered by the 'trace'
			//
			// PREFIX : <http://www.example.org/>
			// SELECT distinct ?trace ?att 
			// WHERE {
			//   ?trace :TcontainsA ?att .  
			// }
			public static final String qTraceAtt_OrderByTrace = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace ?att \n"+
				"WHERE { "
					+ "?trace "+ T_CONTAINS_A_ROLE+" ?att . "
					+ "} ORDER BY ?trace";

			public static final String qTraceAtt_OrderByTraceAnsVarTrace = "trace";	
			public static final String qTraceAtt_OrderByTraceAnsVarAtt = "att";	
			//====================================================================================================
			
			//====================================================================================================
			// - Query for retrieving the traces
			//
			// PREFIX : <http://www.example.org/>
			// SELECT distinct ?trace 
			// WHERE {
			//   ?trace a :Trace . 
			// }
			public static final String qTrace_Simple = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace \n"+
				"WHERE { ?trace a "+ TRACE_CONCEPT+" . }";

			public static final String qTraceAnsVarTrace = "trace";	
			//====================================================================================================

		/////////////////////////////////////////////////////////////////////////////////////////////
		//END OF QUERIES FOR MATERIALIZATION APPROACH
		/////////////////////////////////////////////////////////////////////////////////////////////

			
			
			
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
			
			
			
			
		/////////////////////////////////////////////////////////////////////////////////////////////
		//QUERIES FOR VIRTUAL APPROACH
		/////////////////////////////////////////////////////////////////////////////////////////////

			//====================================================================================================
			// - Query for retrieving the traces
			// - The results are ordered by the traces URIs
			//
			// PREFIX : <http://www.example.org/>
			// SELECT distinct ?trace 
			// WHERE {
			//   ?trace a :Trace . 
			// }
			// ORDER BY ?trace
			public static final String qGetTraces = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?trace \n"+
				"WHERE { ?trace a "+ TRACE_CONCEPT+" . }"+ 
				"ORDER BY ?trace";

			public static final String qGetTraceAnsVarTrace = "trace";	
			//====================================================================================================

			
			//====================================================================================================
			// - Query for retrieving  the information about the events of the given trace.
			// - the '[%s]' is a parameter that should be substituted by the trace URI
			// - This query only retrieves the event that has the mandatory attributes, namely: timestamp, 
			//   name, and lifecycle. 
			// - The results are ordered by the timestamp of the events
			//
			// PREFIX : <http://www.example.org/>
			// SELECT Distinct ?event ?timestampvalue
			// WHERE {
			//   [%s] :TcontainsE ?event . 
			//   ?event :EcontainsA ?timestamp . ?timestamp :keyA "time:timestamp"^^xsd:string; 
			//						?timestamp :valueA ?timestampvalue . 
			//   ?event :EcontainsA ?name . ?name :keyA "concept:name"^^xsd:string . 
			//   ?event :EcontainsA ?lifecycle . ?lifecycle :keyA "lifecycle:transition"^^xsd:string . 
			// }
			// ORDER BY ASC(?timestampvalue)
			public static final String qGetEventsOfATrace = 	
				"PREFIX : <"+eventOntoPrefix+"> \n" +
				"SELECT Distinct ?event ?timestampValue \n"+
				"WHERE { "
					+ "<%s> "+ T_CONTAINS_E_ROLE+" ?event . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?timestamp . "
						+ "?timestamp "	+ ATT_KEY_ATT+" \"time:timestamp\"^^xsd:string; "
										+ ATT_VAL_ATT+" ?timestampValue . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?name . "
						+ "?name "+ATT_KEY_ATT+" \"concept:name\"^^xsd:string . "
					+ "?event "+ E_CONTAINS_A_ROLE+" ?lifecycle . "
						+ "?lifecycle "+ATT_KEY_ATT+" \"lifecycle:transition\"^^xsd:string . "
					+ "} "
					+ "ORDER BY ASC(?timestampValue)";

			public static final String qGetEventsOfATraceAnsVar = "event";	
			//====================================================================================================

			
		/////////////////////////////////////////////////////////////////////////////////////////////
		//END OF QUERIES FOR VIRTUAL APPROACH
		/////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////
	//END OF JANGAN DISUBMIT KE KAOS
	/////////////////////////////////////////////////////////////////////////////////////////////


}
