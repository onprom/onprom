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

package it.unibz.inf.kaos.logextractor.xes;

import it.unibz.inf.kaos.logextractor.EBDAReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class XESEBDAReasoner extends EBDAReasoner<XAttribute, XEvent, XTrace> {
    private static final Logger logger = LoggerFactory.getLogger(XESEBDAReasoner.class);

    private final XESFactory factory;

    XESEBDAReasoner(SQLPPMapping obdaModel, Properties dataSourceProperties, XESFactory factory) throws OWLOntologyCreationException {
        super(obdaModel, dataSourceProperties, XESConstants.getDefaultEventOntology());
        this.factory = factory;
    }

    public boolean printUnfoldedQueries() {
        return super.printUnfoldedQueries(new String[]{
                XESConstants.qAttTypeKeyVal_Simple,
                XESConstants.qEvtAtt_Simple,
                XESConstants.qTraceAtt_Simple,
                XESConstants.qTraceEvt_Simple
        });
    }


    @Override
    protected Map<String, XAttribute> getAttributes() {
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
                        XExtension extension = factory.getPredefinedExtension(key);
                        XAttribute attribute = factory.createAttribute(type, key, value, extension);
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

    @Override
    protected Map<String, XEvent> getEvents(Map<String, XAttribute> attributes) {
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

    @Override
    protected Collection<XTrace> getObjects(Map<String, XEvent> events, Map<String, XAttribute> attributes) {
        Map<String, XTrace> traces = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();
            long start = System.currentTimeMillis();
            //get the Set of trace and att
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

                    //Process the string into a form that matches the key
                    // it is not a good solution by using string parsing
//                    String prefix = "<http://onprom.inf.unibz.it/";
//                    String tmp = eventKey.substring(prefix.length(),eventKey.length());
//                    int index1= tmp.indexOf("/");
//                    String tmp2 = tmp.substring(index1+1,tmp.length());
//                    String finalEventKey = prefix + tmp2;

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


        //  Collection<XTrace> sortedTraces = ToolUtil.sortTrace(traces.values(), "time:timestamp");

        return traces.values();
        //  return sortedTraces;
    }

}
