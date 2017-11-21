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

package org.processmining.plugins.kaos;

import it.unibz.inf.kaos.logextractor.XESOntoToXESLogConverter;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class XESOntoToXESLogConverterPlugin {

	
	@Plugin(
	        name = "XES Event Onto To XES Log", 
	        parameterLabels = {"Materialized XES Event Ontology"},
            returnLabels = {"XES Event Log"},
            returnTypes = {XLog.class},
            help = "A converter from materialized XES event ontology into XES event log",
	        quality = PluginQuality.VeryGood, 
	        categories = {PluginCategory.Analytics}
	)
    @UITopiaVariant(
            affiliation = "Free University of Bozen-Bolzano and University of Innsbruck",
            author = "OnProm team",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {0})
    public XLog extractXESLog(final UIPluginContext context, OWLOntology ontology) {

        context.getProgress().setIndeterminate(true);

		XLog xlog = new XESOntoToXESLogConverter().convertToXESLog(ontology);

        context.getFutureResult(0).setLabel("XES log");
    	return xlog; 
    }
}
