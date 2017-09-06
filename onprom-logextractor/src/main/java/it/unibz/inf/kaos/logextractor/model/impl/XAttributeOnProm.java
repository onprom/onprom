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


import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;

import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class XAttributeOnProm {

	private static final long serialVersionUID = -3582382124674042825L;
	private String uri;
	private String key;
	private String type;
	private String value;
	private XExtension xext;
	private XAttribute xatt;
	
	public Object clone(){
		//TODO: need to be implemented
		return null; 
	}
	
	XAttributeOnProm(){
	
		this(null);
	}

	XAttributeOnProm(String uri){
		
		this.uri = uri;
		this.key = null;
		this.type = null;
		this.value = null;
		this.xext = null;
		this.xatt = null;
	}

	public boolean hasCompleteInfo(){
		
		if(this.uri != null && this.key != null && this.type != null && this.value != null)
			return true;

		return false;
	}

	public XAttribute toXESXAttribute() throws UnsupportedAttributeTypeException{
		
		if(this.xatt == null)
			this.xatt = XFactoryOnProm.getInstance().createXAttribute(this.type, this.key, this.value, this.xext);
		
		return this.xatt;
	}
	
	/////////////////////////////////////////////////////////////////
	// JUST SETTER AND GETTER
	/////////////////////////////////////////////////////////////////
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		
		//---------------------------------------------------
		//BEGIN: arsa - changes on 2017.07.20
		
		/*
		if(LEConstants.xesconstants.containsKey(key))
			this.key = LEConstants.xesconstants.get(key);
		else
			this.key = key;
		*/
		
		this.key = key.intern();
		
		//END: arsa - changes on 2017.07.20
		//---------------------------------------------------
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public XExtension getXext() {
		return xext;
	}
	public void setXext(XExtension xext) {
		this.xext = xext;
	}
	/////////////////////////////////////////////////////////////////
	// END OF JUST SETTER AND GETTER
	/////////////////////////////////////////////////////////////////

	public String toString(){
		return this.value;
	}
}
