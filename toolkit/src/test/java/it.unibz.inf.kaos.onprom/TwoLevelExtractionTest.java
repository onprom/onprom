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

package it.unibz.inf.onprom.onprom;

import it.unibz.inf.onprom.data.query.AnnotationQueries;
import it.unibz.inf.onprom.logextractor.xes.XESLogExtractor;
import it.unibz.inf.onprom.obdamapper.OBDAMapper;
import it.unibz.inf.onprom.obdamapper.utility.OntopUtility;
import it.unibz.inf.onprom.ui.utility.IOUtility;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

public class TwoLevelExtractionTest {

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        // Assuming script is started in project folder
        String folder = new File("./example/").getCanonicalPath() + "/";
        if (args.length > 0) {
            // It is possible to give a different folder
            folder = args[0];
        }
        // prepare files
        File domainMappingsFile = new File(folder + "conference.obda");
        File domainOntologyFile = new File(folder + "conference.owl");
        File eventOntologyFile = new File(folder + "custom-eo.owl");
        File firstLevelFile = new File(folder + "conference-lvl1.aqr");
        File secondLevelFile = new File(folder + "conference-lvl2.aqr");
        // prepare output files
        String outputFileName = domainOntologyFile.getParent() + "/" + domainMappingsFile.getName();
        File finalMappingsFile = new File(outputFileName + "_lvl2_generated.obda");
        File firstMappingsFile = new File(outputFileName + "_lvl1_generated.obda");
        File output = new File(outputFileName + ".xes.gz");
        // load mappings
        Properties dataSourceProperties = new Properties();
        dataSourceProperties.load(new FileReader(folder + "conference.properties"));
        SQLPPMapping obdaModel = OntopUtility.getOBDAModel(domainMappingsFile, dataSourceProperties);
        // load ontologies
        OWLOntology domainOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(domainOntologyFile);
        OWLOntology eventOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(eventOntologyFile);
        // load annotation queries
        AnnotationQueries firstLevel = IOUtility.readYAML(firstLevelFile, AnnotationQueries.class)
                .orElseThrow(IllegalArgumentException::new);
        AnnotationQueries secondLevel = IOUtility.readYAML(secondLevelFile, AnnotationQueries.class)
                .orElseThrow(IllegalArgumentException::new);
//        AnnotationQueries firstLevel = IOUtility.readJSON(firstLevelFile, AnnotationQueries.class)
//                .orElseThrow(IllegalArgumentException::new);
//        AnnotationQueries secondLevel = IOUtility.readJSON(secondLevelFile, AnnotationQueries.class)
//                .orElseThrow(IllegalArgumentException::new);
        //generate mappings
        SQLPPMapping firstMapping = new OBDAMapper(domainOntology, eventOntology, obdaModel, dataSourceProperties, firstLevel).getOBDAModel();
        OntopUtility.saveModel(firstMapping, firstMappingsFile);
        XESLogExtractor extractor = new XESLogExtractor();
        SQLPPMapping finalMapping = new OBDAMapper(eventOntology, extractor.getOntology(), firstMapping, dataSourceProperties, secondLevel).getOBDAModel();
        OntopUtility.saveModel(finalMapping, finalMappingsFile);
        // extract log
        XLog xTraces = extractor.extractLog(finalMapping, dataSourceProperties);
        // serialize extracted log
        new XesXmlGZIPSerializer().serialize(xTraces, new FileOutputStream(output));
        System.out.println("TOTAL EXTRACTION TIME: " + (System.currentTimeMillis() - start) / 1000 + "s");
    }
}
