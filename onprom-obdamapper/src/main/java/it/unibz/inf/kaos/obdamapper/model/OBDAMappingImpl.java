/*
 * 
 * Copyright (c) 2017 Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 * 
 */

package it.unibz.inf.kaos.obdamapper.model;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openrdf.query.MalformedQueryException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import it.unibz.inf.kaos.obdamapper.ontopext.SQLWithVarMap;

import it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.obdamapper.reasoner.QuestOWLReasonerExt;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.data.query.AnnotationQueryVisitor;
import it.unibz.inf.kaos.data.query.BinaryAnnotationQuery;
import it.unibz.inf.kaos.data.query.UnaryAnnotationQuery;
import it.unibz.inf.kaos.obdamapper.constants.OMConstants;

/*
 * Note (2017.07 - ario): the reason of making a separation among OBDAMapping interface, 
 * OBDAMapping abstract class and OBDAMappingImpl class is to allow different kind of 
 * OBDAMapping implementation. This implementation is just one possible implementation. 
 * Another possible implementation would be the implementation that utilize paralel 
 * processing.
 */
//Note: we only support 1 data source


/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class OBDAMappingImpl extends OBDAMappingAbstractImpl implements OBDAMapping {

	private static final long serialVersionUID = 40062215694643168L;
	
	private final String objPropTripleTemplate = " <http://onprom.inf.unibz.it/%s> %s <http://onprom.inf.unibz.it/%s> . "; //<[Object]> [ObjectProperty] <[Object]>
	private final String dataPropTripleTemplate = " <http://onprom.inf.unibz.it/%s> %s %s . "; //<[Object> [DataProperty] <[Value]>
	private final String conceptTripleTemplate = " <http://onprom.inf.unibz.it/%s> a %s . "; //<[Object]> rdf:type <[Class]>

	
	
	protected OBDAMappingImpl(OBDADataSource obdaDataSource, OWLOntology targetOntology){
		
		super(obdaDataSource, targetOntology);
		logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "An OBDA Mapping is initialized"));
	}

	protected OBDAMappingImpl(
			OWLOntology sourceOntology, OWLOntology targetOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ) 
					throws InvalidAnnotationException, InvalidDataSourcesNumberException{
		
		super(sourceObdaModel.getSources(), targetOntology);
		this.addMapping(sourceOntology, sourceObdaModel, annoQ);
		
		logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "An OBDA Mapping is initialized"));
	}

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
	@Override
	public void addMapping(OWLOntology sourceOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ) 
			throws InvalidAnnotationException, InvalidDataSourcesNumberException{
		
    	//Create an instance of Extended Quest OWL Reasoner.
			QuestOWLConfiguration config = createDefaultQuestOWLConfiguration(sourceObdaModel);
			QuestOWLReasonerExt questReasoner = new QuestOWLReasonerExt(sourceOntology, config);
    	//END OF Creating an instance of Extended Quest OWL Reasoner.

		try{
	
			//========================================================================================
			// Handling the number of data sources
			//========================================================================================

			List<OBDADataSource> newOdsList = sourceObdaModel.getSources();
			List<OBDADataSource> odsList = this.getSources();

			if(newOdsList.size() > 1){
				
				//TODO: generate an error message: too many data sources
				System.out.println("kebanyakan source 1 - kebanyakan yang mo ditambahin");
				return;
			}
			
			if(odsList.size() > 1){
				
				//TODO: generate an error message: too many data sources
				System.out.println("kebanyakan source 2 - udah kebanyakan");
				return;
				
			}else if(odsList.size() == 1){
	
				if(odsList.get(0) != newOdsList.get(0)){				
					
					//TODO: generate an error message: too many data sources, 
					System.out.println("kebanyakan source 3 - klo ditambahin jadi kebanyakan");
					return;
				}
				
			}else if(odsList.size() < 1){
				
				this.addSource(newOdsList.get(0));
			}
			
			//========================================================================================
			// END OF Handling the number of data sources
			//========================================================================================
				
			AnnotationQueriesProcessor mappingAdder = 
				new AnnotationQueriesProcessor(questReasoner, this.getSources().get(0).getSourceID());
			
			for(AnnotationQuery aq : annoQ.getAllQueries())				
				aq.accept(mappingAdder);
			
		}finally{
				try {
					questReasoner.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				questReasoner.dispose();
		}
	}	
		
	private void addMapping(BinaryAnnotationQuery annoQ, QuestOWLReasonerExt reasoner, URI obdaDataSourceID) {

		String[] firstComponent = annoQ.getFirstComponent();
		String[] secondComponent = annoQ.getSecondComponent();
		IRI targetURI = annoQ.getTargetURI();
		String sourceSQLQuery = annoQ.getQuery();
		
		if(firstComponent == null || secondComponent == null || targetURI == null || sourceSQLQuery == null){
			//TODO: generate a message - invalid input - some inputs contain null value
			System.out.println("TODO: generate a message - invalid input - some inputs contain null value");
			return;
		}
		
		List<SQLWithVarMap> unfoldedQueries = null;
		OWLEntity targetEntity = null;
 
		//some variables for handling the cases when the target entity is a data property
		OWLDatatype dataType = null;
		OWLDatatype defaultDataType = new OWLDatatypeImpl(OWL2Datatype.RDFS_LITERAL.getIRI());
		//END OF some variables for handling the cases when the target entity is a data property


		//###########################################################################
		//processing the target entity - preparing the target query creation
		//###########################################################################
		
		try {
			
			targetEntity = getOWLTargetEntity(targetURI);
			
		} catch (Exception e) {
			//TODO: generate a better message 
			System.out.println("TODO: generate a message - "+ e.getMessage());
			e.printStackTrace();
		}
		
		//----------------------------------------------------------------------------
		// Handling the case where the targetEntity is data property
		//----------------------------------------------------------------------------

			if(targetEntity.isOWLDataProperty()){
				
				if(secondComponent.length > 1){
					//TODO: generate an error message - wrong annotation - 
					//second component must contain exactly one answer variable/constant
					
					System.out.println(
							"\n\nERROR!!! --- TODO: generate an error message - "
							+ "wrong annotation - for the mapping to data property"
							+ "the second component must contain exactly one answer variable/constant\n");
					
					return;
				}
				
				try {
					dataType = getDataType(targetEntity.asOWLDataProperty());
					
					if(dataType == null)
						dataType = defaultDataType;
					
					//System.out.println("DEBUGA: dataType: "+dataType);
					
				} catch (Exception e) {
					//TODO: generate a more meaningful message
					logger.info(e.getMessage());
					return;
				}
			}

		//----------------------------------------------------------------------------
		// END OF Handling the case where the targetEntity is data property
		//----------------------------------------------------------------------------

		//###########################################################################
		//END OF processing the target entity
		//###########################################################################


		
		//###########################################################################
		//Reformulate (rewrite & unfold) the Source Query
		//###########################################################################
		
		try {
			unfoldedQueries = reasoner.reformulateSPARQL2(sourceSQLQuery);
			
		} catch (OBDAException | MalformedQueryException | OWLException e) {
			//TODO: generate a message that we fail to reformulate the query annoQ, and skip this query
			e.printStackTrace();
			return;
		}
		
		//Just in case there is a failure in reformulating the source query
		if(unfoldedQueries == null){

			//TODO: generate a message that we fail to process the query annoQ, and skip this query
			System.out.println("TODO: generate a message that we fail to process the query annoQ, and skip this query");
			return;
		}
		
		//System.out.println("DEBUGAGA: number of reformulated queries: " + unfoldedQueries.size());
		//System.out.println("DEBUGAGA: reformulated queries: \n\n" + unfoldedQueries.get(0).getSQL());
		
		//###########################################################################
		//END OF Reformulate (rewrite & unfold) the Source Query
		//###########################################################################

		
		
		//###########################################################################
		//Generate the mappings - create a mapping for each reformulated source query
		//###########################################################################
		
		String targetQuery = "";
		HashMap<String, String> varMap = null;
		String firstComp;
		String secondComp;
		StringBuilder firstURITemplate = new StringBuilder("");
		StringBuilder secondURITemplate = new StringBuilder("");
		int idx = 0;
		int smallestNumOfAnsVars = 0 ;

		int iter = 1;
		
		/*
		 * The following loop processes each reformulated query. Note that the target query 
		 * could be different depending on the varmap that is obtain from the query reformulation. 
		 * Thus we need to generate the target query in every loop.
		 */
		for(SQLWithVarMap sqlWithVarMap: unfoldedQueries){
			System.out.println("======================\nIteration: "+ iter++ +"\n======================");

			
			//###########################################################################
			//Generating the target query
			//###########################################################################
			
			varMap = sqlWithVarMap.getVariableMap();
			
			if(varMap == null) continue;
			
			//===========================================================================
			//generating the target URI first and second components
			//===========================================================================
			
			System.out.println("\n\nDEBUGA: ------------ Generating the target URI Template ------------\n");

			idx = 0;
			smallestNumOfAnsVars = Math.min(firstComponent.length, secondComponent.length)-1;
			firstURITemplate = firstURITemplate.delete(0, firstURITemplate.length());
			secondURITemplate = secondURITemplate.delete(0, secondURITemplate.length());
			firstComp = null;
			secondComp = null;

			while(idx <= smallestNumOfAnsVars){
				
				System.out.println("DEBUGA: First Comp: "+firstComponent[idx]+" => "+varMap.get(firstComponent[idx]));
				System.out.println("DEBUGA: Second Comp: "+secondComponent[idx]+" => "+varMap.get(secondComponent[idx]));

				firstComp = varMap.get(firstComponent[idx]);
				secondComp = varMap.get(secondComponent[idx]);
				
				if(firstComp != null) {

					//TODO: putting some warning here would be good
					firstURITemplate.append(firstComp);
				}
				
				if(secondComp != null) {
					
					//TODO: putting some warning here would be good
					secondURITemplate.append(secondComp);				
				}
				
				if(idx < smallestNumOfAnsVars && firstComp != null)
					firstURITemplate.append("/");				

				if(idx < smallestNumOfAnsVars && secondComp != null)
					secondURITemplate.append("/");				

				idx++;
			}
			while(idx < firstComponent.length){
				
				System.out.println("DEBUGA: First Comp: "+firstComponent[idx]+" => "+varMap.get(firstComponent[idx]));
				
				
				firstComp = varMap.get(firstComponent[idx]);

				if(firstComp != null) {
					
					firstURITemplate.append("/");				
					firstURITemplate.append(firstComp);
				}

				idx++;
			}
			while(idx < secondComponent.length){

				System.out.println("DEBUGA: Second Comp: "+secondComponent[idx]+" => "+varMap.get(secondComponent[idx]));
				
				secondComp = varMap.get(secondComponent[idx]);

				if(secondComp != null) {
					
					secondURITemplate.append("/");			
					secondURITemplate.append(secondComp);				
				}

				idx++;
			}
			
			if(firstURITemplate.length() == 0 || secondURITemplate.length() == 0){
				
				//TODO: generate an error message - something wrong with the answer variables information - skip
				System.out.println("DEBUGA: something wrong with the answer variables information - skip");
				continue;
			}

			System.out.println("\nDEBUGA: firstURITemplate: "+firstURITemplate);
			System.out.println("DEBUGA: secondURITemplate: "+secondURITemplate);
			
			System.out.println("\nDEBUGA: ------------ END OF Generating the target URI Template ------------\n\n");
			//===========================================================================
			//END OF generating the target URI first and second components
			//===========================================================================

			
			
			//===========================================================================
			//generating the target Query/URI template
			//===========================================================================
			if(targetEntity.isOWLObjectProperty()){
				
				//========================================================================
				//handling the case of adding a mapping that populates an OBJECT PROPERTY
				//========================================================================
				
				logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "Add a mapping to an OBJECT PROPERTY"));
				
				targetQuery = String.format(this.objPropTripleTemplate, 
												cleanURI(firstURITemplate.toString()), 
												targetEntity.toString(), 
												cleanURI(secondURITemplate.toString()));
								
				//========================================================================
				//END OF handling the case of adding a mapping that populates an OBJECT PROPERTY
				//========================================================================
				
			}else if(targetEntity.isOWLDataProperty()){
				
				//========================================================================
				//handling the case of adding a mapping that populates a DATA PROPERTY
				//========================================================================

				logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "Add a mapping to a DATA PROPERTY"));
				
				
				if(isConstant(secondURITemplate.toString())){
					secondURITemplate.insert(0, "\"");
					secondURITemplate.append("\"");
				}
				
				//append data type
				secondURITemplate.append("^^");
				secondURITemplate.append(dataType);
				
				targetQuery = String.format(this.dataPropTripleTemplate, 
												cleanURI(firstURITemplate.toString()), 
												targetEntity.toString(), 
												secondURITemplate);

				//========================================================================
				//END OF handling the case of adding a mapping that populates a DATA PROPERTY
				//========================================================================
			}
			//===========================================================================
			//END OF generating the target Query/URI template
			//===========================================================================

			//###########################################################################
			//END OF Generating the target query
			//###########################################################################
			
			//###########################################################################
			//get the source query
			//###########################################################################

			String sourceQuery = sqlWithVarMap.getSQL();
			
			System.out.println("\nDEBUGA: sourceQuery: \n"+sourceQuery);
			
			//###########################################################################
			//END OF getting the source query
			//###########################################################################
			
			//###########################################################################
			//Finally, add a new mapping to the set of OBDA Mappings
			//###########################################################################

			if( obdaDataSourceID != null &&
				sourceQuery != null && !sourceQuery.equals("") && 
				targetQuery != null && !targetQuery.equals("")){
				
				this.addMapping(obdaDataSourceID, sourceQuery, targetQuery);
			}

			//###########################################################################
			//END OF adding a new mapping to the set of OBDA Mappings
			//###########################################################################
		}		
		//###########################################################################
		//END OF Generating the mappings
		//###########################################################################

	}
	
	private void addMapping(UnaryAnnotationQuery annoQ, QuestOWLReasonerExt reasoner, URI obdaDataSourceID) {
		
		String[] uriComponent = annoQ.getComponent();
		IRI targetURI = annoQ.getTargetURI();
		String sourceSQLQuery = annoQ.getQuery();
		
		if(uriComponent == null || targetURI == null || sourceSQLQuery == null){
			//TODO: generate a message - invalid input - some inputs contain null value
			System.out.println("TODO: generate a message - invalid input - some inputs contain null value");
			return;
		}
		
		List<SQLWithVarMap> unfoldedQueries = null;
		OWLEntity targetEntity = null;
 
		//###########################################################################
		//processing the target entity - preparing the target query creation
		//###########################################################################
		
		try {
			
			targetEntity = getOWLTargetEntity(targetURI);
			
		} catch (Exception e) {
			//TODO: generate a better message 
			System.out.println("TODO: generate a message - "+ e.getMessage());
			e.printStackTrace();
		}

		//###########################################################################
		//END OF processing the target entity
		//###########################################################################
		
		//###########################################################################
		//Reformulate (rewrite & unfold) the Source Query
		//###########################################################################
		
		try {
			unfoldedQueries = reasoner.reformulateSPARQL2(sourceSQLQuery);
			
		} catch (OBDAException | MalformedQueryException | OWLException e) {
			//TODO: generate a message that we fail to reformulate the query annoQ, and skip this query
			e.printStackTrace();
			return;
		}
		
		//Just in case there is a failure in reformulating the source query
		if(unfoldedQueries == null){

			//TODO: generate a message that we fail to process the query annoQ, and skip this query
			System.out.println("TODO: generate a message that we fail to process the query annoQ, and skip this query");
			return;
		}
		
		//###########################################################################
		//END OF Reformulate (rewrite & unfold) the Source Query
		//###########################################################################
		
		//###########################################################################
		//Generate the mappings - create a mapping for each reformulated source query
		//###########################################################################
		
		String targetQuery = "";
		HashMap<String, String> varMap = null;
		String uriComp;
		StringBuilder uriTemplate = new StringBuilder("");

		/*
		 * The following loop processes each reformulated query. Note that the target query 
		 * could be different depending on the varmap that is obtain from the query reformulation. 
		 * Thus we need to generate the target query in every loop.
		 */
		for(SQLWithVarMap sqlWithVarMap: unfoldedQueries){
			
			//###########################################################################
			//Generating the target query
			//###########################################################################
			
			varMap = sqlWithVarMap.getVariableMap();
			
			if(varMap == null) continue;
			
			//===========================================================================
			//generating the target URI component
			//===========================================================================
			
			System.out.println("\n\nDEBUGA: ------------ Generating the target URI Template ------------\n");

			uriTemplate = uriTemplate.delete(0, uriTemplate.length());
			uriComp = null;
			
			for(int ii = 0; ii < uriComponent.length; ii++){
				System.out.println("DEBUGA: URI Comp: "+uriComponent[ii]+" => "+varMap.get(uriComponent[ii]));

				uriComp = varMap.get(uriComponent[ii]);

				if(uriComp != null) 
					uriTemplate.append(uriComp);
				
				if(ii < uriComponent.length && uriComp != null)
					uriTemplate.append("/");				

			}
			
			if(uriTemplate.length() == 0){
				
				//TODO: generate an error message - something wrong with the answer variables information - skip
				System.out.println("DEBUGA: something wrong with the answer variables information - skip");
				continue;
			}

			System.out.println("DEBUGA: uriTemplate: "+ uriTemplate);
			
			System.out.println("\nDEBUGA: ------------ END OF Generating the target URI Template ------------\n\n");
			
			//===========================================================================
			//END OF generating the target URI component
			//===========================================================================

			//===========================================================================
			//generating the target Query/URI template
			//===========================================================================
			
			logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "Add a mapping to a CONCEPT"));
			
			targetQuery = String.format(this.conceptTripleTemplate, 
							cleanURI(uriTemplate.toString()), targetEntity.toString());

			//===========================================================================
			//END OF generating the target Query/URI template
			//===========================================================================

			//###########################################################################
			//END OF Generating the target query
			//###########################################################################
			
			//###########################################################################
			//get the source query
			//###########################################################################

			String sourceQuery = sqlWithVarMap.getSQL();
			
			//###########################################################################
			//END OF getting the source query
			//###########################################################################
			
			//###########################################################################
			//Finally, add a new mapping to the set of OBDA Mappings
			//###########################################################################

			if( obdaDataSourceID != null &&
				sourceQuery != null && !sourceQuery.equals("") && 
				targetQuery != null && !targetQuery.equals("")){
				
				this.addMapping(obdaDataSourceID, sourceQuery, targetQuery);
			}

			//###########################################################################
			//END OF adding a new mapping to the set of OBDA Mappings
			//###########################################################################
		}		
		//###########################################################################
		//END OF Generating the mappings
		//###########################################################################
	}
		
	private class AnnotationQueriesProcessor implements AnnotationQueryVisitor {

		private QuestOWLReasonerExt questReasoner;
		private URI obdaDataSourceID;
		
		public AnnotationQueriesProcessor(QuestOWLReasonerExt questReasoner, URI obdaDataSourceID){
			this.questReasoner = questReasoner;
			this.obdaDataSourceID = obdaDataSourceID;
		}

		////////////////////////////////////////////////////////
		// Methods from AnnotationQueryVisitor
		////////////////////////////////////////////////////////	
		
		@Override
		public void visit(BinaryAnnotationQuery query) {
			addMapping(query, questReasoner, obdaDataSourceID);
		}

		@Override
		public void visit(UnaryAnnotationQuery query) {
			addMapping(query, questReasoner, obdaDataSourceID);
		}
		
		////////////////////////////////////////////////////////
		// END OF Methods from AnnotationQueryVisitor
		////////////////////////////////////////////////////////		

	}

	
}


