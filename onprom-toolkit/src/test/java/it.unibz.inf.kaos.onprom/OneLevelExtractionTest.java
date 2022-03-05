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
import it.unibz.inf.kaos.logextractor.xes.XESLogExtractor;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.utility.OntopUtility;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

public class OneLevelExtractionTest {

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
        File propertiesFile = new File(folder + "conference.properties");
        File domainOntologyFile = new File(folder + "conference.owl");
        File queriesFile = new File(folder + "conference.aqr");
        // prepare output files
        String outputFileName = domainOntologyFile.getParent() + "/" + domainMappingsFile.getName();
        File generatedMappingsFile = new File(outputFileName + "_generated.obda");
        File output = new File(outputFileName + ".xes.gz");
        // load mappings
        Properties dataSourceProperties = new Properties();
        dataSourceProperties.load(new FileReader(propertiesFile));
        SQLPPMapping obdaModel = OntopUtility.getOBDAModel(domainMappingsFile, propertiesFile);
        // load domain ontology
        OWLOntology domainOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(domainOntologyFile);
        // load annotation queries
        AnnotationQueries firstLevel = IOUtility.readJSON(queriesFile, AnnotationQueries.class)
                .orElseThrow(IllegalArgumentException::new);
        // generate new mapping
        SQLPPMapping newMapping = new OBDAMapper(domainOntology, XESLogExtractor.getDefaultEventOntology(), obdaModel, dataSourceProperties, firstLevel).getOBDAModel();
        OntopUtility.saveModel(newMapping, generatedMappingsFile);
        // extract log
        XLog xTraces = new XESLogExtractor().extractLog(newMapping, dataSourceProperties);
        // serialize extracted log
        new XesXmlGZIPSerializer().serialize(xTraces, new FileOutputStream(output));
        System.out.println("TOTAL EXTRACTION TIME: " + (System.currentTimeMillis() - start) / 1000 + "s");
    }
}
