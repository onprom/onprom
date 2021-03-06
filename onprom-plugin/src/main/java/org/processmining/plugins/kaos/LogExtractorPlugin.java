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

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.EOToXESLogConverter;
import it.unibz.inf.kaos.logextractor.SimpleXESLogExtractor;
import it.unibz.inf.kaos.logextractor.XOToXESLogConverter;
import it.unibz.inf.ontop.model.OBDAModel;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.semanticweb.owlapi.model.OWLOntology;

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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // STANDARD LOG EXTRACTOR PLUG IN
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Plugin(
            name = "OnProM Log Extractor",
            parameterLabels = {"Domain Ontology", "OBDA Mapping", "Annotation"},
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
    @PluginVariant(requiredParameterLabels = {0, 1, 2})
    public XLog extractXESLog(final UIPluginContext context,
                              OWLOntology ontology, OBDAModel obdaModel, AnnotationQueries annotationQueries) {

        context.getProgress().setIndeterminate(true);
        XLog xlog = null;
        SimpleXESLogExtractor logExtractor = new SimpleXESLogExtractor();

        try {
            xlog = logExtractor.extractXESLog(ontology, obdaModel, annotationQueries);

        } catch (Exception e) {
            context.log(e);
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
    @PluginVariant(requiredParameterLabels = {0, 1, 2})
    public Object[] extractXESLog2(final UIPluginContext context,
                                   OWLOntology ontology, OBDAModel obdaModel, AnnotationQueries annotationQueries) {

        context.getProgress().setIndeterminate(true);
        SimpleXESLogExtractor logExtractor = new SimpleXESLogExtractor();
        XLog xlog = null;
        OBDAModel ebdaMapping = null;

        try {
            ebdaMapping = logExtractor.createEBDAMapping(ontology, obdaModel, annotationQueries);
            xlog = logExtractor.extractXESLog(ebdaMapping);

        } catch (Exception e) {
            context.log(e);
        }

        context.getFutureResult(0).setLabel("XES log extracted from DB");

        return new Object[]{xlog, ebdaMapping};
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
    @PluginVariant(requiredParameterLabels = {0})
    public XLog extractXESLog(final UIPluginContext context, OBDAModel ebdaMapping) {

        context.getProgress().setIndeterminate(true);
        SimpleXESLogExtractor logExtractor = new SimpleXESLogExtractor();
        XLog xlog = null;

        try {
            xlog = logExtractor.extractXESLog(ebdaMapping);

        } catch (Exception e) {
            context.log(e);
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LOG EXTRACTOR PLUG IN that takes into account the event ontology variant
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
    @PluginVariant(requiredParameterLabels = {0, 1, 2, 3, 4})
    public XLog extractXESLog(final UIPluginContext context,
                              OWLOntology domainOnto, OBDAModel obdaModel, AnnotationQueries firstAnnoQueries,
                              OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) {

        context.getProgress().setIndeterminate(true);

        XLog xlog = null;
        SimpleXESLogExtractor logExtractor = new SimpleXESLogExtractor();

        try {
            xlog = logExtractor.extractXESLog(
                    domainOnto, obdaModel, firstAnnoQueries, eventOntoVariant, secondAnnoQueries);

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
