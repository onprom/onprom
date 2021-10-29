/*
 * onprom-plugin
 *
 * OBDAMapperPlugin.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
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

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.OBDAMaterializer;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Properties;

public class OBDAMapperPlugin {


    @Plugin(
            name = "OnProM - OBDA Mapper",
            parameterLabels = {"Source Ontology", "Target Ontology", "Source OBDAModel", "Datasource Properties", "Annotation Queries"},
            returnLabels = {"OBDA Model"},
            returnTypes = {OBDAModel.class},
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
    @PluginVariant(requiredParameterLabels = {0, 1, 2, 3, 4})
    public SQLPPMapping createOBDAMapping(final UIPluginContext context,
                                       OWLOntology sourceOnto,
                                       OWLOntology targetOnto,
                                          SQLPPMapping sourceObdaModel,
                                       Properties datasourceProperties,
                                       AnnotationQueries annotationQueries) {

        context.getProgress().setIndeterminate(true);

        SQLPPMapping obdaMapping = null;
        try {

            obdaMapping = new OBDAMapper(sourceOnto,
                    targetOnto,
                    sourceObdaModel,
                    datasourceProperties,
                    annotationQueries
            ).getOBDAModel();

        } catch (Exception e) {
            context.log(e);
        }

        context.getFutureResult(0).setLabel("OBDA Mapping");

        return obdaMapping;
    }

    @Plugin(
            name = "OnProM - OBDA Materializer",
            parameterLabels = {"Target Ontology", "OBDA Model", "Datasource Properties"},
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
    @PluginVariant(requiredParameterLabels = {0, 1, 2})
    public OWLOntology createOBDAMapping(final UIPluginContext context, OWLOntology targetOntology, SQLPPMapping obdaMapping, Properties datasourceProperties) {

        context.getProgress().setIndeterminate(true);

        OWLOntology materializedOnto = null;

        try {
            materializedOnto = OBDAMaterializer.getMaterializedOWLOntology(targetOntology, obdaMapping, datasourceProperties);
        } catch (Exception e) {
            context.log(e);
        }

        context.getFutureResult(0).setLabel("Materialized Ontology");

        return materializedOnto;
    }
}
