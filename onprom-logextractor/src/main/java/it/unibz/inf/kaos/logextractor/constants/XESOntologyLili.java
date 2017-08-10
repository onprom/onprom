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

/**
 * This class provides some constants declaration related to the Event Ontology that was 
 * made by Alifah Syamsiyah for her master thesis 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class XESOntologyLili implements XESOntology{

	//Event Ontology location
		private static final String eventOntoPath = "/_EVENTONTO/eo1.owl";
	
	//Prefix for event ontology vocabularies that are defined for OnProm
		private static final String eventOntoPrefixAbbr = ":";
		private static final String eventOntoPrefix = "http://www.example.org/";
	
	//OnProm event ontology vocabularies
		//some vocabularies for concept names/classes
		private static final String LOG_CONCEPT = "<"+eventOntoPrefix + "Log"+">";
		private static final String TRACE_CONCEPT = "<"+eventOntoPrefix + "Trace"+">";
		private static final String EVENT_CONCEPT = "<"+eventOntoPrefix + "Event"+">";
		private static final String ATTRIBUTE_CONCEPT = "<"+eventOntoPrefix + "Attribute"+">";
	
		//some vocabularies for role names/object properties
		private static final String T_CONTAINS_E_ROLE = "<"+eventOntoPrefix + "TcontainsE" +">";
		private static final String T_CONTAINS_A_ROLE = "<"+eventOntoPrefix + "TcontainsA" +">";
		private static final String E_CONTAINS_A_ROLE = "<"+eventOntoPrefix + "EcontainsA" +">";
	
		//some vocabularies for attribute/data properties
		private static final String ATT_TYPE_ATT = "<"+eventOntoPrefix + "typeA" +">";
		private static final String ATT_KEY_ATT = "<"+eventOntoPrefix + "keyA" +">";
		private static final String ATT_VAL_ATT = "<"+eventOntoPrefix + "valueA" +">";
	//END OF OnProm event ontology vocabularies
	
		
		
	@Override 
	public String getEventOntoPath() {
		return eventOntoPath;
	}
	
	@Override 
	public String getEventOntoPrefixAbbr() {
		return eventOntoPrefixAbbr;
	}
	
	@Override 
	public String getEventOntoPrefix() {
		return eventOntoPrefix;
	}
	
	@Override 
	public String getLogConcept() {
		return LOG_CONCEPT;
	}
	
	@Override 
	public String getTraceConcept() {
		return TRACE_CONCEPT;
	}
	
	@Override 
	public String getEventConcept() {
		return EVENT_CONCEPT;
	}
	
	@Override 
	public String getAttributeConcept() {
		return ATTRIBUTE_CONCEPT;
	}
	
	@Override 
	public String getTContainsERole() {
		return T_CONTAINS_E_ROLE;
	}
	
	@Override 
	public String getTContainsARole() {
		return T_CONTAINS_A_ROLE;
	}
	
	@Override 
	public String getEContainsARole() {
		return E_CONTAINS_A_ROLE;
	}
	
	@Override 
	public String getAttTypeAtt() {
		return ATT_TYPE_ATT;
	}
	
	@Override 
	public String getAttKeyAtt() {
		return ATT_KEY_ATT;
	}
	
	@Override 
	public String getAttValAtt() {
		return ATT_VAL_ATT;
	}
	


}
