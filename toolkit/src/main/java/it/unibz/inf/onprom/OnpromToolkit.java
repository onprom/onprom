/*
 * onprom-toolkit
 *
 * OnpromToolkit.java
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

package it.unibz.inf.onprom;

import it.unibz.inf.onprom.data.FileType;
import it.unibz.inf.onprom.data.query.AnnotationQueries;
import it.unibz.inf.onprom.interfaces.AnnotationEditorListener;
import it.unibz.inf.onprom.interfaces.Diagram;
import it.unibz.inf.onprom.interfaces.DiagramShape;
import it.unibz.inf.onprom.ui.component.*;
import it.unibz.inf.onprom.ui.form.InformationDialog;
import it.unibz.inf.onprom.ui.panel.DatasourcePropertiesPanel;
import it.unibz.inf.onprom.ui.panel.ExtractionPanel;
import it.unibz.inf.onprom.ui.utility.DrawingUtility;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import it.unibz.inf.onprom.utility.ToolkitMessages;
import it.unibz.inf.onprom.utility.VersionUtility;

import it.unibz.inf.pm.ocel.entity.OcelLog;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
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
import java.util.Properties;
import java.util.Set;

/**
 * @author T. E. Kalayci on 26-Apr-2017
 */
public class OnpromToolkit extends JFrame implements AnnotationEditorListener {
    private static final Logger logger = LoggerFactory.getLogger(OnpromToolkit.class.getSimpleName());
    private final JProgressBar progressBar = new JProgressBar();
    private final FileType[] supportedFormats = {
            FileType.ONTOLOGY, FileType.UML, FileType.ANNOTATION, FileType.MAPPING, FileType.QUERIES, FileType.XLOG, FileType.DS_PROPERTIES
    };
    private final JDesktopPane desktop = new JDesktopPane();

    private final ObjectTree objects;
    private final WindowTree windows;

    private OnpromToolkit() {
        setIconImage(DrawingUtility.getLogo());
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

    private void checkForUpdate() {
        UIUtility.executeInBackground(() -> {
            progressBar.setIndeterminate(true);
            String message = VersionUtility.checkVersion();
            progressBar.setIndeterminate(false);
            InformationDialog.display(message, "Update Information");
        });
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
        JMenuItem showExportItem = new JMenuItem("Export Log", KeyEvent.VK_E);
        showExportItem.addActionListener(e -> UIUtility.executeInBackground(this::showExportPanel, progressBar));
        mnTools.add(showExportItem);
        menuBar.add(mnTools);

        JMenu mnHelp = new JMenu("Help");
        mnHelp.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> InformationDialog.display(ToolkitMessages.ABOUT.getMessage(), "About onprom"));
        mnHelp.add(aboutItem);
        JMenuItem versionCheckItem = new JMenuItem("Check for Updates", KeyEvent.VK_C);
        versionCheckItem.addActionListener(e -> checkForUpdate());
        mnHelp.add(versionCheckItem);
        menuBar.add(mnHelp);

        return menuBar;
    }

    @Nonnull
    public Set<TreeNode<Object>> getResourceNodes() {
        return objects.getAllNodes();
    }

    public void displayUMLEditor() {
        displayEditor(new UMLEditor(this));
    }

    public void displayAnnotationEditor() {
        displayEditor(new AnnotationEditor(this));
    }

    public void displayPropertiesEditor(TreeNode<Object> node) {
        node.getUserObjectProvider().ifPresent(selectedObject -> {
            if (selectedObject instanceof Properties) {
                showInternalFrame(new InternalFrame("Database Properties", new DatasourcePropertiesPanel((Properties) selectedObject)));
            }
        });
    }

    private void displayEditor(UMLEditor editor) {
        editor.setProgressBar(progressBar);
        showInternalFrame(editor);
        loadShapes(editor);
    }

    private void loadShapes(UMLEditor umlEditor) {
        objects.getSelectedNode().ifPresent(node ->
                node.getUserObjectProvider().ifPresent(selectedObject -> {
                    if (selectedObject instanceof OWLOntology) {
                        umlEditor.loadOntology(node.getIdentifier(), (OWLOntology) selectedObject);
                    } else if (selectedObject instanceof Set) {
                        umlEditor.load(node.getIdentifier(), (Set<DiagramShape<? extends Diagram>>) selectedObject);
                    }
                })
        );
    }

    private void showExportPanel() {
        showInternalFrame(new InternalFrame("Log Extraction", new ExtractionPanel(this)));
    }

    private void showInternalFrame(JInternalFrame internalFrame) {
        internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                windows.removeNodeWithObject(e.getInternalFrame());
            }
        });
        desktop.add(internalFrame, BorderLayout.CENTER);
        try {
            internalFrame.setMaximum(true);
        } catch (Exception e) {
            logError(e);
        }
        windows.add(internalFrame.getTitle(), FileType.OTHER, internalFrame);
    }


    public void displayLogSummary(Object extractedLog) {
        if (extractedLog instanceof XLog) {
            displayLogSummary(objects.addObject("Extracted Log", FileType.XLOG, extractedLog));
        } else if (extractedLog instanceof OcelLog) {
            displayLogSummary(objects.addObject("Extracted Log", FileType.OCEL, extractedLog));
        }
    }

    public void displayLogSummary(TreeNode<Object> node) {
        node.getUserObjectProvider().ifPresent(selectedObject -> {
            try {
                JInternalFrame infoFrame = null;
                if (node.getType() == FileType.XLOG)
                    infoFrame = new XesLogSummaryPanel(XLogInfoFactory.createLogInfo((XLog) selectedObject));
                else if (node.getType() == FileType.OCEL) {
                    OcelLog log = (OcelLog) selectedObject;
                    //infoFrame = new OcelLogSummaryPanel(log.getInfo(log.getClassifiers().get(0)));
                }
                if (infoFrame != null) {
                    infoFrame.addInternalFrameListener(new InternalFrameAdapter() {
                        @Override
                        public void internalFrameClosed(InternalFrameEvent e) {
                            windows.removeNodeWithObject(e.getInternalFrame());
                        }
                    });
                    desktop.add(infoFrame, BorderLayout.CENTER);
                    infoFrame.setMaximum(true);
                    windows.add(infoFrame.getTitle(), FileType.OTHER, infoFrame);
                }
            } catch (Exception e) {
                logError(e);
            }
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
    public void store(String identifier, FileType type, Collection<DiagramShape<? extends Diagram>> shapes) {
        objects.addObject(identifier, type, shapes);
    }
}
