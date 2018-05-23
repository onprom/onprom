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
package it.unibz.inf.kaos.logextractor.exception;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class UnsupportedAttributeTypeException extends Exception{

	private static final long serialVersionUID = -1893052774453001372L;
	private String attType = null;
	private String attKey = null;
	private String attValue = null;
	
	public UnsupportedAttributeTypeException(String attType, String attKey, String attValue){
		this.attType = attType;
		this.attKey = attKey;
		this.attValue = attValue;
	}

	public UnsupportedAttributeTypeException(String attType){
		this.attType = attType;
		this.attKey = "";
		this.attValue = "";
	}

	public String getAttType(){
		return attType;
	}
	
	public String getAttKey() {
		return attKey;
	}

	public String getAttValue() {
		return attValue;
	}

	@Override
	public String getMessage() {
		//TODO still need to implement more informative message		 
		
		if(attType != null)
			return "Unsupported/Invalid XES Log attribute type: "+attType;
		
		return "Unsupported/Invalid XES Log attribute type";
	}
}