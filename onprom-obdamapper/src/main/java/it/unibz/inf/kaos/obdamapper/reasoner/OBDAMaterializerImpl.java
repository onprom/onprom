package it.unibz.inf.kaos.obdamapper.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConnection;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration.Builder;

/*
 * the idea is that this class contains various ways of materializing the given OBDA System
 */
public class OBDAMaterializerImpl extends OBDAMaterializerAbstract implements OBDAMaterializer{

	private static final Logger logger = (Logger) LoggerFactory.getLogger(OBDAMaterializer.class);
	private QuestOWL questReasoner;
	private OWLOntology targetOntology;
	private OWLOntologyManager ontoMan = null;
	private OWLDataFactory owlDataFactory = null;
	
	//Some query templates
	private String qClassRetrieverTemplate = 
			  "SELECT DISTINCT ?class "
			+ "WHERE{ ?class a %s . "
			+ "}";
	private String qClassRetrieverTemplateAnsVar = "class";

	private String qObjectPropertyRetrieverTemplate = 
			  "SELECT DISTINCT ?firstComp ?secondComp "
			+ "WHERE{ ?firstComp %s ?secondComp . "
			+ "}";
	private String qObjectPropertyRetrieverTemplateFirstAnsVar = "firstComp";
	private String qObjectPropertyRetrieverTemplateSecondAnsVar = "secondComp";

	private String qDataPropertyRetrieverTemplate = 
			  "SELECT DISTINCT ?firstComp ?secondComp "
			+ "WHERE{ ?firstComp %s ?secondComp . "
			+ "}";
	private String qDataPropertyRetrieverTemplateFirstAnsVar = "firstComp";
	private String qDataPropertyRetrieverTemplateSecondAnsVar = "secondComp";
	//END OF Some query templates
	
	/**
	 * Initializes OBDAMaterializer based on the given OBDAMapping.
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param obdaMapping
	 */
	public OBDAMaterializerImpl(OBDAMapping obdaMapping){
		
		super.setExecutionLogListener(null);
		
		this.ontoMan = OWLManager.createOWLOntologyManager();
		this.owlDataFactory = ontoMan.getOWLDataFactory();
		
    	//Create an instance of Quest OWL reasoner.
			QuestOWLFactory factory = new QuestOWLFactory();
			QuestPreferences preferences = new QuestPreferences();
			preferences.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			preferences.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);
	
	        Builder builder = QuestOWLConfiguration.builder();
			builder.obdaModel(obdaMapping);
			builder.preferences(preferences);
			
			QuestOWLConfiguration config = builder.build();
			
			this.targetOntology = obdaMapping.getTargetOntology();
			
			//create a quest reasoner with the given obdaMapping and the corresponding target ontology
			questReasoner = factory.createReasoner(this.targetOntology, config);
    	//END OF Creating an instance of Quest OWL reasoner.
			
