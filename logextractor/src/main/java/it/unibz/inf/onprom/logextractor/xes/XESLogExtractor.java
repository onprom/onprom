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

package it.unibz.inf.onprom.logextractor.xes;

import it.unibz.inf.onprom.data.query.AnnotationQueries;
import it.unibz.inf.onprom.logextractor.Extractor;
import it.unibz.inf.onprom.obdamapper.OBDAMapper;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class XESLogExtractor implements Extractor<XLog> {
    private static final Logger logger = LoggerFactory.getLogger(XESLogExtractor.class);

    public OWLOntology getOntology() {
        try {
            return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
                    XESLogExtractor.class.getResourceAsStream(XESConstants.eventOntoPath)
            );
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public XLog extractLog(OWLOntology domainOnto, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) {
        try {
            SQLPPMapping obdaMapping = new OBDAMapper(domainOnto, eventOntoVariant, obdaModel, dataSourceProperties, firstAnnoQueries).getOBDAModel();
            if (obdaMapping != null) {
                return extractLog(eventOntoVariant, obdaMapping, dataSourceProperties, secondAnnoQueries);
            } else {
                logger.error("OBDA Mapping is NULL!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public XLog extractLog(SQLPPMapping ebdaModel, Properties dataSourceProperties) {
        try {
//            if (ebdaModel != null) {
            XESFactory factory = new XESFactory();
            logger.info("Factory in use: " + factory.getDescription());
            XFactoryRegistry.instance().setCurrentDefault(factory);

            logger.info("Start extracting XES Log from the EBDA Mapping");
            long start = System.currentTimeMillis();
            XESEBDAReasoner ebdaR = new XESEBDAReasoner(ebdaModel, dataSourceProperties, factory);
            if (ebdaR.printUnfoldedQueries()) {
                logger.info("Initialized reasoner in " + (System.currentTimeMillis() - start) + " ms");
                Map<String, XAttribute> attributes = ebdaR.getAttributes();
                Map<String, XEvent> events = ebdaR.getEvents(attributes);
                Collection<XTrace> traces = ebdaR.getObjects(events, attributes);
                ebdaR.dispose();
                XLog xlog = factory.createLog();
                factory.addDefaultExtensions(xlog);
                xlog.addAll(traces);
                return xlog;
            } else {
                logger.error("Can't unfold queries, something is wrong, please check logs");
                return null;
            }
//            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public XLog extractLog(OWLOntology domainOntology, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries annotation) {
        try {
            SQLPPMapping ebdaModel = new OBDAMapper(domainOntology,
                    getOntology(),
                    obdaModel,
                    dataSourceProperties,
                    annotation
            ).getOBDAModel();
            return extractLog(ebdaModel, dataSourceProperties);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "XES Extractor";
    }
}





