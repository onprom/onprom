/*
 * onprom-obdamapper
 *
 * OBDAMaterializer.java
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

package it.unibz.inf.kaos.obdamapper;

import it.unibz.inf.kaos.obdamapper.utility.OntopUtility;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class OBDAMaterializer {

    private static final OWLOntologyManager OWL_ONTOLOGY_MANAGER = OWLManager.createOWLOntologyManager();
    private static final OWLDataFactory OWL_DATA_FACTORY = OWL_ONTOLOGY_MANAGER.getOWLDataFactory();

    public static OWLOntology getMaterializedOWLOntology(OWLOntology targetOntology, OBDAModel obdaModel, Properties datasourceProperties) {
        try {
            OntopSQLOWLAPIConfiguration config = OntopUtility.getConfiguration(targetOntology, obdaModel, datasourceProperties);
            OntopOWLReasoner reasoner = OntopOWLFactory.defaultFactory().createReasoner(config);

            OWLOntology result = cloneOntology(targetOntology);

            ArrayList<AddAxiom> newAxioms = new ArrayList<>();

            for (OWLClassAssertionAxiom clsAxiom : retrieveAllClassInstances(targetOntology, reasoner.getConnection().createStatement())) {
                newAxioms.add(new AddAxiom(result, clsAxiom));
            }

            for (OWLObjectPropertyAssertionAxiom opAxiom : retrieveAllObjectPropertiesInstances(targetOntology, reasoner.getConnection().createStatement())) {
                newAxioms.add(new AddAxiom(result, opAxiom));
            }

            List<OWLDataPropertyAssertionAxiom> dataPropertyAxioms = retrieveAllDataPropertiesInstances(targetOntology, reasoner.getConnection().createStatement());

            for (OWLDataPropertyAssertionAxiom dpAxiom : dataPropertyAxioms) {
                newAxioms.add(new AddAxiom(result, dpAxiom));
            }

            //add all new axioms to the ontology
            result.getOWLOntologyManager().applyChanges(newAxioms);

            reasoner.close();
            reasoner.dispose();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<OWLClassAssertionAxiom> retrieveAllClassInstances(OWLOntology targetOntology, OntopOWLStatement statement) throws OWLException {

        //Collect all class names in the target ontology
        Set<OWLClass> classes = targetOntology.getClassesInSignature();
        ArrayList<OWLClassAssertionAxiom> results = new ArrayList<>();

        for (OWLClass cls : classes) {

            String q = String.format("SELECT DISTINCT ?class "
                    + "WHERE{ ?class a %s . "
                    + "}", cls.toString());
            TupleOWLResultSet rs = statement.executeSelectQuery(q);

            while (rs.hasNext()) {
                OWLBindingSet result = rs.next();
                OWLIndividual clsInstance = result.getOWLIndividual("class");
                results.add(OWL_DATA_FACTORY.getOWLClassAssertionAxiom(cls, clsInstance));
            }
        }
        return results;
    }

    private static List<OWLObjectPropertyAssertionAxiom> retrieveAllObjectPropertiesInstances(OWLOntology targetOntology, OntopOWLStatement statement) throws OWLException {

        //Collect all class names in the target ontology
        Set<OWLObjectProperty> objectProperties = targetOntology.getObjectPropertiesInSignature();
        ArrayList<OWLObjectPropertyAssertionAxiom> results = new ArrayList<>();

        for (OWLObjectProperty op : objectProperties) {
            String q = String.format("SELECT DISTINCT ?firstComp ?secondComp "
                    + "WHERE{ ?firstComp %s ?secondComp . "
                    + "}", op.toString());
            TupleOWLResultSet rs = statement.executeSelectQuery(q);

            while (rs.hasNext()) {
                OWLBindingSet result = rs.next();
                OWLIndividual firstInst = result.getOWLIndividual("firstComp");
                OWLIndividual secondInst = result.getOWLIndividual("secondComp");
                results.add(OWL_DATA_FACTORY.getOWLObjectPropertyAssertionAxiom(op, firstInst, secondInst));
            }
        }

        return results;
    }

    private static List<OWLDataPropertyAssertionAxiom> retrieveAllDataPropertiesInstances(OWLOntology targetOntology, OntopOWLStatement statement) throws OWLException {

        Set<OWLDataProperty> dataProperties = targetOntology.getDataPropertiesInSignature();
        ArrayList<OWLDataPropertyAssertionAxiom> results = new ArrayList<>();

        for (OWLDataProperty dp : dataProperties) {
            String q = String.format("SELECT DISTINCT ?firstComp ?secondComp "
                    + "WHERE{ ?firstComp %s ?secondComp . "
                    + "}", dp.toString());
            TupleOWLResultSet rs = statement.executeSelectQuery(q);

            while (rs.hasNext()) {
                OWLBindingSet result = rs.next();
                OWLIndividual firstInst = result.getOWLIndividual("firstComp");
                OWLLiteral secondInst = result.getOWLLiteral("secondComp");
                results.add(OWL_DATA_FACTORY.getOWLDataPropertyAssertionAxiom(dp, firstInst, secondInst));
            }
        }
        return results;
    }

    private static OWLOntology cloneOntology(OWLOntology owlOntology) throws OWLOntologyCreationException {
        HashSet<OWLOntology> targets = new HashSet<>();
        IRI targetOntoIRI = owlOntology.getOntologyID().getOntologyIRI().get();
        targets.add(owlOntology);
        return OWL_ONTOLOGY_MANAGER.createOntology(targetOntoIRI, targets, false);
    }
}
