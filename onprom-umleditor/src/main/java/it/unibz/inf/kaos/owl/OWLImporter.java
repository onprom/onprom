/*
 * onprom-umleditor
 *
 * OWLImporter.java
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

import it.unibz.inf.kaos.data.Association;
import it.unibz.inf.kaos.data.AssociationClass;
import it.unibz.inf.kaos.data.Attribute;
import it.unibz.inf.kaos.data.Cardinality;
import it.unibz.inf.kaos.data.DataType;
import it.unibz.inf.kaos.data.Disjoint;
import it.unibz.inf.kaos.data.Inheritance;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

/**
 * Class used to import OWL ontologies to the tool.
 * <p>
 * @author T. E. Kalayci
 * 11-Oct-16
 */
public class OWLImporter extends OWLUtility {
  public static Set<DiagramShape> getShapes(OWLOntology ontology) {
    LinkedList<String> messages = new LinkedList<>();

    HashMap<OWLClass, UMLClass> umlClasses = new HashMap<>();
    HashMap<String, AssociationClass> associationClasses = new HashMap<>();

    HashSet<OWLDataPropertyExpression> existentialDataProperties = new HashSet<>();
    HashSet<OWLDataPropertyExpression> functionalDataProperties = new HashSet<>();

    HashSet<OWLObjectPropertyExpression> existentialObjectProperties = new HashSet<>();
    HashSet<OWLObjectPropertyExpression> inverseExistentialObjectProperties = new HashSet<>();

    HashSet<OWLObjectPropertyExpression> functionalObjectProperties = new HashSet<>();
    HashSet<OWLObjectPropertyExpression> inverserFunctionalObjectProperties = new HashSet<>();

    Set<DiagramShape> shapes = new LinkedHashSet<>();

    UMLClass thingClass = new UMLClass("Thing");
    boolean thingAdded = false;
    //classes
    for (OWLClass owlClass : ontology.getClassesInSignature()) {
      if (!owlClass.isTopEntity()) {
        UMLClass umlClass;
        String assocString = getAssociation(ontology, owlClass.getIRI());
        if (assocString != null) {
          Association association = new Association(owlClass.getIRI().getShortForm(), owlClass.getIRI().toString());
          umlClass = new AssociationClass(association);
          associationClasses.put(assocString, (AssociationClass) umlClass);
        } else {
          umlClass = new UMLClass(owlClass.getIRI().getShortForm(), owlClass.getIRI().toString());
        }
        umlClass.setCoordinates(getCoordinates(ontology, owlClass.getIRI(), OWLExporter.getCoordinatesIRI()));
        umlClasses.put(owlClass, umlClass);
      }
    }
    //disjoint relations
    for (OWLDisjointClassesAxiom owlDisjointClassesAxiom : ontology.getAxioms(AxiomType.DISJOINT_CLASSES)) {
      OWLClass[] disjointClassess = owlDisjointClassesAxiom.getClassesInSignature().toArray(new OWLClass[]{});
      UMLClass first = umlClasses.get(disjointClassess[0]);
      UMLClass second = umlClasses.get(disjointClassess[1]);
      if (first != null && second != null) {
        Disjoint disjoint = new Disjoint(first, second);
        disjoint.setAnchorCoordinates(getCoordinates(ontology, disjointClassess[0].getIRI(), getDisjointCoordinatesIRI(disjointClassess[1].getIRI().getShortForm())));
        shapes.add(disjoint);
      }
    }
    //isa relations
    for (OWLSubClassOfAxiom subClassOfAxiom : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
      if (subClassOfAxiom.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
        OWLObjectSomeValuesFrom owlObjectSomeValuesFrom = (OWLObjectSomeValuesFrom) subClassOfAxiom.getSuperClass();
        if (owlObjectSomeValuesFrom.getProperty().equals(owlObjectSomeValuesFrom.getProperty().getNamedProperty())) {
          existentialObjectProperties.add(owlObjectSomeValuesFrom.getProperty());
        } else {
          inverseExistentialObjectProperties.add(owlObjectSomeValuesFrom.getProperty().getNamedProperty());
        }
      } else if (subClassOfAxiom.getSuperClass() instanceof OWLDataSomeValuesFrom) {
        existentialDataProperties.add(((OWLDataSomeValuesFrom) subClassOfAxiom.getSuperClass()).getProperty());
      } else if (subClassOfAxiom.getSuperClass() instanceof OWLClass) {
        OWLClass superClass = subClassOfAxiom.getSuperClass().asOWLClass();
        OWLClass subClass = null;
        if (subClassOfAxiom.getSubClass() instanceof OWLClass) {
          subClass = subClassOfAxiom.getSubClass().asOWLClass();
        } else {
          messages.add("Couldn't get subclass " + subClassOfAxiom.getSubClass().toString());
        }
        UMLClass superUMLClass = umlClasses.get(superClass);
        if (superUMLClass == null || superClass.isTopEntity()) {
          superUMLClass = thingClass;
          thingAdded = true;
        }
        UMLClass subUMLClass = umlClasses.get(subClass);
        if (subUMLClass == null || (subClass != null && subClass.isTopEntity())) {
          subUMLClass = thingClass;
          thingAdded = true;
        }
        Inheritance relation = new Inheritance(superUMLClass, subUMLClass);
        relation.setAnchorCoordinates(getCoordinates(ontology, superClass.getIRI(), getISACoordinatesIRI(subUMLClass.getName())));
        shapes.add(relation);
      } else if (subClassOfAxiom.getSuperClass() instanceof OWLDataCardinalityRestriction) {
        if (subClassOfAxiom.getSuperClass() instanceof OWLDataMinCardinality) {
          if (((OWLDataMinCardinality) subClassOfAxiom.getSuperClass()).getCardinality() == 1) {
            existentialDataProperties.add(((OWLDataMinCardinality) subClassOfAxiom.getSuperClass()).getProperty());
          }
        }
        if (subClassOfAxiom.getSuperClass() instanceof OWLDataMaxCardinality) {
          if (((OWLDataMaxCardinality) subClassOfAxiom.getSuperClass()).getCardinality() == 1) {
            functionalDataProperties.add(((OWLDataMaxCardinality) subClassOfAxiom.getSuperClass()).getProperty());
          }
        }
      } else if (subClassOfAxiom.getSuperClass() instanceof OWLObjectCardinalityRestriction) {
        if (subClassOfAxiom.getSuperClass() instanceof OWLObjectMinCardinality) {
          OWLObjectMinCardinality minCardinality = (OWLObjectMinCardinality) subClassOfAxiom.getSuperClass();
          if (minCardinality.getCardinality() == 1) {
            if (minCardinality.getProperty().equals(minCardinality.getProperty().getNamedProperty())) {
              existentialObjectProperties.add(minCardinality.getProperty());
            } else {
              inverseExistentialObjectProperties.add(minCardinality.getProperty().getNamedProperty());
            }
          }
        }
        if (subClassOfAxiom.getSuperClass() instanceof OWLObjectMaxCardinality) {
          OWLObjectMaxCardinality maxCardinality = (OWLObjectMaxCardinality) subClassOfAxiom.getSuperClass();
          if (maxCardinality.getCardinality() == 1) {
            if (maxCardinality.getProperty().equals(maxCardinality.getProperty().getNamedProperty())) {
              functionalObjectProperties.add(maxCardinality.getProperty());
            } else {
              inverserFunctionalObjectProperties.add(maxCardinality.getProperty().getNamedProperty());
            }
          }
        }
      } else {
        messages.add("Ignored " + subClassOfAxiom);
      }
    }
    //attributes
    for (OWLDataProperty dataProperty : ontology.getDataPropertiesInSignature()) {
      String attrName = dataProperty.getIRI().getShortForm();
      Attribute attr = new Attribute(attrName);
      attr.setLongName(dataProperty.getIRI().toString());
      final Collection<OWLClassExpression> domains = EntitySearcher.getDomains(dataProperty, ontology);
      if (domains.size() > 1) {
        messages.add("Multiple domains are found for for <em>" + attrName + "</em> " + domains.toString());
      }
      for (OWLClassExpression owlClassExpression : domains) {
        UMLClass domainClass = umlClasses.get(owlClassExpression.asOWLClass());
        if (domainClass != null) {
          OWLDataRange range = EntitySearcher.getRanges(dataProperty, ontology).stream().findFirst().orElse(null);
          if (range != null) {
            attr.setType(DataType.get(range.toString()));
          } else {
            messages.add("No range is found for " + dataProperty + " setting its data type as String");
            attr.setType(DataType.STRING);
          }
          attr.setMultiplicity(Cardinality.get(existentialDataProperties.contains(dataProperty), EntitySearcher.isFunctional(dataProperty, ontology) || functionalDataProperties.contains(dataProperty)));
          domainClass.addAttribute(attr);
        }
      }
      if (domains.size() < 1) {
        messages.add("No domain is found for " + dataProperty.getIRI() + " adding it to the Thing class");
        thingClass.addAttribute(attr);
        thingAdded = true;
      }
    }
    //relations
    for (OWLObjectProperty objectProperty : ontology.getObjectPropertiesInSignature()) {
      Association association;
      UMLClass domainClass;
      UMLClass rangeClass;

      final IRI objectPropertyIRI = objectProperty.getIRI();

      OWLClassExpression domainClassExpression = EntitySearcher.getDomains(objectProperty, ontology).stream().findFirst().orElse(null);
      if (domainClassExpression != null) {
        domainClass = umlClasses.get(domainClassExpression.asOWLClass());
      } else {
        domainClass = thingClass;
        thingAdded = true;
        messages.add("No domain is found for " + objectPropertyIRI + " setting Thing as domain class");
      }

      OWLClassExpression rangeClassExpression = EntitySearcher.getRanges(objectProperty, ontology).stream().findFirst().orElse(null);
      if (rangeClassExpression != null) {
        rangeClass = umlClasses.get(rangeClassExpression.asOWLClass());
      } else {
        rangeClass = thingClass;
        thingAdded = true;
        messages.add("No range is found for " + objectPropertyIRI + " setting Thing as range class");
      }
      String assocString = getAssociation(ontology, objectPropertyIRI);
      if (assocString != null) {
        String typeString = getType(ontology, objectPropertyIRI);
        association = associationClasses.get(assocString).getAssociation();
        if (isDomain(typeString)) {
          association.setFirstClass(rangeClass);
          association.setSecondMultiplicity(Cardinality.get(inverseExistentialObjectProperties.contains(objectProperty),
            isInverseFunctional(objectProperty, ontology) || inverserFunctionalObjectProperties.contains(objectProperty)));
        } else {
          association.setSecondClass(rangeClass);
          association.setFirstMultiplicity(Cardinality.get(inverseExistentialObjectProperties.contains(objectProperty),
            EntitySearcher.isFunctional(objectProperty.getInverseProperty(), ontology) || inverserFunctionalObjectProperties.contains(objectProperty)));
        }
      } else {
        association = new Association(objectPropertyIRI.getShortForm(), objectPropertyIRI.toString(), domainClass, rangeClass);
        association.setFirstMultiplicity(Cardinality.get(inverseExistentialObjectProperties.contains(objectProperty),
          isInverseFunctional(objectProperty, ontology) || inverserFunctionalObjectProperties.contains(objectProperty)));
        association.setSecondMultiplicity(Cardinality.get(existentialObjectProperties.contains(objectProperty),
          EntitySearcher.isFunctional(objectProperty, ontology) || functionalObjectProperties.contains(objectProperty)));
      }
      association.setAnchorCoordinates(getCoordinates(ontology, objectPropertyIRI, OWLExporter.getCoordinatesIRI()));
      shapes.add(association);
    }

    shapes.addAll(new ArrayList<>(umlClasses.values()));
    if (thingAdded) {
      shapes.add(thingClass);
    }
    if (messages.size() > 0) {
      StringBuilder messageBuilder = new StringBuilder();
      messages.forEach(message -> messageBuilder.append(message).append("<br/>"));
      UIUtility.error(messageBuilder.toString());
    }
    return shapes;
  }

