package it.unibz.inf.kaos.obdamapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.obdamapper.constants.OMConstants;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
import it.unibz.inf.kaos.obdamapper.model.OMObjectFactory;
import it.unibz.inf.kaos.obdamapper.reasoner.OBDAMaterializerImpl;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorOWL2QL;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.Quest;
import it.unibz.inf.ontop.owlrefplatform.core.QuestQueryProcessor;
import it.unibz.inf.ontop.owlrefplatform.core.QuestStatement;
import it.unibz.inf.ontop.owlrefplatform.core.QuestUnfolder;
import it.unibz.inf.ontop.owlrefplatform.core.translator.MappingVocabularyRepair;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.parser.SQLQueryDeepParser;
import it.unibz.inf.ontop.parser.TableNameVisitor;
import org.openrdf.query.parser.QueryParserRegistry;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.rdf.rdfxml.parser.TripleLogger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;

public class OBDAMapper {

	
	private static final Logger logger = (Logger) LoggerFactory.getLogger(OMConstants.LOGGER_NAME);
	private boolean allowToSnapshotMemory = false;

	public OBDAMapper(){
		
		this.disableAllOntopLogger();
	}
	
	
	public OBDAMapping createOBDAMapping(OWLOntology sourceOntology, OWLOntology targetOntology, OBDAModel sourceObdaModel, AnnotationQueries annoQ) 
			throws InvalidDataSourcesNumberException{

		//TODO: check the number of data sources
		List<OBDADataSource> odsList = sourceObdaModel.getSources();
		if(odsList.size() > 1)
			throw new InvalidDataSourcesNumberException(odsList.size());

		//Construct EBDA Model
		logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "Start constucting an OBDA Mapping"));
		OMObjectFactory omFact = OMObjectFactory.getInstance();
		OBDAMapping obdaMapping = null;
		try {
			obdaMapping = omFact.createOBDAMapping(sourceOntology, targetOntology, sourceObdaModel, annoQ);
		} catch (it.unibz.inf.kaos.obdamapper.exception.InvalidAnnotationException e) {
			e.printStackTrace();
		}
		
		if(obdaMapping == null)
			//TODO: throws OBDAMapping creation failure exception
		
		logger.info(String.format(OMConstants.LOG_INFO_TEMPLATE, "Finish constucting OBDA Mapping"));

		return obdaMapping;	
	}

	public OWLOntology materializeTargetOntology(OBDAMapping obdaMapping) throws Exception{

		if (obdaMapping == null || !obdaMapping.isValid())
			throw new Exception("Invalid OBDAMapping input");
			//TODO: create a better exception? A more informative one?

		return new OBDAMaterializerImpl(obdaMapping).getMaterializedOWLOntology();
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// LOGGER RELATED STUFF
	///////////////////////////////////////////////////////////////////////////////////
		
		public void setVerboseMode(boolean verbose){
			
			if(verbose){
				
				logger.setLevel(Level.ALL);
				((Logger) LoggerFactory.getLogger("EBDAReasoner")).setLevel(Level.ALL);
				
			}else{
				
				logger.setLevel(Level.OFF);
				((Logger) LoggerFactory.getLogger("EBDAReasoner")).setLevel(Level.OFF);
			}
		}
			
		public void disableAllOntopLogger(){
			
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestStatement.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Quest.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestQueryProcessor.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SparqlAlgebraToDatalogTranslator.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestOWL.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestUnfolder.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SQLQueryDeepParser.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OBDAModelImpl.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TripleLogger.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OWLAPITranslatorUtility.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OWLAPITranslatorOWL2QL.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(MappingVocabularyRepair.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QueryParserRegistry.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(DatalogUnfolder.class)).setLevel(ch.qos.logback.classic.Level.OFF);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TableNameVisitor.class)).setLevel(ch.qos.logback.classic.Level.OFF);
		}
	
		public void enableAllOntopLogger(){
			
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestStatement.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Quest.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestQueryProcessor.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SparqlAlgebraToDatalogTranslator.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestOWL.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QuestUnfolder.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SQLQueryDeepParser.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OBDAModelImpl.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TripleLogger.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OWLAPITranslatorUtility.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(OWLAPITranslatorOWL2QL.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(MappingVocabularyRepair.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(QueryParserRegistry.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(DatalogUnfolder.class)).setLevel(ch.qos.logback.classic.Level.ALL);
			((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TableNameVisitor.class)).setLevel(ch.qos.logback.classic.Level.ALL);
		}
	
		public void turnOnMemorySnapshot(){
			this.allowToSnapshotMemory = true;
		}
	
		public void turnOffMemorySnapshot(){
			this.allowToSnapshotMemory = false;
		}
	
		private void snapshotMemory(){
			
			if(allowToSnapshotMemory){
				DecimalFormat f = new DecimalFormat("###,###.###");
				logger.info(String.format("maxMemory: \t %15s", f.format(Runtime.getRuntime().maxMemory())));
				logger.info(String.format("freeMemory: \t %15s", f.format(Runtime.getRuntime().freeMemory())));
				logger.info(String.format("totalMemory: \t %15s", f.format(Runtime.getRuntime().totalMemory())));
				logger.info(String.format("UsedMemory: \t %15s", f.format((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()))));
			}
		}
		
	///////////////////////////////////////////////////////////////////////////////////
	// END OF LOGGER RELATED STUFF
	///////////////////////////////////////////////////////////////////////////////////

	
	
}
