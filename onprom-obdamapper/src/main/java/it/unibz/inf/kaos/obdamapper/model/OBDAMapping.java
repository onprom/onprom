/*
 * 
 * Copyright (c) 2017 Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 * 
 */

package it.unibz.inf.kaos.obdamapper.model;

import org.semanticweb.owlapi.model.OWLOntology;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.ontop.model.OBDAModel;

public interface OBDAMapping extends OBDAModel {

	/**
	 * Add mappings based on the given annotated OBDA System
	 * 
	 * @param sourceOntology - the ontology of the annotated OBDA System
	 * @param sourceObdaModel - the OBDA Model/Mappings of the annotated OBDA System
	 * @param annoQ - the annotations
	 * @throws InvalidAnnotationException
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	public void addMapping(OWLOntology sourceOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ) throws InvalidAnnotationException, InvalidDataSourcesNumberException;
	
	//validate whether the OBDAMapping really contains the mappings to the given Target Ontology
	public boolean isValid();

	public OWLOntology getTargetOntology();
	
}
