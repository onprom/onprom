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
import it.unibz.inf.kaos.logextractor.model.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.reasoner.SimpleEBDAReasonerImpl;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleXESLogExtractorWithEBDAMapping {
    private static final Logger logger = LoggerFactory.getLogger(SimpleXESLogExtractorWithEBDAMapping.class);

    public XLog extractXESLog(OWLOntology domainOnto, OBDAModel obdaModel, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) {
        try {
            OBDAMapping obdaMapping = new OBDAMapper().createOBDAMapping(domainOnto, eventOntoVariant, obdaModel, firstAnnoQueries);
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
            EBDAMapping ebdaModel = createEBDAMapping(domainOntology, obdaModel, annotation);
            System.gc();
            if (ebdaModel != null) {
                logger.info("Start extracting XES Log from the EBDA Mapping");
                long start = System.currentTimeMillis();
                SimpleEBDAReasonerImpl ebdaR = new SimpleEBDAReasonerImpl(ebdaModel);
                logger.info("Initialized reasoner in " + (System.currentTimeMillis() - start) + " ms");
                logger.info("Retrieving XES attributes information");
                start = System.currentTimeMillis();
                Map<String, XAttribute> attributes = ebdaR.getAttributes();
                logger.info(attributes.size() + " attributes are extracted in " + (System.currentTimeMillis() - start) + " ms");
                System.gc();
                logger.info("Retrieving XES events information");
                start = System.currentTimeMillis();
                Map<String, XEvent> events = ebdaR.getEvents(attributes);
                logger.info(events.size() + " events are extracted in " + (System.currentTimeMillis() - start) + " ms");
                System.gc();
                logger.info("Retrieving XES traces information");
                start = System.currentTimeMillis();
                Collection<XTrace> traces = ebdaR.getTraces(events, attributes);
                logger.info(traces.size() + " traces are extracted in " + (System.currentTimeMillis() - start) + " ms");
                ebdaR.dispose();
                System.gc();
                logger.info("Constructing XES log");
                XLog xlog = XFactoryOnProm.getInstance().createXLogOnProm(true);
                xlog.addAll(traces);
                System.gc();
                logger.info("Finished extracting XES Log from the EBDA Model");
                return xlog;
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
}





