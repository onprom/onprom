package it.unibz.inf.onprom.logextractor;

import it.unibz.inf.onprom.data.query.AnnotationQueries;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.Properties;

public interface Extractor<L> {
    L extractLog(SQLPPMapping ebdaModel, Properties dataSourceProperties) throws Exception;

    L extractLog(OWLOntology domainOntology, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries annotation) throws Exception;

    L extractLog(OWLOntology domainOnto, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries) throws Exception;
}
