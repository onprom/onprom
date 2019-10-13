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
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.model.OBDAMappingImpl;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleXESLogExtractor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleXESLogExtractor.class);

    public XLog extractXESLog(OWLOntology domainOnto, OBDAModel obdaModel, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) {
        try {
            OBDAModel obdaMapping = new OBDAMapper().createOBDAMapping(domainOnto, eventOntoVariant, obdaModel, firstAnnoQueries);
            if (obdaMapping != null) {
                return extractXESLog(eventOntoVariant, obdaMapping, secondAnnoQueries);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public XLog extractXESLog(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueries annotation) {
        try {
            OBDAModel ebdaModel = createEBDAMapping(domainOntology, obdaModel, annotation);
            XLog xlog = extractXESLog(ebdaModel);
            if (xlog != null) return xlog;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public XLog extractXESLog(OBDAModel ebdaModel) {
        try {
            if (ebdaModel != null) {
                SimpleXESFactory factory = new SimpleXESFactory();
                logger.info("Factory in use: " + factory.getDescription());
                XFactoryRegistry.instance().setCurrentDefault(factory);

                logger.info("Start extracting XES Log from the EBDA Mapping");
                long start = System.currentTimeMillis();
                SimpleEBDAReasoner ebdaR = new SimpleEBDAReasoner(ebdaModel, factory);
                if (ebdaR.printUnfoldedQueries()) {
                    logger.info("Initialized reasoner in " + (System.currentTimeMillis() - start) + " ms");
                    Map<String, XAttribute> attributes = ebdaR.getAttributes();
                    Map<String, XEvent> events = ebdaR.getEvents(attributes);
                    Collection<XTrace> traces = ebdaR.getTraces(events, attributes);
                    ebdaR.dispose();
                    XLog xlog = factory.createLog();
                    factory.addDefaultExtensions(xlog);
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

    public OBDAModel createEBDAMapping(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueries annotation) {
        try {
            List<OBDADataSource> odsList = obdaModel.getSources();
            if (odsList.size() == 1) {
                logger.info("Constructing EBDA Mapping");
                return new OBDAMappingImpl(domainOntology,
                        OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
                                SimpleXESLogExtractor.class.getResourceAsStream(XESConstants.eventOntoPath)
                        ),
                        obdaModel,
                        annotation
                );
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}





