/*
 * onprom-dynamiceditor
 *
 * DynamicAnnotationEditor.java
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

package it.unibz.inf.kaos.dynamic;

import it.unibz.inf.kaos.annotation.AnnotationEditor;
import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.AnnotationEditorListener;
import it.unibz.inf.kaos.interfaces.AnnotationFactory;
import it.unibz.inf.kaos.interfaces.AnnotationProperties;
import it.unibz.inf.kaos.owl.OWLImporter;
import it.unibz.inf.kaos.owl.OWLUtility;
import it.unibz.inf.kaos.ui.action.DiagramPanelAction;
import it.unibz.inf.kaos.ui.action.ToolbarAction;
import it.unibz.inf.kaos.ui.form.AnnotationSelectionDialog;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by T. E. Kalayci on 16-Oct-2017.
 */
public class DynamicAnnotationEditor extends AnnotationEditor {
    private static final Logger logger = LoggerFactory.getLogger(DynamicAnnotationEditor.class.getName());
    private static final Map<String, UMLClass> annotations = new HashMap<>();

    public DynamicAnnotationEditor(OWLOntology upperOntology, OWLOntology domainOntology, AnnotationEditorListener _listener) {
        super(domainOntology, _listener, getAnnotationFactory());
        loadAnnotationTypes(upperOntology);
    }

    private static AnnotationFactory getAnnotationFactory() {
        return (panel, currentAction, selectedCls) -> {
            try {
                UMLClass annotationClass = annotations.get(currentAction.toString());
                if (annotationClass != null) {
                    return Optional.of(new DynamicAnnotation(annotationClass, selectedCls, getAnnotationProperties(annotationClass)));
                }
            } catch (RuntimeException e) {
                logger.error(String.format("A runtime exception occurred: %s", e.getMessage()));
            }
            return Optional.empty();
        };
    }

    public static void main(String[] a) {
        new DynamicAnnotationEditor(null, null, null).display();
    }

    public static AnnotationProperties getAnnotationProperties(final UMLClass annotationClass) {
        return new AnnotationProperties() {
            @Override
            public String color() {
                return UIUtility.strToHexColor(annotationClass.getName());
            }

            @Override
            public char mnemonic() {
                return annotationClass.getName().charAt(0);
            }

            @Override
            public String tooltip() {
                return String.format("Create %s annotation", annotationClass.getName());
            }

            @Override
            public String title() {
                return annotationClass.getName();
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return AnnotationProperties.class;
            }
        };
    }

    private void loadAnnotationTypes(OWLOntology upperOntology) {
        annotations.clear();
        if (upperOntology != null) {
            new AnnotationSelectionDialog(
                    OWLImporter.getShapes(upperOntology).stream()
                            .filter(UMLClass.class::isInstance)
                            .map(UMLClass.class::cast))
                    .getSelectedClasses()
                    .forEach(umlClass -> annotations.put(umlClass.getName(), umlClass));
            setTitle("Annotation Editor for " + upperOntology.getOntologyID().getOntologyIRI().or(IRI.create("")));
        } else {
            OWLUtility.loadOntologyFromStream(DynamicAnnotationEditor.class.getResourceAsStream("/default-eo.owl"))
                    .ifPresent(defaultOntology -> OWLImporter.getShapes(defaultOntology).stream()
                            .filter(UMLClass.class::isInstance)
                            .map(UMLClass.class::cast)
                            .forEach(
                                    umlClass -> annotations.put(umlClass.getName(), umlClass)
                            ));
            setTitle("Default XES Annotation Editor");
        }
        initUI();
    }

    @Override
    protected JToolBar createToolbar() {
        JToolBar toolBar = super.createToolbar();
        toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.disable, this, diagramPanel)), 3);
        toolBar.add(UIUtility.createToolbarButton(loadXESOntology()), 4);
        toolBar.add(UIUtility.createToolbarButton(selectCustomUpperOntology()), 5);
        return toolBar;
    }

    private ToolbarAction selectCustomUpperOntology() {
        return new ToolbarAction(new AbstractActionType() {
            @Override
            public String getTooltip() {
                return "Select Custom Upper Ontology";
            }

            @Override
            public String getTitle() {
                return "import";
            }
        }) {
            @Override
            public void execute() {
                UIUtility.selectFileToOpen(FileType.ONTOLOGY).flatMap(OWLUtility::loadOntologyFromFile).ifPresent(ontology -> loadAnnotationTypes(ontology));
            }
        };
    }

    private ToolbarAction loadXESOntology() {
        return new ToolbarAction(new AbstractActionType() {
            @Override
            public String getTooltip() {
                return "Load Default XES Ontology";
            }

            @Override
            public String getTitle() {
                return "xes-ontology";
            }
        }) {
            @Override
            public void execute() {
                loadAnnotationTypes(null);
            }
        };
    }

    @Override
    protected Collection<AnnotationProperties> getAnnotationProperties() {
        return annotations.values().stream().map(DynamicAnnotationEditor::getAnnotationProperties)
                .collect(Collectors.toList());
    }

}
