/*
 * onprom-umleditor
 *
 * OWLExporter.java
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

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * <p>This class contains static methods for generating <b>OWL ontology</b>,
 * storing created ontology to the file system and checking its compliance with
 * OWL2QL profile</p>
 * <p>
 *
 * @author T. E. Kalayci
 * 05-Oct-16
 */
public class OWLExporter extends OWLUtility {
    public static final String REIFICATION_SEPARATOR = "_2_";
    private static final Logger LOGGER = LoggerFactory.getLogger(OWLExporter.class.getName());

    public static OWLOntology export(String documentIRI, Collection<DiagramShape> shapes, File file) {
        try {
            OWLOntology ontology = createOWLOntology(documentIRI, shapes);
            if (ontology != null) {
                checkOWL2QLCompliance(ontology);
                if (file != null) {
                    saveOntology(ontology, file);
                }
                ONTOLOGY_MANAGER.removeOntology(ontology);
                return ontology;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static OWLOntology createOWLOntology(String documentIRI, Collection<DiagramShape> shapes) {
        try {
            if (!documentIRI.endsWith("/")) {
                documentIRI = documentIRI + "/";
            }
            final IRI defaultIRI = IRI.create(documentIRI);
            final OWLOntology ontology = ONTOLOGY_MANAGER.createOntology(new OWLOntologyID(Optional.of(defaultIRI), Optional.absent()));
            shapes.forEach(shape -> {
                if (shape instanceof UMLClass) {
                    ONTOLOGY_MANAGER.addAxioms(ontology, getClassAxioms(defaultIRI, (UMLClass) shape));
                }
                if (shape instanceof Relationship) {
                    ONTOLOGY_MANAGER.addAxioms(ontology, getRelationAxioms(defaultIRI, (Relationship) shape));
                }
            });
            return ontology;
        } catch (OWLOntologyCreationException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

    }

    private static Set<OWLAxiom> getRelationAxioms(IRI defaultIRI, Relationship relation) {
        final Set<OWLAxiom> axioms = Sets.newHashSet();
        //IRI defining the first (domain) class of the relation
        String firstClassName = relation.getFirstClass().getName();
        IRI firstClassIRI = getIRI(defaultIRI, firstClassName);
        //IRI defining the second (range) class of the relation
        String secondClassName = relation.getSecondClass().getName();
        IRI secondClassIRI = getIRI(defaultIRI, secondClassName);
        //get classes from factory
        OWLClass firstClass = DATA_FACTORY.getOWLClass(firstClassIRI);
        OWLClass secondClass = DATA_FACTORY.getOWLClass(secondClassIRI);
        //we are going to have different axioms for different relation types
        if (relation instanceof Disjoint) {
            //disjointness axiom for Disjoint classes
            axioms.add(DATA_FACTORY.getOWLDisjointClassesAxiom(secondClass, firstClass));
            if (relation.getAnchorCount() > 0) {
                axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(firstClassIRI, getDisjointCoordinateAnnotation(secondClassIRI.getShortForm(), relation.getAnchorsString())));
            }
        } else if (relation instanceof Inheritance) {
            //subclass axiom for IS-A relationship
            axioms.add(DATA_FACTORY.getOWLSubClassOfAxiom(secondClass, firstClass));
            if (relation.getAnchorCount() > 0) {
                axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(firstClassIRI, getISACoordinateAnnotation(secondClassIRI.getShortForm(), relation.getAnchorsString())));
            }
        } else {
            Association rel = (Association) relation;
            if (rel.hasAssociation()) {
                //reification of the relation as an association class
                OWLClass associationClass = DATA_FACTORY.getOWLClass(getIRI(defaultIRI, rel.getAssociationClass().getName()));
                //first object property of reification
                OWLObjectProperty firstRelation = DATA_FACTORY.getOWLObjectProperty(getIRI(defaultIRI, relation.getName() + REIFICATION_SEPARATOR + firstClassName));
                axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(firstRelation.getIRI(), getCoordinatesAnnotation(rel.getAnchorsString())));
                axioms.add(DATA_FACTORY.getOWLObjectPropertyDomainAxiom(firstRelation, associationClass));
                axioms.add(DATA_FACTORY.getOWLObjectPropertyRangeAxiom(firstRelation, firstClass));
                axioms.add(DATA_FACTORY.getOWLFunctionalObjectPropertyAxiom(firstRelation));
                axioms.add(DATA_FACTORY.getOWLSubClassOfAxiom(associationClass, DATA_FACTORY.getOWLObjectSomeValuesFrom(firstRelation, DATA_FACTORY.getOWLThing())));
                if (rel.getSecondMultiplicity().isFunctional()) {
                    axioms.add(DATA_FACTORY.getOWLFunctionalObjectPropertyAxiom(firstRelation.getInverseProperty()));
                }
                if (rel.getSecondMultiplicity().isExistential()) {
                    axioms.add(DATA_FACTORY.getOWLSubClassOfAxiom(firstClass, DATA_FACTORY.getOWLObjectSomeValuesFrom(firstRelation.getInverseProperty(), DATA_FACTORY.getOWLThing())));
                }
                //indicate first association object property as a part of reification
                final OWLAnnotation associationAnnotation = OWLUtility.getAssociationAnnotation(relation.getName());
                axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(firstRelation.getIRI(), associationAnnotation));
                axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(firstRelation.getIRI(), getDomainAnnotation()));
                //second object property of reification
                OWLObjectProperty secondRelation = DATA_FACTORY.getOWLObjectProperty(getIRI(defaultIRI, relation.getName() + REIFICATION_SEPARATOR + secondClassName));
                axioms.add(DATA_FACTORY.getOWLObjectPropertyDomainAxiom(secondRelation, associationClass));
                axioms.add(DATA_FACTORY.getOWLObjectPropertyRangeAxiom(secondRelation, secondClass));
                axioms.add(DATA_FACTORY.getOWLFunctionalObjectPropertyAxiom(secondRelation));
                axioms.add(DATA_FACTORY.getOWLSubClassOfAxiom(associationClass, DATA_FACTORY.getOWLObjectSomeValuesFrom(secondRelation, DATA_FACTORY.getOWLThing())));
                if (rel.getFirstMultiplicity().isFunctional()) {
                    axioms.add(DATA_FACTORY.getOWLFunctionalObjectPropertyAxiom(secondRelation.getInverseProperty()));
                }
                if (rel.getFirstMultiplicity().isExistential()) {
                    axioms.add(DATA_FACTORY.getOWLSubClassOfAxiom(secondClass, DATA_FACTORY.getOWLObjectSomeValuesFrom(secondRelation.getInverseProperty(), DATA_FACTORY.getOWLThing())));
                }
                //indicate second association object property as a part of reification
                axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(secondRelation.getIRI(), associationAnnotation));
                //indicate association class as a part of reification
                axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(associationClass.getIRI(), associationAnnotation));
                axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(secondRelation.getIRI(), getRangeAnnotation()));
            } else {
                IRI relationIRI = getIRI(defaultIRI, relation.getName());
                //getCardinality IRI of the object property
                //create object property for the relation
                OWLObjectProperty relationProperty = DATA_FACTORY.getOWLObjectProperty(relationIRI);
                //first class is the domain of the object property
                OWLObjectPropertyDomainAxiom domainAxiom = DATA_FACTORY.getOWLObjectPropertyDomainAxiom(relationProperty, firstClass);
                axioms.add(domainAxiom);
                //second class is the range of the object property
                OWLObjectPropertyRangeAxiom rangeAxiom = DATA_FACTORY.getOWLObjectPropertyRangeAxiom(relationProperty, secondClass);
                axioms.add(rangeAxiom);
                //domain cardinality
                if (rel.getFirstMultiplicity().isFunctional()) {
                    axioms.add(DATA_FACTORY.getOWLInverseFunctionalObjectPropertyAxiom(relationProperty));
                }
                if (rel.getFirstMultiplicity().isExistential()) {
                    OWLObjectSomeValuesFrom owlSomeValuesFrom = DATA_FACTORY.getOWLObjectSomeValuesFrom(relationProperty.getInverseProperty(), DATA_FACTORY.getOWLThing());
                    axioms.add(DATA_FACTORY.getOWLSubClassOfAxiom(secondClass, owlSomeValuesFrom));
                }
                //range cardinality
                if (rel.getSecondMultiplicity().isFunctional()) {
                    axioms.add(DATA_FACTORY.getOWLFunctionalObjectPropertyAxiom(relationProperty));
                }
                if (rel.getSecondMultiplicity().isExistential()) {
                    OWLObjectSomeValuesFrom owlSomeValuesFrom = DATA_FACTORY.getOWLObjectSomeValuesFrom(relationProperty, DATA_FACTORY.getOWLThing());
                    axioms.add(DATA_FACTORY.getOWLSubClassOfAxiom(firstClass, owlSomeValuesFrom));
                }
                //anchors annotation axiom
                if (relation.getAnchorCount() > 0) {
                    axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(relationIRI, getCoordinatesAnnotation(relation.getAnchorsString())));
                }
            }
        }
        return axioms;
    }

    private static Set<OWLAxiom> getClassAxioms(IRI defaultIRI, UMLClass cls) {
        Set<OWLAxiom> axioms = Sets.newHashSet();
        OWLClass owlClass = DATA_FACTORY.getOWLClass(getIRI(defaultIRI, cls.getName()));
        //add class to the ontology
        axioms.add(DATA_FACTORY.getOWLDeclarationAxiom(owlClass));
        //add data properties
        cls.getAttributes().forEach(attribute -> {
            //add attribute as data property to the class
            OWLDataProperty dataProperty = DATA_FACTORY.getOWLDataProperty(getIRI(defaultIRI, attribute.getName()));
            OWLDatatype dataType = DATA_FACTORY.getOWLDatatype(attribute.getType().getIRI());
            axioms.add(DATA_FACTORY.getOWLDeclarationAxiom(dataProperty));
            //add its domain
            axioms.add(DATA_FACTORY.getOWLDataPropertyDomainAxiom(dataProperty, owlClass));
            //add its range
            axioms.add(DATA_FACTORY.getOWLDataPropertyRangeAxiom(dataProperty, dataType));
            //add cardinalities
            if (attribute.isFunctional()) {
                axioms.add(DATA_FACTORY.getOWLFunctionalDataPropertyAxiom(dataProperty));
            }
            if (attribute.isExistential()) {
                axioms.add(DATA_FACTORY.getOWLSubClassOfAxiom(owlClass, DATA_FACTORY.getOWLDataSomeValuesFrom(dataProperty, DATA_FACTORY.getTopDatatype())));
            }
        });
        //store diagram coordinates of class
        axioms.add(DATA_FACTORY.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), getCoordinatesAnnotation(cls.getCoordinates())));
        return axioms;
    }
}