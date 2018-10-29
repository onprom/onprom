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

import java.util.Objects;
import java.util.Set;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class XAttLiteralEfficient extends XAttributeLiteralImpl implements XAtt {

	private String key;
	private XExtension extension;
	private boolean hasValue;
	private boolean hasKey;

	private static final long serialVersionUID = -237574273147293391L;

	XAttLiteralEfficient() {
		super(null, "");
		this.extension = null;
		this.hasKey = false;
		this.hasValue = false;
	}
	
	@Override
	public void setVal(String val) throws IllegalArgumentException{

		this.hasValue = true;
		super.setValue(val.intern());
	}

	@Override
	public void setValue(String val) throws IllegalArgumentException{

		this.hasValue = true;
		super.setValue(val.intern());
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
		
		return (this.hasKey && this.hasValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getKey(), getValue());
	}

	@Override
	public String toString() {
		return super.getValue();
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

	@Override
	public void setUri(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUri() {
		throw new UnsupportedOperationException();
	}

}
