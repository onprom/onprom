/*
 * onprom-toolkit
 *
 * ObjectTree.java
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

package it.unibz.inf.onprom.ui.component;

import it.unibz.inf.onprom.OnpromToolkit;
import it.unibz.inf.onprom.data.FileType;
import it.unibz.inf.onprom.data.query.AnnotationQueries;
import it.unibz.inf.onprom.obdamapper.utility.OntopUtility;
import it.unibz.inf.onprom.ui.form.InformationDialog;
import it.unibz.inf.onprom.ui.utility.IOUtility;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.ocel.model.OcelLog;
import it.unibz.ocel.out.OcelXmlSerializer;
import org.apache.commons.io.FilenameUtils;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ObjectTree {
    private static final Logger logger = LoggerFactory.getLogger(ObjectTree.class.getSimpleName());
    private static final XesXmlParser XES_PARSER = new XesXmlParser();
    private static final XesXmlSerializer XES_SERIALIZER = new XesXmlSerializer();
    private static final OcelXmlSerializer OCEL_SERIALIZER = new OcelXmlSerializer();
    private final OnpromToolkit toolkit;
    private final CustomTree<Object> objects = new CustomTree<>(new TreeNode<>(-1, "Objects", FileType.OTHER, null));

    public ObjectTree(OnpromToolkit _toolkit) {
        toolkit = _toolkit;
        objects.setDoubleClickAction(() -> {
            objects.getSelectedNode().ifPresent(selectedNode -> {
                switch (selectedNode.getType()) {
                    case ONTOLOGY:
                    case UML:
                        toolkit.displayUMLEditor();
                        break;
                    case ANNOTATION:
                        toolkit.displayAnnotationEditor();
                        break;
                    case DS_PROPERTIES:
                        toolkit.displayPropertiesEditor(selectedNode);
                        break;
                    case OCEL:
                    case XLOG:
                        toolkit.displayLogSummary(selectedNode);
                        break;
                }
            });
            return null;
        });
        objects.setPopMenu(new JPopupMenu() {
            {
                JMenuItem menuItem = new JMenuItem("Delete", KeyEvent.VK_D);
                menuItem.addActionListener(e -> objects.removeSelected());
                add(menuItem);
                menuItem = new JMenuItem("Save as file", KeyEvent.VK_S);
                menuItem.addActionListener(e -> UIUtility.executeInBackground(ObjectTree.this::saveSelected,
                        toolkit.getProgressBar()));
                add(menuItem);
                menuItem = new JMenuItem("Open with Annotation Editor", KeyEvent.VK_A);
                menuItem.addActionListener(e -> toolkit.displayAnnotationEditor());
                add(menuItem);
            }
        });
        objects.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    objects.removeSelected();
                }
            }
        });

    }

    public Optional<TreeNode<Object>> getSelectedNode() {
        return objects.getSelectedNode();
    }

    public TreePath[] getSelectionPaths() {
        return objects.getSelectionPaths();
    }

    @Nonnull
    public Set<TreeNode<Object>> getAllNodes() {
        return objects.getAllNodes();
    }

    private void logError(Exception e) {
        logger.error(e.getMessage(), e);
        InformationDialog.display(e.getMessage());
    }

    public void openFiles(@Nonnull File[] files) {
        List<File> propertiesFiles = Arrays.stream(files).filter(file -> IOUtility.getFileType(file) == FileType.DS_PROPERTIES).collect(Collectors.toList());
        for (File properties : propertiesFiles) {
            try {
                Properties dsProperties = new Properties();
                dsProperties.load(new FileInputStream(properties));
                addObject(properties.getName(), FileType.DS_PROPERTIES, dsProperties);
            } catch (Exception e) {
                logError(e);
            }
            break;
        }
        List<File> otherFiles = Arrays.stream(files).filter(file -> IOUtility.getFileType(file) != FileType.DS_PROPERTIES).collect(Collectors.toList());
        for (File selectedFile : files) {
            switch (IOUtility.getFileType(selectedFile)) {
                case ONTOLOGY:
                    try {
                        addObject(selectedFile.getName(), FileType.ONTOLOGY,
                                OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(selectedFile));
                    } catch (Exception e) {
                        logError(e);
                    }
                    break;
                case MAPPING:
                    try {
                        //check if corresponding properties available
                        List<TreeNode<Object>> allProperties = objects.getAllNodes().stream().filter(objectTreeNode -> objectTreeNode.getType() == FileType.DS_PROPERTIES).collect(Collectors.toList());
                        if (allProperties.size() < 1) {
                            InformationDialog.display("Please load database connection properties file first! Please note that utility currently assumes that database properties and OBDA file has same name!");
                        } else {
                            Optional<TreeNode<Object>> property = allProperties.stream().filter(p -> IOUtility.getFileName(p.getTitle()).equals(IOUtility.getFileName(selectedFile))).findFirst();
                            if (property.isPresent()) {
                                addObject(selectedFile.getName(), FileType.MAPPING, OntopUtility.getOBDAModel(selectedFile, (Properties) property.get().getUserObject()));
                            } else {
                                InformationDialog.display("Please load database connection properties file first! Please note that utility currently assumes that database properties and OBDA file has same name!");
                            }
                        }
                    } catch (Exception e) {
                        logError(e);
                    }
                    break;
                case QUERIES:
                    IOUtility.readJSON(selectedFile, AnnotationQueries.class).ifPresent(
                            q -> addObject(selectedFile.getName(), FileType.QUERIES, q));
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

    public void saveAll() {
        UIUtility.selectFileToSave(FileType.ONTOLOGY).ifPresent(selectedFile -> {
            String fileName = FilenameUtils.removeExtension(selectedFile.getAbsolutePath());
            for (int i = 0; i < objects.getCount(); i++) {
                TreeNode node = objects.getNode(i);
                saveObject(fileName, node.getType(), node.getUserObject());
            }
        });
    }

    public void saveSelected() {
        if (objects.getSelectionPaths() != null) {
            for (TreePath path : objects.getSelectionPaths()) {
                try {
                    TreeNode node = (TreeNode) path.getLastPathComponent();
                    if (node.getType() != FileType.OTHER) {
                        UIUtility.selectFileToSave(node.getType()).ifPresent(selectedFile ->
                                saveObject(FilenameUtils.removeExtension(selectedFile.getAbsolutePath()),
                                        node.getType(), node.getUserObject())
                        );
                    }
                } catch (Exception e) {
                    logError(e);
                }
            }
        }
    }

    private void saveObject(String fileName, FileType type, Object object) {
        String filePath = fileName + "." + type.getDefaultExtension();
        switch (type) {
            case MAPPING:
                try {
                    OntopUtility.saveModel((SQLPPMapping) object, new File(filePath));
                } catch (Exception e) {
                    logError(e);
                }
                break;
            case DS_PROPERTIES:
                try {
                    ((Properties) object).store(new FileOutputStream(filePath), null);
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
            case OCEL:
                try {
                    OCEL_SERIALIZER.serialize((OcelLog) object, new FileOutputStream(filePath));
                } catch (Exception e) {
                    logError(e);
                }
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

    public TreeNode<Object> addObject(String title, FileType type, Object object) {
        if (title != null) {
            try {
                int i = Integer.parseInt(title);
                TreeNode<Object> node = objects.getNode(i);
                if (node != null && node.getType() == type) {
                    objects.removeNode(i);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
        objects.add(title, type, object);
        return objects.getNode(objects.getCount() - 1);
    }

    public void removeAll() {
        objects.removeAll();
    }

    public CustomTree<Object> getTreeComponent() {
        return objects;
    }

    public TreeNode<Object> getNode(final int i) {
        return objects.getNode(i);
    }
}
