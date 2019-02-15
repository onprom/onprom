/*
 * onprom-logextractor
 *
 * EBDAReasonerImpl.java
 *
 * Copyright (C) 2016-2018 Free University of Bozen-Bolzano
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
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.*;
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
    private static final XExtension TIME_EXTENSION = XTimeExtension.instance();
    private static final XExtension CONCEPT_EXTENSION = XConceptExtension.instance();
    private static final XExtension LIFECYCLE_EXTENSION = XLifecycleExtension.instance();
    private static final XExtension ORGANIZATION_EXTENSION = XOrganizationalExtension.instance();

    private QuestOWL questReasoner;

    private XAttribute createXAttribute(String type, String key, String value, XExtension extension) {

        if (type == null || key == null || value == null) {
            return null;
        }

        if (type.toLowerCase().equals("literal")) {
            return new XAttributeLiteralImpl(key, value, extension);
        } else if (type.toLowerCase().equals("timestamp")) {

            // Note: based on the the method "public static Timestamp valueOf(String s)" in "java.sql.Timestamp"
            // we assume that the timestamp is in format yyyy-[m]m-[d]d hh:mm:ss[.f...].
            // The fractional seconds may be omitted. The leading zero for mm and dd may also be omitted.
            return new XAttributeTimestampImpl(key, Timestamp.valueOf(value).getTime(), extension);
        }
        return null;
    }

    private XExtension getPredefinedXExtension(String key) {
        if (key == null) return null;
        switch (key.toLowerCase()) {
            case "time:timestamp":
                return TIME_EXTENSION;
            case "concept:name":
                return CONCEPT_EXTENSION;
            case "lifecycle:transition":
                return LIFECYCLE_EXTENSION;
            case "org:resource":
                return ORGANIZATION_EXTENSION;
            default:
                return null;
        }
    }

    public SimpleEBDAReasonerImpl(EBDAMapping ebdaMapping) {
        try {
            OWLOntology eventOnto = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(this.getClass().getResource(XESEOConstants.eventOntoPath).openStream());

            QuestPreferences preferences = new QuestPreferences();
            preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
            preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

            Builder builder = QuestOWLConfiguration.builder();
            builder.obdaModel(ebdaMapping);
            builder.preferences(preferences);
            QuestOWLConfiguration config = builder.build();

            questReasoner = new QuestOWLFactory().createReasoner(eventOnto, config);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void dispose() {
        try {
            this.questReasoner.dispose();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public Map<String, XAttribute> getAttributes() {
        try {
            Map<String, XAttribute> attributes = new HashMap<>();
            QuestOWLConnection conn = questReasoner.getConnection();
            QuestOWLStatement st = conn.createStatement();

            long start = System.currentTimeMillis();
            QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qAttTypeKeyVal_Simple);
            logger.info("Finished executing attributes query in " + (System.currentTimeMillis() - start) + "ms");

            while (resultSet.nextRow()) {
                try {
                    String attributeKey = resultSet.getOWLObject(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAtt).toString();
                    String type = resultSet.getOWLLiteral(XESEOConstants.qAttTypeAnsVarAttType).getLiteral();
                    String key = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();

                    if (!attributes.containsKey(attributeKey)) {
                        XExtension extension = getPredefinedXExtension(key);
                        XAttribute attribute = createXAttribute(type, key, value, extension);
                        if (attribute != null) {
                            attributes.put(attributeKey, attribute);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            logger.info("Finished extracting " + attributes.size() + " attributes");
            resultSet.close();
            st.close();
            conn.close();
            return attributes;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public Map<String, XEvent> getEvents(Map<String, XAttribute> attributes) {
        try {
            Map<String, XEvent> events = new HashMap<>();
            QuestOWLConnection conn = questReasoner.getConnection();
            QuestOWLStatement st = conn.createStatement();

            long start = System.currentTimeMillis();
            QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
            logger.info("Finished executing events query in " + (System.currentTimeMillis() - start) + "ms");

            while (resultSet.nextRow()) {
                try {
                    String eventKey = resultSet.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent).toString();

                    XEvent event = events.get(eventKey);
                    if (event == null) {
                        event = new XEventImpl();
                        events.put(eventKey, event);
                    }

                    String attributeKey = resultSet.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt).toString();
                    XAttribute attribute = attributes.get(attributeKey);
                    if (attribute != null) {
                        event.getAttributes().put(attribute.getKey(), attribute);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            resultSet.close();
            st.close();
            conn.close();
            return events;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public Collection<XTrace> getTraces(Map<String, XEvent> events, Map<String, XAttribute> attributes) {

        try {
            Map<String, XTrace> traces = new HashMap<>();
            QuestOWLConnection conn = questReasoner.getConnection();
            QuestOWLStatement st = conn.createStatement();

            logger.info("Handling traces and attributes");
            long start = System.currentTimeMillis();
            QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
            logger.info("Finished executing traces attributes query in " + (System.currentTimeMillis() - start) + "ms");

            while (resultSet.nextRow()) {
                try {
                    String traceKey = resultSet.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace).toString();
                    XTrace trace = traces.get(traceKey);
                    if (trace == null) {
                        trace = new XTraceImpl(new XAttributeMapImpl());
                        traces.put(traceKey, trace);
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
            resultSet.close();

            logger.info("Handling events of traces");
            start = System.currentTimeMillis();
            resultSet = st.executeTuple(XESEOConstants.qTraceEvt_Simple);
            logger.info("Finished executing traces events query in " + (System.currentTimeMillis() - start) + "ms");

            while (resultSet.nextRow()) {
                try {
                    String traceKey = resultSet.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace).toString();
                    String eventKey = resultSet.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent).toString();

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
            resultSet.close();

            st.close();
            conn.close();
            return traces.values();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}