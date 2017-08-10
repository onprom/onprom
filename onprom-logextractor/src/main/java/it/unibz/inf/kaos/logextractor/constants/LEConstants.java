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

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class LEConstants {

	//Logger related constants
		public static final String LOGGER_LINE= "---------------------------------------------";
		public static final String LOGGER_DOUBLE_LINE= "=============================================";
		public static final String LOGGER_LONG_DOUBLE_LINE= LOGGER_DOUBLE_LINE+LOGGER_DOUBLE_LINE;
		public static final String LOGGER_NAME= "LOG_EXTRACTOR_LOGGER";
		public static final String LOG_PREFIX = "ONPROM";
		public static final String LOG_TEMPLATE = LOG_PREFIX+": \n\t%s";
		public static final String LOG_INFO_TEMPLATE = LOG_PREFIX+"-INFO: \n\n\t%s\n";
		public static final String LOG_ERROR_TEMPLATE = LOG_PREFIX+"-ERROR: \n\t%s";
		public static final String LOG_LOG_TEMPLATE = LOG_PREFIX+"-LOG: \n\t%s";
	//END OF Logger related constants

		
	//Some standard message template
		public static final String UNSUPPORTED_XATTRIBUTE_TYPE_TRACE_ATT = 
			"\tThe attribute type '%s' is unsupported. "
			+ "\n\t\tCorresponding Trace: '%s'."
			+ "\n\t\tCorresponding Attribute Key: '%s'."
			+ "\n\t\tCorresponding Attribute Value: '%s'.\n";

		public static final String UNSUPPORTED_XATTRIBUTE_TYPE_EVENT_ATT = 
				"\tThe attribute type '%s' is unsupported. "
				+ "\n\t\tCorresponding Event: '%s'."
				+ "\n\t\tCorresponding Attribute Key: '%s'."
				+ "\n\t\tCorresponding Attribute Value: '%s'.\n";

		public static final String UNSUPPORTED_XATTRIBUTE_TYPE2 = 
				"\tThe attribute type '%s' is unsupported. "
				+ "\n\t\tCorresponding Attribute Key: '%s'."
				+ "\n\t\tCorresponding Attribute Value: '%s'.\n";

		public static final String UNSUPPORTED_XATTRIBUTE_TYPE = 
				"\tThe attribute type '%s' is unsupported. \n";

		public static final String ATT_CREATION_FAILURE_TRACE_ATT = 
				  "\tAttribute creation failure "
				+ "\n\t\tCorresponding Trace: '%s'."
				+ "\n\t\tCorresponding Attribute Type: '%s'."
				+ "\n\t\tCorresponding Attribute Key: '%s'."
				+ "\n\t\tCorresponding Attribute Value: '%s'.\n";

		public static final String ATT_CREATION_FAILURE_EVENT_ATT = 
				  "\tAttribute creation failure "
				+ "\n\t\tCorresponding Event: '%s'."
				+ "\n\t\tCorresponding Attribute Type: '%s'."
				+ "\n\t\tCorresponding Attribute Key: '%s'."
				+ "\n\t\tCorresponding Attribute Value: '%s'.\n";

		public static final String ATT_CREATION_FAILURE = 
				  "\tAttribute creation failure "
				+ "\n\t\tCorresponding Attribute URI: '%s'.\n";
		
		public static final String ATT_CREATION_FAILURE2 = 
				  "\tAttribute creation failure "
				+ "\n\t\tCorresponding Attribute Type: '%s'."
				+ "\n\t\tCorresponding Attribute Key: '%s'."
				+ "\n\t\tCorresponding Attribute Value: '%s'.\n";

		public static final String ATT_MULTIPLE_PROPERTIES = 
			"\tthe attribute '%s' might have either multiple key, values or types.\n"
			+ "\t\tExisting attribute key: %s; type: %s; value: %s.\n"
			+ "\t\tAnother attribute key: %s; type: %s; value: %s.\n"
			+ "\t\tNote: in this case, we ignore the other new information.\n";

		public static final String EVENT_CREATION_FAILURE = 
				"\tFail to create event '%s'\n";

		public static final String EVENT_XATTMAP_CREATION_FAILURE = 
				"\tFail to create the XAttributeMap for the event '%s'\n"; 

		public static final String EVENT_MISS_ATT = 
			"\tThe event '%s' should contain the attribute '%s', \n\t\t"+ 
			"however, we miss the information about this attribute (perhaps the given information about \n\t\t"+
			"all of the attributes doesn't contain any information about this attribute).\n";

		public static final String TRACE_CREATION_FAILURE = 
				"\tFail to create the trace '%s'\n"; 

		public static final String TRACE_MISSING = 
				"\tMissing the trace '%s'\n"; 

		public static final String TRACE_MISS_EVENT = 
			"\tThe trace '%s' should contain the event '%s', \n\t\t"+ 
			"however, we miss the information about this event (perhaps the given information about \n"+
			"all of the events doesn't contain any information about this event).\n";

		public static final String TRACE_MISS_ATT = 
			"\tThe trace '%s' should contain the attribute '%s', \n\t\t"+ 
			"however, we miss the information about this attribute (perhaps the given information about \n"+
			"all of the attributes doesn't contain any information about this attribute).\n";

		public static final String TRACE_XATTMAP_CREATION_FAILURE = 
				"\tFail to create the XAttributeMap for the trace '%s'\n"; 

	//END OF Some standard message template

	//Some standard exception message
		public static final String MSG_EBDA_CONSTRUCTION_FAILURE = "Failed to construct the EBDA Model\n";
		public static final String MSG_ATTRIBUTES_RETRIEVAL_FAILURE = "Failed to retrieve attributes information\n";
		public static final String MSG_EVENTS_RETRIEVAL_FAILURE = "Failed to retrieve events information\n";
		public static final String MSG_TRACES_RETRIEVAL_FAILURE = "Failed to retrive traces information\n";
		public static final String MSG_LOG_CREATION_FAILURE = "Failed to create a XES Log\n";
	//END OF Some standard exception message
		
	//Some XES Standard related constants
		public static final String XES_ATT_KEY_CONCEPT_NAME = "concept:name";
		public static final String XES_ATT_KEY_TIME_TIMESTAMP = "time:timestamp";
		public static final String XES_ATT_KEY_LIFECYCLE_TRANSITION = "lifecycle:transition";
		public static final String XES_ATT_KEY_ORG_RESOURCE = "org:resource";

		public static HashMap<String, String> xesconstants = new HashMap<String,String>();
		
		static{
			xesconstants.put("concept:name", LEConstants.XES_ATT_KEY_CONCEPT_NAME);
			xesconstants.put("time:timestamp", LEConstants.XES_ATT_KEY_TIME_TIMESTAMP);
			xesconstants.put("lifecycle:transition", LEConstants.XES_ATT_KEY_LIFECYCLE_TRANSITION);
			xesconstants.put("org:resource", LEConstants.XES_ATT_KEY_ORG_RESOURCE);
		}

		
		private static final String COLON_SPECIAL_REP = "c#-/0#-=/l=#o=N#";
//		private static final String COLON_SPECIAL_REP = "c0l0N";

		public static final String XES_ATT_KEY_CONCEPT_NAME_SPECIAL_REP = "concept"+COLON_SPECIAL_REP+"name";
		public static final String XES_ATT_KEY_TIME_TIMESTAMP_SPECIAL_REP = "time"+COLON_SPECIAL_REP+"timestamp";
		public static final String XES_ATT_KEY_LIFECYCLE_TRANSITION_SPECIAL_REP = "lifecycle"+COLON_SPECIAL_REP+"transition";
		public static final String XES_ATT_KEY_ORG_RESOURCE_SPECIAL_REP = "org:resource";
		
	//END OF Some XES Standard related constants

	//Some URI related constants
//		public static final String SPECIAL_URI_DELIMETER = "#";
//		public static final String SPECIAL_URI_DELIMETER = "#@$&+$#+@";
		public static final String SPECIAL_URI_DELIMETER = "#D#3#-/#--/l#1#m#-/#--/e#=-/-=/T#3#r=#";
		
		public static final String SPECIAL_DELIMETER = "\u00B6\u00AE\u00C6";
	//END OF Some URI related constants		
	
	//SPECIAL STRING MAPPER
		//it might be needed to deal with unsupported URI characters
		
		/*
		private static HashMap<String, String> specialStringMap = new HashMap<String,String>();
		
		static{
			specialStringMap.put(LEConstants.XES_ATT_KEY_CONCEPT_NAME_SPECIAL_REP, LEConstants.XES_ATT_KEY_CONCEPT_NAME);
			specialStringMap.put(LEConstants.XES_ATT_KEY_LIFECYCLE_TRANSITION_SPECIAL_REP, LEConstants.XES_ATT_KEY_LIFECYCLE_TRANSITION);
			specialStringMap.put(LEConstants.XES_ATT_KEY_ORG_RESOURCE_SPECIAL_REP, LEConstants.XES_ATT_KEY_ORG_RESOURCE);
			specialStringMap.put(LEConstants.XES_ATT_KEY_TIME_TIMESTAMP_SPECIAL_REP, LEConstants.XES_ATT_KEY_TIME_TIMESTAMP);
		}
		*/

		private static ImmutableMap<String, String> specialStringMap = 
				new ImmutableMap.Builder<String, String>()
		           .put(LEConstants.XES_ATT_KEY_CONCEPT_NAME_SPECIAL_REP, LEConstants.XES_ATT_KEY_CONCEPT_NAME)
		           .put(LEConstants.XES_ATT_KEY_LIFECYCLE_TRANSITION_SPECIAL_REP, LEConstants.XES_ATT_KEY_LIFECYCLE_TRANSITION)
		           .put(LEConstants.XES_ATT_KEY_ORG_RESOURCE_SPECIAL_REP, LEConstants.XES_ATT_KEY_ORG_RESOURCE)
		           .put(LEConstants.XES_ATT_KEY_TIME_TIMESTAMP_SPECIAL_REP, LEConstants.XES_ATT_KEY_TIME_TIMESTAMP)
		           .build();

		public static String mapSpecialString(String str){
			return specialStringMap.get(str);
		}
		
	//END OF SPECIAL STRING MAPPER
		
}
