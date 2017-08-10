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

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeMapLazyImpl;
import org.deckfour.xes.model.impl.XTraceImpl;

import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeException;
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
import it.unibz.inf.kaos.logextractor.model.XAtt;

//import it.unibz.inf.kaos.logextractor.reasoner.impl.EBDAReasonerImplForVirtual;
//import it.unibz.inf.kaos.virtualxes.naiveimpl.ImmutableVirtualXEvent;
//import it.unibz.inf.kaos.virtualxes.naiveimpl.ImmutableVirtualXLog;
//import it.unibz.inf.kaos.virtualxes.naiveimpl.ImmutableVirtualXTrace;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class XFactoryOnProm extends XFactoryNaiveImpl{ //XFactoryBufferedImpl{

	private static XFactoryOnProm xfact;	
	private static XExtension timeExt = XTimeExtension.instance();
	private static XExtension conceptExt = XConceptExtension.instance();
	private static XExtension lifecycleExt = XLifecycleExtension.instance();
	private static XExtension organizationalExt = XOrganizationalExtension.instance();
	
	/*
	 * The purpose of having a single constructor in which its 
	 * access modifier is set to private is to prevent 
	 * an instantiation of this class from another class.
	 */
	private XFactoryOnProm() {
	}

	public static XFactoryOnProm getInstance() {
		if (xfact == null) {
			xfact = new XFactoryOnProm();
		}
		return xfact;
	}	

	public XAttribute createXAttribute(String type, String key, String value, XExtension xext) throws UnsupportedAttributeTypeException {

		if(type == null) 
			throw new UnsupportedAttributeTypeException(type, key, value);

		if(key == null || value == null) 
			return null;
			
		XAttribute att;
		
		String typeLC = type.toLowerCase();
		
		try{
			switch(typeLC){
			
				case "literal":
					att = xfact.createAttributeLiteral(key, value, xext);
					break;
	
				case "timestamp":
					/*
					 * Note: based on the the method "public static Timestamp valueOf(String s)" in "java.sql.Timestamp"
					 * we assume that the timestamp is in format yyyy-[m]m-[d]d hh:mm:ss[.f...]. 
					 * The fractional seconds may be omitted. The leading zero for mm and dd may also be omitted.
					 */
					att = xfact.createAttributeTimestamp(key, Timestamp.valueOf(value).getTime(), xext);
					break;
					
				default: 
					throw new UnsupportedAttributeTypeException(type, key, value);
			}
		}catch(IllegalArgumentException iae){
			iae.printStackTrace();
			return null;
		}
		
		return att;
	}

	public XAtt createXAtt(String type, String uri) throws UnsupportedAttributeTypeException {

		if(type == null) 
			throw new UnsupportedAttributeTypeException(type);

		if(uri == null) return null;
			
		XAtt att;
		
		String typeLC = type.toLowerCase();

		switch(typeLC){
		
			case "literal":
				att = new XAttLiteral(uri);
				break;

			case "timestamp":
				att = new XAttTimestamp(uri);
				break;
				
			default: 
				throw new UnsupportedAttributeTypeException(type);
		}
		
		return att;
	}

	public XAtt createXAttNoURI(String type) throws UnsupportedAttributeTypeException {

		if(type == null) 
			throw new UnsupportedAttributeTypeException(type);

		XAtt att;
		
		String typeLC = type.toLowerCase();

		switch(typeLC){
		
			case "literal":
				att = new XAttLiteralEfficient();
				break;

			case "timestamp":
				att = new XAttTimestampEfficient();
				break;
				
			default: 
				throw new UnsupportedAttributeTypeException(type);
		}
		
		return att;
	}

	public XAttribute getXAttribute(HashMap<String,XAttribute> attributesMap, String attURI) 
			throws UnsupportedAttributeTypeException, UnsupportedAttributeException {

		
		XAttribute att = attributesMap.get(attURI);
		
		if(att != null) {
			return att;
		}
		
		String attURICopy = attURI.replace(LEConstants.SPECIAL_URI_DELIMETER, LEConstants.SPECIAL_DELIMETER);
		StringTokenizer strtok = new StringTokenizer(attURICopy, LEConstants.SPECIAL_DELIMETER);

		if(strtok.countTokens() != 5)
			throw new UnsupportedAttributeException(attURI);
			
		strtok.nextToken();
		String type = strtok.nextToken();

		String keyToken = strtok.nextToken(); 
		String keyTemp = LEConstants.mapSpecialString(keyToken);
		String key = (keyTemp == null ? keyToken : keyTemp);

		String value = strtok.nextToken();

		XExtension xext = this.getPredefinedXExtension(key);

		if(type == null) 
			throw new UnsupportedAttributeTypeException(type, key, value);

		if(key == null || value == null) 
			return null;
			
		String typeLC = type.toLowerCase();
		
		switch(typeLC){
		
			case "literal":
				att = xfact.createAttributeLiteral(key, value, xext);
				break;

			case "timestamp":
				/*
				 * Note: based on the the method "public static Timestamp valueOf(String s)" in "java.sql.Timestamp"
				 * we assume that the timestamp is in format yyyy-[m]m-[d]d hh:mm:ss[.f...]. 
				 * The fractional seconds may be omitted. The leading zero for mm and dd may also be omitted.
				 */
				att = xfact.createAttributeTimestamp(key, Timestamp.valueOf(value).getTime(), xext);
				break;
				
			default: 
				throw new UnsupportedAttributeTypeException(type, key, value);
		}
		
		attributesMap.put(attURI, att);
		
		return att;
	}

	public synchronized XAttribute getXAttribute(Hashtable<String,XAttribute> attributesMap, String attURI) 
			throws UnsupportedAttributeTypeException, UnsupportedAttributeException {

		XAttribute att = attributesMap.get(attURI);
		
		if(att != null) 
			return att;
		
		String attURICopy = attURI.replace(LEConstants.SPECIAL_URI_DELIMETER, LEConstants.SPECIAL_DELIMETER);
		StringTokenizer strtok = new StringTokenizer(attURICopy, LEConstants.SPECIAL_DELIMETER);
		
		if(strtok.countTokens() != 5)
			throw new UnsupportedAttributeException(attURI);
			
		strtok.nextToken();
		String type = strtok.nextToken();
		
		String keyToken = strtok.nextToken(); 
		String keyTemp = LEConstants.mapSpecialString(keyToken);
		String key = (keyTemp == null ? keyToken : keyTemp);

		String value = strtok.nextToken();

		XExtension xext = this.getPredefinedXExtension(key);

		if(type == null) 
			throw new UnsupportedAttributeTypeException(type, key, value);

		if(key == null || value == null) 
			return null;
			
		String typeLC = type.toLowerCase();
		
		switch(typeLC){
		
			case "literal":
				att = xfact.createAttributeLiteral(key, value, xext);
				break;

			case "timestamp":
				/*
				 * Note: based on the the method "public static Timestamp valueOf(String s)" in "java.sql.Timestamp"
				 * we assume that the timestamp is in format yyyy-[m]m-[d]d hh:mm:ss[.f...]. 
				 * The fractional seconds may be omitted. The leading zero for mm and dd may also be omitted.
				 */
				att = xfact.createAttributeTimestamp(key, Timestamp.valueOf(value).getTime(), xext);
				break;
				
			default: 
				throw new UnsupportedAttributeTypeException(type, key, value);
		}
		
		attributesMap.put(attURI, att);
		
		return att;
	}
	
	public XExtension createPredefinedXExtension(String key){

		if(key == null) 
			return null;

		XExtension xext = null;
		String keyLC = key.toLowerCase();

		switch(keyLC){
		
			case "time:timestamp": 
				xext = XTimeExtension.instance(); break;
			case "concept:name": 
				xext = XConceptExtension.instance(); break;
			case "lifecycle:transition": 
				xext = XLifecycleExtension.instance(); break;
			case "org:resource": 
				xext = XOrganizationalExtension.instance(); break;
			default: xext = null;
		}
		
		return xext;
	}

	public XExtension getPredefinedXExtension(String key){

		if(key == null) return null;

		String keyLC = key.toLowerCase();

		switch(keyLC){
		
			case "time:timestamp": return XFactoryOnProm.timeExt;
				
			case "concept:name": return XFactoryOnProm.conceptExt;

			case "lifecycle:transition": return XFactoryOnProm.lifecycleExt;
			
			case "org:resource": return XFactoryOnProm.organizationalExt;
			
			default: return null;
		}
	}

	public XLogOnProm createXLogOnProm(){

		return new XLogOnProm();
	}

	public XLogOnProm createXLogOnProm(boolean useOnPromDefaultMetaData){

		return new XLogOnProm(useOnPromDefaultMetaData);
	}

	public XAttribute createGlobalConceptNameAttribute() throws UnsupportedAttributeTypeException{
		return createXAttribute("literal", "concept:name", "DEFAULT", null);
	}

	public XAttribute createGlobalTimeStampAttribute() throws UnsupportedAttributeTypeException{
		return createXAttribute("timestamp", "time:timestamp", "1970-01-01 01:00:00", null);
	}

	public XAttribute createGlobalLifecycleTransitionAttribute() throws UnsupportedAttributeTypeException{
		return createXAttribute("literal", "lifecycle:transition", "complete", null);
	}

	public XAttributeOnProm createXAttributeOnProm(){
		
		return new XAttributeOnProm();
	}

	public XAttributeOnProm createXAttributeOnProm(String uri){
		
		return new XAttributeOnProm(uri);
	}

	public XAttributeOnProm2 createXAttributeOnProm2(String uri){
		
		return new XAttributeOnProm2(uri);
	}

	public XTrace createXTraceNaiveImpl() {
		
		//System.out.println("haha");
		
		return new XTraceImpl(new XAttributeMapLazyImpl<XAttributeMapImpl>(XAttributeMapImpl.class));
	}
	
	public XEventOnProm createXEventOnProm(String URI){

		return new XEventOnProm(URI);
	}

	public XEventOnPromEfficient createXEventOnPromNoURI(){

		return new XEventOnPromEfficient();
	}

	public XAttLiteral createXAttLiteral(String uri){
		return new XAttLiteral(uri);
	}
	
}
