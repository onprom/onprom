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
import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
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
    public final static String REIFICATION_SEPARATOR = "_2_";
    private final static Logger logger = LoggerFactory.getLogger(OWLExporter.class.getName());

    public static OWLOntology export(String documentIRI, Set<DiagramShape> shapes, File file) {
        try {
            OWLOntology ontology = createOWLOntology(documentIRI, shapes);
            if (ontology != null) {
                checkOWL2QLCompliance(ontology);
                if (file != null) {
                    saveOntology(ontology, file);
                }
                manager.removeOntology(ontology);
                return ontology;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static OWLOntology createOWLOntology(String documentIRI, Set<DiagramShape> shapes) {
        try {
            if (!documentIRI.endsWith("/")) {
                documentIRI = documentIRI + "/";
            }
            final IRI defaultIRI = IRI.create(documentIRI);
            final OWLOntology ontology = manager.createOntology(new OWLOntologyID(Optional.of(defaultIRI), Optional.absent()));
            shapes.forEach(shape -> {
                if (shape instanceof UMLClass) {
                    manager.addAxioms(ontology, getClassAxioms(defaultIRI, (UMLClass) shape));
                }
                if (shape instanceof Relationship) {
                    manager.addAxioms(ontology, getRelationAxioms(defaultIRI, (Relationship) shape));
                }
            });
            return ontology;
        } catch (OWLOntologyCreationException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

    }

    private static Set<OWLAxiom> getRelationAxioms(IRI defaultIRI, Relationship relation) {
        final Set<OWLAxiom> axioms = new HashSet<>();
        //IRI defining the first (domain) class of the relation
        String firstClassName = relation.getFirstClass().getName();
        IRI firstClassIRI = getIRI(defaultIRI, firstClassName);
        //IRI defining the second (range) class of the relation
        String secondClassName = relation.getSecondClass().getName();
        IRI secondClassIRI = getIRI(defaultIRI, secondClassName);
        //get classes from factory
        OWLClass firstClass = factory.getOWLClass(firstClassIRI);
        OWLClass secondClass = factory.getOWLClass(secondClassIRI);
        //we are going to have different axioms for different relation types
        if (relation instanceof Disjoint) {
            //disjointness axiom for Disjoint classes
            axioms.add(factory.getOWLDisjointClassesAxiom(secondClass, firstClass));
            if (relation.getAnchorCount() > 0) {
                axioms.add(factory.getOWLAnnotationAssertionAxiom(firstClassIRI, getDisjointCoordinateAnnotation(secondClassIRI.getShortForm(), relation.getAnchorsString())));
            }
        } else if (relation instanceof Inheritance) {
            //subclass axiom for IS-A relationship
            axioms.add(factory.getOWLSubClassOfAxiom(secondClass, firstClass));
            if (relation.getAnchorCount() > 0)
                axioms.add(factory.getOWLAnnotationAssertionAxiom(firstClassIRI, getISACoordinateAnnotation(secondClassIRI.getShortForm(), relation.getAnchorsString())));
        } else {
            Association rel = (Association) relation;
            if (rel.hasAssociation()) {
                //reification of the relation as an association class
                OWLClass associationClass = factory.getOWLClass(getIRI(defaultIRI, rel.getAssociationClass().getName()));
                //first object property of reificiation
                OWLObjectProperty firstRelation = factory.getOWLObjectProperty(getIRI(defaultIRI, relation.getName() + REIFICATION_SEPARATOR + firstClassName));
                axioms.add(factory.getOWLAnnotationAssertionAxiom(firstRelation.getIRI(), getCoordinatesAnnotation(rel.getAnchorsString())));
                axioms.add(factory.getOWLObjectPropertyDomainAxiom(firstRelation, associationClass));
                axioms.add(factory.getOWLObjectPropertyRangeAxiom(firstRelation, firstClass));
                axioms.add(factory.getOWLFunctionalObjectPropertyAxiom(firstRelation));
                axioms.add(factory.getOWLSubClassOfAxiom(associationClass, factory.getOWLObjectSomeValuesFrom(firstRelation, factory.getOWLThing())));
                if (rel.getSecondMultiplicity().isFunctional()) {
                    axioms.add(factory.getOWLFunctionalObjectPropertyAxiom(firstRelation.getInverseProperty()));
                }
                if (rel.getSecondMultiplicity().isExistential()) {
                    axioms.add(factory.getOWLSubClassOfAxiom(firstClass, factory.getOWLObjectSomeValuesFrom(firstRelation.getInverseProperty(), factory.getOWLThing())));
                }
                //indicate first association object property as a part of reification
                final OWLAnnotation associationAnnotation = OWLUtility.getAssociationAnnotation(relation.getName());
                axioms.add(factory.getOWLAnnotationAssertionAxiom(firstRelation.getIRI(), associationAnnotation));
                axioms.add(factory.getOWLAnnotationAssertionAxiom(firstRelation.getIRI(), getDomainAnnotation()));
                //second object property of reificiation
                OWLObjectProperty secondRelation = factory.getOWLObjectProperty(getIRI(defaultIRI, relation.getName() + REIFICATION_SEPARATOR + secondClassName));
                axioms.add(factory.getOWLObjectPropertyDomainAxiom(secondRelation, associationClass));
                axioms.add(factory.getOWLObjectPropertyRangeAxiom(secondRelation, secondClass));
                axioms.add(factory.getOWLFunctionalObjectPropertyAxiom(secondRelation));
                axioms.add(factory.getOWLSubClassOfAxiom(associationClass, factory.getOWLObjectSomeValuesFrom(secondRelation, factory.getOWLThing())));
                if (rel.getFirstMultiplicity().isFunctional()) {
                    axioms.add(factory.getOWLFunctionalObjectPropertyAxiom(secondRelation.getInverseProperty()));
                }
                if (rel.getFirstMultiplicity().isExistential()) {
                    axioms.add(factory.getOWLSubClassOfAxiom(secondClass, factory.getOWLObjectSomeValuesFrom(secondRelation.getInverseProperty(), factory.getOWLThing())));
                }
                //indicate second association object property as a part of reification
                axioms.add(factory.getOWLAnnotationAssertionAxiom(secondRelation.getIRI(), associationAnnotation));
                //indicate association class as a part of reification
                axioms.add(factory.getOWLAnnotationAssertionAxiom(associationClass.getIRI(), associationAnnotation));
                axioms.add(factory.getOWLAnnotationAssertionAxiom(secondRelation.getIRI(), getRangeAnnotation()));
            } else {
                IRI relationIRI = getIRI(defaultIRI, relation.getName());
                //getCardinality IRI of the object property
                //create object property for the relation
                OWLObjectProperty relationProperty = factory.getOWLObjectProperty(relationIRI);
                //first class is the domain of the object property
                OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(relationProperty, firstClass);
                axioms.add(domainAxiom);
                //second class is the range of the object property
                OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(relationProperty, secondClass);
                axioms.add(rangeAxiom);
                //domain cardinality
                if (rel.getFirstMultiplicity().isFunctional()) {
                    axioms.add(factory.getOWLInverseFunctionalObjectPropertyAxiom(relationProperty));
                }
                if (rel.getFirstMultiplicity().isExistential()) {
                    OWLObjectSomeValuesFrom owlSomeValuesFrom = factory.getOWLObjectSomeValuesFrom(relationProperty.getInverseProperty(), factory.getOWLThing());
                    axioms.add(factory.getOWLSubClassOfAxiom(secondClass, owlSomeValuesFrom));
                }
                //range cardinality
                if (rel.getSecondMultiplicity().isFunctional()) {
                    axioms.add(factory.getOWLFunctionalObjectPropertyAxiom(relationProperty));
                }
                if (rel.getSecondMultiplicity().isExistential()) {
                    OWLObjectSomeValuesFrom owlSomeValuesFrom = factory.getOWLObjectSomeValuesFrom(relationProperty, factory.getOWLThing());
                    axioms.add(factory.getOWLSubClassOfAxiom(firstClass, owlSomeValuesFrom));
                }
                //anchors annotation axiom
                if (relation.getAnchorCount() > 0) {
                    axioms.add(factory.getOWLAnnotationAssertionAxiom(relationIRI, getCoordinatesAnnotation(relation.getAnchorsString())));
                }
            }
        }
        return axioms;
    }

    private static Set<OWLAxiom> getClassAxioms(IRI defaultIRI, UMLClass cls) {
        Set<OWLAxiom> axioms = new HashSet<>();
        OWLClass owlClass = factory.getOWLClass(getIRI(defaultIRI, cls.getName()));
        //add class to the ontology
        axioms.add(factory.getOWLDeclarationAxiom(owlClass));
        //add data properties
        cls.getAttributes().forEach(attribute -> {
            //add attribute as data property to the class
            OWLDataProperty dataProperty = factory.getOWLDataProperty(getIRI(defaultIRI, attribute.getName()));
            OWLDatatype dataType = factory.getOWLDatatype(attribute.getType().getIRI());
            axioms.add(factory.getOWLDeclarationAxiom(dataProperty));
            //add its domain
            axioms.add(factory.getOWLDataPropertyDomainAxiom(dataProperty, owlClass));
            //add its range
            axioms.add(factory.getOWLDataPropertyRangeAxiom(dataProperty, dataType));
            //add cardinalities
            if (attribute.isFunctional()) {
                axioms.add(factory.getOWLFunctionalDataPropertyAxiom(dataProperty));
            }
            if (attribute.isExistential()) {
                axioms.add(factory.getOWLSubClassOfAxiom(owlClass, factory.getOWLDataSomeValuesFrom(dataProperty, factory.getTopDatatype())));
            }
        });
        //store diagram coordinates of class
        axioms.add(factory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), getCoordinatesAnnotation(cls.getCoordinates())));
        return axioms;
    }
}