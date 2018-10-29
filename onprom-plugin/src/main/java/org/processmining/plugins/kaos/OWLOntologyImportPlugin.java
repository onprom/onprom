/*
 * onprom-plugin
 *
 * OWLOntologyImportPlugin.java
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

package org.processmining.plugins.kaos;

import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.events.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.InputStream;

/**
 * @author T. E. Kalayci
 */
@Plugin(
        name = "Ontology File",
        parameterLabels = {"Filename"},
        returnLabels = {"Imported OWL Ontology"},
        returnTypes = {OWLOntology.class}
)
@UIImportPlugin(description = "OWL Ontology", extensions = {"owl", "ttl", "rdf"})
public class OWLOntologyImportPlugin extends AbstractImportPlugin {
    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @Override
    protected OWLOntology importFromStream(final PluginContext context, final InputStream input, final String filename,
                                           final long fileSizeInBytes) {
        try {
            context.getFutureResult(0).setLabel("OWL Ontology (" + filename + " ) " + UIUtility.getCurrentDateTime());
            return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(input);
        } catch (final Throwable e) {
            context.log("Couldn't import ontology:" + e.getMessage(), Logger.MessageLevel.ERROR);
        }
        return null;
    }
}