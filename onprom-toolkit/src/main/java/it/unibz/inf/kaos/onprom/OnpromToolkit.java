/*
 * onprom-toolkit
 *
 * OnpromToolkit.java
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

package it.unibz.inf.kaos.onprom;

import it.unibz.inf.kaos.annotation.AnnotationEditor;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.dynamic.DynamicAnnotationEditor;
import it.unibz.inf.kaos.interfaces.AnnotationEditorListener;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.logextractor.XESLogExtractorWithEBDAMapping;
import it.unibz.inf.kaos.ui.component.*;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.UMLEditorMessages;
import it.unibz.inf.kaos.uml.UMLEditor;
import it.unibz.inf.ontop.model.OBDAModel;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * @author T. E. Kalayci on 26-Apr-2017
 */
public class OnpromToolkit extends JFrame implements AnnotationEditorListener {
    private static final Logger logger = LoggerFactory.getLogger(OnpromToolkit.class.getSimpleName());
    private final JProgressBar progressBar = new JProgressBar();
    private final FileType[] supportedFormats = {FileType.ONTOLOGY, FileType.UML, FileType.ANNOTATION, FileType.MAPPING, FileType.QUERIES, FileType.XLOG};
    private final JDesktopPane desktop = new JDesktopPane();

    private final ObjectTree objects;
    private final WindowTree windows;

