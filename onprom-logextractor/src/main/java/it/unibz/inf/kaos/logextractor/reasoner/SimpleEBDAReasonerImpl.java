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

import ch.qos.logback.classic.Logger;
import it.unibz.inf.kaos.logextractor.constants.LEConstants;
import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.logextractor.model.EBDAMapping;
import it.unibz.inf.kaos.logextractor.model.XAtt;
import it.unibz.inf.kaos.logextractor.model.XEventOnPromEfficient;
import it.unibz.inf.kaos.logextractor.model.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.util.EfficientHashMap;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.LoggerFactory;

public class SimpleEBDAReasonerImpl {

    private static final Logger logger = (Logger) LoggerFactory.getLogger("EBDAReasoner");

    private QuestOWL questReasoner;
    private XFactoryOnProm xfact;

    public SimpleEBDAReasonerImpl(EBDAMapping ebdaMapping) {
        try {
            this.xfact = XFactoryOnProm.getInstance();
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
        if (this.questReasoner != null)
            this.questReasoner.dispose();
    }

    public EfficientHashMap<XAtt> getAttributes() throws OWLException {

        EfficientHashMap<XAtt> attributes = new EfficientHashMap<>();
        QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qAttTypeKeyVal_Simple);
        logger.info("Result set has " + resultSet.getFetchSize() + " rows and " + resultSet.getColumnCount() + " columns");

        while (resultSet.nextRow()) {
            try {
                OWLObject attribute = resultSet.getOWLObject(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAtt);
                String type = resultSet.getOWLLiteral(XESEOConstants.qAttTypeAnsVarAttType).getLiteral();
                String key = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                String value = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                XExtension extension = this.xfact.getPredefinedXExtension(key);

                if (attribute != null && !attributes.containsKey(attribute.toString())) {
                    XAtt xatt = xfact.createXAttNoURI(type);
                    xatt.setKey(key);
                    xatt.setVal(value);
                    if (extension != null) xatt.setExtension(extension);
                    attributes.put(attribute.toString(), xatt);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        resultSet.close();
        st.close();
        conn.close();
        questReasoner.dispose();
        return attributes;
    }

    public EfficientHashMap<XEventOnPromEfficient> getXEvents(EfficientHashMap<XAtt> xattmap) throws OWLException {
        EfficientHashMap<XEventOnPromEfficient> events = new EfficientHashMap<>();
        QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qEvtAtt_Simple);
        logger.info("Result set has " + resultSet.getFetchSize() + " rows and " + resultSet.getColumnCount() + " columns");

        while (resultSet.nextRow()) {
            try {
                String eventKey = resultSet.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent).toString();
                String attributeKey = resultSet.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt).toString();
                XAtt attribute = xattmap.get(attributeKey);

                if (events.containsKey(eventKey) && attribute.hasCompleteInfo()) {
                    events.get(eventKey).addXAttribute(attribute);
                } else {
                    XEventOnPromEfficient event = this.xfact.createXEventOnPromNoURI();
                    if (event != null && attribute != null && attribute.hasCompleteInfo()) {
                        event.addXAttribute(attribute);
                    }
                    events.put(eventKey, event);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        resultSet.close();
        st.close();
        conn.close();
        questReasoner.dispose();
        return events;
    }

    public EfficientHashMap<XTrace> getXTraces(EfficientHashMap<XEventOnPromEfficient> xevtmap, EfficientHashMap<XAtt> xattmap) throws OWLException {

        EfficientHashMap<XTrace> traces = new EfficientHashMap<>();
        QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qTrace_Simple);
        logger.info("Result set has " + resultSet.getFetchSize() + " rows and " + resultSet.getColumnCount() + " columns");

        while (resultSet.nextRow()) {
            try {
                String traceKey = resultSet.getOWLObject(XESEOConstants.qTraceAnsVarTrace).toString();
                XTrace trace = this.xfact.createXTraceNaiveImpl();
                if (trace != null) {
                    traces.put(traceKey, trace);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        resultSet.close();

        logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces events"));
        resultSet = st.executeTuple(XESEOConstants.qTraceEvt_Simple);
        logger.info("Result set has " + resultSet.getFetchSize() + " rows and " + resultSet.getColumnCount() + " columns");

        while (resultSet.nextRow()) {
            try {
                String traceKey = resultSet.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace).toString();
                String eventKey = resultSet.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent).toString();

                if (traces.containsKey(traceKey)) {
                    XEvent event = xevtmap.get(eventKey);
                    if (event != null) {
                        traces.get(traceKey).add(event);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        resultSet.close();

        logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces attributes"));
        resultSet = st.executeTuple(XESEOConstants.qTraceAtt_Simple);
        logger.info("Result set has " + resultSet.getFetchSize() + " rows and " + resultSet.getColumnCount() + " columns");

        while (resultSet.nextRow()) {
            try {
                String traceKey = resultSet.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace).toString();
                String attributeKey = resultSet.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt).toString();
                if (traces.containsKey(traceKey)) {
                    XAtt attribute = xattmap.get(attributeKey);
                    if (attribute != null && attribute.hasCompleteInfo()) {
                        traces.get(traceKey).getAttributes().put(attribute.getKey(), attribute);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        resultSet.close();
        st.close();
        conn.close();
        questReasoner.dispose();
        return traces;
    }
}