		this.disableLocalLogging();
	}

	
	public OWLOntology getMaterializedOWLOntology() throws OWLException{
		
		//----------------------------------------------------------------------------------------
		//Preparing the resulting Ontology
		//----------------------------------------------------------------------------------------
		OWLOntology result = cloneTargetOntology();
		//----------------------------------------------------------------------------------------
		//END OF Preparing the resulting Ontology
		//----------------------------------------------------------------------------------------

		ArrayList<AddAxiom> newAxioms = new ArrayList<AddAxiom>();
		StringBuilder log = new StringBuilder();

		//----------------------------------------------------------------------------------------
		//Populating the classes
		//----------------------------------------------------------------------------------------
		List<OWLClassAssertionAxiom> classAxioms = retrieveAllClassInstances();
		
		log.append("\n--------------------------------------\nAdding class assertion axioms\n--------------------------------------\n");
		if(classAxioms != null){
			for(OWLClassAssertionAxiom clsAxiom: classAxioms){
	
				log.append("New class assertion Axiom: ");
				log.append(clsAxiom);
				log.append("\n");
				newAxioms.add(new AddAxiom(result, clsAxiom));
			}
			
			logger.info(log.toString());
			log.delete(0, log.length());
		}
		//----------------------------------------------------------------------------------------
		//END OF Populating the classes
		//----------------------------------------------------------------------------------------

		//----------------------------------------------------------------------------------------
		//Populating the object properties
		//----------------------------------------------------------------------------------------
		List<OWLObjectPropertyAssertionAxiom> objectPropertyAxioms = retrieveAllObjectPropertiesInstances();
		
		log.append("\n--------------------------------------\nAdding object property assertion axioms\n--------------------------------------\n");
		if(objectPropertyAxioms != null){
			for(OWLObjectPropertyAssertionAxiom opAxiom: objectPropertyAxioms){
				
				log.append("New object property assertion Axiom: ");
				log.append(opAxiom);
				log.append("\n");
				newAxioms.add(new AddAxiom(result, opAxiom));
			}
			
			logger.info(log.toString());
			log.delete(0, log.length());
		}

		//----------------------------------------------------------------------------------------
		//END OF Populating the object properties
		//----------------------------------------------------------------------------------------

		//----------------------------------------------------------------------------------------
		//Populating the data properties
		//----------------------------------------------------------------------------------------
		List<OWLDataPropertyAssertionAxiom> dataPropertyAxioms = retrieveAllDataPropertiesInstances();
		
		log.append("\n--------------------------------------\nAdding data property assertion axioms\n--------------------------------------\n");
		if(dataPropertyAxioms != null){
						
			for(OWLDataPropertyAssertionAxiom dpAxiom: dataPropertyAxioms){
				
				log.append("New data property assertion Axiom: ");
				log.append(dpAxiom);
				log.append("\n");
				newAxioms.add(new AddAxiom(result, dpAxiom));
			}
			
			logger.info(log.toString());
			log.delete(0, log.length());
		}

		//----------------------------------------------------------------------------------------
		//END OF Populating the data properties
		//----------------------------------------------------------------------------------------

		//add all new axioms to the ontology
		result.getOWLOntologyManager().applyChanges(newAxioms);
				
		return result;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Some supporting methods
	//////////////////////////////////////////////////////////////////////////////

	public List<OWLClassAssertionAxiom> retrieveAllClassInstances() throws OWLException{

		//Collect all class names in the target ontology
		Set<OWLClass> classes = this.targetOntology.getClassesInSignature();
		ArrayList<OWLClassAssertionAxiom> results = new ArrayList<OWLClassAssertionAxiom>();
		
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	

        	OWLIndividual clsInstance;
        	
	        for(OWLClass cls: classes){
	        	clsInstance = null;
	        	
	    		//System.out.println("\n\n------------------------------------\nProcessing the OWLClass "+ cls+"\n------------------------------------");
	        	
	        	String q = String.format(this.qClassRetrieverTemplate, cls.toString());
	        	
	        	//System.out.println("query: \n\t" + q +"\n");
	        	
	    		QuestOWLResultSet rs = st.executeTuple(q);
	    		
	    		//System.out.println("Creating Class Axiom for the class "+ cls);
	    		while(rs.nextRow()){
	    			
	    			clsInstance = rs.getOWLIndividual(this.qClassRetrieverTemplateAnsVar);
	    			//System.out.println("clsInstance: "+clsInstance);
	    			
	    			results.add(this.owlDataFactory.getOWLClassAssertionAxiom(cls, clsInstance));
	    		}
	        }
		}finally {
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
		}

        return results;
	}

	public List<OWLObjectPropertyAssertionAxiom> retrieveAllObjectPropertiesInstances() throws OWLException{

		//Collect all class names in the target ontology
		Set<OWLObjectProperty> objectProperties = this.targetOntology.getObjectPropertiesInSignature();
		ArrayList<OWLObjectPropertyAssertionAxiom> results = new ArrayList<OWLObjectPropertyAssertionAxiom>();
		
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	

        	OWLIndividual firstInst;
        	OWLIndividual secondInst;
        	
	        for(OWLObjectProperty op: objectProperties){
	        	firstInst = null; secondInst = null;
	        	
	    		//System.out.println("\n\n------------------------------------\nProcessing the OWLObjectProperty "+ op+"\n------------------------------------");
	        	
	        	String q = String.format(this.qObjectPropertyRetrieverTemplate, op.toString());
	        	
	        	//System.out.println("query: \n\t" + q +"\n");
	        	
	    		QuestOWLResultSet rs = st.executeTuple(q);
	    		
	    		//System.out.println("Creating Object Property Axiom for the object property"+ op);
	    		while(rs.nextRow()){
	    			
	    			firstInst = rs.getOWLIndividual(this.qObjectPropertyRetrieverTemplateFirstAnsVar);
	    			secondInst = rs.getOWLIndividual(this.qObjectPropertyRetrieverTemplateSecondAnsVar);
	    			results.add(this.owlDataFactory.getOWLObjectPropertyAssertionAxiom(op, firstInst, secondInst));
	    		}
	        }
		}finally {
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
		}

        return results;
	}

	public List<OWLDataPropertyAssertionAxiom> retrieveAllDataPropertiesInstances() throws OWLException{

		//Collect all class names in the target ontology
		Set<OWLDataProperty> dataProperties = this.targetOntology.getDataPropertiesInSignature();
		ArrayList<OWLDataPropertyAssertionAxiom> results = new ArrayList<OWLDataPropertyAssertionAxiom>();
		
		QuestOWLConnection conn = questReasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {	

        	OWLIndividual firstInst;
        	OWLLiteral secondInst;
        	
	        for(OWLDataProperty dp: dataProperties){
	        	firstInst = null; secondInst = null;
	        	
	    		//System.out.println("\n\n------------------------------------\nProcessing the OWLDataProperty "+ dp+"\n------------------------------------");
	        	
	        	String q = String.format(this.qDataPropertyRetrieverTemplate, dp.toString());
	        	
	        	//System.out.println("query: \n\t" + q +"\n");
	        	
	    		QuestOWLResultSet rs = st.executeTuple(q);
	    		
	    		//System.out.println("Creating Object Property Axiom for the object property"+ op);
	    		while(rs.nextRow()){
	    			
	    			firstInst = rs.getOWLIndividual(this.qDataPropertyRetrieverTemplateFirstAnsVar);
	    			secondInst = rs.getOWLLiteral(this.qDataPropertyRetrieverTemplateSecondAnsVar);
	    			results.add(this.owlDataFactory.getOWLDataPropertyAssertionAxiom(dp, firstInst, secondInst));
	    		}
	        }
		}finally {
			if (st != null && !st.isClosed()) st.close();
			if (conn != null && !conn.isClosed()) conn.close();
		}

        return results;
	}
	
	private OWLOntology cloneTargetOntology() throws OWLOntologyCreationException{
		HashSet<OWLOntology> targets = new HashSet<OWLOntology>();
		IRI targetOntoIRI = this.targetOntology.getOntologyID().getOntologyIRI().get();
		targets.add(this.targetOntology);
		return ontoMan.createOntology(targetOntoIRI, targets, false);
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// END OF Some supporting methods
	//////////////////////////////////////////////////////////////////////////////

	
	
	//////////////////////////////////////////////////////////////////////////////
	// Some utility methods
	//////////////////////////////////////////////////////////////////////////////
	
	public void disableLocalLogging(){
		((ch.qos.logback.classic.Logger) logger).setLevel(ch.qos.logback.classic.Level.OFF);
	}

	public void enableLocalLogging(){
		((ch.qos.logback.classic.Logger) logger).setLevel(ch.qos.logback.classic.Level.ALL);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}


	//////////////////////////////////////////////////////////////////////////////
	// END OF Some utility methods
	//////////////////////////////////////////////////////////////////////////////

}
