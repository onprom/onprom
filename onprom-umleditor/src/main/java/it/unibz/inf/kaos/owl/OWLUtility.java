/*
 * onprom-umleditor
 *
 * OWLUtility.java
 *
 * Copyright (C) 2016-2017 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 *  KAOS: Knowledge-Aware Operational Support project
 *  (https://kaos.inf.unibz.it).
 *
 *  Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.owl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Utility class to use with OWL ontologies
 * <p>
 * @author T. E. Kalayci
 * 17-Oct-16
 */
public class OWLUtility {
    final static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    final static OWLDataFactory factory = manager.getOWLDataFactory();
    private static final Logger logger = LoggerFactory.getLogger(OWLUtility.class.getSimpleName());
    private final static String DOMAIN = "domain";
    private final static String RANGE = "range";
    private final static String ONPROM_IRI = "http://kaos.inf.unibz.it/onprom/";
    private final static String COORDINATES = "coordinates";
    private final static String DISJOINT_COORDINATES = "disjointCoordinates";
    private final static String ISA_COORDINATES = "subclassCoordinates";
    private final static String ASSOCIATION = "association";
    private final static String ASSOCIATION_TYPE = "type";

    static IRI getIRI(IRI defaultIRI, String name) {
        return IRI.create(defaultIRI + name);
    }

    static OWLAnnotation getDisjointCoordinateAnnotation(String cls, String coordinates) {
        return getCoordinatesAnnotation(getDisjointCoordinatesIRI(cls), coordinates);
    }

    static OWLAnnotation getISACoordinateAnnotation(String cls, String coordinates) {
        return getCoordinatesAnnotation(getISACoordinatesIRI(cls), coordinates);
    }

    static OWLAnnotation getCoordinatesAnnotation(String coordinates) {
        return getCoordinatesAnnotation(getCoordinatesIRI(), coordinates);
    }

    private static OWLAnnotation getCoordinatesAnnotation(IRI iri, String coordinates) {
        return factory.getOWLAnnotation(factory.getOWLAnnotationProperty(iri), factory.getOWLLiteral(coordinates));
    }

    static OWLAnnotation getAssociationAnnotation(String associationName) {
        return factory.getOWLAnnotation(factory.getOWLAnnotationProperty(getAssociationIRI()), factory.getOWLLiteral(associationName));
    }

    static OWLAnnotation getCommentAnnotation(String value) {
        return factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(value));
    }

    static IRI getAssociationIRI() {
        return IRI.create(ONPROM_IRI + ASSOCIATION);
    }

    private static OWLAnnotation getTypeAnnotation(String type) {
        return factory.getOWLAnnotation(factory.getOWLAnnotationProperty(getTypeIRI()), factory.getOWLLiteral(type));
    }

    static OWLAnnotation getDomainAnnotation() {
        return getTypeAnnotation(DOMAIN);
    }

    static OWLAnnotation getRangeAnnotation() {
        return getTypeAnnotation(RANGE);
    }

    static boolean isDomain(String string) {
        return string.equalsIgnoreCase(DOMAIN);
    }

    static boolean isRange(String string) {
        return string.equalsIgnoreCase(RANGE);
    }

    static IRI getTypeIRI() {
        return IRI.create(ONPROM_IRI + ASSOCIATION_TYPE);
    }

    static IRI getCoordinatesIRI() {
        return IRI.create(ONPROM_IRI + COORDINATES);
    }

    static IRI getDisjointCoordinatesIRI(String cls) {
        return IRI.create(ONPROM_IRI + DISJOINT_COORDINATES + cls);
    }

    static IRI getISACoordinatesIRI(String cls) {
        return IRI.create(ONPROM_IRI + ISA_COORDINATES + cls);
    }

    static void checkOWL2QLCompliance(OWLOntology ontology) {
        new OWL2QLProfile().checkOntology(ontology).getViolations().forEach(violation -> logger.error(violation.toString()));
    }

    static void saveOntology(OWLOntology ontology, File file) throws Exception {
        if (file.getName().endsWith(".ttl")) {
            manager.saveOntology(ontology, new TurtleDocumentFormat(), new FileOutputStream(file));
        } else if (file.getName().endsWith(".xml")) {
            manager.saveOntology(ontology, new OWLXMLDocumentFormat(), new FileOutputStream(file));
        } else {
            manager.saveOntology(ontology, new RDFXMLDocumentFormat(), new FileOutputStream(file));
        }
    }

    public static OWLOntology loadOntologyFromFile(File file) {
        if (file == null)
            return null;
        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
            manager.removeOntology(ontology);
            return ontology;
        } catch (Exception e) {
            logger.error("An error is occured during loading " + file.getName() + ": " + e.getMessage(), e);
        }
        return null;
    }

    public static String getDocumentIRI(OWLOntology ontology) {
        final IRI iri = ontology.getOntologyID().getOntologyIRI().orNull();
        if (iri != null) return iri.toString();
        return null;
    }
}
