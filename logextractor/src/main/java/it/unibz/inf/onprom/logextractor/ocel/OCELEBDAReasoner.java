/*
 * onprom-logextractor
 *
 * SimpleEBDAReasoner.java
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

import it.unibz.inf.onprom.logextractor.EBDAReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.pm.ocel.entity.OcelAttribute;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class OCELEBDAReasoner extends EBDAReasoner<OcelAttribute, OcelEvent, OcelObject> {
    private static final Logger logger = LoggerFactory.getLogger(OCELEBDAReasoner.class);

    private final OCELFactory factory;

    OCELEBDAReasoner(SQLPPMapping obdaModel, Properties dataSourceProperties, OCELFactory factory) throws OWLOntologyCreationException {
        super(obdaModel, dataSourceProperties, OCELConstants.getDefaultEventOntology());
        this.factory = factory;
    }

    boolean printUnfoldedQueries() {
        return super.printUnfoldedQueries(new String[]{
                OCELConstants.qAttTypeKeyVal_Simple,
                OCELConstants.qEventAtt_Simple,
                OCELConstants.qObjectAtt_Simple,
                OCELConstants.qEvtObj_Simple
        });
    }

    protected Map<String, OcelAttribute> getAttributes() {
        Map<String, OcelAttribute> attributes = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();
            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qAttTypeKeyVal_Simple);

            logger.info("Finished executing attributes query in " + (System.currentTimeMillis() - start) + "ms");

            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                try {
                    String attributeKey = result.getOWLObject(OCELConstants.qAtt).toString();
                    String type = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                    if (!attributes.containsKey(attributeKey)) {
//                        XExtension extension = factory.getPredefinedExtension(key);
                        OcelAttribute attribute = factory.createAttribute(type, key, value);

//                        XExtension extension = factory.getPredefinedExtension(key);
//                        XAttribute attribute = factory.createAttribute(type, key, value, extension);
                        if (attribute != null) {
                            attributes.put(attributeKey, attribute);
                        }
                    }
                    if (attributes.size() % 1000000 == 0) logger.info(attributes.size() + " attributes added!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            logger.info("Finished extracting " + attributes.size() + " attributes in " + (System.currentTimeMillis() - start) + "ms");
            resultSet.close();
            st.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return attributes;
    }

    public Map<String, OcelEvent> getEvents() {
        Map<String, OcelEvent> events = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();

            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEvents);

            logger.info("Finished executing events query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String evt = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);

                if (result.getOWLObject(OCELConstants.qAtt) != null) {
                    String type = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                    event.getVmap().put(key, factory.createAttribute(type, key, value));
                }
            }
            logger.info("Finished extracting " + events.size() + " events in " + (System.currentTimeMillis() - start) + "ms");
            resultSet.close();
            st.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return events;
    }


    public Map<String, OcelObject> getObjects() throws Exception {
        Map<String, OcelObject> objects = new HashMap<>();
        long start = System.currentTimeMillis();
        OntopOWLStatement st = getStatement();
        TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qObjects);

        logger.info("Finished executing objects query in " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        while (resultSet.hasNext()) {
            OWLBindingSet result = resultSet.next();
            String obj = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarObject).toString();
            OcelObject object = objects.computeIfAbsent(obj, OcelObject::new);

            if (result.getOWLObject(OCELConstants.qAtt) != null) {
                String type = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                String key = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                String value = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                object.getOvmap().put(key, factory.createAttribute(type, key, value));
            }
            if (objects.size() % 1000000 == 0) logger.info(objects.size() + " objects added!");

        }
        logger.info("Finished extracting " + objects.size() + " objects in " + (System.currentTimeMillis() - start) + "ms");
        resultSet.close();
        st.close();
        return objects;
    }

    public Map<String, OcelEvent> getEventsWithObjects() {
        Map<String, OcelEvent> events = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();

            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEventsWithObjects);

            logger.info("Finished executing events with objects query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String evt = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);

                if (result.getOWLObject(OCELConstants.qAtt) != null) {
                    String type = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                    event.getOmap().add(value);
                    event.getVmap().put(key, factory.createAttribute(type, key, value));
                }
            }
            logger.info("Finished extracting " + events.size() + " events with objects in " + (System.currentTimeMillis() - start) + "ms");
            resultSet.close();
            st.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return events;
    }

}
