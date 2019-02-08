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
package it.unibz.inf.kaos.logextractor.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
import it.unibz.inf.kaos.logextractor.model.XAtt;
import it.unibz.inf.kaos.logextractor.model.XAttributeOnProm;
import it.unibz.inf.kaos.logextractor.model.XEventOnPromEfficient;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class Print {

	public static StringBuilder logXAttributesExtraction(HashMap<String,XAttribute> attributes){

		StringBuilder info = 
			new StringBuilder(	"\n\n"+LEConstants.LOGGER_LONG_DOUBLE_LINE+
								"\nResults of XAttributes Extraction\n");
		info.append(getStringOfXAttributes(attributes));
		info.append("\nEND OF Results of XAttributes Extraction\n"+
					LEConstants.LOGGER_LONG_DOUBLE_LINE+"\n");
		
		return info;
	}
	
	public static StringBuilder getStringOfXAttributes(HashMap<String,XAttribute> attributes){

		StringBuilder info = new StringBuilder();

		for (Entry<String, XAttribute> xae : attributes.entrySet()) {
			XAttribute xa = xae.getValue();

			info.append("\tXAttribute URI: \t").append(xae.getKey()).append("\n\tXAttribute Type: \t").append(xa.getClass().getSimpleName()).append("\n\tXAttribute Key: \t").append(xa.getKey()).append("\n\tXAttribute value: \t").append(xa).append("\n");
		}
		
		return info;
	}

	public static StringBuilder getStringOfXAttributes(EfficientHashMap<XAtt> attributes){

        StringBuilder info = new StringBuilder();

        attributes.values().forEach(xa -> {
            info.append("\tXAttribute Type: \t").append(xa.getClass().getSimpleName()).append("\n\tXAttribute Key: \t").append(xa.getKey()).append("\n\tXAttribute value: \t").append(xa).append("\n");
        });
		return info;
	}

	public static StringBuilder getStringOfXAttributeOnProm(HashMap<String,XAttributeOnProm> attributes){

		StringBuilder info = new StringBuilder();

		for (Entry<String, XAttributeOnProm> xae : attributes.entrySet()) {
			XAttribute xa = null;
			try {
				xa = xae.getValue().toXESXAttribute();
			} catch (UnsupportedAttributeTypeException e) {
				e.printStackTrace();
			}

			if (xa != null) {
				info.append("\tXAttribute URI: \t").append(xae.getKey()).append("\n\tXAttribute Type: \t").append(xa.getClass().getSimpleName()).append("\n\tXAttribute Key: \t").append(xa.getKey()).append("\n\tXAttribute value: \t").append(xa).append("\n");
			}
		}
		
		return info;
	}

	public static StringBuilder logXEventsExtraction(HashMap<String, XEvent> events){

		StringBuilder info = 
			new StringBuilder(	"\n\n"+LEConstants.LOGGER_LONG_DOUBLE_LINE+"\n"+
								"Results of XEvents Extraction\n");

		info.append(getStringOfXEvents(events));

		info.append("\nEND OF Results of XEvents Extraction\n"+
					LEConstants.LOGGER_LONG_DOUBLE_LINE+"\n");

		return info;
	}

	public static <T extends XEvent> StringBuilder getStringOfXEvents(HashMap<String, T> events){

		Iterator<Entry<String, T>> evit = events.entrySet().iterator();
		StringBuilder info = new StringBuilder();
		
		while(evit.hasNext()){
			Entry<String, T> xee = evit.next();
			XEvent xe = xee.getValue();
			
			info.append(LEConstants.LOGGER_LINE+"\n");
			info.append("XEvent URI: \t").append(xee.getKey()).append("\n");

			//handling event's attributes
			XAttributeMap xattmap = xe.getAttributes();
			info.append(getStringOfXAttributeMap("\t", xattmap));
			//END OF handling event's attributes
		}
		
		return 	info;
	}

	public static StringBuilder getStringOfXEvents(EfficientHashMap<XEventOnPromEfficient> events){

		StringBuilder info = new StringBuilder();

        events.values().forEach(xe -> {
			info.append(LEConstants.LOGGER_LINE+"\n");
			info.append("XEvent: \t").append(xe).append("\n");

			//handling event's attributes
            info.append(getStringOfXAttributeMap("\t", xe.getAttributes()));
			//END OF handling event's attributes

        });
		
		return 	info;
	}

	public static StringBuilder logXTracesExtraction(HashMap<String,XTrace> traces){

		StringBuilder info = new StringBuilder(	
					"\n\n"+LEConstants.LOGGER_LONG_DOUBLE_LINE+
					"\nResults of XTraces Extraction\n");

		info.append(getStringOfXTraces(traces));
		
		info.append("\nEND OF Results of XTraces Extraction\n"+
					LEConstants.LOGGER_LONG_DOUBLE_LINE+"\n");

		return info;
	}
	
	public static StringBuilder getStringOfXTraces(HashMap<String,XTrace> traces){

		Iterator<Entry<String, XTrace>> trit = traces.entrySet().iterator();
		StringBuilder info = new StringBuilder("");
		
		while(trit.hasNext()){
			Entry<String, XTrace> xte = trit.next();
			XTrace xt = xte.getValue();
			
			info.append(LEConstants.LOGGER_LINE+"\n");
			info.append("XTrace URI: \t"+xte.getKey()+"\n");

			//handling trace's attributes
			XAttributeMap xattmap = xt.getAttributes();
			info.append(getStringOfXAttributeMap("\t", xattmap));
			//END OF handling trace's attributes
			
			//iterate over trace's events
			for (XEvent xe : xt) {
				info.append("\t" + LEConstants.LOGGER_LINE + "\n");
				info.append("\tXEvent: ").append(xe.toString()).append("\n");

				//handling event's attributes
				info.append(getStringOfXAttributeMap("\t\t", xe.getAttributes()));
			}
			//END OF iterate over trace's events
		}
		
		return info;
	}

	public static StringBuilder getStringOfXTraces(EfficientHashMap<XTrace> traces){

		StringBuilder info = new StringBuilder("");

        traces.values().forEach(xt -> {
			info.append(LEConstants.LOGGER_LINE+"\n");
			info.append("XTrace: \t"+xt+"\n");

			//handling trace's attributes
			XAttributeMap xattmap = xt.getAttributes();
			info.append(getStringOfXAttributeMap("\t", xattmap));
			//END OF handling trace's attributes

			//iterate over trace's events
			for(int ii = 0; ii < xt.size(); ii++){
				XEvent xe = xt.get(ii);
				info.append("\t"+LEConstants.LOGGER_LINE+"\n");
				info.append("\tXEvent: "+xe.toString()+"\n");

				//handling event's attributes
				info.append(getStringOfXAttributeMap("\t\t",xe.getAttributes()));
			}
			//END OF iterate over trace's events

        });
		return info;
	}

	public static StringBuilder getStringOfXAttributeMap(String tab, XAttributeMap xattmap){
		
		Iterator<Entry<String, XAttribute>> atit = xattmap.entrySet().iterator();

		StringBuilder info = new StringBuilder("");
		
		while(atit.hasNext()){
			Entry<String, XAttribute> xae = atit.next();
			XAttribute xa = xae.getValue();
			
			info.append(tab+LEConstants.LOGGER_LINE+"\n");
			info.append(tab+"XAttribute URI: \t"+xae.getKey()+
			"\n"+tab+"XAttribute Type: \t"+xa.getClass().getSimpleName()+
			"\n"+tab+"XAttribute Key: \t"+xa.getKey()+
			"\n"+tab+"XAttribute value: \t"+xa+"\n");
		}

		return info;
	}

}
