package it.unibz.inf.kaos.logextractor;

import java.util.HashMap;
import java.util.Set;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import it.unibz.inf.kaos.logextractor.exception.UnsupportedAttributeTypeException;
import it.unibz.inf.kaos.logextractor.model.XFactoryOnProm;
import it.unibz.inf.kaos.logextractor.model.XLogOnProm;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

public class XESOntoToXESLogConverter {

	private XFactoryOnProm xfact = XFactoryOnProm.getInstance();

	public XLog convertToXESLog(OWLOntology onto){
		
		//TODO: do some check whether the given ontology is a materializatin of the XES Event Ontology

		HashMap<String, XAttribute> xatts = getXAttributes(onto);
		HashMap<String, XEvent> xevts = getXEvents(onto, xatts);
		HashMap<String, XTrace> xtraces = getXTraces(onto, xevts, xatts);
		
		XLogOnProm xlog = XFactoryOnProm.getInstance().createXLogOnProm(true);
		xlog.addAll(xtraces.values());
		
		return xlog;
	}
	
	private HashMap<String, XAttribute> getXAttributes(OWLOntology onto){
		
		HashMap<String, XAttribute> xatts = new HashMap<String, XAttribute>();
		
		// get all instances of the class "http://onprom.inf.unibz.it/Attribute"
		Set<OWLClassAssertionAxiom> ocaas = onto.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/Attribute")));
		
		//processing each attribute (i.e., getting its key, type, value)
		for(OWLClassAssertionAxiom ocaa : ocaas){
			OWLIndividual i = ocaa.getIndividual();
			
			//get all data property axioms for the current individual (a step towards getting the attKey, attType, attValue)
			Set<OWLDataPropertyAssertionAxiom> dpaas = onto.getDataPropertyAssertionAxioms(i);
			
			String key = "";
			String type = "";
			String value = "";
			
			//processing each data property axiom for the current instance of the Attribute class
			for(OWLDataPropertyAssertionAxiom dpaa : dpaas){

				Set<OWLDataProperty> odps = dpaa.getDataPropertiesInSignature();
				OWLDataProperty dataProp = null;
				String dataPropValue = "";

				//TODO: put some error message if it happens? because there are more than one data property involving in one data property assertion axiom
				if(odps.size() > 1)
					continue;
				else			
					dataProp = odps.iterator().next();

				dataPropValue = dpaa.getObject().getLiteral().toString();
				
				switch(dataProp.toString()){
					case "<http://onprom.inf.unibz.it/attKey>": key = dataPropValue;break;
					case "<http://onprom.inf.unibz.it/attType>": type = dataPropValue; break;
					case "<http://onprom.inf.unibz.it/attValue>": value = dataPropValue; break;
				}
			}
			
//			System.out.println("key: "+key);
//			System.out.println("type: "+type);
//			System.out.println("value: "+value);

			XExtension ext = xfact.getPredefinedXExtension(key);
			XAttribute xatt = null;
			try {
				xatt = xfact.createXAttribute(type, key, value, ext);
			} catch (UnsupportedAttributeTypeException e) {
				e.printStackTrace();
				continue;
			}
			
//			System.out.println("put into HashMap<String, XAttribute>: "+ i.toString() + "; "+ xatt);
			xatts.put(i.toString(), xatt);
		}		
		
		return xatts;
	}

