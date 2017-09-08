/*
 * onprom-plugin
 *
 * AnnotationEditorPlugin.java
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

import it.unibz.inf.kaos.annotation.AnnotationEditor;
import it.unibz.inf.kaos.data.EditorObjects;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.interfaces.AnnotationEditorListener;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * @author T. E. Kalayci
 */
@Plugin(
        name = "onprom Annotation Editor",
        parameterLabels = {"Ontology", "Annotation"},
        returnLabels = {"Annotations"},
        returnTypes = {AnnotationQueries.class}
)
public class AnnotationEditorPlugin implements AnnotationEditorListener {

    private UIPluginContext context;

    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {})
    public void displayEditor(final UIPluginContext _context) {
        loadEditor(_context, null);
    }

    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {0})
    public void displayEditor(final UIPluginContext _context, OWLOntology ontology) {
        loadEditor(_context, ontology);
    }

    @UITopiaVariant(
            author = "onprom team",
            affiliation = "Free University of Bozen-Bolzano",
            email = "onprom@inf.unibz.it",
            website = "http://onprom.inf.unibz.it"
    )
    @PluginVariant(requiredParameterLabels = {1})
    public void displayEditor(final UIPluginContext _context, EditorObjects editorObjects) {
        //set context to use with listener method
        context = _context;
        context.getProgress().setIndeterminate(true);
        //display editor dialog with loaded ontology
        AnnotationEditor editor = new AnnotationEditor(null, this);
        editor.load("", editorObjects.getShapes());
        editor.display(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    @Override
    public void store(String ontologyName, OWLOntology ontology) {

    }

    @Override
    public void store(String identifier, FileType type, Set<DiagramShape> shapes) {

    }

    @Override
    public void store(String name, AnnotationQueries annotations) {
        if (annotations != null) {
            if (name != null) {
                context.getProvidedObjectManager().createProvidedObject(name + " " + UIUtility.getCurrentDateTime(), annotations, AnnotationQueries.class, context);
            } else {
                context.getProvidedObjectManager().createProvidedObject("Queries" + " " + UIUtility.getCurrentDateTime(), annotations, AnnotationQueries.class, context);
            }
        } else {
            context.log("annotations are null", Logger.MessageLevel.ERROR);
        }
    }

    private void loadEditor(final UIPluginContext _context, final OWLOntology ontology) {
        //set context to use with listener method
        context = _context;
        context.getProgress().setIndeterminate(true);
        //display editor dialog with loaded ontology
        new AnnotationEditor(ontology, this).display(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
}