/*
 * onprom-annoeditor
 *
 * AnnotationEditor.java
 *
 * Copyright (C) 2016-2017 Free University of Bozen-Bolzano
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

import it.unibz.inf.kaos.data.AbstractAnnotation;
import it.unibz.inf.kaos.data.ActionType;
import it.unibz.inf.kaos.data.AnnotationProperties;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.factory.DefaultAnnotationFactory;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.AnnotationEditorListener;
import it.unibz.inf.kaos.interfaces.AnnotationFactory;
import it.unibz.inf.kaos.owl.OWLImporter;
import it.unibz.inf.kaos.ui.action.DrawingPanelAction;
import it.unibz.inf.kaos.ui.form.QueryEditor;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.UMLEditorMessages;
import it.unibz.inf.kaos.uml.UMLEditor;
import org.reflections.Reflections;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;

/**
 * Graphical editor for annotating ontologies with XES standard using UML class diagram
 * <p>
 * @author T. E. Kalayci on 25-Oct-16.
 */
public class AnnotationEditor extends UMLEditor {
  public AnnotationEditor(OWLOntology _ontology, AnnotationEditorListener _listener) {
    this(_ontology, _listener, new DefaultAnnotationFactory());
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
        loadedFile = IOUtility.exportJSON(FileType.ANNOTATION, diagramPanel.getAllShapes(true));
      } else {
        AnnotationQueries annotationsQueries = new AnnotationQueries();
        for (Annotation annotation : diagramPanel.getItems(Annotation.class)) {
          annotationsQueries.addQuery(annotation.getQuery());
        }
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
        if (fileType.equals(FileType.ONTOLOGY)) {
          loadedFile = IOUtility.exportJSON(FileType.ANNOTATION, diagramPanel.getAllShapes(true));
        } else {
          IOUtility.exportJSON(loadedFile, diagramPanel.getAllShapes(true));
        }
      }
      if (listener != null) {
        listener.store(identifier, FileType.ANNOTATION, diagramPanel.getAllShapes(true));
      }
      return null;
    }, progressBar);
  }

  @Override
  protected JToolBar createToolbar() {
    JToolBar toolBar = getMainToolbar(diagramPanel);
    new Reflections("it.unibz.inf.kaos.data").getSubTypesOf(AbstractAnnotation.class).forEach(annotation -> {
      final AnnotationProperties annotationProperties = annotation.getAnnotation(AnnotationProperties.class);
      toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(diagramPanel, new ActionType() {
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

        @Override
        public String toString() {
          return annotationProperties.label();
        }
      })));
    });
    return toolBar;
  }
}