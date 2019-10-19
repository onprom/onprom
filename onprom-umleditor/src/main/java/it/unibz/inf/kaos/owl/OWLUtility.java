/*
 * onprom-umleditor
 *
 * OWLUtility.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
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
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Optional;

/**
 * Utility class to use with OWL ontologies
 * <p>
 * @author T. E. Kalayci
 */
public class OWLUtility {
    static final OWLOntologyManager ONTOLOGY_MANAGER = OWLManager.createOWLOntologyManager();
    static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();
    private static final Logger LOGGER = LoggerFactory.getLogger(OWLUtility.class.getSimpleName());
    private static final String DOMAIN = "domain";
    private static final String RANGE = "range";
    private static final String ONPROM_IRI = "http://kaos.inf.unibz.it/onprom/";
    private static final String COORDINATES = "coordinates";
    private static final String DISJOINT_COORDINATES = "disjointCoordinates";
    private static final String ISA_COORDINATES = "subclassCoordinates";
    private static final String ASSOCIATION = "association";
    private static final String ASSOCIATION_TYPE = "type";

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
        return DATA_FACTORY.getOWLAnnotation(DATA_FACTORY.getOWLAnnotationProperty(iri), DATA_FACTORY.getOWLLiteral(coordinates));
    }

    static OWLAnnotation getAssociationAnnotation(String associationName) {
        return DATA_FACTORY.getOWLAnnotation(DATA_FACTORY.getOWLAnnotationProperty(getAssociationIRI()), DATA_FACTORY.getOWLLiteral(associationName));
    }

    static OWLAnnotation getCommentAnnotation(String value) {
        return DATA_FACTORY.getOWLAnnotation(DATA_FACTORY.getRDFSComment(), DATA_FACTORY.getOWLLiteral(value));
    }

    static IRI getAssociationIRI() {
        return IRI.create(ONPROM_IRI + ASSOCIATION);
    }

    private static OWLAnnotation getTypeAnnotation(String type) {
        return DATA_FACTORY.getOWLAnnotation(DATA_FACTORY.getOWLAnnotationProperty(getTypeIRI()), DATA_FACTORY.getOWLLiteral(type));
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
        new OWL2QLProfile().checkOntology(ontology).getViolations().forEach(violation -> LOGGER.warn(violation.toString()));
    }

    static void saveOntology(OWLOntology ontology, File file) throws Exception {
        if (file.getName().endsWith(".ttl")) {
            ONTOLOGY_MANAGER.saveOntology(ontology, new TurtleDocumentFormat(), new FileOutputStream(file));
        } else if (file.getName().endsWith(".xml")) {
            ONTOLOGY_MANAGER.saveOntology(ontology, new OWLXMLDocumentFormat(), new FileOutputStream(file));
        } else {
            ONTOLOGY_MANAGER.saveOntology(ontology, new RDFXMLDocumentFormat(), new FileOutputStream(file));
        }
    }

    public static Optional<OWLOntology> loadOntologyFromStream(InputStream stream) {
        if (stream != null) {
            try {
                OWLOntology ontology = ONTOLOGY_MANAGER.loadOntologyFromOntologyDocument(stream);
                ONTOLOGY_MANAGER.removeOntology(ontology);
                return Optional.of(ontology);
            } catch (Exception e) {
                LOGGER.error("An error is occurred during loading: " + e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    public static Optional<OWLOntology> loadOntologyFromFile(File file) {
        if (file != null) {
            try {
                OWLOntology ontology = ONTOLOGY_MANAGER.loadOntologyFromOntologyDocument(file);
                ONTOLOGY_MANAGER.removeOntology(ontology);
                return Optional.of(ontology);
            } catch (Exception e) {
                LOGGER.error("An error is occurred during loading " + file.getName() + ": " + e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    public static String getDocumentIRI(@Nonnull OWLOntology ontology) {
        com.google.common.base.Optional<IRI> ontologyIRI = ontology.getOntologyID().getOntologyIRI();
        if (ontologyIRI.isPresent()) {
            return ontologyIRI.get().toString();
        }
        return "http://www.example.com/example.owl";
    }
}
