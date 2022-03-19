/*
 * onprom-plugin
 *
 * OWLOntologyExportPlugin.java
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

package org.processmining.plugins.kaos;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author T. E. Kalayci
 */
@Plugin(
        name = "Ontology File",
        parameterLabels = {"OWL Ontology", "File"},
        returnLabels = {},
        returnTypes = {}
)
@UIExportPlugin(description = "OWL Ontology", extension = "owl")
public class OWLOntologyExportPlugin {
    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {0, 1})
    public void export(PluginContext context, OWLOntology ontology, File file) {
        try {
            //default format is RDF/XML format for the ontology
            RDFDocumentFormat format = new RDFXMLDocumentFormat();
            //save the given ontology using the format to the file
            OWLManager.createOWLOntologyManager().saveOntology(ontology, format, new FileOutputStream(file));
            context.log("Exported ontology to the file: " + file.getName());
        } catch (Exception e) {
            context.log("Couldn't export ontology to the file: " + file.getName() + " ->" + e.getMessage());
        }
    }
}