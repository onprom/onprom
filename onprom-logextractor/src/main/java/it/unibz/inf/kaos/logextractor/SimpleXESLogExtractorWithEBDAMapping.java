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

package it.unibz.inf.kaos.logextractor;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.model.EBDAMapping;
import it.unibz.inf.kaos.logextractor.model.LEObjectFactory;
import it.unibz.inf.kaos.logextractor.reasoner.SimpleEBDAReasonerImpl;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xeslite.lite.factory.XFactoryLiteImpl;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleXESLogExtractorWithEBDAMapping {
    private static final Logger logger = LoggerFactory.getLogger(SimpleXESLogExtractorWithEBDAMapping.class);

    public XLog extractXESLog(OWLOntology domainOnto, OBDAModel obdaModel, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) {
        return extractXESLog(domainOnto, obdaModel, firstAnnoQueries, eventOntoVariant, secondAnnoQueries, null);
    }

    public XLog extractXESLog(OWLOntology domainOnto, OBDAModel obdaModel, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries, XFactory factory) {
        try {
            OBDAMapping obdaMapping = new OBDAMapper().createOBDAMapping(domainOnto, eventOntoVariant, obdaModel, firstAnnoQueries);
            if (obdaMapping != null) {
                return extractXESLog(eventOntoVariant, obdaMapping, secondAnnoQueries, factory);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public XLog extractXESLog(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueries annotation) {
        return extractXESLog(domainOntology, obdaModel, annotation, null);
    }

    public XLog extractXESLog(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueries annotation, XFactory factory) {
        try {
            EBDAMapping ebdaModel = createEBDAMapping(domainOntology, obdaModel, annotation);
            if (ebdaModel != null) {
                if (factory == null) {
                    factory = new XFactoryLiteImpl();
                }
                logger.info("Factory in use: " + factory.getDescription());
                XFactoryRegistry.instance().setCurrentDefault(factory);

                logger.info("Start extracting XES Log from the EBDA Mapping");
                long start = System.currentTimeMillis();
                SimpleEBDAReasonerImpl ebdaR = new SimpleEBDAReasonerImpl(ebdaModel, factory);
                if (ebdaR.printUnfoldedQueries()) {
                    logger.info("Initialized reasoner in " + (System.currentTimeMillis() - start) + " ms");
                    Map<String, XAttribute> attributes = ebdaR.getAttributes();
                    Map<String, XEvent> events = ebdaR.getEvents(attributes);
                    Collection<XTrace> traces = ebdaR.getTraces(events, attributes);
                    ebdaR.dispose();
                    XLog xlog = factory.createLog();
                    addDefaultExtensions(factory, xlog);
                    xlog.addAll(traces);
                    return xlog;
                } else {
                    logger.error("Can't unfold queries, something is wrong, please check logs");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private EBDAMapping createEBDAMapping(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueries annotation) {
        try {
            List<OBDADataSource> odsList = obdaModel.getSources();
            if (odsList.size() == 1) {
                logger.info("Constructing EBDA Mapping");
                return LEObjectFactory.getInstance().createEBDAMapping(domainOntology, obdaModel, annotation);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void addDefaultExtensions(XFactory factory, XLog xlog) {
        try {
            xlog.getGlobalTraceAttributes().add(factory.createAttributeLiteral("concept:name", "DEFAULT", null));

            xlog.getGlobalEventAttributes().add(factory.createAttributeTimestamp("time:timestamp", Timestamp.valueOf("1970-01-01 01:00:00").getTime(), null));
            xlog.getGlobalEventAttributes().add(factory.createAttributeLiteral("lifecycle:transition", "complete", null));
            xlog.getGlobalEventAttributes().add(factory.createAttributeLiteral("concept:name", "DEFAULT", null));

            xlog.getClassifiers().add(new XEventAttributeClassifier("Time timestamp", "time:timestamp"));
            xlog.getClassifiers().add(new XEventLifeTransClassifier());
            xlog.getClassifiers().add(new XEventNameClassifier());
            xlog.getClassifiers().add(new XEventResourceClassifier());

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}