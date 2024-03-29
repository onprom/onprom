/*
 * onprom-logextractor
 *
 * XOToXESLogConverter.java
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

package it.unibz.inf.onprom.logextractor;

import it.unibz.inf.onprom.logextractor.xes.XESFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.HashMap;
import java.util.Set;

public class XOToXESLogConverter {
    private static final Logger logger = LoggerFactory.getLogger(XOToXESLogConverter.class);
    private final XESFactory factory = new XESFactory();

    public XLog convertToXESLog(OWLOntology ontology) {
        HashMap<String, XAttribute> attributes = getAttributes(ontology);
        HashMap<String, XEvent> events = getEvents(ontology, attributes);
        HashMap<String, XTrace> traces = getTraces(ontology, events, attributes);

        XLog xlog = factory.createLog();
        xlog.addAll(traces.values());
        return xlog;
    }

    private HashMap<String, XAttribute> getAttributes(OWLOntology onto) {
        HashMap<String, XAttribute> attributes = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/Attribute"
        Set<OWLClassAssertionAxiom> ocaas = onto.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/Attribute")));
        //processing each attribute (i.e., getting its key, type, value)
        for (OWLClassAssertionAxiom ocaa : ocaas) {
            OWLIndividual individual = ocaa.getIndividual();
            //get all data property axioms for the current individual (a step towards getting the attKey, attType, attValue)
            Set<OWLDataPropertyAssertionAxiom> dpaas = onto.getDataPropertyAssertionAxioms(individual);

            String key = "";
            String type = "";
            String value = "";
            //processing each data property axiom for the current instance of the Attribute class
            for (OWLDataPropertyAssertionAxiom dpaa : dpaas) {
                Set<OWLDataProperty> odps = dpaa.getDataPropertiesInSignature();
                if (odps.size() == 1) {
                    OWLDataProperty dataProp = odps.iterator().next();
                    String dataPropValue = dpaa.getObject().getLiteral();
                    String s = dataProp.toString();
                    switch (s) {
                        case "<http://onprom.inf.unibz.it/attKey>":
                            key = dataPropValue;
                            break;
                        case "<http://onprom.inf.unibz.it/attType>":
                            type = dataPropValue;
                            break;
                        case "<http://onprom.inf.unibz.it/attValue>":
                            value = dataPropValue;
                            break;
                    }
                } else {
                    logger.error("there are more than one data property involving in one data property assertion axiom");
                }
            }
            try {
                attributes.put(individual.toString(), factory.createAttribute(type, key, value, factory.getPredefinedExtension(key)));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return attributes;
    }

    private HashMap<String, XEvent> getEvents(OWLOntology ontology, HashMap<String, XAttribute> attributes) {
        HashMap<String, XEvent> events = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/Attribute"
        Set<OWLClassAssertionAxiom> ocaas = ontology.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/Event")));

        //processing each attribute (i.e., getting its key, type, value)
        for (OWLClassAssertionAxiom ocaa : ocaas) {
            OWLIndividual individual = ocaa.getIndividual();
            XEvent event = factory.createEvent();
            for (OWLObjectPropertyAssertionAxiom opaa : ontology.getObjectPropertyAssertionAxioms(individual)) {
                Set<OWLObjectProperty> odps = opaa.getObjectPropertiesInSignature();
                if (odps.size() == 1) {
                    OWLObjectProperty objProp = odps.iterator().next();
                    if (objProp != null) {
                        String objPropValue = opaa.getObject().toString();
                        if (!objPropValue.equals("")) {
                            //handle the case of event's attributes
                            if ("<http://onprom.inf.unibz.it/e-has-a>".equals(objProp.toString())) {
                                XAttribute attribute = attributes.get(objPropValue);
                                if (attribute != null) {
                                    event.getAttributes().put(attribute.getKey(), attribute);
                                }
                            }
                        }
                    }
                } else {
                    logger.error("there are more than one object property involving in one object property assertion axiom");
                }
            }
            events.put(individual.toString(), event);
        }
        return events;
    }

    private HashMap<String, XTrace> getTraces(OWLOntology ontology, HashMap<String, XEvent> events, HashMap<String, XAttribute> attributes) {
        HashMap<String, XTrace> result = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/Attribute"
        Set<OWLClassAssertionAxiom> ocaas =
                ontology.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/Trace")));
        //processing each attribute (i.e., getting its key, type, value)
        for (OWLClassAssertionAxiom ocaa : ocaas) {
            OWLIndividual individual = ocaa.getIndividual();
            XTrace xtrace = factory.createTrace();
            for (OWLObjectPropertyAssertionAxiom opaa : ontology.getObjectPropertyAssertionAxioms(individual)) {
                Set<OWLObjectProperty> odps = opaa.getObjectPropertiesInSignature();
                if (odps.size() == 1) {
                    OWLObjectProperty objProp = odps.iterator().next();
                    if (objProp != null) {
                        String objPropValue = opaa.getObject().toString();
                        switch (objProp.toString()) {
                            //handle the case of trace's attributes
                            case "<http://onprom.inf.unibz.it/t-has-a>":
                                XAttribute xatt = attributes.get(objPropValue);
                                if (xatt != null) {
                                    xtrace.getAttributes().put(xatt.getKey(), xatt);
                                }
                                break;
                            //handle the case of trace's events
                            case "<http://onprom.inf.unibz.it/t-contains-e>":
                                XEvent xevt = events.get(objPropValue);
                                if (xevt != null) {
                                    xtrace.insertOrdered(xevt);
                                }
                                break;
                        }
                    }
                } else {
                    logger.error("there are more than one data property involving in one data property assertion axiom");
                }
            }
            result.put(individual.toString(), xtrace);
        }
        return result;
    }

}
