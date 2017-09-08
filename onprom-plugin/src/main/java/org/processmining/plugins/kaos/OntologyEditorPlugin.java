/*
 * onprom-plugin
 *
 * OntologyEditorPlugin.java
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

import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.interfaces.UMLEditorListener;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.uml.UMLEditor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * @author T. E. Kalayci
 */
@Plugin(
        name = "onprom Ontology Editor",
        parameterLabels = {"Ontology"},
        returnLabels = {"Ontology"},
        returnTypes = {OWLOntology.class}
)
public class OntologyEditorPlugin implements UMLEditorListener {
    private UIPluginContext context;

    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {})
    public void displayEditor(final UIPluginContext _context) {
        loadUMLEditor(_context, null);
    }

    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {0})
    public void displayEditor(final UIPluginContext _context, OWLOntology ontology) {
        loadUMLEditor(_context, ontology);
    }

    public void store(String name, OWLOntology ontology) {
        if (ontology != null) {
            //Create OWL Ontology in the context
            context.getProvidedObjectManager().createProvidedObject(name + " " + UIUtility.getCurrentDateTime(), ontology, OWLOntology.class, context);
        } else {
            context.log("OWL ontology is not created", MessageLevel.ERROR);
        }
    }

    @Override
    public void store(String identifier, FileType type, Set<DiagramShape> shapes) {

    }

    private void loadUMLEditor(final UIPluginContext _context, OWLOntology ontology) {
        //set context to use with listener method
        context = _context;
        context.getProgress().setIndeterminate(true);
        //display editor dialog with loaded ontology
        new UMLEditor(ontology, this).display(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

}