/*
 * onprom-annoeditor
 *
 * AnnotationEditor.java
 *
 * Copyright (C) 2016-2018 Free University of Bozen-Bolzano
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
package it.unibz.inf.kaos.annotation;

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.interfaces.*;
import it.unibz.inf.kaos.owl.OWLImporter;
import it.unibz.inf.kaos.ui.action.DiagramPanelAction;
import it.unibz.inf.kaos.ui.form.QueryEditor;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import it.unibz.inf.kaos.ui.utility.*;
import it.unibz.inf.kaos.uml.UMLEditor;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Graphical editor for annotating ontologies with XES standard using UML class diagram
 * <p>
 *
 * @author T. E. Kalayci on 25-Oct-16.
 */
public class AnnotationEditor extends UMLEditor {
    public AnnotationEditor(OWLOntology _ontology, AnnotationEditorListener _listener) {
        this(_ontology, _listener, new AnnotationFactory() {
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
        });
    }

    public AnnotationEditor(OWLOntology _ontology, AnnotationEditorListener _listener, AnnotationFactory factory) {
        super(_ontology);
        supportedFormats = new FileType[]{FileType.ONTOLOGY, FileType.UML, FileType.ANNOTATION};
        listener = _listener;
        diagramPanel = new AnnotationDiagramPanel(this, factory);
        initUI();
        if (ontology != null) {
            diagramPanel.load(OWLImporter.getShapes(ontology));
        }
        setTitle("Annotation Editor");
    }

    public static void main(String a[]) {
        new AnnotationEditor(null, null).display();
    }

    @Override
    public void export(boolean asFile) {
        UIUtility.executeInBackground(() -> {
            if (asFile) {
                loadedFile = IOUtility.exportJSON(FileType.ANNOTATION, diagramPanel.getShapes(true));
            } else {
                AnnotationQueries annotationsQueries = new AnnotationQueries();
                diagramPanel.getAll(Annotation.class).map(Annotation::getQuery).forEach(annotationsQueries::addQuery);
                if (annotationsQueries.getQueryCount() > 0) {
                    new QueryEditor(annotationsQueries);
                    if (listener != null) {
                        String title = getOntologyName();
                        if (title == null || title.isEmpty()) {
                            title = "Exported Queries";
                        }
                        ((AnnotationEditorListener) listener).store(title, annotationsQueries);
                    }
                    if (UIUtility.confirm(UMLEditorMessages.SAVE_FILE)) {
                        IOUtility.exportJSON(FileType.QUERIES, annotationsQueries);
                    }
                }
            }
            return null;
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
            return null;
        }, progressBar);
    }

    protected Collection<AnnotationProperties> getAnnotationProperties() {
        return Arrays.asList(
                CaseAnnotation.class.getAnnotation(AnnotationProperties.class),
                EventAnnotation.class.getAnnotation(AnnotationProperties.class)
        );
    }

    @Override
    protected JToolBar createToolbar() {
        JToolBar toolBar = createMainToolbar();
        getAnnotationProperties().forEach(annotationProperties -> toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(new AbstractActionType() {
            @Override
            public char getMnemonic() {
                return annotationProperties.mnemonic();
            }

            @Override
            public String getTooltip() {
                return annotationProperties.tooltip();
            }

            @Override
            public String getTitle() {
                return annotationProperties.title();
            }
        }, this, diagramPanel))));
        return toolBar;
    }

}