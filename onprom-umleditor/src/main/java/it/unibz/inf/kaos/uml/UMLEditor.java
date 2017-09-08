/*
 * onprom-umleditor
 *
 * UMLEditor.java
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

package it.unibz.inf.kaos.uml;

import it.unibz.inf.kaos.data.EditorObjects;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.UMLActionType;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.interfaces.UMLEditorListener;
import it.unibz.inf.kaos.owl.OWLExporter;
import it.unibz.inf.kaos.owl.OWLImporter;
import it.unibz.inf.kaos.owl.OWLUtility;
import it.unibz.inf.kaos.ui.action.DialogAction;
import it.unibz.inf.kaos.ui.action.DrawingPanelAction;
import it.unibz.inf.kaos.ui.action.ZoomAction;
import it.unibz.inf.kaos.ui.interfaces.DiagramEditor;
import it.unibz.inf.kaos.ui.panel.UMLDiagramPanel;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.UMLEditorMessages;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Set;

/**
 * @author T. E. Kalayci
 */
public class UMLEditor extends JInternalFrame implements DiagramEditor {
  protected JProgressBar progressBar;
  protected UMLDiagramPanel diagramPanel;
  protected OWLOntology ontology;
  protected File loadedFile = null;
  protected FileType[] supportedFormats;
  protected UMLEditorListener listener;
  protected String identifier;
  private JSplitPane splitPane;
  private JScrollPane scrollPane;

  public UMLEditor(OWLOntology _ontology) {
    super("", true, true, true, true);
    ontology = _ontology;
  }

  public UMLEditor(OWLOntology _ontology, UMLEditorListener _listener) {
    this(_ontology);
    listener = _listener;
    supportedFormats = new FileType[]{FileType.ONTOLOGY, FileType.UML};
    //if ontology is not null, import it and load it as UML diagram
    diagramPanel = new UMLDiagramPanel(this);
    initUI();
    if (ontology != null) {
      loadOntology(null, ontology);
    }
    setTitle("UML Editor");
  }

  public static void main(String args[]) {
    new UMLEditor(null, null).display();
  }

