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
import it.unibz.inf.pm.ocel.entity.OcelAttribute;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;
import it.unibz.inf.pm.ocel.entity.OcelObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OCELLogExtractor implements Extractor<OcelLog> {
    private static final Logger logger = LoggerFactory.getLogger(OCELLogExtractor.class);

    public static OWLOntology getOntology() throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
                OCELLogExtractor.class.getResourceAsStream(OCELConstants.eventOntoPath)
        );
    }

    public OcelLog extractLog(OWLOntology domainOnto, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) throws Exception {
        SQLPPMapping obdaMapping = new OBDAMapper(domainOnto, eventOntoVariant, obdaModel, dataSourceProperties, firstAnnoQueries).getOBDAModel();
        return extractLog(eventOntoVariant, obdaMapping, dataSourceProperties, secondAnnoQueries);
    }

    public OcelLog extractLog(SQLPPMapping ebdaModel, Properties dataSourceProperties) throws Exception {
        logger.info("Start extracting OCEL Log from the EBDA Mapping");
        long start = System.currentTimeMillis();
        OCELFactory factory = new OCELFactory();
        OCELEBDAReasoner ebdaR = new OCELEBDAReasoner(ebdaModel, dataSourceProperties, factory);
        ebdaR.printUnfoldedQueries();
        logger.info("Initialized reasoner in " + (System.currentTimeMillis() - start) + " ms");
        Map<String, OcelAttribute> attributes = ebdaR.getAttributes();
        Map<String, OcelObject> objects = ebdaR.getObjects();
        Map<String, OcelEvent> events = ebdaR.getEvents();
        List<String> allTimestamps = ebdaR.getAllTimestamps();
        Set<String> objectTypes = ebdaR.getObjectTypes();
        Map<String, Object> globalInfo = ebdaR.getGlobalInfo();
        ebdaR.dispose();
        OcelLog ocelLog = new OcelLog(globalInfo, events, objects, attributes, allTimestamps, new ArrayList<String>(objectTypes));

        return ocelLog;
    }

    public OcelLog extractLog(OWLOntology domainOntology, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries annotation) throws Exception {
        logger.info("Constructing EBDA Mapping");
        SQLPPMapping ebdaModel = new OBDAMapper(domainOntology,
                getOntology(),
                obdaModel,
                dataSourceProperties,
                annotation
        ).getOBDAModel();
        return extractLog(ebdaModel, dataSourceProperties);
    }

    @Override
    public String toString() {
        return "OCEL Extractor";
    }

}



