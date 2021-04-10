/*
 * onprom-umleditor
 *
 * UMLEditor.java
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

package it.unibz.inf.kaos.uml;

import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.UMLDiagramActions;
import it.unibz.inf.kaos.interfaces.Diagram;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.interfaces.UMLEditorListener;
import it.unibz.inf.kaos.owl.OWLExporter;
import it.unibz.inf.kaos.owl.OWLImporter;
import it.unibz.inf.kaos.owl.OWLUtility;
import it.unibz.inf.kaos.ui.action.DiagramEditorAction;
import it.unibz.inf.kaos.ui.action.DiagramPanelAction;
import it.unibz.inf.kaos.ui.action.ZoomAction;
import it.unibz.inf.kaos.ui.interfaces.DiagramEditor;
import it.unibz.inf.kaos.ui.panel.UMLDiagramPanel;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.UMLEditorMessages;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Set;

/**
 * @author T. E. Kalayci
 */
public class UMLEditor extends JInternalFrame implements DiagramEditor {
  protected JProgressBar progressBar = new JProgressBar();
  protected UMLDiagramPanel diagramPanel;
  protected OWLOntology ontology;
  protected File loadedFile;
  protected FileType[] supportedFormats;
  protected UMLEditorListener listener;
  protected String identifier;
  private JSplitPane splitPane;

    protected UMLEditor(OWLOntology _ontology) {
    super("", true, true, true, true);
    ontology = _ontology;
  }

  public UMLEditor(OWLOntology _ontology, UMLEditorListener _listener) {
    this(_ontology);
    listener = _listener;
    supportedFormats = new FileType[]{FileType.ONTOLOGY, FileType.UML};
    diagramPanel = new UMLDiagramPanel(this);
    initUI();
      loadOntology(null, ontology);
    setTitle("UML Editor");
  }

    public static void main(String[] args) {
    new UMLEditor(null, null).display();
  }