  private static boolean isInverseFunctional(OWLObjectProperty objectProperty, OWLOntology ontology) {
    return EntitySearcher.isInverseFunctional(objectProperty, ontology) || EntitySearcher.isFunctional(objectProperty.getInverseProperty(), ontology);
  }

  private static String getCoordinates(OWLOntology ontology, IRI classIRI, IRI coordinatesIRI) {
    Optional<OWLAnnotationAssertionAxiom> owlAnnotationAssertionAxiom = ontology.getAnnotationAssertionAxioms(classIRI).stream().filter(x -> x.getProperty().getIRI().getShortForm().equals(coordinatesIRI.getShortForm())).findFirst();
    return owlAnnotationAssertionAxiom.map(axiom -> ((OWLLiteral) axiom.getValue()).getLiteral()).orElse(null);
  }

  private static String getAssociation(OWLOntology ontology, IRI classIRI) {
    Optional<OWLAnnotationAssertionAxiom> owlAnnotationAssertionAxiom = ontology.getAnnotationAssertionAxioms(classIRI).stream().filter(x -> x.getProperty().getIRI().getShortForm().equals(getAssociationIRI().getShortForm())).findFirst();
    return owlAnnotationAssertionAxiom.map(axiom -> ((OWLLiteral) axiom.getValue()).getLiteral()).orElse(null);
  }

  private static String getType(OWLOntology ontology, IRI classIRI) {
    Optional<OWLAnnotationAssertionAxiom> owlAnnotationAssertionAxiom = ontology.getAnnotationAssertionAxioms(classIRI).stream().filter(x -> x.getProperty().getIRI().getShortForm().equals(getTypeIRI().getShortForm())).findFirst();
    return owlAnnotationAssertionAxiom.map(axiom -> ((OWLLiteral) axiom.getValue()).getLiteral()).orElse(null);
  }
}