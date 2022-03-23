/*
 * annotationeditor
 *
 * AnnotationEditor.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
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

package it.unibz.inf.onprom;

import it.unibz.inf.onprom.data.*;
import it.unibz.inf.onprom.data.query.AnnotationQueries;
import it.unibz.inf.onprom.interfaces.*;
import it.unibz.inf.onprom.logextractor.ocel.OCELLogExtractor;
import it.unibz.inf.onprom.logextractor.xes.XESLogExtractor;
import it.unibz.inf.onprom.owl.OWLImporter;
import it.unibz.inf.onprom.owl.OWLUtility;
import it.unibz.inf.onprom.ui.action.DiagramPanelAction;
import it.unibz.inf.onprom.ui.action.ToolbarAction;
import it.unibz.inf.onprom.ui.form.AnnotationSelectionDialog;
import it.unibz.inf.onprom.ui.form.QueryEditor;
import it.unibz.inf.onprom.ui.panel.AnnotationDiagramPanel;
import it.unibz.inf.onprom.ui.utility.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by T. E. Kalayci on 16-Oct-2017.
 */
public class AnnotationEditor extends UMLEditor {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationEditor.class.getName());
    private static final Map<String, UMLClass> annotations = new HashMap<>();

    private AnnotationEditor(OWLOntology domainOntology, AnnotationEditorListener _listener) {
        super(domainOntology);
        supportedFormats = new FileType[]{FileType.ONTOLOGY, FileType.UML, FileType.ANNOTATION};
        listener = _listener;
        diagramPanel = new AnnotationDiagramPanel(this);
        initUI();
        if (ontology != null) {
            diagramPanel.load(OWLImporter.getShapes(ontology));
        }
        setTitle("Annotation Editor");
    }

    public AnnotationEditor(OWLOntology upperOntology, OWLOntology domainOntology, AnnotationEditorListener _listener) {
        this(domainOntology, _listener);
        load(upperOntology);
    }

    public AnnotationEditor(AnnotationEditorListener _listener) {
        this(null, _listener);
        loadDefault(null);
    }

    public AnnotationEditor() {
        this(null, null);
        loadDefault(null);
    }

    private static AnnotationFactory getDynamicFactory() {
        return (panel, currentAction, selectedCls) -> {
            try {
                UMLClass annotationClass = annotations.get(currentAction.getTitle());
                if (annotationClass != null) {
                    return Optional.of(new DynamicAnnotation(annotationClass, selectedCls, getAnnotationProperties(annotationClass)));
                }
            } catch (RuntimeException e) {
                logger.error(String.format("A runtime exception occurred: %s", e.getMessage()));
            }
            return Optional.empty();
        };
    }

    private static AnnotationFactory getDefaultFactory() {
        return new AnnotationFactory() {
            @Override
            public Optional<Annotation> createAnnotation(AnnotationDiagram panel, ActionType currentAction, UMLClass selectedCls) {
                CaseAnnotation caseAnnotation = panel.findFirst(CaseAnnotation.class);

                if (currentAction.toString().equals(CaseAnnotation.class.getAnnotation(AnnotationProperties.class).title())) {
                    if (caseAnnotation == null || UIUtility.confirm(AnnotationEditorMessages.CHANGE_CASE)) {
                        return Optional.of(new CaseAnnotation(selectedCls));
                    }
                } else if (currentAction.toString().equals(EventAnnotation.class.getAnnotation(AnnotationProperties.class).title())) {
                    if (caseAnnotation == null) {
                        UIUtility.error(AnnotationEditorMessages.SELECT_CASE);
                    } else {
                        if (!NavigationUtility.isConnected(selectedCls, caseAnnotation.getRelatedClass(), false)) {
                            UIUtility.error("Event class is not connected to Trace class!");
                        } else {
                            return Optional.of(new EventAnnotation("event" + panel.count(EventAnnotation.class), caseAnnotation, selectedCls));
                        }
                    }
                }
                return Optional.empty();
            }

            @Override
            public boolean checkRemoval(AnnotationDiagram panel, Annotation annotation) {
                return !(annotation instanceof CaseAnnotation) || panel.count(Annotation.class) < 2 || UIUtility.confirm(AnnotationEditorMessages.CASE_DELETE_CONFIRMATION);
            }
        };
    }

    public static void main(String[] a) {
        System.setProperty("sun.java2d.opengl", "True");
        new AnnotationEditor().display();
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

    @Override
    public void export(boolean asFile) {
        UIUtility.executeInBackground(() -> {
            if (asFile) {
                loadedFile = IOUtility.exportJSON(FileType.ANNOTATION, diagramPanel.getShapes(true));
            } else {
                AnnotationQueries annotationsQueries = new AnnotationQueries();
                diagramPanel.getAll(Annotation.class)
                        .filter(annotation -> !annotation.isDisabled())
                        .map(Annotation::getQuery)
                        .forEach(annotationsQueries::addQuery);

                if (annotationsQueries.getQueryCount() > 0) {
                    new QueryEditor(annotationsQueries);
                    if (listener != null) {
                        String title = getOntologyName();
                        if (title.isEmpty()) {
                            title = "Exported Queries";
                        }
                        ((AnnotationEditorListener) listener).store(title, annotationsQueries);
                    }
                    if (UIUtility.confirm(UMLEditorMessages.SAVE_FILE)) {
                        IOUtility.exportJSON(FileType.QUERIES, annotationsQueries); //save as *.aqr
                    }
                }
            }
        }, progressBar);
    }

    @Override
    public void save() {
        UIUtility.executeInBackground(() -> {
            if (loadedFile != null) {
                FileType fileType = IOUtility.getFileType(loadedFile);
                if (fileType == FileType.ONTOLOGY) {
                    loadedFile = IOUtility.exportJSON(FileType.ANNOTATION, diagramPanel.getShapes(true));
                } else {
                    IOUtility.exportJSON(loadedFile, diagramPanel.getShapes(true));
                }
            }
            if (listener != null) {
                listener.store(identifier, FileType.ANNOTATION, diagramPanel.getShapes(true));
            }
        }, progressBar);
    }

    private void load(OWLOntology upperOntology) {
        if (upperOntology != null) {
            annotations.clear();
            new AnnotationSelectionDialog(
                    OWLImporter.getShapes(upperOntology).stream()
                            .filter(UMLClass.class::isInstance)
                            .map(UMLClass.class::cast))
                    .getSelectedClasses()
                    .forEach(umlClass -> annotations.put(umlClass.getName(), umlClass));
            setTitle("Annotation Editor for " + upperOntology.getOntologyID().getOntologyIRI().or(IRI.create("")));
            ((AnnotationDiagramPanel) diagramPanel).setFactory(getDynamicFactory());
            initUI();
        } else {
            annotations.clear();
            setTitle("Default Annotation Editor");
            ((AnnotationDiagramPanel) diagramPanel).setFactory(getDefaultFactory());
            initUI();
        }
    }

    private void loadDefault(OWLOntology upperOntology) {
        if (upperOntology != null) {
            annotations.clear();
            OWLImporter.getShapes(upperOntology).stream()
                    .filter(UMLClass.class::isInstance)
                    .map(UMLClass.class::cast)
                    .forEach(
                            umlClass -> annotations.put(umlClass.getName(), umlClass)
                    );
            setTitle("Annotation Editor for " + upperOntology.getOntologyID().getOntologyIRI().or(IRI.create("")));
            ((AnnotationDiagramPanel) diagramPanel).setFactory(getDynamicFactory());
            initUI();
        } else {
            annotations.clear();
            setTitle("Default Annotation Editor");
            ((AnnotationDiagramPanel) diagramPanel).setFactory(getDefaultFactory());
            initUI();
        }
    }

    @Override
    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = super.createMenuBar();
        JMenu ontologies = new JMenu("Upper Ontology");
        ontologies.setMnemonic('u');
        ontologies.add(UIUtility.createMenuItem(getDefaultAction()));
        ontologies.add(UIUtility.createMenuItem(getXESOntologyAction()));
        ontologies.add(UIUtility.createMenuItem(getOCELOntologyAction()));
        ontologies.add(UIUtility.createMenuItem(getCustomUpperOntologyAction()));
        menuBar.add(ontologies);
        return menuBar;
    }

    protected Collection<AnnotationProperties> getAnnotationProperties() {
        if (annotations.isEmpty()) {
            return Arrays.asList(
                    CaseAnnotation.class.getAnnotation(AnnotationProperties.class),
                    EventAnnotation.class.getAnnotation(AnnotationProperties.class)
            );
        } else {
            return annotations.values().stream().map(AnnotationEditor::getAnnotationProperties)
                    .collect(Collectors.toList());
        }
    }

    @Override
    protected JToolBar createToolbar() {
        JToolBar toolBar = createMainToolbar();
        toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.disable, this, diagramPanel)));
        toolBar.add(UIUtility.createToolbarButton(getDefaultAction()));
        toolBar.add(UIUtility.createToolbarButton(getXESOntologyAction()));
        toolBar.add(UIUtility.createToolbarButton(getOCELOntologyAction()));
        toolBar.add(UIUtility.createToolbarButton(getCustomUpperOntologyAction()));
        getAnnotationProperties().forEach(annotationProperties -> toolBar.add(UIUtility.createToolbarButton(
                        new DiagramPanelAction(
                                new ActionTypeImpl(annotationProperties.title(), annotationProperties.tooltip(), annotationProperties.title(), annotationProperties.mnemonic()),
                                this, diagramPanel))
                )
        );
        return toolBar;
    }

    private ToolbarAction getCustomUpperOntologyAction() {
        return new ToolbarAction(new ActionTypeImpl("Custom Upper Ontology", "Select Custom Upper Ontology", "owl-ontology")) {
            @Override
            public void execute() {
                UIUtility.selectFileToOpen(FileType.ONTOLOGY).flatMap(OWLUtility::loadOntologyFromFile).ifPresent(ontology -> load(ontology));
            }
        };
    }

    private ToolbarAction getDefaultAction() {
        return new ToolbarAction(new ActionTypeImpl("XES Case-Event Ontology", "XES ontology derived from standard", "xes-ontology")) {
            @Override
            public void execute() {
                loadDefault(null);
            }
        };
    }

    private ToolbarAction getXESOntologyAction() {
        return new ToolbarAction(new ActionTypeImpl("XES Standard Ontology", "XES ontology containing predefined attributes", "xes-ontology")) {
            @Override
            public void execute() {
                loadDefault(XESLogExtractor.getOntology());
            }
        };
    }

    private ToolbarAction getOCELOntologyAction() {
        return new ToolbarAction(new ActionTypeImpl("OCEL Ontology", "OCEL ontology derived from OCEL standard", "ocel-ontology")) {
            @Override
            public void execute() {
                loadDefault(OCELLogExtractor.getOntology());
            }
        };
    }
}