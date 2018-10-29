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

package it.unibz.inf.kaos.logextractor.model;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class XEventOnPromEfficient extends XEventImpl{

	private boolean hasName;
	private boolean hasTimestamp;
	private boolean hasLifeCycle;
		
	XEventOnPromEfficient(){
		super();
		this.hasLifeCycle = false;
		this.hasName = false;
		this.hasTimestamp = false;
	}
	
	public boolean hasAllMandatoryAttributes(){
		return (hasTimestamp && hasName && hasLifeCycle);
	}

	public void addXAttribute(XAttribute xatt){
		
		String key = xatt.getKey();
		
		switch(key){
		
			case LEConstants.XES_ATT_KEY_CONCEPT_NAME:	
				if(xatt instanceof XAttributeLiteralImpl)
					this.hasName = true;
			break;
			
			case LEConstants.XES_ATT_KEY_TIME_TIMESTAMP:	
				
				if(xatt instanceof XAttributeTimestampImpl)
					this.hasTimestamp = true;
					
			break;
			
			case LEConstants.XES_ATT_KEY_LIFECYCLE_TRANSITION:	
				if(xatt instanceof XAttributeLiteralImpl)
					this.hasLifeCycle = true;
			break;

		}	
		
		this.getAttributes().put(key.intern(), xatt);
	}
}
