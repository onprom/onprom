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
import it.unibz.inf.kaos.logextractor.SimpleXESLogExtractor;
import it.unibz.inf.kaos.logextractor.util.ToolUtil;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.utility.OntopUtility;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

public class TwoLevelExtractionTest {

    public static void main(String[] args) throws OWLOntologyCreationException, IOException {
        long start = System.currentTimeMillis();
        // prepare files
        if (args.length < 1) {
            System.out.println("Please use by providing folder of the files");
            System.exit(-1);
        }
        String folder = args[0];
        File domainMappingsFile = new File(folder + "conference.obda");
        File domainOntologyFile = new File(folder + "conference.owl");
        File eventOntologyFile = new File(folder + "custom-eo.owl");
        File firstLevelFile = new File(folder + "level1.aqr");
        File secondLevelFile = new File(folder + "level2.aqr");
        // generate output file names
        //String outputFileName = domainOntologyFile.getParent() + "/" + domainMappingsFile.getName() + System.currentTimeMillis();
        String outputFileName = domainOntologyFile.getParent() + "/" + domainMappingsFile.getName();
        // redirect console output to a text file
//            PrintStream out = new PrintStream(new FileOutputStream(outputFileName + ".txt"));
//            System.setOut(out);
        // prepare XES log output file
        File finalMappingsFile = new File(outputFileName + "_lvl2.obda");
        File firstMappingsFile = new File(outputFileName + "_lvl1.obda");
        File output = new File(outputFileName + ".xes.gz");
        // load mappings
        //Properties dataSourceProperties = OntopUtility.getDataSourceProperties(domainMappingsFile);
        Properties dataSourceProperties = new Properties();
        dataSourceProperties.load(new FileReader(folder + "conference.properties"));

        SQLPPMapping obdaModel = OntopUtility.getOBDAModel(domainMappingsFile, dataSourceProperties);
        // load ontologies
        OWLOntology domainOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(domainOntologyFile);
        OWLOntology eventOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(eventOntologyFile);
        OWLOntology onpromOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(TwoLevelExtractionTest.class.getResourceAsStream("/eo-onprom.owl"));

        AnnotationQueries firstLevel = IOUtility.readJSON(firstLevelFile, AnnotationQueries.class)
                .orElseThrow(IllegalArgumentException::new);
        AnnotationQueries secondLevel = IOUtility.readJSON(secondLevelFile, AnnotationQueries.class)
                .orElseThrow(IllegalArgumentException::new);

        //generate final mapping
        OBDAMapper firstOBDAMapper = new OBDAMapper(domainOntology, eventOntology, obdaModel, dataSourceProperties, firstLevel);
        SQLPPMapping firstMapping = firstOBDAMapper.getOBDAModel();
        OntopUtility.saveModel(firstMapping, firstMappingsFile);
        // ToolUtil.writeToFile(ToolUtil.readFromFile(firstMappingsFile).replaceAll("\t\t\t\n",""),firstMappingsFile.getPath());
        OBDAMapper secondOBDAMapper = new OBDAMapper(eventOntology, onpromOntology, firstMapping, dataSourceProperties, secondLevel);
        SQLPPMapping finalMapping = secondOBDAMapper.getOBDAModel();
        OntopUtility.saveModel(finalMapping, finalMappingsFile);
        // ToolUtil.writeToFile(ToolUtil.readFromFile(finalMappingsFile).replaceAll("\t\t\t\n",""),finalMappingsFile.getPath());
        XLog xTraces = new SimpleXESLogExtractor().extractXESLog(finalMapping, dataSourceProperties);
        // extract log
        new XesXmlGZIPSerializer().serialize(xTraces, new FileOutputStream(output));
        System.out.println("TOTAL EXTRACTION TIME: " + (System.currentTimeMillis() - start) / 1000 + "s");
        System.exit(0);

    }
}
