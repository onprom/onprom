/*
 * 
 * Copyright (c) 2017 Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 * 
 */

package it.unibz.inf.kaos.obdamapper.model;

import ch.qos.logback.classic.Logger;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;
import it.unibz.inf.ontop.parser.TargetQueryParserException;
import it.unibz.inf.ontop.parser.TurtleOBDASyntaxParser;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public abstract class OBDAMappingAbstractImpl extends OBDAModelImpl implements OBDAMapping {

	private static final long serialVersionUID = 4475998912896004087L;
	protected OWLOntology targetOntology;
	protected static final Logger logger = (Logger) LoggerFactory.getLogger(OBDAMappingImpl.class);


	protected OBDAMappingAbstractImpl(List<OBDADataSource> obdaDataSources, OWLOntology targetOntology){
		this.targetOntology = targetOntology;

		if(obdaDataSources.size() > 1){
			//generate a message that we only support 1 OBDA Data Source, trus cuma diambil yang pertama
			logger.error("we only support 1 OBDA Data Source");
		}
		
		if(obdaDataSources.size() >= 1)
			this.addSource(obdaDataSources.get(0));
	}

	protected OBDAMappingAbstractImpl(OBDADataSource obdaDataSource, OWLOntology targetOntology){
		this.targetOntology = targetOntology;
		this.addSource(obdaDataSource);
	}

	//Note that at the moment here we only support a single data source
	protected OBDADataSource getOBDADataSource(){
		
		return this.getSources().get(0);
	}
		
	protected void addMapping(URI obdaDataSourceID, String sourceQuery, String targetQuery){

		logger.debug("--------------------------------------------------------------------");
		logger.debug("ADD MAPPING");
		logger.debug("sourceQuery: \n\n" + sourceQuery.replace("\n", " "));
		logger.debug("targetQuery: \n\n\t" + targetQuery);
		logger.debug("--------------------------------------------------------------------");
		
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
			this.addMapping(obdaDataSourceID, newMapping, false);
		}
	}

	protected String cleanURI(String str){
		
		return str.replaceAll("://", "/").replaceAll(":", "");
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

	@Override
	public OWLOntology getTargetOntology() {
		return targetOntology;
	}

	public boolean setTargetOntology(OWLOntology newTargetOntology){
		
		if(this.targetOntology == null){
			
			this.targetOntology = newTargetOntology;
			return true;
			
		}else{
			
			if(isValid(newTargetOntology)){
				
				this.targetOntology = newTargetOntology;
				return true;
			} 
		}
		
		return false;
	}
	
	//////////////////////////////////////////////////////////////////////
	// Some methods related to OWL Ontology inspections
	//////////////////////////////////////////////////////////////////////
	
	//validate whether the OBDAMapping really contains the mappings to the target Ontology
	public boolean isValid(){
				
		return isValid(this.targetOntology);
	}
	
	//validate whether the OBDAMapping really contains the mappings to the given Ontology
	public boolean isValid(OWLOntology ontology){
				
		for(ArrayList<OBDAMappingAxiom> mapList: getMappings().values()){
			for(OBDAMappingAxiom map : mapList){
				for(Function f : map.getTargetQuery()){
					
					String targetPred = f.getFunctionSymbol().toString();
					
					if(!ontology.containsEntityInSignature(IRI.create(targetPred))){
						
						//System.out.println("Invalid Ontology Vocabulary: "+IRI.create(targetPred));
						
						return false;
					}
				}
			}
		}
		
		return true;
	}

	public boolean isPartOfTargetOntology(String ontoVocab){
		
		return targetOntology.containsEntityInSignature(IRI.create(ontoVocab));
	}

	public boolean isPartOfTargetOntology(IRI ontoVocab){
		
		return targetOntology.containsEntityInSignature(ontoVocab);
	}

	public OWLEntity getASingleOWLEntity(IRI ontoVocab){
		
		Set<OWLEntity> owlEntities = this.targetOntology.getEntitiesInSignature(ontoVocab);
		
		if(owlEntities.size() == 1)
			return owlEntities.iterator().next();

		return null;
	}
	
	/**
	 * Get the corresponding datatype for the given dataProperty based on the 
	 * information in the target ontologey. In the default case where there is 
	 * no datatype range axiom, the rdfs:Literal will be returned
	 * 
	 * @param dataProperty
	 * @return OWLDatatype - datatype
	 * @throws Exception
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 */
	protected OWLDatatype getDataType(OWLDataProperty dataProperty) throws Exception{
		
		Set<OWLDataPropertyRangeAxiom> datPropRangeAxioms = this.targetOntology.getDataPropertyRangeAxioms(dataProperty);
		int numOfDatPropRangeAxioms = datPropRangeAxioms.size();
	
		if(numOfDatPropRangeAxioms > 1){
			
			throw new Exception("Invalid data type");
		
		}else if(numOfDatPropRangeAxioms == 1){
			
			Set<OWLDatatype> datTypeSet = datPropRangeAxioms.iterator().next().getDatatypesInSignature();
			int numOfDatTypeSet = datTypeSet.size();

			if(numOfDatTypeSet > 1){
				
				throw new Exception("Invalid data type");
				
			}else if(numOfDatTypeSet == 1){
				
				return datTypeSet.iterator().next();
			}
		}
		
		//return default data type (i.e., "rdfs:Literal")
		return new OWLDatatypeImpl(OWL2Datatype.RDFS_LITERAL.getIRI());
	}

	protected OWLEntity getOWLTargetEntity(String targetIRI) throws Exception{
		
		return getOWLTargetEntity(IRI.create(targetIRI));
	}

	protected OWLEntity getOWLTargetEntity(IRI targetIRI) throws Exception{
		
		Set<OWLEntity> owlTargetEntities = this.targetOntology.getEntitiesInSignature(targetIRI);
		int numOfOWLEntities = owlTargetEntities.size();
		
		if(numOfOWLEntities != 1){
			
			//TODO: generate a better message - ambiguous target entity - too much matching target entity
			if(numOfOWLEntities > 1)
				throw new Exception("ambiguous target entity - too much matching target entity for "+targetIRI.toString());
	
			//TODO: generate a better message - unknown target entity (possibly wrong target ontology entity) - no matching target entity
			if(numOfOWLEntities < 1)
				throw new Exception("unknown target entity (possibly wrong target ontology entity) - no matching target entity for "+targetIRI.toString());

			return null;
		}
		
		OWLEntity targetEntity = owlTargetEntities.iterator().next();
		
		//TODO: generate a better message - unknown target entity (possibly wrong target ontology entity) - no matching target entity
		if(targetEntity == null)
			throw new Exception("unknown target entity");
		
		return targetEntity;
	}
	
	//////////////////////////////////////////////////////////////////////
	// END OF Some methods related to OWL Ontology inspections
	//////////////////////////////////////////////////////////////////////

}
