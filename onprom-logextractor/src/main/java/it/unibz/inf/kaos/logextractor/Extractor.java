package it.unibz.inf.kaos.logextractor;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Properties;

public interface Extractor<L> {
    L extractLog(SQLPPMapping ebdaModel, Properties dataSourceProperties);

    L extractLog(OWLOntology domainOntology, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries annotation);

    L extractLog(OWLOntology domainOnto, SQLPPMapping obdaModel, Properties dataSourceProperties, AnnotationQueries firstAnnoQueries, OWLOntology eventOntoVariant, AnnotationQueries secondAnnoQueries);
}
