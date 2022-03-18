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
import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelEvent;
import it.unibz.ocel.model.OcelObject;
import it.unibz.ocel.model.impl.OcelEventImpl;
import it.unibz.ocel.model.impl.OcelObjectImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
                OCELConstants.qEvtAtt_Simple,
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
                    String attributeKey = result.getOWLObject(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAtt).toString();

                    String type = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                    if (!attributes.containsKey(attributeKey)) {
                        OcelExtension extension = factory.getPredefinedExtension(key);
                        OcelAttribute attribute = factory.createAttribute(type, key, value, extension);
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

    public Map<String, OcelEvent> getEvents(Map<String, OcelAttribute> attributes) {
        Map<String, OcelEvent> events = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();

            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEvtObj_Simple);

            logger.info("Finished executing events query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                try {
                    String eventKey = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                    OcelEvent event = events.get(eventKey);
                    if (event == null) {
                        event = new OcelEventImpl();
                        events.put(eventKey, event);
                        if (events.size() % 1000000 == 0) logger.info(events.size() + " events added!");
                    }
//                    try {
//                        String attributeKey = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarAtt).toString();
//                        OcelAttribute attribute = attributes.get(attributeKey);
//                        if (attribute != null) {
//                            event.getAttributes().put(attribute.getKey(), attribute);
//                        }
//                    } catch (Exception e) {
//                        logger.error(e.getMessage());
//                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
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

    protected Collection<OcelObject> getObjects(Map<String, OcelEvent> events, Map<String, OcelAttribute> attributes) {
        Map<String, OcelObject> objects = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();

            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEvtAtt_Simple);
            logger.info("Finished executing objects query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                try {
                    String objectKey = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarObject).toString();
                    OcelObject object = objects.get(objectKey);
                    if (object == null) {
                        object = new OcelObjectImpl();
                        objects.put(objectKey, object);
                        if (objects.size() % 1000000 == 0) logger.info(objects.size() + " objects added!");
                    }
                    try {
                        String attributeKey = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarAtt).toString();
                        OcelAttribute attribute = attributes.get(attributeKey);
                        if (attribute != null) {
                            object.getAttributes().put(attribute.getKey(), attribute);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            logger.info("Finished extracting " + objects.size() + " objects in " + (System.currentTimeMillis() - start) + "ms");
            resultSet.close();
            st.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return objects.values();
    }

//    Collection<OcelTrace> getTraces(Map<String, OcelEvent> events, Map<String, OcelAttribute> attributes) {
//        Map<String, OcelTrace> traces = new HashMap<>();
//        try {
//            OntopOWLStatement st = getStatement();
//            long start = System.currentTimeMillis();
//            //get the Set of trace and att
//            TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEvt_SimpleAnsVarObject);
//            logger.info("Finished executing traces attributes query in " + (System.currentTimeMillis() - start) + "ms");
//            start = System.currentTimeMillis();
//            while (resultSet.hasNext()) {
//                OWLBindingSet result = resultSet.next();
//                try {
//                    String traceKey = result.getOWLObject(OCELConstants.qTraceAtt_SimpleAnsVarTrace).toString();
//                    OcelTrace trace = traces.get(traceKey);
//                    if (trace == null) {
//                        trace = new OcelTraceImpl(new OcelAttributeMapImpl());
//                        traces.put(traceKey, trace);
//                        if (traces.size() % 1000000 == 0) logger.info(traces.size() + " traces added!");
//                    }
//
//                    String attributeKey = result.getOWLObject(OCELConstants.qTraceAtt_SimpleAnsVarAtt).toString();
//                    OcelAttribute attribute = attributes.get(attributeKey);
//                    if (attribute != null) {
//                        trace.getAttributes().put(attribute.getKey(), attribute);
//                    }
//                } catch (Exception e) {
//                    logger.error(e.getMessage());
//                }
//            }
//            logger.info("Finished extracting " + traces.size() + " traces in " + (System.currentTimeMillis() - start) + "ms");
//            resultSet.close();
//            st.close();
//
//            st = getStatement();
//            start = System.currentTimeMillis();
//            resultSet = st.executeSelectQuery(OCELConstants.qTraceEvt_Simple);
//            logger.info("Finished executing ocel events query in " + (System.currentTimeMillis() - start) + "ms");
//            start = System.currentTimeMillis();
//
//            while (resultSet.hasNext()) {
//                OWLBindingSet result = resultSet.next();
//                try {
//                    String traceKey = result.getOWLObject(OCELConstants.qTraceEvt_SimpleAnsVarTrace).toString();
//                    String eventKey = result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent).toString();
//
//                    OcelTrace trace = traces.get(traceKey);
//                    if (trace != null) {
//                        OcelEvent event = events.get(eventKey);
//                        if (event != null) {
//                            trace.add(event);
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.error(e.getMessage());
//                }
//            }
//            logger.info("Finished updates traces events in " + (System.currentTimeMillis() - start) + "ms");
//            resultSet.close();
//            st.close();
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
//
//
//      //  Collection<XTrace> sortedTraces = ToolUtil.sortTrace(traces.values(), "time:timestamp");
//
//        return traces.values();
//      //  return sortedTraces;
//    }

}
