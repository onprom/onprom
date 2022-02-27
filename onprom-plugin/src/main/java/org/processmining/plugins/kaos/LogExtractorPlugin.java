/*
 * onprom-plugin
 *
 * LogExtractorPlugin.java
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
import it.unibz.inf.kaos.logextractor.EOToXESLogConverter;
import it.unibz.inf.kaos.logextractor.XOToXESLogConverter;
import it.unibz.inf.kaos.logextractor.xes.XESConstants;
import it.unibz.inf.kaos.logextractor.xes.XESLogExtractor;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Properties;

/**
 * Prom plug in for onprom XES log extraction
 *
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class LogExtractorPlugin {

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
    public XLog extractXESLogFromXO(final UIPluginContext context, OWLOntology ontology) {

        context.getProgress().setIndeterminate(true);

        XLog xlog = new XOToXESLogConverter().convertToXESLog(ontology);

        context.getFutureResult(0).setLabel("XES log");
        return xlog;
    }

    @Plugin(
            name = "Event Ontology Variant To XES event Log",
            parameterLabels = {"Materialized Event Ontology Variant"},
            returnLabels = {"XES Event Log"},
            returnTypes = {XLog.class},
            help = "A converter from materialized Event Onto Variant V into XES event log",
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
    public XLog extractXESLogFromEO(final UIPluginContext context, OWLOntology ontology) {

        context.getProgress().setIndeterminate(true);

        XLog xlog = new EOToXESLogConverter().convertToXESLog(ontology);

        context.getFutureResult(0).setLabel("XES log");
        return xlog;
    }

    @Plugin(
            name = "OnProM Log Extractor",
            parameterLabels = {"Domain Ontology", "OBDA Mapping", "Datasource Properties", "Annotation"},
            returnLabels = {"XES Event Log"},
            returnTypes = {XLog.class},
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
    @PluginVariant(requiredParameterLabels = {0, 1, 2, 3})
    public XLog extractXESLog(final UIPluginContext context,
                              OWLOntology ontology, SQLPPMapping obdaModel, Properties properties, AnnotationQueries annotationQueries) {

        XLog xlog = null;

        try {
            context.getProgress().setIndeterminate(true);
            xlog = new XESLogExtractor().extractLog(ontology, obdaModel, properties, annotationQueries);
            context.getFutureResult(0).setLabel("XES log extracted from DB");
        } catch (Exception e) {
            context.log(e);
        }

        context.getProgress().setIndeterminate(false);
        return xlog;
    }

    @Plugin(
            name = "OnProM Log Extractor (Out: Log & EBDA Model)",
            parameterLabels = {"Domain Ontology", "OBDA Mapping", "Datasource Properties", "Annotation"},
            returnLabels = {"XES Event Log", "EBDA Model"},
            returnTypes = {XLog.class, OBDAModel.class},
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
    @PluginVariant(requiredParameterLabels = {0, 1, 2, 3})
    public Object[] extractXESLog2(final UIPluginContext context,
                                   OWLOntology ontology, SQLPPMapping obdaModel, Properties datasourceProperties, AnnotationQueries annotationQueries) {

        context.getProgress().setIndeterminate(true);
        XLog xlog = null;
        SQLPPMapping ebdaMapping = null;

        try {
            ebdaMapping = new OBDAMapper(ontology,
                    XESConstants.getDefaultEventOntology(),
                    obdaModel,
                    datasourceProperties,
                    annotationQueries
            ).getOBDAModel();
            xlog = new XESLogExtractor().extractLog(ebdaMapping, datasourceProperties);
            context.getFutureResult(0).setLabel("XES log extracted from DB");
        } catch (Exception e) {
            context.log(e);
        }

        context.getProgress().setIndeterminate(false);
        return new Object[]{xlog, ebdaMapping};
    }

    @Plugin(
            name = "OnProM Log Extractor (Input: EBDA Model)",
            parameterLabels = {"EBDA Model", "Datasource Properties"},
            returnLabels = {"XES Event Log"},
            returnTypes = {XLog.class},
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
    @PluginVariant(requiredParameterLabels = {0, 1})
    public XLog extractXESLog(final UIPluginContext context, SQLPPMapping ebdaMapping, Properties datasourceProperties) {

        XLog xlog = null;

        try {
            context.getProgress().setIndeterminate(true);
            xlog = new XESLogExtractor().extractLog(ebdaMapping, datasourceProperties);
            context.getFutureResult(0).setLabel("XES log extracted from DB");
        } catch (Exception e) {
            context.log(e);
        }

        context.getProgress().setIndeterminate(false);
        return xlog;
    }

    @Plugin(
            name = "OnProM Log Extractor (with EO Variant)",
            parameterLabels = {"Domain Ontology",
                    "OBDA Mapping",
                    "Annotation To Event Ontology Variant",
                    "Event Ontology Variant",
                    "Annotation To XES Ontology"},
            returnLabels = {"XES Event Log"},
            returnTypes = {XLog.class},
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
    @PluginVariant(requiredParameterLabels = {0, 1, 2, 3, 4, 5})
    public XLog extractXESLog(final UIPluginContext context,
                              OWLOntology domainOnto,
                              SQLPPMapping obdaModel,
                              Properties datasourceProperties,
                              AnnotationQueries firstAnnoQueries,
                              OWLOntology eventOntoVariant,
                              AnnotationQueries secondAnnoQueries) {

        context.getProgress().setIndeterminate(true);

        XLog xlog = null;
        XESLogExtractor logExtractor = new XESLogExtractor();

        try {
            xlog = logExtractor.extractLog(
                    domainOnto, obdaModel, datasourceProperties, firstAnnoQueries, eventOntoVariant, secondAnnoQueries);

        } catch (Exception e) {
            context.log(e);
        }

        context.getFutureResult(0).setLabel("XES log extracted from DB");
        return xlog;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // END OF LOG EXTRACTOR PLUG IN that takes into account the event ontology variant
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
