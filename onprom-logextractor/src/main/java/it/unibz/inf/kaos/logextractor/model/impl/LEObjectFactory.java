/*
 * Copyright (C) 2017 Free University of Bozen-Bolzano
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.logextractor.model.impl;


import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.model.EBDAModelWithOptimizedXAttributesEncoding;
import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;


/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class LEObjectFactory {

	private static LEObjectFactory objectFactoryInstance;

	//the access modifier for this constructor is to prevent an instantiation of this class from another class
	private LEObjectFactory() {
	}

	public static LEObjectFactory getInstance() {
		if (objectFactoryInstance == null) 
			objectFactoryInstance = new LEObjectFactory();

		return objectFactoryInstance;
	}

	public EBDAModelNaiveImpl createEBDAModelNaiveImpl(){
		return new EBDAModelNaiveImpl();
	}

	public EBDAModelImpl2 createEBDAModelImpl2(){
		return new EBDAModelImpl2();
	}

	public EBDAModelImpl3 createEBDAModelImpl3(){
		return new EBDAModelImpl3();
	}

	public EBDAModelWithOptimizedXAttributesEncoding createEBDAModelWithOptimizedXAttributesEncoding(){
		return new EBDAModelWithOptimizedXAttributesEncodingImpl();
	}

	public EBDAMapping createEBDAMapping(OWLOntology sourceOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ) 
			throws OWLOntologyCreationException, IOException, InvalidAnnotationException, InvalidDataSourcesNumberException{

		/*
		URL eventOntology = this.getClass().getResource(XESEOConstants.eventOntoPath);
		
		OWLOntology targetOntology = 
			OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(eventOntology.openStream());
		
		EBDAMapping ebdaMapping = new EBDAMapping(sourceOntology, sourceObdaModel, annoQ);
		ebdaMapping.setTargetOntology(targetOntology);
		*/
		
		return new EBDAMapping(sourceOntology, sourceObdaModel, annoQ);
	}
	
	public EBDAMapping createEBDAMapping(OBDADataSource obdaDataSource, OWLOntology targetOntology) throws OWLOntologyCreationException, IOException, InvalidAnnotationException{
		
		return new EBDAMapping(obdaDataSource, targetOntology);
	}

}
