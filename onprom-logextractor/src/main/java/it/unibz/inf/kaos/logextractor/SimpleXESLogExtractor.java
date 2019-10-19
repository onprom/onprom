/*
 * onprom-logextractor
 *
 * SimpleXESLogExtractor.java
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

package it.unibz.inf.kaos.logextractor;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.ontop.protege.core.OBDAModel;
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
import java.util.Map;
import java.util.Properties;

public class SimpleXESLogExtractor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleXESLogExtractor.class);

    public XLog extractXESLog(OWLOntology domainOnto, OBDAModel obdaModel, Properties dataSourceProperties, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) {
        try {
            OBDAModel obdaMapping = new OBDAMapper(domainOnto, eventOntoVariant, obdaModel, dataSourceProperties, firstAnnoQueries).getOBDAModel();
            if (obdaMapping != null) {
                return extractXESLog(eventOntoVariant, obdaMapping, dataSourceProperties, secondAnnoQueries);
            } else {
                logger.error("OBDA Mapping is NULL!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public XLog extractXESLog(OWLOntology domainOntology, OBDAModel obdaModel, Properties dataSourceProperties, AnnotationQueries annotation) {
        try {
            logger.info("Constructing EBDA Mapping");
            OBDAModel ebdaModel = new OBDAMapper(domainOntology,
                    OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
                            SimpleXESLogExtractor.class.getResourceAsStream(XESConstants.eventOntoPath)
                    ),
                    obdaModel,
                    dataSourceProperties,
                    annotation
            ).getOBDAModel();
            return extractXESLog(ebdaModel, dataSourceProperties);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public XLog extractXESLog(OBDAModel ebdaModel, Properties dataSourceProperties) {
        try {
            if (ebdaModel != null) {
                SimpleXESFactory factory = new SimpleXESFactory();
                logger.info("Factory in use: " + factory.getDescription());
                XFactoryRegistry.instance().setCurrentDefault(factory);

                logger.info("Start extracting XES Log from the EBDA Mapping");
                long start = System.currentTimeMillis();
                SimpleEBDAReasoner ebdaR = new SimpleEBDAReasoner(ebdaModel, dataSourceProperties, factory);
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

}





