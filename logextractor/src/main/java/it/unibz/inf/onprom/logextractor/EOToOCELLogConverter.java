/*
 * onprom-logextractor
 *
 * EOToXESLogConverter.java
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

import it.unibz.inf.onprom.logextractor.ocel.OCELFactory;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelEvent;
import it.unibz.ocel.model.OcelLog;
import it.unibz.ocel.model.OcelTrace;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.HashMap;
import java.util.Set;

/**
 * This class converts event ontology variant to a XES file
 */
public class EOToOCELLogConverter {
    private static final Logger logger = LoggerFactory.getLogger(EOToOCELLogConverter.class);
    private final OCELFactory factory = new OCELFactory();

    public OcelLog convertToOCELLog(OWLOntology ontology) {
        HashMap<String, OcelAttribute> attributes = getAttributes(ontology);
        HashMap<String, OcelEvent> events = getEvents(ontology, attributes);
        HashMap<String, OcelTrace> traces = getTraces(ontology, events, attributes);
        OcelLog ocelLog = factory.createLog();
        ocelLog.addAll(traces.values());

        return ocelLog;
    }

    private HashMap<String, OcelAttribute> getAttributes(OWLOntology ontology) {
        HashMap<String, OcelAttribute> attributes = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/ocel/Attribute"
        Set<OWLClassAssertionAxiom> ocaas = ontology.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/ocel/A")));

        //processing each attribute (i.e., getting its key, type, value)
        for (OWLClassAssertionAxiom ocaa : ocaas) {
            OWLIndividual individual = ocaa.getIndividual();

            //get all data property axioms for the current individual (a step towards getting the attKey, attType, attValue)
            Set<OWLDataPropertyAssertionAxiom> dpaas = ontology.getDataPropertyAssertionAxioms(individual);

            String key = "";
            String type = "";
            String value = "";

            //processing each data property axiom for the current instance of the Attribute class
            for (OWLDataPropertyAssertionAxiom dpaa : dpaas) {
                Set<OWLDataProperty> odps = dpaa.getDataPropertiesInSignature();
                if (odps.size() == 1) {
                    OWLDataProperty dataProp = odps.iterator().next();
                    String dataPropValue = dpaa.getObject().getLiteral();

                    switch (dataProp.toString()) {
                        case "<http://onprom.inf.unibz.it/ocel/ak>":
                            key = dataPropValue;
                            break;
                        case "<http://onprom.inf.unibz.it/ocel/at>":
                            type = dataPropValue;
                            break;
                        case "<http://onprom.inf.unibz.it/ocel/av>":
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

    private HashMap<String, OcelEvent> getEvents(OWLOntology ontology, HashMap<String, OcelAttribute> attributes) {
        HashMap<String, OcelEvent> events = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/ocel/Attribute"
        Set<OWLClassAssertionAxiom> ocaas = ontology.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/ocel/E")));
        //processing each attribute (i.e., getting its key, type, value)
        for (OWLClassAssertionAxiom ocaa : ocaas) {
            OWLIndividual individual = ocaa.getIndividual();
            OcelEvent xevt = factory.createEvent();

            for (OWLObjectPropertyAssertionAxiom opaa : ontology.getObjectPropertyAssertionAxioms(individual)) {
                Set<OWLObjectProperty> odps = opaa.getObjectPropertiesInSignature();

                if (odps.size() == 1) {
                    OWLObjectProperty objProp = odps.iterator().next();

                    if (objProp != null) {
                        String objPropValue = opaa.getObject().toString();

                        if (!objPropValue.equals("")) {
                            //handle the case of event's attributes
                            if ("<http://onprom.inf.unibz.it/ocel/e-a>".equals(objProp.toString())) {
                                OcelAttribute xatt = attributes.get(objPropValue);
                                if (xatt != null)
                                    xevt.getAttributes().put(xatt.getKey(), xatt);
                            }
                        }
                    }
                } else {
                    logger.error("there are more than one object property involving in one object property assertion axiom");
                }
            }
            events.put(individual.toString(), xevt);
        }
        return events;
    }

    private HashMap<String, OcelTrace> getTraces(OWLOntology ontology, HashMap<String, OcelEvent> events, HashMap<String, OcelAttribute> attributes) {
        HashMap<String, OcelTrace> traces = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/ocel/Attribute"
        Set<OWLClassAssertionAxiom> ocaas =
                ontology.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/ocel/T")));
        //processing each attribute (i.e., getting its key, type, value)
        for (OWLClassAssertionAxiom ocaa : ocaas) {
            OWLIndividual individual = ocaa.getIndividual();
            OcelTrace OcelTrace = factory.createTrace();
            for (OWLObjectPropertyAssertionAxiom opaa : ontology.getObjectPropertyAssertionAxioms(individual)) {
                Set<OWLObjectProperty> odps = opaa.getObjectPropertiesInSignature();
                if (odps.size() == 1) {
                    OWLObjectProperty objProp = odps.iterator().next();
                    if (objProp != null) {
                        String objPropValue = opaa.getObject().toString();
                        String s = objProp.toString();
                        if ("<http://onprom.inf.unibz.it/ocel/t-a>".equals(s)) {
                            //handle the case of trace's attributes
                            OcelAttribute xatt = attributes.get(objPropValue);
                            if (xatt != null) {
                                OcelTrace.getAttributes().put(xatt.getKey(), xatt);
                            }
                        } else if ("<http://onprom.inf.unibz.it/ocel/t-e>".equals(s)) {
                            //handle the case of trace's events
                            OcelEvent xevt = events.get(objPropValue);
                            if (xevt != null) {
                                OcelTrace.insertOrdered(xevt);
                            }
                        }
                    }
                } else {
                    logger.error("there are more than one data property involving in one data property assertion axiom");
                }
                traces.put(individual.toString(), OcelTrace);
            }
        }
        return traces;
    }
}