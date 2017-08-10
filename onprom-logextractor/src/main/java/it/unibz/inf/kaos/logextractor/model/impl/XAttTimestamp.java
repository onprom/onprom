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
import java.util.Date;
import java.util.Set;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

import it.unibz.inf.kaos.logextractor.model.XAtt;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class XAttTimestamp extends XAttributeTimestampImpl implements XAtt {

	private String uri;
	private String key;
	private XExtension extension;
	private boolean hasValue;
	private boolean hasKey;

	private static final long serialVersionUID = -5156275276563730290L;

	XAttTimestamp(String uri) {
		super(null, 1l);
		this.uri = uri.intern();
		this.extension = null;
		this.hasKey = false;
		this.hasValue = false;
	}

	@Override
	public void setVal(String val) throws IllegalArgumentException {

		try{
			super.setValueMillis(Timestamp.valueOf(val).getTime());
			
			this.hasValue = true;
			
		}catch(IllegalArgumentException iae){
			throw iae;
		}

	}

	@Override
	public void setValue(Date val) throws IllegalArgumentException {
		super.setValue(val);
		this.hasValue = true;
	}

	@Override
	public void setValueMillis(long val) throws IllegalArgumentException {
		super.setValueMillis(val);
		this.hasValue = true;
	}

	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@Override
	public void setKey(String key) {
		this.key = key.intern();
		this.hasKey = true;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public boolean hasCompleteInfo() {

		return (this.uri != null && this.hasKey && this.hasValue);
	}

	public int hashCode() {
		//return Objects.hash(getKey(), getValue());
		return this.uri.hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/////////////////////////////////////////////////////////
	// RELATED TO EXTENSION
	/////////////////////////////////////////////////////////
	
	@Override
	public XExtension getExtension() {
		return extension;
	}
	
	@Override
	public void setExtension(XExtension extension){
		this.extension = extension;
	}

	@Override
	public Set<XExtension> getExtensions() {
		Set<XExtension> exts = super.getExtensions();
		exts.add(this.extension);
		return exts;
	}
	
	/////////////////////////////////////////////////////////
	// END OF RELATED TO EXTENSION
	/////////////////////////////////////////////////////////
}
