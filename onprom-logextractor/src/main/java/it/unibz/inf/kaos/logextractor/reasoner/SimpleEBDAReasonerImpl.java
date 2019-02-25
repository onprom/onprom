/*
 * onprom-logextractor
 *
 * SimpleEBDAReasonerImpl.java
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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.logextractor.reasoner;

import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.logextractor.model.EBDAMapping;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SimpleEBDAReasonerImpl {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEBDAReasonerImpl.class);

    private QuestOWL questReasoner;
    private XFactory factory;
    private QuestOWLConnection connection;


    public SimpleEBDAReasonerImpl(EBDAMapping ebdaMapping, XFactory factory) {
        try {
            this.factory = factory;
            OWLOntology eventOnto = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(this.getClass().getResource(XESEOConstants.eventOntoPath).openStream());

            QuestPreferences preferences = new QuestPreferences();
            preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
            preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);
            preferences.setCurrentValueOf(QuestPreferences.DISTINCT_RESULTSET, QuestConstants.TRUE);

            Builder configurationBuilder = QuestOWLConfiguration.builder();
            configurationBuilder.obdaModel(ebdaMapping);
            configurationBuilder.preferences(preferences);

            this.questReasoner = new QuestOWLFactory().createReasoner(eventOnto, configurationBuilder.build());
            this.connection = this.questReasoner.getConnection();
            // fix for large query results
            this.connection.setAutoCommit(false);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private QuestOWLStatement getStatement() throws Exception {
        QuestOWLStatement st = connection.createStatement();
        // fix for large query results
        st.setFetchSize(1000000);
        return st;
    }

    public boolean printUnfoldedQueries() {
        try {
            QuestOWLStatement st = getStatement();
            // Unfold queries
            logger.info("Attributes Query:" + st.getUnfolding(XESEOConstants.qAttTypeKeyVal_Simple));
            logger.info("Events Query:" + st.getUnfolding(XESEOConstants.qEvtAtt_Simple));
            logger.info("Traces attributes Query:" + st.getUnfolding(XESEOConstants.qTraceAtt_Simple));
            logger.info("Traces events Query:" + st.getUnfolding(XESEOConstants.qTraceEvt_Simple));
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    private XAttribute createXAttribute(String type, String key, String value, XExtension extension) {

        if (type != null && key != null && value != null) {
            if (type.toLowerCase().equals("timestamp")) {
                // we assume that the timestamp is in format yyyy-[m]m-[d]d hh:mm:ss[.f...].
                // The fractional seconds may be omitted. The leading zero for mm and dd may also be omitted.
                return factory.createAttributeTimestamp(key, Timestamp.valueOf(value).getTime(), extension);
            } else {
                return factory.createAttributeLiteral(key, value, extension);
            }
        }
        return null;
    }

    private XExtension getPredefinedXExtension(String key) {
        if (key != null) {
            switch (key.toLowerCase()) {
                case "time:timestamp":
                    return XTimeExtension.instance();
                case "concept:name":
                    return XConceptExtension.instance();
                case "lifecycle:transition":
                    return XLifecycleExtension.instance();
                case "org:resource":
                    return XOrganizationalExtension.instance();
            }
        }
        return null;
    }

    public void dispose() {
        try {
            questReasoner.dispose();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public Map<String, XAttribute> getAttributes() {
        Map<String, XAttribute> attributes = new HashMap<>();
        try {
            QuestOWLStatement st = getStatement();
            long start = System.currentTimeMillis();
            QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qAttTypeKeyVal_Simple);
            logger.info("Finished executing attributes query in " + (System.currentTimeMillis() - start) + "ms");

            start = System.currentTimeMillis();
            while (resultSet.nextRow()) {
                try {
                    String attributeKey = resultSet.getOWLObject(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAtt).toString();
                    String type = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();

                    if (!attributes.containsKey(attributeKey)) {
                        XExtension extension = getPredefinedXExtension(key);
                        XAttribute attribute = createXAttribute(type, key, value, extension);
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

    public Map<String, XEvent> getEvents(Map<String, XAttribute> attributes) {
        Map<String, XEvent> events = new HashMap<>();
        try {
            QuestOWLStatement st = getStatement();

            long start = System.currentTimeMillis();
            QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
            logger.info("Finished executing events query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.nextRow()) {
                try {
                    String eventKey = resultSet.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                    XEvent event = events.get(eventKey);
                    if (event == null) {
                        event = new XEventImpl();
                        events.put(eventKey, event);
                        if (events.size() % 1000000 == 0) logger.info(events.size() + " events added!");
                    }
                    try {
                        String attributeKey = resultSet.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt).toString();
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

    public Collection<XTrace> getTraces(Map<String, XEvent> events, Map<String, XAttribute> attributes) {
        Map<String, XTrace> traces = new HashMap<>();
        try {
            QuestOWLStatement st = getStatement();

            long start = System.currentTimeMillis();
            QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
            logger.info("Finished executing traces attributes query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.nextRow()) {
                try {
                    String traceKey = resultSet.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace).toString();
                    XTrace trace = traces.get(traceKey);
                    if (trace == null) {
                        trace = new XTraceImpl(new XAttributeMapImpl());
                        traces.put(traceKey, trace);
                        if (traces.size() % 1000000 == 0) logger.info(traces.size() + " traces added!");
                    }

                    String attributeKey = resultSet.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt).toString();
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
            resultSet = st.executeTuple(XESEOConstants.qTraceEvt_Simple);
            logger.info("Finished executing traces events query in " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            while (resultSet.nextRow()) {
                try {
                    String traceKey = resultSet.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace).toString();
                    String eventKey = resultSet.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent).toString();

                    XTrace trace = traces.get(traceKey);

                    if (trace != null) {
                        XEvent event = events.get(eventKey);
                        if (event != null) {
                            trace.insertOrdered(event);
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