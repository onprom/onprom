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

package it.unibz.inf.kaos.logextractor.model;

import org.semanticweb.owlapi.model.OWLOntology;

import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueriesV2;
import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.ontop.model.OBDAModel;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public interface EBDAModel extends OBDAModel {

	public void addMapping(OWLOntology ontology, OBDAModel obdaModel, AnnotationQueriesV2 annoQ) throws InvalidAnnotationException, InvalidDataSourcesNumberException;
	
	//validate whether the EBDAModel really contains the mappings to XES Event Ontology
	public boolean isValidEBDAModel();

}
