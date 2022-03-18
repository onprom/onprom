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

package it.unibz.inf.onprom.logextractor.ocel;

import it.unibz.inf.onprom.data.query.AnnotationQueries;
import it.unibz.inf.onprom.logextractor.Extractor;
import it.unibz.inf.onprom.obdamapper.OBDAMapper;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.ocel.factory.OcelFactoryRegistry;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelEvent;
import it.unibz.ocel.model.OcelLog;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class OCELLogExtractor implements Extractor<OcelLog> {
    private static final Logger logger = LoggerFactory.getLogger(OCELLogExtractor.class);

    public OWLOntology getOntology() {
        try {
            return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
                    OCELLogExtractor.class.getResourceAsStream(OCELConstants.eventOntoPath)
            );
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public OcelLog extractLog(OWLOntology domainOnto, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) {
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

    public OcelLog extractLog(SQLPPMapping ebdaModel, Properties dataSourceProperties) {
        try {
            if (ebdaModel != null) {
                OCELFactory factory = new OCELFactory();
                logger.info("Factory in use: " + factory.getDescription());
                OcelFactoryRegistry.instance().setCurrentDefault(factory);

                logger.info("Start extracting OCEL Log from the EBDA Mapping");
                long start = System.currentTimeMillis();
                OCELEBDAReasoner ebdaR = new OCELEBDAReasoner(ebdaModel, dataSourceProperties, factory);
                if (ebdaR.printUnfoldedQueries()) {
                    logger.info("Initialized reasoner in " + (System.currentTimeMillis() - start) + " ms");
                    Map<String, OcelAttribute> attributes = ebdaR.getAttributes();
                    Map<String, OcelEvent> events = ebdaR.getEvents(attributes);
//                    Map<String, OcelObject> objects = ebdaR.getObjects(attributes);
//                    Collection<OcelTrace> traces = ebdaR.getTraces(events, attributes);
                    ebdaR.dispose();
                    OcelLog ocelLog = factory.createLog();

                    factory.addDefaultExtensions(ocelLog);
//                    ocelLog.add(attributes);
//                    ocelLog.add(events);
//                    ocelLog.add(objects);
//                    ocelLog.addAll(traces);
                    return ocelLog;
                } else {
                    logger.error("Can't unfold queries, something is wrong, please check logs");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public OcelLog extractLog(OWLOntology domainOntology, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries annotation) {
        try {
            logger.info("Constructing EBDA Mapping");
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
        return "OCEL Extractor";
    }

}



