/*
 * onprom-plugin
 *
 * LogExtractionPlugin.java
 *
 * Copyright (C) 2016-2017 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 *  KAOS: Knowledge-Aware Operational Support project
 *  (https://kaos.inf.unibz.it).
 *
 *  Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.processmining.plugins.kaos;

import org.deckfour.xes.model.XLog;
import org.openrdf.query.MalformedQueryException;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.XESLogExtractor;
import it.unibz.inf.kaos.logextractor.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.logextractor.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.logextractor.exception.XESLogExtractionFailureException;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;

/**
 * 
 * Prom plug in for onprom XES log extraction
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class LogExtractorPlugin {
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// STANDARD LOG EXTRACTOR PLUG IN
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Plugin(
	        name = "OnProM Log Extractor", 
	        parameterLabels = {"Domain Ontology", "OBDA Mapping", "Annotation"}, 
	        returnLabels = { "XES Event Log" }, 
	        returnTypes = { XLog.class }, 
	        userAccessible = true, 
	    	help = "OnProM Log Extractor - Extract XES event logs from relational databases based on OnProm Methodology (see http://onprom.inf.unibz.it)",
	        quality = PluginQuality.VeryGood, 
	        categories = {PluginCategory.Analytics}
	)
    @UITopiaVariant(
            affiliation = "Free University of Bozen-Bolzano and University of Innsbruck",
            author = "OnProm team",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {0,1,2})
    public XLog extractXESLog(final UIPluginContext context, 
    		OWLOntology ontology, OBDAModel obdaModel, AnnotationQueries annotationQueries) {

        context.getProgress().setIndeterminate(true);
		XLog xlog = null;
		XESLogExtractor logExtractor = new XESLogExtractor();
		
		try {
			xlog = logExtractor.extractXESLog(ontology, obdaModel, annotationQueries);
			
		} catch (InvalidDataSourcesNumberException | InvalidAnnotationException | OWLException | 
				XESLogExtractionFailureException | OBDAException | MalformedQueryException e) {
			e.printStackTrace();
            context.log("Couldn't extract log from database: " + e.getMessage());
		}
		
		//logExtractor.printExecutionNote();
        context.getFutureResult(0).setLabel("XES log extracted from DB");
    	return xlog; 
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// END OF STANDARD LOG EXTRACTOR PLUG IN
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LOG EXTRACTOR PLUG IN VARIANT - Produce XES Log and EBDAModel
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Plugin(
	        name = "OnProM Log Extractor (Out: Log & EBDA Model)", 
	        parameterLabels = {"Domain Ontology", "OBDA Mapping", "Annotation"}, 
	        returnLabels = { "XES Event Log", "EBDA Model" }, 
	        returnTypes = { XLog.class, EBDAModel.class }, 
	        userAccessible = true, 
	    	help = "OnProM Log Extractor - Extract XES event logs from relational databases based on OnProm Methodology (see http://onprom.inf.unibz.it)",
	        quality = PluginQuality.VeryGood, 
	        categories = {PluginCategory.Analytics}
	)
    @UITopiaVariant(
            affiliation = "Free University of Bozen-Bolzano and University of Innsbruck",
            author = "OnProm team",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {0,1,2})
    public Object[] extractXESLog2(final UIPluginContext context, 
    		OWLOntology ontology, OBDAModel obdaModel, AnnotationQueries annotationQueries) {

        context.getProgress().setIndeterminate(true);
		XESLogExtractor logExtractor = new XESLogExtractor();
		XLog xlog = null;
		EBDAModel ebdaModel = null;
		
		try {
			ebdaModel = logExtractor.createEBDAModelImpl3(ontology, obdaModel, annotationQueries);
			xlog = logExtractor.extractXESLog(ebdaModel);
			
		} catch (InvalidDataSourcesNumberException | InvalidAnnotationException | OWLException | 
				XESLogExtractionFailureException | OBDAException | MalformedQueryException e) {
			e.printStackTrace();
            context.log("Couldn't extract log from database: " + e.getMessage());
		}
    	
		logExtractor.printExecutionNote();
        context.getFutureResult(0).setLabel("XES log extracted from DB");

    	return new Object[]{xlog, ebdaModel}; 
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// END OF LOG EXTRACTOR PLUG IN VARIANT - Produce XES Log and EBDAModel
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LOG EXTRACTOR PLUG IN VARIANT - Takes EBDAModel as the input
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Plugin(
	        name = "OnProM Log Extractor (Input: EBDA Model)", 
	        parameterLabels = {"EBDA Model"}, 
	        returnLabels = { "XES Event Log"}, 
	        returnTypes = { XLog.class}, 
	        userAccessible = true, 
	    	help = "OnProM Log Extractor - Extract XES event logs from relational databases based on OnProm Methodology (see http://onprom.inf.unibz.it)",
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
    public XLog extractXESLog3(final UIPluginContext context, EBDAModel ebdaModel) {

        context.getProgress().setIndeterminate(true);
		XESLogExtractor logExtractor = new XESLogExtractor();
		XLog xlog = null;
		
		try {
			xlog = logExtractor.extractXESLog(ebdaModel);
			
		} catch (XESLogExtractionFailureException | OBDAException e) {
			e.printStackTrace();
            context.log("Couldn't extract log from database: " + e.getMessage());
		}

		//logExtractor.printExecutionNote();
        context.getFutureResult(0).setLabel("XES log extracted from DB");
    	return xlog; 
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// END OF LOG EXTRACTOR PLUG IN VARIANT - Takes EBDAModel as the input
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
