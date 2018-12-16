package it.unibz.inf.kaos.obdamapper.constants;

import java.util.HashMap;

public class OMConstants {

	//Logger related constants
		public static final String LOGGER_LINE= "---------------------------------------------";
		public static final String LOGGER_DOUBLE_LINE= "=============================================";
		public static final String LOGGER_LONG_DOUBLE_LINE= LOGGER_DOUBLE_LINE+LOGGER_DOUBLE_LINE;
		public static final String LOGGER_NAME= "ONTOLOGY_MAPPER_LOGGER";
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

    public static HashMap<String, String> xesconstants = new HashMap<>();
		
}
