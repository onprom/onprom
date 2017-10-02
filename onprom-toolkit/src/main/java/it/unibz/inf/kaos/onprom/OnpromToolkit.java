/*
 * onprom-toolkit
 *
 * OnpromToolkit.java
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

package it.unibz.inf.kaos.onprom;

import it.unibz.inf.kaos.annotation.AnnotationEditor;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.interfaces.AnnotationEditorListener;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.logextractor.XESLogExtractorWithEBDAMapping;
import it.unibz.inf.kaos.ui.component.CustomTree;
import it.unibz.inf.kaos.ui.component.ExtractionFrame;
import it.unibz.inf.kaos.ui.component.LogSummaryPanel;
import it.unibz.inf.kaos.ui.component.TreeNode;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.UMLEditorMessages;
import it.unibz.inf.kaos.uml.UMLEditor;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import org.apache.commons.io.FilenameUtils;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.TreePath;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;

/**
 * @author T. E. Kalayci on 26-Apr-2017
 */
public class OnpromToolkit extends JFrame implements AnnotationEditorListener {
    private static final Logger logger = LoggerFactory.getLogger(OnpromToolkit.class.getSimpleName());
    private static final XesXmlSerializer XES_SERIALIZER = new XesXmlSerializer();
    private static final XesXmlParser XES_PARSER = new XesXmlParser();
    private final JProgressBar progressBar = new JProgressBar();
    private final FileType[] supportedFormats = new FileType[]{FileType.ONTOLOGY, FileType.UML, FileType.ANNOTATION, FileType.MAPPING, FileType.QUERIES, FileType.XLOG};
    private final JDesktopPane desktop = new JDesktopPane();
    private final CustomTree<Object> objects = new CustomTree<>(new TreeNode<>(-1, "Objects", FileType.OTHER, null));
    private final CustomTree<JInternalFrame> windows = new CustomTree<>(new TreeNode<>(-1, "Windows", FileType.OTHER, null));