  protected static JToolBar getMainToolbar(UMLDiagramPanel panel) {
    JToolBar toolBar = new JToolBar("mainToolBar", JToolBar.VERTICAL);
    toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(panel, UMLActionType.select)));
    toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(panel, UMLActionType.objects)));
    toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(panel, UMLActionType.delete)));
    return toolBar;
  }

  public void loadOntology(String _identifier, OWLOntology _ontology) {
    ontology = _ontology;
    identifier = _identifier;
    diagramPanel.load(OWLImporter.getShapes(ontology));
  }

  public void setProgressBar(JProgressBar _bar) {
    progressBar = _bar;
  }

  public void load(String _identifier, Set<DiagramShape> shapes) {
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
    JProgressBar pgBar = new JProgressBar();
    setProgressBar(pgBar);
    frame.getContentPane().add(pgBar, BorderLayout.SOUTH);
    frame.setDefaultCloseOperation(howToClose);
    frame.setSize(this.getSize());
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    return frame;
  }

  protected void initUI() {
    this.setJMenuBar(getMenuBar(diagramPanel));
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(createToolbar(), BorderLayout.WEST);
    scrollPane = new JScrollPane(diagramPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, null);
    this.getContentPane().add(splitPane, BorderLayout.CENTER);
    this.setSize(new Dimension(1024, 768));
    this.setVisible(true);
  }

  @Override
  public void open(File file) {
    UIUtility.executeInBackground(() -> {
      EditorObjects editorObjects = IOUtility.open(file, supportedFormats);
      if (editorObjects != null) {
        identifier = null;
        loadedFile = editorObjects.getFile();
        ontology = editorObjects.getOntology();
        diagramPanel.load(editorObjects.getShapes());
      }
      loadEditor(null);
      return null;
    }, progressBar);
  }

  @Override
  public void export(boolean jsonFile) {
    UIUtility.executeInBackground(() -> {
      if (jsonFile) {
        //export as JSON file
        loadedFile = IOUtility.exportJSON(FileType.UML, diagramPanel.getAllShapes(true));
      } else {
        String documentIRI = null;
        if (ontology != null) {
          documentIRI = OWLUtility.getDocumentIRI(ontology);
        }
        if (documentIRI == null)
          documentIRI = "http://www.example.com/example.owl";
        String iri = UIUtility.input("Please enter document IRI", documentIRI);
        File file = null;
        if (iri != null) {
          if (UIUtility.confirm(UMLEditorMessages.SAVE_FILE)) {
            file = IOUtility.selectOntologyFileToSave();
          }
          ontology = OWLExporter.export(iri, diagramPanel.getAllShapes(false), file);
          //loaded file is changed
          loadedFile = file;
        }
        if (listener != null)
          if (identifier != null && !identifier.isEmpty()) {
            listener.store(identifier, ontology);
          } else {
            listener.store(getOntologyName(), ontology);
          }
      }
      return null;
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
        if (fileType.equals(FileType.ONTOLOGY)) {
          if (ontology != null) {
            ontology = OWLExporter.export(OWLUtility.getDocumentIRI(ontology), diagramPanel.getAllShapes(false), loadedFile);
          }
        }
        if (fileType.equals(FileType.JSON) | fileType.equals(FileType.UML)) {
          IOUtility.exportJSON(loadedFile, diagramPanel.getAllShapes(true));
        }
      }
      if (listener != null) {
        if (ontology != null) {
          listener.store(identifier, OWLExporter.export(OWLUtility.getDocumentIRI(ontology), diagramPanel.getAllShapes(false), loadedFile));
        } else {
          listener.store(identifier, FileType.UML, diagramPanel.getAllShapes(true));
        }
      }

      return null;
    }, progressBar);
  }

  public void loadEditor(JPanel panel) {
    splitPane.setTopComponent(scrollPane);
    splitPane.setBottomComponent(panel);
    splitPane.setDividerLocation(0.8);
    diagramPanel.setCurrentAction(UMLActionType.select);
  }

  protected String getOntologyName() {
    if (ontology != null) {
      return ontology.getOntologyID().getOntologyIRI().toString();
    }
    return null;
  }

  protected JToolBar createToolbar() {
    //Get default toolbar
    JToolBar toolBar = getMainToolbar(diagramPanel);
    //add additional buttons for this editor
    toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(diagramPanel, UMLActionType.umlclass)));
    toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(diagramPanel, UMLActionType.association)));
    toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(diagramPanel, UMLActionType.isarelation)));
    toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(diagramPanel, UMLActionType.relation)));
    toolBar.add(UIUtility.createToolbarButton(new DrawingPanelAction(diagramPanel, UMLActionType.disjoint)));
    return toolBar;
  }

  private JMenuBar getMenuBar(UMLDiagramPanel panel) {
    JMenuBar menuBar = new JMenuBar();
    JMenu mnFile = new JMenu("File");
    mnFile.setMnemonic('f');
    mnFile.add(UIUtility.createMenuItem(new DrawingPanelAction(panel, UMLActionType.newdiagram)));
    mnFile.add(UIUtility.createMenuItem(new DialogAction(this, UMLActionType.open)));
    mnFile.add(UIUtility.createMenuItem(new DialogAction(this, UMLActionType.save)));
    mnFile.add(UIUtility.createMenuItem(new DialogAction(this, UMLActionType.saveas)));
    mnFile.add(UIUtility.createMenuItem(new DialogAction(this, UMLActionType.export)));
    mnFile.add(UIUtility.createMenuItem(new DrawingPanelAction(panel, UMLActionType.image)));
    mnFile.add(UIUtility.createMenuItem(new DrawingPanelAction(panel, UMLActionType.print)));
    mnFile.add(UIUtility.createMenuItem(new DialogAction(this, UMLActionType.close)));
    menuBar.add(mnFile);
    //Edit menu
    JMenu mnEdit = new JMenu("Edit");
    mnEdit.setMnemonic('e');
    mnEdit.add(UIUtility.createMenuItem(new DrawingPanelAction(panel, UMLActionType.undo)));
    mnEdit.add(UIUtility.createMenuItem(new DrawingPanelAction(panel, UMLActionType.redo)));
    menuBar.add(mnEdit);
    //Edit menu
    JMenu mnDiagram = new JMenu("Diagram");
    mnDiagram.setMnemonic('d');
    mnDiagram.add(UIUtility.createMenuItem(new DrawingPanelAction(panel, UMLActionType.layout)));
    mnDiagram.add(UIUtility.createMenuItem(new ZoomAction(panel, UMLActionType.zoomin)));
    mnDiagram.add(UIUtility.createMenuItem(new ZoomAction(panel, UMLActionType.zoomout)));
    mnDiagram.add(UIUtility.createMenuItem(new ZoomAction(panel, UMLActionType.resetzoom)));
    mnDiagram.add(UIUtility.createMenuItem(new DrawingPanelAction(panel, UMLActionType.grid)));
    menuBar.add(mnDiagram);
    return menuBar;
  }
}