/*
 * 
 * Copyright (c) 2017 Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 * 
 */

package org.processmining.plugins.kaos;

import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
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
public class OBDAMaterializerPlugin {

	
	@Plugin(
	        name = "OnProM - OBDA Materializer", 
	        parameterLabels = {"OBDAMapping"},
			returnLabels = {"Materialized Ontology"},
			returnTypes = {OWLOntology.class},
			help = "OBDA Materializer - Materialize the given OBDA System into an OWL file",
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
    public OWLOntology createOBDAMapping(final UIPluginContext context, OBDAMapping obdaMapping) {

        context.getProgress().setIndeterminate(true);

		OWLOntology materializedOnto = null;
		
		try {
			materializedOnto = new OBDAMapper().materializeTargetOntology(obdaMapping);
		} catch (Exception e) {
			context.log(e);
		}

        context.getFutureResult(0).setLabel("Materialized Ontology");

    	return materializedOnto; 
    }
}
