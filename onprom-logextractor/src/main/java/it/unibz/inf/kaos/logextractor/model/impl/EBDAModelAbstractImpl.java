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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueriesV2;
import it.unibz.inf.kaos.logextractor.constants.XESEOConstants;
import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;
import it.unibz.inf.ontop.parser.TargetQueryParserException;
import it.unibz.inf.ontop.parser.TurtleOBDASyntaxParser;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public abstract class EBDAModelAbstractImpl extends OBDAModelImpl implements EBDAModel {

	private static final long serialVersionUID = 4475998912896004087L;
//	private TurtleOBDASyntaxParser turtleParser;


	protected EBDAModelAbstractImpl(){
//		this.turtleParser = new TurtleOBDASyntaxParser(this.getPrefixManager());

		//add Event Ontology Prefix
		this.getPrefixManager().addPrefix(
				XESEOConstants.eventOntoPrefixAbbr, XESEOConstants.eventOntoPrefix);
	}
	
	//Note that at the moment here we only support a single data source
	protected OBDADataSource getOBDADataSource(){
		
		return this.getSources().get(0);
	}
		
	protected void addMapping(String sourceQuery, String targetQuery){
		
		/*
		System.out.println("DEBUGA: --------------------------------------------------------------------");
		System.out.println("DEBUGA: ADD MAPPING");
		System.out.println("DEBUGA: sourceQuery: \n\n"+sourceQuery);
		System.out.println("\nDEBUGA: targetQuery: \n\n"+targetQuery);
		System.out.println("\nDEBUGA: --------------------------------------------------------------------\n");
		*/
		
		OBDADataFactory fact = this.getDataFactory();
		
		OBDASQLQuery sourceQ = fact.getSQLQuery(sourceQuery);
		List<Function> targetQ = null;
		
		try {
			targetQ = new TurtleOBDASyntaxParser(this.getPrefixManager()).parse(targetQuery);
		} catch (TargetQueryParserException e) {
			e.printStackTrace();
			//TODO: add some warning message when this error happens
			// because basically this mapping will be skipped
		}
		
		if(targetQ != null && sourceQ != null){
			
			OBDAMappingAxiom newMapping = fact.getRDBMSMappingAxiom(sourceQ, targetQ);
			this.addMapping(this.getOBDADataSource().getSourceID(), newMapping, false);
		}
	}

	protected String cleanURI(String str){
		
		return str.replaceAll("://", "/");
	}

	protected boolean isConstant(String answerVar){
		
		return !(answerVar.charAt(0) == '{' && answerVar.charAt(answerVar.length()-1) == '}');
	}

	protected QuestOWLConfiguration createDefaultQuestOWLConfiguration(OBDAModel obdaModel){
		
		QuestPreferences preferences = new QuestPreferences();
		preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

        Builder builder = QuestOWLConfiguration.builder();
		builder.obdaModel(obdaModel);
		builder.preferences(preferences);
		
		QuestOWLConfiguration config = builder.build();

		return config;
	}

	public abstract void addMapping(OWLOntology ontology, OBDAModel obdaModel, AnnotationQueriesV2 annoQ) throws InvalidAnnotationException, InvalidDataSourcesNumberException;

	//validate whether the EBDAModel really contains the mappings to XES Event Ontology
	@Override
	public boolean isValidEBDAModel(){
		
		OWLOntologyManager eventOntoMan = OWLManager.createOWLOntologyManager();
		URL eventOntoURL = this.getClass().getResource(XESEOConstants.eventOntoPath);
		OWLOntology eventOnto = null;
		
		try {
			eventOnto = eventOntoMan.loadOntologyFromOntologyDocument(eventOntoURL.openStream());
		} catch (OWLOntologyCreationException | IOException e) {
			e.printStackTrace();
		}
		
		for(ArrayList<OBDAMappingAxiom> mapList: getMappings().values()){
			for(OBDAMappingAxiom map : mapList){
				for(Function f : map.getTargetQuery()){
					
					String targetPred = f.getFunctionSymbol().toString();
					
					if(!eventOnto.containsEntityInSignature(IRI.create(targetPred))){
						
						//System.out.println("Invalid Ontology Vocabulary: "+IRI.create(targetPred));
						
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