  protected JToolBar createMainToolbar() {
    JToolBar toolBar = new JToolBar("mainToolBar", JToolBar.VERTICAL);
    toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.select, this, diagramPanel)));
    toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.objects, this, diagramPanel)));
    toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.delete, this, diagramPanel)));
    return toolBar;
  }

  public void loadOntology(String _identifier, OWLOntology _ontology) {
    ontology = _ontology;
    identifier = _identifier;
      if (ontology != null) {
          diagramPanel.load(OWLImporter.getShapes(ontology));
      }
  }

  public void setProgressBar(JProgressBar _bar) {
    progressBar = _bar;
  }

  public JProgressBar getProgressBar() {
    return progressBar;
  }

  public void load(String _identifier, Set<DiagramShape<? extends Diagram>> shapes) {
    identifier = _identifier;
    diagramPanel.load(shapes);
  }

  public void display() {
    JFrame frame = display(WindowConstants.EXIT_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        UIUtility.stopWorkers();
      }
    });
  }

  public JFrame display(int howToClose) {
    final JFrame frame = new JFrame();
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(this, BorderLayout.CENTER);
    frame.getContentPane().add(progressBar, BorderLayout.SOUTH);
    frame.setDefaultCloseOperation(howToClose);
    frame.pack();
    frame.setVisible(true);
    return frame;
  }

  protected void initUI() {
    this.getContentPane().removeAll();
    this.setJMenuBar(createMenuBar());
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(createToolbar(), BorderLayout.WEST);
    JScrollPane scrollPane = new JScrollPane(diagramPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, null);
    this.getContentPane().add(splitPane, BorderLayout.CENTER);
    this.pack();
    this.setVisible(true);
      diagramPanel.clear(true);
  }

  @Override
  public void open(File file) {
    UIUtility.executeInBackground(() -> {
      IOUtility.open(file, supportedFormats).ifPresent(editorObjects -> {
        identifier = "";
        loadedFile = editorObjects.getFile();
        ontology = editorObjects.getOntology();
        diagramPanel.load(editorObjects.getShapes());
      });
      unloadForm();
    }, progressBar);
  }

  @Override
  public void export(boolean jsonFile) {
    UIUtility.executeInBackground(() -> {
      if (jsonFile) {
        //export as JSON file
        loadedFile = IOUtility.exportJSON(FileType.UML, diagramPanel.getShapes(true));
      } else {
        String documentIRI = null;
        if (ontology != null) {
          documentIRI = OWLUtility.getDocumentIRI(ontology);
        }
        if (documentIRI == null)
          documentIRI = "http://www.example.com/example.owl";
        String iri = UIUtility.input("Please enter document IRI", documentIRI);
        if (iri != null) {
          if (UIUtility.confirm(UMLEditorMessages.SAVE_FILE)) {
              UIUtility.selectFileToSave(FileType.ONTOLOGY).ifPresent(f ->
                      OWLExporter.export(iri, diagramPanel.getShapes(false), f).ifPresent(o -> {
                          ontology = o;
                          loadedFile = f;
                      }));
          }
        }
        if (listener != null)
          if (!identifier.isEmpty()) {
            listener.store(identifier, ontology);
          } else {
            listener.store(getOntologyName(), ontology);
          }
      }
    }, progressBar);
  }

  @Override
  public void close() {
    if (UIUtility.confirm(UMLEditorMessages.CLOSE_EDITOR)) {
      this.setVisible(false);
    }
  }

  @Override
  public void save() {
    UIUtility.executeInBackground(() -> {
      if (loadedFile != null) {
        FileType fileType = IOUtility.getFileType(loadedFile);
          if (fileType == FileType.ONTOLOGY) {
          if (ontology != null) {
              OWLExporter.export(OWLUtility.getDocumentIRI(ontology), diagramPanel.getShapes(false), loadedFile)
                      .ifPresent(o -> ontology = o);
          }
        }
          if (fileType == FileType.JSON | fileType == FileType.UML) {
            IOUtility.exportJSON(loadedFile, diagramPanel.getShapes(true));
        }
      }
      if (listener != null) {
        if (ontology != null) {
            OWLExporter.export(OWLUtility.getDocumentIRI(ontology), diagramPanel.getShapes(false), loadedFile).ifPresent(o ->
                    listener.store(identifier, o));
        } else {
          listener.store(identifier, FileType.UML, diagramPanel.getShapes(true));
        }
      }
    }, progressBar);
  }

  @Override
  public void unloadForm() {
    splitPane.setBottomComponent(null);
    diagramPanel.setCurrentAction(UMLDiagramActions.select);
  }

  @Override
  public void loadForm(@Nonnull JPanel form) {
    splitPane.setDividerLocation(0.75);
    splitPane.setBottomComponent(new JScrollPane(form));
    form.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentHidden(ComponentEvent e) {
        unloadForm();
        super.componentHidden(e);
        }
      });
    form.setVisible(true);
    diagramPanel.setCurrentAction(UMLDiagramActions.select);
  }

  @Nonnull
  protected String getOntologyName() {
    if (ontology != null) {
      return ontology.getOntologyID().getOntologyIRI().toString();
    }
    return "";
  }

  protected JToolBar createToolbar() {
    //Get default toolbar
    JToolBar toolBar = createMainToolbar();
    //add additional buttons for this editor
    toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.umlclass, this, diagramPanel)));
    toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.association, this, diagramPanel)));
    toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.isarelation, this, diagramPanel)));
    toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.relation, this, diagramPanel)));
    toolBar.add(UIUtility.createToolbarButton(new DiagramPanelAction(UMLDiagramActions.disjoint, this, diagramPanel)));
    return toolBar;
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu mnFile = new JMenu("File");
    mnFile.setMnemonic('f');
    mnFile.add(UIUtility.createMenuItem(new DiagramPanelAction(UMLDiagramActions.newdiagram, this, diagramPanel)));
    mnFile.add(UIUtility.createMenuItem(new DiagramEditorAction(UMLDiagramActions.open, this)));
    mnFile.add(UIUtility.createMenuItem(new DiagramEditorAction(UMLDiagramActions.save, this)));
    mnFile.add(UIUtility.createMenuItem(new DiagramEditorAction(UMLDiagramActions.saveas, this)));
    mnFile.add(UIUtility.createMenuItem(new DiagramEditorAction(UMLDiagramActions.export, this)));
    mnFile.add(UIUtility.createMenuItem(new DiagramPanelAction(UMLDiagramActions.image, this, diagramPanel)));
    mnFile.add(UIUtility.createMenuItem(new DiagramPanelAction(UMLDiagramActions.print, this, diagramPanel)));
    mnFile.add(UIUtility.createMenuItem(new DiagramEditorAction(UMLDiagramActions.close, this)));
    menuBar.add(mnFile);

    JMenu mnEdit = new JMenu("Edit");
    mnEdit.setMnemonic('e');
    mnEdit.add(UIUtility.createMenuItem(new DiagramPanelAction(UMLDiagramActions.undo, this, diagramPanel)));
    mnEdit.add(UIUtility.createMenuItem(new DiagramPanelAction(UMLDiagramActions.redo, this, diagramPanel)));
    menuBar.add(mnEdit);

    JMenu mnDiagram = new JMenu("Diagram");
    mnDiagram.setMnemonic('d');
    mnDiagram.add(UIUtility.createMenuItem(new DiagramPanelAction(UMLDiagramActions.layout, this, diagramPanel)));
    mnDiagram.add(UIUtility.createMenuItem(new ZoomAction(diagramPanel, UMLDiagramActions.zoomin)));
    mnDiagram.add(UIUtility.createMenuItem(new ZoomAction(diagramPanel, UMLDiagramActions.zoomout)));
    mnDiagram.add(UIUtility.createMenuItem(new ZoomAction(diagramPanel, UMLDiagramActions.resetzoom)));
    mnDiagram.add(UIUtility.createMenuItem(new DiagramPanelAction(UMLDiagramActions.grid, this, diagramPanel)));
    menuBar.add(mnDiagram);
    return menuBar;
  }
}