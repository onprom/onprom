/*
 * onprom-toolkit
 *
 * SimpleExtractionTest.java
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
import it.unibz.inf.kaos.logextractor.SimpleOCELLogExtractor;
import it.unibz.inf.kaos.obdamapper.OCELOBDAMapper;
import it.unibz.inf.kaos.obdamapper.utility.OntopUtility;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.ocel.model.OcelLog;
import it.unibz.ocel.out.OcelXmlGZIPSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class OneLevelExtractionofOCELTest {

    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();
            // prepare files
            if (args.length < 1) {
                System.out.println("Please use by providing folder of the files");
                System.exit(-1);
            }
            String folder = args[0];
            File domainMappingsFile = new File(folder + "OCELERP.obda");
            File domainOntologyFile = new File(folder + "OCELERP.owl");
            File queriesFile = new File(folder + "OCELERP.aqr");
            // generate output file names
            String outputFileName = domainOntologyFile.getParent() + "/" + domainMappingsFile.getName() + System.currentTimeMillis();
            // redirect console output to a text file
//            PrintStream out = new PrintStream(new FileOutputStream(outputFileName + ".txt"));
//            System.setOut(out);
            // prepare OCEL log output file
            File generatedMappingsFile = new File(outputFileName + "_generated.obda");
            File output = new File(outputFileName + ".ocel.gz");
            // load mappings
            Properties dataSourceProperties = OntopUtility.getDataSourceProperties(domainMappingsFile);
            OBDAModel obdaModel = OntopUtility.getOBDAModel(domainMappingsFile, dataSourceProperties);
            // load ontologies
            OWLOntology domainOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(domainOntologyFile);
//            OWLOntology onpromOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(OneLevelExtractionofOCELTest.class.getResourceAsStream("/ocel-eo.owl"));
            // start extraction process
            if (output.createNewFile()) {
                IOUtility.readJSON(queriesFile, AnnotationQueries.class).ifPresent(firstLevel -> {
                    try {
                        //generate final mapping
                        OBDAModel firstMapping = new OCELOBDAMapper(domainOntology, SimpleOCELLogExtractor.getDefaultEventOntology(), obdaModel, dataSourceProperties, firstLevel).getOBDAModel();

                        OntopUtility.saveModel(firstMapping, generatedMappingsFile);
                        OcelLog ocelLog = new SimpleOCELLogExtractor().extractOCELLog(firstMapping, dataSourceProperties);
                        // extract log
                        new OcelXmlGZIPSerializer().serialize(ocelLog, new FileOutputStream(output));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            System.out.println("TOTAL EXTRACTION TIME: " + (System.currentTimeMillis() - start) / 1000 + "s");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
