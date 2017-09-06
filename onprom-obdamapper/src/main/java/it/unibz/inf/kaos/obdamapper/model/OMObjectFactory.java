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
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class OMObjectFactory {

	private static OMObjectFactory objectFactoryInstance;

	//the access modifier for this constructor is to prevent an instantiation of this class from another class
	private OMObjectFactory() {
	}

	public static OMObjectFactory getInstance() {
		if (objectFactoryInstance == null) 
			objectFactoryInstance = new OMObjectFactory();

		return objectFactoryInstance;
	}

	public OBDAMapping createOBDAMapping(OBDADataSource dataSource, OWLOntology targetOntology){
		
		return new OBDAMappingImpl(dataSource, targetOntology);
	}
	
	public OBDAMapping createOBDAMapping(
			OWLOntology sourceOntology, OWLOntology targetOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ) throws InvalidAnnotationException, InvalidDataSourcesNumberException{
		
		return new OBDAMappingImpl(sourceOntology, targetOntology, sourceObdaModel, annoQ);
	}

}
