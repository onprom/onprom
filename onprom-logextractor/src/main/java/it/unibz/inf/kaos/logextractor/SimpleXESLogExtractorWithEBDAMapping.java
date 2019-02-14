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

import ch.qos.logback.classic.Logger;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.model.*;
import it.unibz.inf.kaos.logextractor.reasoner.SimpleEBDAReasonerImpl;
import it.unibz.inf.kaos.logextractor.util.EfficientHashMap;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SimpleXESLogExtractorWithEBDAMapping {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(LEConstants.LOGGER_NAME);

    public XLog extractXESLog(OWLOntology domainOnto, OBDAModel obdaModel, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) {
        try {
            OBDAMapping obdaMapping = new OBDAMapper().createOBDAMapping(domainOnto, eventOntoVariant, obdaModel, firstAnnoQueries);
            if (obdaMapping != null) {
                return extractXESLogUsingOnlyAtomicQueriesMO(eventOntoVariant, obdaMapping, secondAnnoQueries);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public XLog extractXESLogUsingOnlyAtomicQueriesMO(OWLOntology domainOntology, OBDAModel obdaModel, AnnotationQueries annotation) {
        try {
            logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start creating an EBDA Mapping"));
            EBDAMapping ebdaModel = createEBDAMapping(domainOntology, obdaModel, annotation);
            if (ebdaModel != null) {
                logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Start extracting XES Log from the EBDA Mapping"));
                SimpleEBDAReasonerImpl ebdaR = new SimpleEBDAReasonerImpl(ebdaModel);
                logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES attributes information"));
                EfficientHashMap<XAtt> xatts = ebdaR.getAttributes();
                logger.info("result: " + xatts.values().size() + " attributes are extracted.");
                logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES events information"));
                EfficientHashMap<XEventOnPromEfficient> xevents = ebdaR.getXEvents(xatts);
                logger.info("result: " + xevents.values().size() + " events are extracted.");
                logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Retrieving XES traces information"));
                EfficientHashMap<XTrace> xtraces = ebdaR.getXTraces(xevents, xatts);
                logger.info("result: " + xtraces.values().size() + " traces are extracted.");
                ebdaR.dispose();
                logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing XES log"));
                XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm(true);
                if (xlog != null) {
                    //Add traces to the log
                    xlog.addAll(xtraces.values());
                    logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Finish extracting XES Log from the EBDA Model"));
                    return xlog;
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
                logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Constructing EBDA Mapping"));
                return LEObjectFactory.getInstance().createEBDAMapping(domainOntology, obdaModel, annotation);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}





