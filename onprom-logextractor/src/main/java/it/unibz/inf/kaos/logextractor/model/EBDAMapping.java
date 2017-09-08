package it.unibz.inf.kaos.logextractor.model;

import java.io.IOException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.obdamapper.model.OBDAMappingImpl;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;

/**
 * 
 * A specialization of OBDAMapping in which the target ontology is the OnProm XES Event Ontology.
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class EBDAMapping extends OBDAMappingImpl{

	private static final long serialVersionUID = -6979725721077454919L;

	EBDAMapping(OWLOntology sourceOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ) 
			throws InvalidAnnotationException, OWLOntologyCreationException, InvalidDataSourcesNumberException, IOException {

		super(	sourceOntology, 
				OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
						new Object().getClass().getResource(XESEOConstants.eventOntoPath).openStream()), 
				sourceObdaModel, 
				annoQ
			);
	}	

	EBDAMapping(OBDADataSource obdaDataSource, OWLOntology targetOntology) throws InvalidAnnotationException, OWLOntologyCreationException, IOException {

		super(	obdaDataSource, 
				OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
						new Object().getClass().getResource(XESEOConstants.eventOntoPath).openStream())
			);
	}	

}
