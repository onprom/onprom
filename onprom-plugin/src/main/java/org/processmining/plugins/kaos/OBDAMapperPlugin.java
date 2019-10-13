/*
 *
 * Copyright (c) 2017 Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
package org.processmining.plugins.kaos;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
import it.unibz.inf.ontop.model.OBDAModel;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class OBDAMapperPlugin {


    @Plugin(
            name = "OnProM - OBDA Mapper",
            parameterLabels = {"Source Ontology", "Target Ontology", "Source OBDAModel", "Annotation Queries"},
            returnLabels = {"OBDA Mapping"},
            returnTypes = {OBDAMapping.class},
            help = "OBDA Mapper - Generate an OBDA Mapping from the given data source into the target ontology",
            quality = PluginQuality.VeryGood,
            categories = {PluginCategory.Analytics}
    )
    @UITopiaVariant(
            affiliation = "Free University of Bozen-Bolzano and University of Innsbruck",
            author = "OnProm team",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {0, 1, 2, 3})
    public OBDAMapping createOBDAMapping(final UIPluginContext context,
                                         OWLOntology sourceOnto, OWLOntology targetOnto, OBDAModel sourceObdaModel, AnnotationQueries annotationQueries) {

        context.getProgress().setIndeterminate(true);

        OBDAMapping obdaMapping = null;
        try {

            obdaMapping = new OBDAMapper().createOBDAMapping(sourceOnto, targetOnto, sourceObdaModel, annotationQueries);

        } catch (Exception e) {
            context.log(e);
        }

        context.getFutureResult(0).setLabel("OBDA Mapping");

        return obdaMapping;
    }

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