    private OnpromToolkit() {
        this.setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        dtde.acceptDrop(dtde.getDropAction());
                        Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (transferData != null && transferData instanceof java.util.List) {
                            java.util.List<File> files = (java.util.List<File>) transferData;
                            if (files.size() > 0) {
                                UIUtility.executeInBackground(() -> openFiles(files.toArray(new File[]{})), progressBar);
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
        objects.setDoubleClickAction(() -> {
            TreeNode selectedNode = objects.getSelectedNode();
            if (selectedNode != null) {
                switch (selectedNode.getType()) {
                    case ONTOLOGY:
                    case UML:
                        displayUMLEditor();
                        break;
                    case ANNOTATION:
                        displayAnnotationEditor();
                        break;
                    case XLOG:
                        displayLogSummary(selectedNode);
                        break;
                }
            }
            return null;
        });
        objects.setPopMenu(new JPopupMenu() {
            {
                JMenuItem menuItem = new JMenuItem("Delete", KeyEvent.VK_D);
                menuItem.addActionListener(e -> objects.removeSelected());
                add(menuItem);
                menuItem = new JMenuItem("Save as file", KeyEvent.VK_S);
                menuItem.addActionListener(e -> UIUtility.executeInBackground(OnpromToolkit.this::saveSelected, progressBar));
                add(menuItem);
            }
        });
        windows.setPopMenu(new JPopupMenu() {
            {
                JMenuItem menuItem = new JMenuItem("Close", KeyEvent.VK_C);
                menuItem.addActionListener(e -> closeSelectedWindow());
                add(menuItem);
            }
        });
        windows.addTreeSelectionListener(e -> {
            if (windows.getSelectedNode() != null && windows.getSelectedNode().getUserObject() != null) {
                windows.getSelectedNode().getUserObject().toFront();
            }
        });

        JPanel treePanel = new JPanel(new GridLayout(0, 1));
        treePanel.setPreferredSize(new Dimension(300, 0));
        treePanel.add(new JScrollPane(objects));
        treePanel.add(new JScrollPane(windows));

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
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screenSize.width, screenSize.height);
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

    private void closeSelectedWindow() {
        if (windows.isRootNotSelected() && windows.getSelectedObject() != null) {
            windows.getSelectedObject().dispose();
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);

        JMenuItem openItem = new JMenuItem("Load...", KeyEvent.VK_O);
        openItem.addActionListener(e -> UIUtility.executeInBackground(this::openFile, progressBar));
        mnFile.add(openItem);
        JMenuItem saveItem = new JMenuItem("Save Selected...", KeyEvent.VK_S);
        saveItem.addActionListener(e -> UIUtility.executeInBackground(this::saveSelected, progressBar));
        mnFile.add(saveItem);
        JMenuItem saveAllItem = new JMenuItem("Save All...", KeyEvent.VK_A);
        saveAllItem.addActionListener(e -> UIUtility.executeInBackground(this::saveAll, progressBar));
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
        JMenuItem exportItem = new JMenuItem("Export Log", KeyEvent.VK_E);
        exportItem.addActionListener(e -> UIUtility.executeInBackground(this::exportLog, progressBar));
        mnTools.add(exportItem);
        JMenuItem showExportItem = new JMenuItem("Export Diagram", KeyEvent.VK_D);
        showExportItem.addActionListener(e -> UIUtility.executeInBackground(this::showExportDiagram, progressBar));
        //mnTools.add(showExportItem );
        menuBar.add(mnTools);

        JMenu mnHelp = new JMenu("Help");
        mnHelp.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> InformationDialog.display(UMLEditorMessages.ABOUT.getMessage()));
        mnHelp.add(aboutItem);
        menuBar.add(mnHelp);

        return menuBar;
    }

    private Void saveSelected() {
        if (objects.getSelectionPaths() != null) {
            for (TreePath path : objects.getSelectionPaths()) {
                try {
                    TreeNode node = (TreeNode) path.getLastPathComponent();
                    if (!node.getType().equals(FileType.OTHER)) {
                        File selectedFile = IOUtility.selectFileToSave(node.getType());
                        if (selectedFile != null) {
                            saveObject(FilenameUtils.removeExtension(selectedFile.getAbsolutePath()), node.getType(), node.getUserObject());
                        }
                    }
                } catch (Exception e) {
                    logError(e);
                }
            }
        }
        return null;
    }

    private Void saveAll() {
        File selectedFile = IOUtility.selectFileToSave(FileType.ONTOLOGY);
        if (selectedFile != null) {
            String fileName = FilenameUtils.removeExtension(selectedFile.getAbsolutePath());
            for (int i = 0; i < objects.getCount(); i++) {
                TreeNode node = objects.getNode(i);
                saveObject(fileName, node.getType(), node.getUserObject());
            }
        }
        return null;
    }

    private void saveObject(String fileName, FileType type, Object object) {
        String filePath = fileName + "." + type.getDefaultExtension();
        switch (type) {
            case MAPPING:
                try {
                    new ModelIOManager((OBDAModel) object).save(new File(filePath));
                } catch (Exception e) {
                    logError(e);
                }
                break;
            case ONTOLOGY:
                try {
                    OWLManager.createOWLOntologyManager().saveOntology((OWLOntology) object, new RDFXMLDocumentFormat(), new FileOutputStream(filePath));
                } catch (Exception e) {
                    logError(e);
                }
                break;
            case ANNOTATION:
            case QUERIES:
            case UML:
                IOUtility.exportJSON(new File(filePath), object);
                break;
            case XLOG:
                try {
                    XES_SERIALIZER.serialize((XLog) object, new FileOutputStream(filePath));
                } catch (Exception e) {
                    logError(e);
                }

                break;
        }
    }

    private Void openFile() {
        return openFiles(IOUtility.selectFiles(supportedFormats));
    }

    public TreeNode getResourceNode(int i) {
        return objects.getNode(i - 1);
    }

    private Void openFiles(File[] files) {
        if (files != null) {
            for (File selectedFile : files) {
                switch (IOUtility.getFileType(selectedFile)) {
                    case ONTOLOGY:
                        try {
                            addObject(selectedFile.getName(), FileType.ONTOLOGY, OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(selectedFile));
                        } catch (Exception e) {
                            e.printStackTrace();
                            logError(e);
                        }
                        break;
                    case MAPPING:
                        try {
                            OBDAModel obdaModel = OBDADataFactoryImpl.getInstance().getOBDAModel();
                            new ModelIOManager(obdaModel).load(selectedFile);
                            addObject(selectedFile.getName(), FileType.MAPPING, obdaModel);
                        } catch (Exception e) {
                            logError(e);
                        }
                        break;
                    case QUERIES:
                        addObject(selectedFile.getName(), FileType.QUERIES, IOUtility.readJSON(selectedFile, AnnotationQueries.class));
                        break;
                    case ANNOTATION:
                        addObject(selectedFile.getName(), FileType.ANNOTATION, IOUtility.importJSON(selectedFile));
                        break;
                    case UML:
                        addObject(selectedFile.getName(), FileType.UML, IOUtility.importJSON(selectedFile));
                        break;
                    case JSON:
                        addObject(selectedFile.getName(), FileType.JSON, IOUtility.importJSON(selectedFile));
                        break;
                    case XLOG:
                        try {
                            for (XLog xlog : XES_PARSER.parse(selectedFile)) {
                                addObject(selectedFile.getName(), FileType.XLOG, xlog);
                            }
                        } catch (Exception e) {
                            logError(e);
                        }
                        break;
                }
            }
        }
        return null;
    }

    private Void reset() {
        objects.removeAll();
        for (JInternalFrame internalFrame : desktop.getAllFrames()) {
            internalFrame.dispose();
        }
        return null;
    }

    private Void displayUMLEditor() {
        return displayEditor(new UMLEditor(null, this));
    }

    private Void displayAnnotationEditor() {
        return displayEditor(new AnnotationEditor(null, this));
    }

    private Void displayEditor(UMLEditor editor) {
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
        return null;
    }

    private void loadShapes(UMLEditor umlEditor) {
        TreeNode node = objects.getSelectedNode();
        if (node != null) {
            Object selectedObject = node.getUserObject();
            if (selectedObject != null) {
                if (selectedObject instanceof OWLOntology) {
                    umlEditor.loadOntology(node.getIdentifier(), (OWLOntology) selectedObject);
                } else if (selectedObject instanceof Set) {
                    umlEditor.load(node.getIdentifier(), (Set<DiagramShape>) selectedObject);
                }
            }
        }
    }

    private Void showExportDiagram() {
        ExtractionFrame editor = new ExtractionFrame(this);
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
        return null;
    }

    private Void exportLog() {
        TreePath[] paths = objects.getSelectionPaths();
        if (paths != null) {
            OWLOntology ontology = null;
            OBDAModel model = null;
            AnnotationQueries queries = null;
            for (TreePath path : paths) {
                Object object = ((TreeNode) path.getLastPathComponent()).getUserObject();
                if (object instanceof OWLOntology)
                    ontology = (OWLOntology) object;
                if (object instanceof OBDAModel)
                    model = (OBDAModel) object;
                if (object instanceof AnnotationQueries)
                    queries = (AnnotationQueries) object;
            }
            if (ontology != null && model != null && queries != null) {
                try {
                    long start = System.currentTimeMillis();
                    XLog xlog = new XESLogExtractorWithEBDAMapping().extractXESLog(ontology, model, queries);
                    logger.error("It took " + (System.currentTimeMillis() - start) + " ms to export log");
                    displayLogSummary(addObject("Extracted Log", FileType.XLOG, xlog));
                } catch (Exception e) {
                    logError(e);
                }
            } else {
                InformationDialog.display("Please select Ontology, Mapping and Queries to start log exporting!");
            }
        }
        return null;
    }

    private TreeNode<Object> addObject(String title, FileType type, Object object) {
        if (title != null) {
            try {
                Integer i = Integer.parseInt(title);
                TreeNode<Object> node = objects.getNode(i);
                if (node != null && node.getType().equals(type)) {
                    objects.removeNode(i);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
        objects.add(title, type, object);
        return objects.getNode(objects.getCount() - 1);
    }

    private void displayLogSummary(TreeNode node) {
        Object selectedObject = node.getUserObject();
        if (selectedObject != null) {
            XLog log = (XLog) selectedObject;
            JInternalFrame infoFrame = new LogSummaryPanel(XLogInfoFactory.createLogInfo(log));
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
        }
    }

    private void logError(Exception e) {
        logger.error(e.getMessage(), e);
        InformationDialog.display(e.getMessage());
    }

    @Override
    public void store(String identifier, AnnotationQueries annotationQueries) {
        addObject(identifier, FileType.QUERIES, annotationQueries);
    }

    @Override
    public void store(String identifier, OWLOntology ontology) {
        addObject(identifier, FileType.ONTOLOGY, ontology);
    }

    @Override
    public void store(String identifier, FileType type, Set<DiagramShape> shapes) {
        addObject(identifier, type, shapes);
    }
}
