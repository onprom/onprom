///*
// * onprom-toolkit
// *
// * SimpleExtractionTest.java
// *
// * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
// *
// * This product includes software developed under
// * KAOS: Knowledge-Aware Operational Support project
// * (https://kaos.inf.unibz.it).
// *
// * Please visit https://onprom.inf.unibz.it for more information.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package it.unibz.inf.kaos.onprom;
//
//import it.unibz.inf.kaos.data.query.AnnotationQueries;
//import it.unibz.inf.kaos.logextractor.SimpleXESLogExtractor;
//import it.unibz.inf.kaos.obdamapper.OBDAMapper;
//import it.unibz.inf.kaos.obdamapper.utility.OntopUtility;
//import it.unibz.inf.kaos.ui.utility.IOUtility;
//import it.unibz.inf.ontop.protege.core.OBDAModel;
//import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
//import org.deckfour.xes.model.XLog;
//import org.deckfour.xes.out.XesXmlGZIPSerializer;
//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.OWLOntology;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.util.Properties;
//
//public class OneLevelExtractionofERPTest {
//
//    public static void main(String[] args) {
//        try {
//            long start = System.currentTimeMillis();
//            // prepare files
//            if (args.length < 1) {
//                System.out.println("Please use by providing folder of the files");
//                System.exit(-1);
//            }
//            String folder = args[0];
//            File domainMappingsFile = new File(folder + "ERPOrder.obda");
//            File domainOntologyFile = new File(folder + "ERPOrder.owl");
//            File queriesFile = new File(folder + "ERPOrder.aqr");
//            // generate output file names
//            String outputFileName = domainOntologyFile.getParent() + "/" + domainMappingsFile.getName() + System.currentTimeMillis();
//            // redirect console output to a text file
////            PrintStream out = new PrintStream(new FileOutputStream(outputFileName + ".txt"));
////            System.setOut(out);
//            // prepare XES log output file
//            File generatedMappingsFile = new File(outputFileName + "_generated.obda");
//            File output = new File(outputFileName + ".xes.gz");
//            // load mappings
//            Properties dataSourceProperties = OntopUtility.getDataSourceProperties(domainMappingsFile);
//            OBDAModel obdaModel = OntopUtility.getOBDAModel(domainMappingsFile, dataSourceProperties);
//            // load ontologies
//            OWLOntology domainOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(domainOntologyFile);
//            OWLOntology onpromOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(OneLevelExtractionofERPTest.class.getResourceAsStream("/xes-ontology.owl"));
//            // start extraction process
//            if (output.createNewFile()) {
//                IOUtility.readJSON(queriesFile, AnnotationQueries.class).ifPresent(firstLevel -> {
//                    try {
//                        //generate final mapping
//                        SQLPPMapping firstMapping = new OBDAMapper(domainOntology, SimpleXESLogExtractor.getDefaultEventOntology(), obdaModel, dataSourceProperties, firstLevel).getOBDAModel();
//                        OntopUtility.saveModel(firstMapping, generatedMappingsFile);
//                        XLog xTraces = new SimpleXESLogExtractor().extractXESLog(firstMapping, dataSourceProperties);
//                        // extract log
//                        new XesXmlGZIPSerializer().serialize(xTraces, new FileOutputStream(output));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//            System.out.println("TOTAL EXTRACTION TIME: " + (System.currentTimeMillis() - start) / 1000 + "s");
//            System.exit(0);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
//}
