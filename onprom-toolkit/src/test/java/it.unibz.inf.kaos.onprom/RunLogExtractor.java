/*
 * onprom-toolkit
 *
 * RunLogExtractor.java
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

package it.unibz.inf.kaos.onprom;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.SimpleXESLogExtractor;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.utility.OntopUtility;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class RunLogExtractor {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Usage: RunLogExtractor domain_ontology domain_mappings event_ontology first_level_mappings second_level_mappings");
            return;
        }
        try {
            long start = System.currentTimeMillis();
            // prepare files
            File domainOntologyFile = new File(args[0]);
            File domainMappingsFile = new File(args[1]);
            File eventOntologyFile = new File(args[2]);
            File firstLevelFile = new File(args[3]);
            File secondLevelFile = new File(args[4]);
            // generate output file names
            String outputFileName = domainOntologyFile.getParent() + "/" + firstLevelFile.getName() + System.currentTimeMillis();
            // redirect console output to a text file
            PrintStream out = new PrintStream(new FileOutputStream(outputFileName + ".txt"));
            System.setOut(out);
            // prepare XES log output file
            File finalMappingsFile = new File(outputFileName + ".obda");
            File output = new File(outputFileName + ".xes.gz");
            Properties dataSourceProperties = OntopUtility.getDataSourceProperties(domainMappingsFile);
            OBDAModel obdaModel = OntopUtility.getOBDAModel(domainMappingsFile, dataSourceProperties);
            // load ontologies
            OWLOntology domainOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(domainOntologyFile);
            OWLOntology eventOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(eventOntologyFile);
            OWLOntology onpromOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(RunLogExtractor.class.getResourceAsStream("/eo-onprom.owl"));
            // start extraction process
            if (output.createNewFile()) {
                IOUtility.readJSON(firstLevelFile, AnnotationQueries.class).ifPresent(firstLevel -> IOUtility.readJSON(secondLevelFile, AnnotationQueries.class).ifPresent(secondLevel -> {
                    try {
                        //generate final mapping
                        OBDAModel firstMapping = new OBDAMapper(domainOntology, eventOntology, obdaModel, dataSourceProperties, firstLevel).getOBDAModel();
                        OBDAModel finalMapping = new OBDAMapper(eventOntology, onpromOntology, firstMapping, dataSourceProperties, secondLevel).getOBDAModel();
                        // extract log
                        new XesXmlGZIPSerializer().serialize(
                                new SimpleXESLogExtractor().extractXESLog(domainOntology, finalMapping, dataSourceProperties, firstLevel, eventOntology, secondLevel),
                                new FileOutputStream(output)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            }
            System.out.println("TOTAL EXTRACTION TIME: " + (System.currentTimeMillis() - start) / 1000 + "s");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
