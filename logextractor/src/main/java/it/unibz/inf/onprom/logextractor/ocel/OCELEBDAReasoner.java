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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
                        OcelAttribute attribute = factory.createAttribute(type, key, value);

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

    public Map<String, OcelObject> getObjects() throws Exception {
        Map<String, OcelObject> objects = new HashMap<>();
        long start = System.currentTimeMillis();
        extractObjectsAndAttributes(objects);
        extractObjectAndType(objects);
        logger.info("Finished extracting " + objects.size() + " objects in " + (System.currentTimeMillis() - start) + "ms");
        return objects;
    }

    public Map<String, OcelEvent> getEvents() throws Exception {
        Map<String, OcelEvent> events = new HashMap<>();
        long start = System.currentTimeMillis();
        extractEventsAndAttributes(events);
        extractEventsAndObjects(events);
        extractEventsAndTimestamp(events);
        extractEventsAndActivity(events);
        logger.info("Finished extracting " + events.size() + " events in " + (System.currentTimeMillis() - start) + "ms");
        return events;
    }

    public Map<String, Object> getGlobalInfo() throws Exception {
        Map<String, Object> content = new HashMap<>();
        //init global-log
        content.put("ocel:version", "1.0");
        content.put("ocel:attribute-names", new ArrayList<String>() {{
            add("color");
            add("costs");
            add("customer");
            add("size");
        }});
        content.put("ocel:object-types", new ArrayList<String>() {{
            add("customer");
            add("item");
            add("order");
            add("package");
            add("produce");
        }});
        //init global-event
        content.put("ocel:global-event", new HashMap<String, String>() {{
            put("ocel-activity", "__INVALID__");
        }});

        //init global-object
        content.put("ocel:global-object", new HashMap<String, String>() {{
            put("ocel-type", "__INVALID__");
        }});
        return content;
    }


    private void extractEventsAndObjects(Map<String, OcelEvent> events) throws Exception {
        try (
                OntopOWLStatement st = getStatement();
                TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEventsWithObjects)) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String evt = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);
                String objectId = result.getOWLObject("object").toString();
                event.getOmap().add(objectId);
            }
        }
    }

    private void extractEventsAndAttributes(Map<String, OcelEvent> events) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEvents)) {
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
        }
    }


    private void extractEventsAndTimestamp(Map<String, OcelEvent> events) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEventsWithTimestamps)) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String evt = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);
                String timestamp = result.getOWLLiteral(OCELConstants.qEvtAtt_SimpleAnsVarTimestamp).getLiteral();
                event.setTimestamp(timestamp);
            }
        }
    }

    private void extractEventsAndActivity(Map<String, OcelEvent> events) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEventsWithActivities)) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String evt = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);
                String activity = result.getOWLLiteral(OCELConstants.qEvtAtt_SimpleAnsVarActivity).getLiteral();
                event.setActivity(activity);
            }
        }
    }

    private void extractObjectsAndAttributes(Map<String, OcelObject> objects) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qObjects)) {
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
            }
        }
    }

    private void extractObjectAndType(Map<String, OcelObject> objects) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qObjectWithType)) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String obj = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarObject).toString();
                OcelObject object = objects.computeIfAbsent(obj, OcelObject::new);
                String type = result.getOWLLiteral(OCELConstants.qType_SimpleAnsVarObject).getLiteral();
                object.setType(type);
            }
        }
    }


}
