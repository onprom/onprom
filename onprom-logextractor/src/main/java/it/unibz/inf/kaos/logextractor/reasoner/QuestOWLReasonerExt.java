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
package it.unibz.inf.kaos.logextractor.reasoner;

import java.util.Collection;
import java.util.List;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.ParsedQuery;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import it.unibz.inf.kaos.logextractor.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.logextractor.ontopext.SQLGeneratorExt;
import it.unibz.inf.kaos.logextractor.ontopext.SQLWithVarMap;
import it.unibz.inf.ontop.model.DatalogProgram;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestQueryProcessor;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SQLAdapterFactory;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.sql.DBMetadata;

/**
 * This class extends the "it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL" class. 
 * It provides some "direct interfaces" for getting the reformulation of SPARQL queries into SQL Query in a form
 * that is needed for OnProm.
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class QuestOWLReasonerExt extends QuestOWL{

	private SQLGeneratorExt sqlGeneratorExt;
	
	public QuestOWLReasonerExt(OWLOntology ontology, QuestOWLConfiguration configuration) throws InvalidDataSourcesNumberException {
		super(ontology, configuration);
		this.sqlGeneratorExt = createSQLGeneratorExt(ontology, configuration.getObdaModel());
	}

	private SQLGeneratorExt createSQLGeneratorExt(OWLOntology ontology, OBDAModel inputOBDAModel) throws InvalidDataSourcesNumberException {

		//Get the data source
			Collection<OBDADataSource> sources = inputOBDAModel.getSources();
			if (sources == null || !(sources.size() == 1))
				throw new InvalidDataSourcesNumberException(sources.size());
			OBDADataSource obdaSource = sources.iterator().next();
		//END OF Get the data source

		DBMetadata metadata = this.getDBMetadata();
		String jdbcClassName = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
		String jdbcDatabaseName = metadata.getDbmsVersion();
		
	    SQLDialectAdapter sqladapter = SQLAdapterFactory.getSQLDialectAdapter(jdbcClassName, jdbcDatabaseName);
			
		return new SQLGeneratorExt(metadata, sqladapter, this.isSqlGenerateReplace(), this.hasDistinctResultSet(), this.getUriMap());
	}
	
	////////////////////////////////////////////////////////////
	//Methods for SPARQL reformulations
	////////////////////////////////////////////////////////////	
	
	public String reformulateSPARQL1(String sparqlQuery) throws OWLException, MalformedQueryException {

		QuestQueryProcessor qqueryProcessor = this.getQueryProcessor();
		
		//parse the given SPARQL query
		ParsedQuery parsedSparql = qqueryProcessor.getParsedQuery(sparqlQuery);
		
		//applying the reformulation algorithm to the parsed SPARQL query, 
		//the resulting reformulated query is an SQL
		return qqueryProcessor.getSQL(parsedSparql);
	}

	public List<SQLWithVarMap> reformulateSPARQL2(String sparqlQuery) throws OWLException, OBDAException, MalformedQueryException {

		QuestQueryProcessor qqueryProcessor = this.getQueryProcessor();

		//parse the given SPARQL query
		ParsedQuery parsedSparql = qqueryProcessor.getParsedQuery(sparqlQuery);
		
		//applying the reformulation algorithm to the parsed SPARQL query, 
		//the resulting reformulated query is in the form of DatalogProgram
		DatalogProgram reformulatedQuery = qqueryProcessor.reformulateSPARQL(parsedSparql);
		List<String> qs = qqueryProcessor.getQuerySignature(parsedSparql);

		
		if(qs == null)
			throw new OBDAException("\nA problem occurred while reformulating the query: \n\n"+
				sparqlQuery+"\n\nthe signature of the reformulated query can't be obtained. Please check the Ontop configuration.\n");
		
		//translate the reformulated query (which is in the form of DatalogProgram) into (a list of) SQL
		return this.sqlGeneratorExt.generateSourceQueries(reformulatedQuery, qs);
	}

	////////////////////////////////////////////////////////////
	//END OF Methods for SPARQL reformulations
	////////////////////////////////////////////////////////////	


}