	private HashMap<String, XEvent> getXEvents(OWLOntology onto, HashMap<String, XAttribute> xatts){
		
		HashMap<String, XEvent> result = new HashMap<String, XEvent>();
		
		// get all instances of the class "http://onprom.inf.unibz.it/Attribute"
		Set<OWLClassAssertionAxiom> ocaas = onto.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/Event")));
		
		XEvent xevt = null;
		XAttribute xatt = null;
		OWLIndividual eventIndividual = null;
		Set<OWLObjectProperty> odps = null;
		OWLObjectProperty objProp = null;
		String objPropValue = "";
		
		//processing each attribute (i.e., getting its key, type, value)
		for(OWLClassAssertionAxiom ocaa : ocaas){
			eventIndividual = ocaa.getIndividual();
//			System.out.println(i);
			
			if(eventIndividual == null) continue;
			
			xevt = xfact.createEvent();
			
			for(OWLObjectPropertyAssertionAxiom opaa: onto.getObjectPropertyAssertionAxioms(eventIndividual)){
				
//				System.out.println("\t"+opaa);
//				System.out.println("\t opaa.getObjectPropertiesInSignature(): "+opaa.getObjectPropertiesInSignature());
//				System.out.println("\t opaa.getObject(): "+opaa.getObject());
//				System.out.println(xatts.containsKey(opaa.getObject().toString()));
				
				odps = opaa.getObjectPropertiesInSignature();
				
				if(odps == null)
					continue;
				
				objProp = null; objPropValue = "";

				//TODO: put some error message if there are more than one object property involving in one object property assertion axiom
				if(odps.size() == 1){
					objProp = odps.iterator().next();
					
					if(objProp != null){
						objPropValue = opaa.getObject().toString();

						if(objPropValue != null && !objPropValue.equals("")){
							switch(objProp.toString()){
								
								//handle the case of event's attributes
								case "<http://onprom.inf.unibz.it/e-has-a>": 
									xatt = xatts.get(objPropValue);
									if(xatt != null)
										xevt.getAttributes().put(xatt.getKey(), xatt);
									
								break;
							}
						}
					}
				}
			}

//			System.out.println("put into HashMap<String, XEvent>: "+ i.toString() + "; "+ xevt);
			result.put(eventIndividual.toString(), xevt);
		}		
		
		return result;
	}

	private HashMap<String, XTrace> getXTraces(OWLOntology onto, HashMap<String, XEvent> xevts, HashMap<String, XAttribute> xatts){
		
		HashMap<String, XTrace> result = new HashMap<String, XTrace>();
		
		// get all instances of the class "http://onprom.inf.unibz.it/Attribute"
		Set<OWLClassAssertionAxiom> ocaas = 
			onto.getClassAssertionAxioms(new OWLClassImpl(IRI.create("http://onprom.inf.unibz.it/Trace")));
		
		XTrace xtrace = null;
		
		Set<OWLObjectProperty> odps = null;
		OWLObjectProperty objProp = null;
		String objPropValue = "";

		//processing each attribute (i.e., getting its key, type, value)
		for(OWLClassAssertionAxiom ocaa : ocaas){
			OWLIndividual i = ocaa.getIndividual();
//			System.out.println("XTrace: "+i);
			
			if(i == null) continue;
			
			xtrace = xfact.createTrace();
			
			for(OWLObjectPropertyAssertionAxiom opaa: onto.getObjectPropertyAssertionAxioms(i)){
				
//				System.out.println("\t"+opaa);
//				System.out.println("\t opaa.getObjectPropertiesInSignature(): "+opaa.getObjectPropertiesInSignature());
//				System.out.println("\t opaa.getObject(): "+opaa.getObject());
//				System.out.println("============================");
			
				odps = opaa.getObjectPropertiesInSignature();
				
				if(odps == null)
					continue;
				
				objProp = null;
				objPropValue = "";

				//TODO: put some error message if it happens? because there are more than one data property involving in one data property assertion axiom
				if(odps.size() == 1){
					objProp = odps.iterator().next();

					if(objProp != null){
						objPropValue = opaa.getObject().toString();

						switch(objProp.toString()){
							
							//handle the case of trace's attributes
							case "<http://onprom.inf.unibz.it/t-has-a>": 
								XAttribute xatt = xatts.get(objPropValue);
								if(xatt != null)
									xtrace.getAttributes().put(xatt.getKey(), xatt);
								
							break;
							
							//handle the case of trace's events
							case "<http://onprom.inf.unibz.it/t-contains-e>": 
								XEvent xevt = xevts.get(objPropValue);
								if(xevt != null)
									xtrace.insertOrdered(xevt);
									
							break;
						}
					}
				}
			}

//			System.out.println("put into HashMap<String, XTrace>: "+ i.toString() + "; "+ xtrace);
			result.put(i.toString(), xtrace);
		}		
		
		return result;
	}

}