    private OnpromToolkit() {
        objects = new ObjectTree(this);
        windows = new WindowTree();
        this.setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        dtde.acceptDrop(dtde.getDropAction());
                        Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (transferData instanceof java.util.List) {
                            java.util.List<File> files = (java.util.List<File>) transferData;
                            if (!files.isEmpty()) {
                                UIUtility.executeInBackground(() -> objects.openFiles(files.toArray(new File[]{})), progressBar);
                                dtde.dropComplete(true);
                            }
                        }
                    } catch (Exception e) {
                        logError(e);
                    }
                } else {
                    dtde.rejectDrop();
                }
            }
        }));


        JPanel treePanel = new JPanel(new GridLayout(0, 1));
        treePanel.setPreferredSize(new Dimension(300, 0));
        treePanel.add(new JScrollPane(objects.getTreeComponent()));
        treePanel.add(new JScrollPane(windows.getTreeComponent()));

        getContentPane().setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane();
        splitPane.setTopComponent(treePanel);
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        splitPane.setBottomComponent(desktop);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(progressBar, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                UIUtility.stopWorkers();
            }
        });
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "True");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        new OnpromToolkit();
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);

        JMenuItem openItem = new JMenuItem("Load...", KeyEvent.VK_O);
        openItem.addActionListener(e -> UIUtility.executeInBackground(() ->
                objects.openFiles(UIUtility.selectFiles(supportedFormats)), progressBar));
        mnFile.add(openItem);
        JMenuItem saveItem = new JMenuItem("Save Selected...", KeyEvent.VK_S);
        saveItem.addActionListener(e -> UIUtility.executeInBackground(objects::saveSelected, progressBar));
        mnFile.add(saveItem);
        JMenuItem saveAllItem = new JMenuItem("Save All...", KeyEvent.VK_A);
        saveAllItem.addActionListener(e -> UIUtility.executeInBackground(objects::saveAll, progressBar));
        mnFile.add(saveAllItem);
        JMenuItem clearItem = new JMenuItem("Remove All", KeyEvent.VK_R);
        clearItem.addActionListener(e -> UIUtility.executeInBackground(this::reset, progressBar));
        mnFile.add(clearItem);
        JMenuItem exitItem = new JMenuItem("Quit", KeyEvent.VK_Q);
        exitItem.addActionListener(e -> this.dispose());
        mnFile.add(exitItem);

        JMenu mnTools = new JMenu("Tools");
        mnTools.setMnemonic(KeyEvent.VK_T);
        JMenuItem umlItem = new JMenuItem("Open UML Editor", KeyEvent.VK_U);
        umlItem.addActionListener(e -> UIUtility.executeInBackground(this::displayUMLEditor, progressBar));
        mnTools.add(umlItem);
        JMenuItem annoItem = new JMenuItem("Open Annotation Editor", KeyEvent.VK_A);
        annoItem.addActionListener(e -> UIUtility.executeInBackground(this::displayAnnotationEditor, progressBar));
        mnTools.add(annoItem);
        JMenuItem dynamicItem = new JMenuItem("Open Dynamic Annotation Editor", KeyEvent.VK_D);
        dynamicItem.addActionListener(e -> UIUtility.executeInBackground(this::displayDynamicAnnotationEditor, progressBar));
        mnTools.add(dynamicItem);
        JMenuItem exportItem = new JMenuItem("Export Log", KeyEvent.VK_E);
        exportItem.addActionListener(e -> UIUtility.executeInBackground(this::exportLog, progressBar));
        mnTools.add(exportItem);
        JMenuItem showExportItem = new JMenuItem("Export Log using Custom Event Ontology", KeyEvent.VK_D);
        showExportItem.addActionListener(e -> UIUtility.executeInBackground(this::showExportDiagram, progressBar));
        mnTools.add(showExportItem);
        menuBar.add(mnTools);

        JMenu mnHelp = new JMenu("Help");
        mnHelp.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> InformationDialog.display(UMLEditorMessages.ABOUT.getMessage()));
        mnHelp.add(aboutItem);
        menuBar.add(mnHelp);

        return menuBar;
    }

    @Nonnull
    public Set<TreeNode<Object>> getResourceNodes() {
        return objects.getAllNodes();
    }

    public void displayUMLEditor() {
        displayEditor(new UMLEditor(null, this));
    }

    public void displayAnnotationEditor() {
        displayEditor(new AnnotationEditor(null, this));
    }

    public void displayDynamicAnnotationEditor() {
        displayEditor(new DynamicAnnotationEditor(null, null, this));
    }

    private void displayEditor(UMLEditor editor) {
        editor.setProgressBar(progressBar);
        editor.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                windows.removeNodeWithObject(e.getInternalFrame());
            }
        });
        desktop.add(editor, BorderLayout.CENTER);
        try {
            editor.setMaximum(true);
        } catch (Exception e) {
            logError(e);
        }
        windows.add(editor.getTitle(), FileType.OTHER, editor);
        loadShapes(editor);
    }

    private void loadShapes(UMLEditor umlEditor) {
        objects.getSelectedNode().ifPresent(node ->
                node.getUserObjectProvider().ifPresent(selectedObject -> {
                    if (selectedObject instanceof OWLOntology) {
                        umlEditor.loadOntology(node.getIdentifier(), (OWLOntology) selectedObject);
                    } else if (selectedObject instanceof Set) {
                        umlEditor.load(node.getIdentifier(), (Set<DiagramShape>) selectedObject);
                    }
                })
        );
    }

    private void showExportDiagram() {
        ExtractionFrame editor = new ExtractionFrame(this);
        editor.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                windows.removeNodeWithObject(e.getInternalFrame());
            }
        });
        desktop.add(editor, BorderLayout.CENTER);
        try {
            editor.setMaximum(true);
        } catch (Exception e) {
            logError(e);
        }
        windows.add(editor.getTitle(), FileType.OTHER, editor);
    }

    private void exportLog() {
        TreePath[] paths = objects.getSelectionPaths();
        if (paths != null) {
            OWLOntology ontology = null;
            OBDAModel model = null;
            AnnotationQueries queries = null;
            for (TreePath path : paths) {
                Object object = ((TreeNode) path.getLastPathComponent()).getUserObject();
                if (object instanceof OWLOntology) {
                    ontology = (OWLOntology) object;
                }
                if (object instanceof OBDAModel) {
                    model = (OBDAModel) object;
                }
                if (object instanceof AnnotationQueries) {
                    queries = (AnnotationQueries) object;
                }
            }
            if (ontology != null && model != null && queries != null) {
                try {
                    long start = System.currentTimeMillis();
                    XLog xlog = new XESLogExtractorWithEBDAMapping().extractXESLog(ontology, model, queries);
                    logger.error("It took " + (System.currentTimeMillis() - start) + " ms to export log");
                    displayLogSummary(xlog);
                } catch (Exception e) {
                    logError(e);
                }
            } else {
                InformationDialog.display("Please select Ontology, Mapping and Queries to start log exporting!");
            }
        }
    }

    public void displayLogSummary(XLog xlog) {
        displayLogSummary(objects.addObject("Extracted Log", FileType.XLOG, xlog));
    }

    public void displayLogSummary(TreeNode<Object> node) {
        node.getUserObjectProvider().ifPresent(selectedObject -> {
            JInternalFrame infoFrame = new LogSummaryPanel(XLogInfoFactory.createLogInfo((XLog) selectedObject));
            infoFrame.addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    windows.removeNodeWithObject(e.getInternalFrame());
                }
            });
            desktop.add(infoFrame, BorderLayout.CENTER);
            try {
                infoFrame.setMaximum(true);
            } catch (Exception e) {
                logError(e);
            }
            windows.add(infoFrame.getTitle(), FileType.OTHER, infoFrame);
        });
    }

    private void reset() {
        objects.removeAll();
        for (JInternalFrame internalFrame : desktop.getAllFrames()) {
            internalFrame.dispose();
        }
    }

    private void logError(Exception e) {
        logger.error(e.getMessage(), e);
        InformationDialog.display(e.getMessage());
    }

    @Override
    public void store(String identifier, AnnotationQueries annotationQueries) {
        objects.addObject(identifier, FileType.QUERIES, annotationQueries);
    }

    @Override
    public void store(String identifier, OWLOntology ontology) {
        objects.addObject(identifier, FileType.ONTOLOGY, ontology);
    }

    @Override
    public void store(String identifier, FileType type, Collection<DiagramShape> shapes) {
        objects.addObject(identifier, type, shapes);
    }
}
