package it.unibz.inf.onprom.logextractor;

import it.unibz.inf.onprom.obdamapper.utility.OntopUtility;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public abstract class EBDAReasoner<A, E, O> {
    private static final Logger logger = LoggerFactory.getLogger(EBDAReasoner.class);

    final OntopOWLReasoner reasoner;
    final OntopOWLConnection connection;

    protected EBDAReasoner(SQLPPMapping obdaModel, Properties dataSourceProperties, OWLOntology eventOntology) throws OWLOntologyCreationException {
        try {
            OntopSQLOWLAPIConfiguration config = OntopUtility.getConfiguration(
                    eventOntology,
                    obdaModel,
                    dataSourceProperties
            );
            this.reasoner = OntopOWLFactory.defaultFactory().createReasoner(config);
            this.connection = this.reasoner.getConnection();
            // fix for large query results
            this.connection.setAutoCommit(false);
        } catch (OWLException e) {
            throw new RuntimeException(e);
        }
    }

    public void dispose() {
        try {
            connection.close();
            reasoner.dispose();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


    protected OntopOWLStatement getStatement() throws Exception {
        // fix for large query results
        // st.setFetchSize(1000000);
        return connection.createStatement();
    }

    protected boolean printUnfoldedQueries(String[] queries) {
        try {
            OntopOWLStatement st = getStatement();
            // Unfold queries
            for (String query : queries) {
                st.getRewritingRendering(query);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }


    protected abstract Map<String, A> getAttributes();

    protected abstract Map<String, E> getEvents(Map<String, A> attributes);

    protected abstract Collection<O> getObjects(Map<String, E> events, Map<String, A> attributes);
}
