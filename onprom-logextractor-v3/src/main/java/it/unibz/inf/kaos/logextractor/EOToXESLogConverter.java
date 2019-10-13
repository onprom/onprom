package it.unibz.inf.kaos.logextractor;

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

/**
 * This class converts event ontology variant to a XES file
 */
public class EOToXESLogConverter {
    private static final Logger logger = LoggerFactory.getLogger(EOToXESLogConverter.class);
    private final SimpleXESFactory factory = new SimpleXESFactory();

    public XLog convertToXESLog(OWLOntology ontology) {

        //TODO: do some check whether the given ontology is a materializatin of the XES Event Ontology

        HashMap<String, XAttribute> attributes = getAttributes(ontology);
        HashMap<String, XEvent> events = getEvents(ontology, attributes);
        HashMap<String, XTrace> traces = getTraces(ontology, events, attributes);

        XLog xlog = factory.createLog();
        xlog.addAll(traces.values());

        return xlog;
    }

    private HashMap<String, XAttribute> getAttributes(OWLOntology ontology) {
        HashMap<String, XAttribute> attributes = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/Attribute"
        Set<OWLClassAssertionAxiom> ocaas = ontology.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/A")));

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
                        case "<http://onprom.inf.unibz.it/ak>":
                            key = dataPropValue;
                            break;
                        case "<http://onprom.inf.unibz.it/at>":
                            type = dataPropValue;
                            break;
                        case "<http://onprom.inf.unibz.it/av>":
                            value = dataPropValue;
                            break;
                    }
                } else {
                    logger.error("there are more than one data property involving in one data property assertion axiom");
                }
            }
            try {
                attributes.put(individual.toString(), factory.createXAttribute(type, key, value, factory.getPredefinedXExtension(key)));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return attributes;
    }

    private HashMap<String, XEvent> getEvents(OWLOntology ontology, HashMap<String, XAttribute> attributes) {
        HashMap<String, XEvent> events = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/Attribute"
        Set<OWLClassAssertionAxiom> ocaas = ontology.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/E")));
        //processing each attribute (i.e., getting its key, type, value)
        for (OWLClassAssertionAxiom ocaa : ocaas) {
            OWLIndividual individual = ocaa.getIndividual();
            XEvent xevt = factory.createEvent();

            for (OWLObjectPropertyAssertionAxiom opaa : ontology.getObjectPropertyAssertionAxioms(individual)) {
                Set<OWLObjectProperty> odps = opaa.getObjectPropertiesInSignature();

                if (odps.size() == 1) {
                    OWLObjectProperty objProp = odps.iterator().next();

                    if (objProp != null) {
                        String objPropValue = opaa.getObject().toString();

                        if (!objPropValue.equals("")) {
                            //handle the case of event's attributes
                            if ("<http://onprom.inf.unibz.it/e-a>".equals(objProp.toString())) {
                                XAttribute xatt = attributes.get(objPropValue);
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

    private HashMap<String, XTrace> getTraces(OWLOntology ontology, HashMap<String, XEvent> events, HashMap<String, XAttribute> attributes) {
        HashMap<String, XTrace> traces = new HashMap<>();
        // get all instances of the class "http://onprom.inf.unibz.it/Attribute"
        Set<OWLClassAssertionAxiom> ocaas =
                ontology.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/T")));
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
                        String s = objProp.toString();
                        if ("<http://onprom.inf.unibz.it/t-a>".equals(s)) {
                            //handle the case of trace's attributes
                            XAttribute xatt = attributes.get(objPropValue);
                            if (xatt != null) {
                                xtrace.getAttributes().put(xatt.getKey(), xatt);
                            }
                        } else if ("<http://onprom.inf.unibz.it/t-e>".equals(s)) {
                            //handle the case of trace's events
                            XEvent xevt = events.get(objPropValue);
                            if (xevt != null) {
                                xtrace.insertOrdered(xevt);
                            }
                        }
                    }
                } else {
                    logger.error("there are more than one data property involving in one data property assertion axiom");
                }
                traces.put(individual.toString(), xtrace);
            }
        }
        return traces;
    }
}