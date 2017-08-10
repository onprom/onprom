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
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;

import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.XExtension;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class XLogOnProm extends XLogImpl {


	private static final long serialVersionUID = 6474966388430800057L;
	private List<XAttribute> globaEventAtt;
	private List<XAttribute> globaTraceAtt;
	private List<XEventClassifier> classifier;
	private Set<XExtension> defaultExtensionSet;
	
	// if the following flag is true, then we use all OnProm assumptions on XES log content
	private boolean useOnPromDefaultMetaData = false;
	
	XLogOnProm() {
		super(XFactoryOnProm.getInstance().createAttributeMap());
		this.useOnPromDefaultMetaData = false;
		this.init();
	}

	XLogOnProm(boolean useOnPromDefaultMetaData){

		super(XFactoryOnProm.getInstance().createAttributeMap());
		this.useOnPromDefaultMetaData = useOnPromDefaultMetaData;
		this.init();
	}

//	XLogOnProm(XAttributeMap attributeMap) {
//		super(attributeMap);
//		this.useOnPromDefaultMetaData = false;
//		this.init();
//	}

	private void init(){
		
		this.globaTraceAtt = new ArrayList<XAttribute>();
		this.globaEventAtt = new ArrayList<XAttribute>();
		this.classifier = new ArrayList<XEventClassifier>();
		this.defaultExtensionSet = new HashSet<XExtension>();
		
		if(useOnPromDefaultMetaData)
			addDefaultXLogProperties();
	}

	private void addDefaultXLogProperties(){

		XFactoryOnProm xfact = XFactoryOnProm.getInstance();

		//Adding Trace/Event Global Attribute information and also classifier
		try {
			//create trace concept:name global attribute
			XAttribute traceConceptNameAtt = xfact.createGlobalConceptNameAttribute();

			//create event concept:name global attribute
			XAttribute eventConceptNameAtt = xfact.createGlobalConceptNameAttribute();
			
			//create event time:timestamp global attribute
			XAttribute timeStampAtt = xfact.createGlobalTimeStampAttribute();

			//create event lifecycle:transition global attribute
			XAttribute lifecycleAtt = xfact.createGlobalLifecycleTransitionAttribute();
			
			//adding event global attribute 'time:timestamp' and time:timestamp classifier
			if(timeStampAtt != null){
				this.addGlobalEventAttribute(timeStampAtt);
				this.addEventClassifier(new XEventTimeStampClassifier());
			}
			
			//adding event global attribute 'lifecycle:transition' and lifecycle:transition classifier
			if(lifecycleAtt != null) {
				this.addGlobalEventAttribute(lifecycleAtt);
				this.addEventClassifier(new XEventLifeTransClassifier());
			}
			
			//adding event global attribute 'concept:name' and concept:name classifier
			if(eventConceptNameAtt != null) {
				this.addGlobalEventAttribute(eventConceptNameAtt);
				this.addEventClassifier(new XEventNameClassifier());
			}
			
			//adding trace global attribute 'concept:name' and concept:name classifier
			if(traceConceptNameAtt != null) {
				this.addGlobalTraceAttribute(traceConceptNameAtt);
			}
			
		} catch (UnsupportedAttributeTypeException e) {
			e.printStackTrace();
		}
		//END OF Adding Trace/Event Global Attribute information and also classifier

		//adding default extensions
		this.defaultExtensionSet.add(xfact.getPredefinedXExtension("time:timestamp"));
		this.defaultExtensionSet.add(xfact.getPredefinedXExtension("concept:name"));
		this.defaultExtensionSet.add(xfact.getPredefinedXExtension("lifecycle:transition"));
		//end of adding default extensions
	}

	public Set<XExtension> getExtensions(){

		if(this.useOnPromDefaultMetaData)
			return defaultExtensionSet;
		
		Set<XExtension> extensionSet = new HashSet<XExtension>();
		
		for(int ii = 0; ii < this.size(); ii++){
			
			XTrace xt = this.get(ii);
			
			Iterator<XAttribute> itat = xt.getAttributes().values().iterator();
			while(itat.hasNext()){
				XAttribute xa = itat.next();
				if(xa.getExtension() != null)
					extensionSet.add(xa.getExtension());
			}

			Iterator<XEvent> ite = xt.iterator();
			while(ite.hasNext()){
				XEvent xe = ite.next();

				Iterator<XAttribute> ita = xe.getAttributes().values().iterator();
				while(ita.hasNext()){
					XAttribute xa = ita.next();
					if(xa.getExtension() != null)
						extensionSet.add(xa.getExtension());
				}
			}
		}
		
		return extensionSet;

	}
	
	public List<XAttribute> getGlobalEventAttributes(){
		return globaEventAtt;
	}

	public List<XAttribute> getGlobalTraceAttributes(){
		return globaTraceAtt;
	}

	public void addGlobalTraceAttribute(XAttribute att){
		
		if(!globaTraceAtt.contains(att))
			this.globaTraceAtt.add(att);
	}

	public void addGlobalEventAttribute(XAttribute att){
		
		if(!globaEventAtt.contains(att))
			this.globaEventAtt.add(att);
	}

	public List<XEventClassifier> getClassifiers(){
		return classifier;
	}

	public void addEventClassifier(XEventClassifier xEventClassifier){
		if(!this.classifier.contains(xEventClassifier))
			this.classifier.add(xEventClassifier);
	}
	
}

