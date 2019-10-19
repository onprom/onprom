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

package it.unibz.inf.kaos.logextractor;

import it.unibz.inf.kaos.obdamapper.utility.OntopUtility;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class SimpleEBDAReasoner {
    private static final Logger logger = LoggerFactory.getLogger(SimpleEBDAReasoner.class);

    private OntopOWLReasoner reasoner;
    private SimpleXESFactory factory;
    private OntopOWLConnection connection;

    SimpleEBDAReasoner(OBDAModel obdaModel, Properties dataSourceProperties, SimpleXESFactory factory) {
        try {
            this.factory = factory;

            OntopSQLOWLAPIConfiguration config = OntopUtility.getConfiguration(
                    XESConstants.getDefaultEventOntology(),
                    obdaModel,
                    dataSourceProperties
            );

            this.reasoner = OntopOWLFactory.defaultFactory().createReasoner(config);
            this.connection = this.reasoner.getConnection();
            // fix for large query results
            this.connection.setAutoCommit(false);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private OntopOWLStatement getStatement() throws Exception {
        // fix for large query results
        // st.setFetchSize(1000000);
        return connection.createStatement();
    }

    boolean printUnfoldedQueries() {
        try {
            OntopOWLStatement st = getStatement();
            // Unfold queries
            st.getRewritingRendering(XESConstants.qAttTypeKeyVal_Simple);
            st.getRewritingRendering(XESConstants.qEvtAtt_Simple);
            st.getRewritingRendering(XESConstants.qTraceAtt_Simple);
            st.getRewritingRendering(XESConstants.qTraceEvt_Simple);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    void dispose() {
        try {
            reasoner.dispose();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    Map<String, XAttribute> getAttributes() {
        Map<String, XAttribute> attributes = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();
            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(XESConstants.qAttTypeKeyVal_Simple);
            logger.info("Finished executing attributes query in " + (System.currentTimeMillis() - start) + "ms");

            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                try {
                    String attributeKey = result.getOWLObject(XESConstants.qAttTypeKeyVal_SimpleAnsVarAtt).toString();
                    String type = result.getOWLLiteral(XESConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = result.getOWLLiteral(XESConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = result.getOWLLiteral(XESConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();

                    if (!attributes.containsKey(attributeKey)) {
                        XExtension extension = factory.getPredefinedXExtension(key);
                        XAttribute attribute = factory.createXAttribute(type, key, value, extension);
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

    Map<String, XEvent> getEvents(Map<String, XAttribute> attributes) {
        Map<String, XEvent> events = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();

            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(XESConstants.qEvtAtt_Simple);
            logger.info("Finished executing events query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                try {
                    String eventKey = result.getOWLObject(XESConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                    XEvent event = events.get(eventKey);
                    if (event == null) {
                        event = new XEventImpl();
                        events.put(eventKey, event);
                        if (events.size() % 1000000 == 0) logger.info(events.size() + " events added!");
                    }
                    try {
                        String attributeKey = result.getOWLObject(XESConstants.qEvtAtt_SimpleAnsVarAtt).toString();
                        XAttribute attribute = attributes.get(attributeKey);
                        if (attribute != null) {
                            event.getAttributes().put(attribute.getKey(), attribute);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
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

    Collection<XTrace> getTraces(Map<String, XEvent> events, Map<String, XAttribute> attributes) {
        Map<String, XTrace> traces = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();

            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(XESConstants.qTraceAtt_Simple);
            logger.info("Finished executing traces attributes query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                try {
                    String traceKey = result.getOWLObject(XESConstants.qTraceAtt_SimpleAnsVarTrace).toString();
                    XTrace trace = traces.get(traceKey);
                    if (trace == null) {
                        trace = new XTraceImpl(new XAttributeMapImpl());
                        traces.put(traceKey, trace);
                        if (traces.size() % 1000000 == 0) logger.info(traces.size() + " traces added!");
                    }

                    String attributeKey = result.getOWLObject(XESConstants.qTraceAtt_SimpleAnsVarAtt).toString();
                    XAttribute attribute = attributes.get(attributeKey);
                    if (attribute != null) {
                        trace.getAttributes().put(attribute.getKey(), attribute);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            logger.info("Finished extracting " + traces.size() + " traces in " + (System.currentTimeMillis() - start) + "ms");
            resultSet.close();
            st.close();

            st = getStatement();
            start = System.currentTimeMillis();
            resultSet = st.executeSelectQuery(XESConstants.qTraceEvt_Simple);
            logger.info("Finished executing traces events query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                try {
                    String traceKey = result.getOWLObject(XESConstants.qTraceEvt_SimpleAnsVarTrace).toString();
                    String eventKey = result.getOWLObject(XESConstants.qTraceEvt_SimpleAnsVarEvent).toString();

                    XTrace trace = traces.get(traceKey);

                    if (trace != null) {
                        XEvent event = events.get(eventKey);
                        if (event != null) {
                            trace.add(event);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            logger.info("Finished updates traces events in " + (System.currentTimeMillis() - start) + "ms");
            resultSet.close();
            st.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return traces.values();
    }
}
