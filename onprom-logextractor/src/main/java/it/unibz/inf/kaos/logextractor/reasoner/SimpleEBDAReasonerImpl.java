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
import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
import it.unibz.inf.kaos.logextractor.model.EBDAMapping;
import it.unibz.inf.kaos.logextractor.model.XAtt;
import it.unibz.inf.kaos.logextractor.model.XEventOnPromEfficient;
import it.unibz.inf.kaos.logextractor.model.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.util.EfficientHashMap;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class SimpleEBDAReasonerImpl {

    private static final Logger logger = (Logger) LoggerFactory.getLogger("EBDAReasoner");

    private QuestOWL questReasoner;
    private XFactoryOnProm xfact;

    public SimpleEBDAReasonerImpl(EBDAMapping ebdaMapping) {

        init(ebdaMapping);
    }

    private void init(OBDAModel ebdaModel) {

        this.xfact = XFactoryOnProm.getInstance();

        OWLOntologyManager eventOntoMan = OWLManager.createOWLOntologyManager();
        URL eventOntoURL = this.getClass().getResource(XESEOConstants.eventOntoPath);


        OWLOntology eventOnto = null;

        try {
            eventOnto = eventOntoMan.loadOntologyFromOntologyDocument(eventOntoURL.openStream());
        } catch (OWLOntologyCreationException | IOException e) {
            e.printStackTrace();
        }

        //Create an instance of Quest OWL reasoner.
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestPreferences preferences = new QuestPreferences();
        preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

        Builder builder = QuestOWLConfiguration.builder();
        builder.obdaModel(ebdaModel);
        builder.preferences(preferences);

        QuestOWLConfiguration config = builder.build();

        if (eventOnto != null)
            questReasoner = factory.createReasoner(eventOnto, config);
        //END OF Creating an instance of Quest OWL reasoner.
    }

    public void dispose() {
        if (this.questReasoner != null)
            this.questReasoner.dispose();
    }


    /**
     * T. E. Kalayci
     *
     * @return
     * @throws OWLException
     */
    public EfficientHashMap<XAtt> getAttributes() throws OWLException {

        EfficientHashMap<XAtt> attributes = new EfficientHashMap<>();
        QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qAttTypeKeyVal_Simple);

        while (resultSet.nextRow()) {
            OWLObject attribute = resultSet.getOWLObject(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAtt);
            String attType = resultSet.getOWLLiteral(XESEOConstants.qAttTypeAnsVarAttType).getLiteral();
            String attKey = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
            String attValue = resultSet.getOWLLiteral(XESEOConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
            XExtension xext = this.xfact.getPredefinedXExtension(attKey);

            if (attribute != null && !attributes.containsKey(attribute.toString())) {
                try {
                    XAtt xatt = xfact.createXAttNoURI(attType);
                    xatt.setKey(attKey);
                    xatt.setVal(attValue);
                    if (xext != null) xatt.setExtension(xext);
                    attributes.put(attribute.toString(), xatt);
                } catch (UnsupportedAttributeTypeException e) {
                    logger.error(e.getMessage());
                }
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

        try {

            QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qEvtAtt_Simple);

            while (resultSet.nextRow()) {

                OWLObject evtObj = resultSet.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarEvent);
                String newEvt = (evtObj == null ? null : evtObj.toString().intern());

                if (newEvt != null) {
                    OWLObject attObj = resultSet.getOWLObject(XESEOConstants.qEvtAtt_SimpleAnsVarAtt);
                    String newAtt = (attObj == null ? null : attObj.toString().intern());

                    XAtt xatt = xattmap.get(newAtt);

                    if (events.containsKey(newEvt)) {
                        if (newAtt != null && xatt != null && xatt.hasCompleteInfo()) {
                            events.get(newEvt).addXAttribute(xatt);
                        }

                    } else {
                        XEventOnPromEfficient xevt = this.xfact.createXEventOnPromNoURI();
                        if (xevt != null) {
                            if (newAtt != null && xatt != null && xatt.hasCompleteInfo()) {
                                xevt.addXAttribute(xatt);
                            }
                            events.put(newEvt, xevt);
                        }
                    }
                }
            }
            resultSet.close();
        } finally {
            st.close();
            conn.close();
            questReasoner.dispose();
        }
        return events;
    }

    public EfficientHashMap<XTrace> getXTraces(EfficientHashMap<XEventOnPromEfficient> xevtmap, EfficientHashMap<XAtt> xattmap) throws OWLException {

        EfficientHashMap<XTrace> traces = new EfficientHashMap<>();
        QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {

            QuestOWLResultSet resultSet = st.executeTuple(XESEOConstants.qTrace_Simple);

            while (resultSet.nextRow()) {
                OWLObject traceObj = resultSet.getOWLObject(XESEOConstants.qTraceAnsVarTrace);
                String newTrace = (traceObj == null ? null : traceObj.toString().intern());
                if (newTrace != null) {
                    XTrace xtrace = this.xfact.createXTraceNaiveImpl();
                    if (xtrace != null) {
                        traces.put(newTrace, xtrace);
                    }
                }
            }
            resultSet.close();

            logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces events"));

            resultSet = st.executeTuple(XESEOConstants.qTraceEvt_Simple);

            while (resultSet.nextRow()) {

                //============================================================================
                //Reading the Query results
                //============================================================================
                OWLObject traceObj = resultSet.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarTrace);
                String newTrace = (traceObj == null ? null : traceObj.toString().intern());

                //if the current trace is null, then skip the rest and move on
                if (newTrace == null) continue;

                OWLObject eventObj = resultSet.getOWLObject(XESEOConstants.qTraceEvt_SimpleAnsVarEvent);
                String newEvent = (eventObj == null ? null : eventObj.toString().intern());

                if (traces.containsKey(newTrace)) {
                    XEvent xevt = xevtmap.get(newEvent);
                    if (xevt != null) {
                        try {
                            traces.get(newTrace).add(xevt);
                        } catch (Exception e) {
                            logger.error("Error while inserting an event into a trace. "
                                    + "One possible reason: there is a mismatch between the XES attribute type and some reserved XES attribute key. "
                                    + "E.g., if the AnnotationQueries says that a certain attribute with the key='time:timestamp' has the type literal, then an exception might be thrown here ");
                        }
                    }
                }
            }

            resultSet.close();

            logger.info(String.format(LEConstants.LOG_INFO_TEMPLATE, "Handling traces attributes"));

            resultSet = st.executeTuple(XESEOConstants.qTraceAtt_Simple);

            while (resultSet.nextRow()) {

                OWLObject traceObj = resultSet.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarTrace);
                String newTrace = (traceObj == null ? null : traceObj.toString().intern());

                if (newTrace != null) {

                    OWLObject attObj = resultSet.getOWLObject(XESEOConstants.qTraceAtt_SimpleAnsVarAtt);
                    String newAtt = (attObj == null ? null : attObj.toString().intern());

                    if (traces.containsKey(newTrace)) {
                        XAtt xatt = xattmap.get(newAtt);
                        if (newAtt != null && xatt != null && xatt.hasCompleteInfo()) {
                            traces.get(newTrace).getAttributes().put(xatt.getKey(), xatt);
                        }
                    }
                }
            }
            resultSet.close();
        } finally {
            st.close();
            conn.close();
            questReasoner.dispose();
        }
        return traces;
    }
}
