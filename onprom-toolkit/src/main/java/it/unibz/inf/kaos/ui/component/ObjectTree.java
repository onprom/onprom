package it.unibz.inf.kaos.ui.component;

import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.onprom.OnpromToolkit;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
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
import java.io.FileOutputStream;
import java.util.Optional;
import java.util.Set;

/**
 * Created by T. E. Kalayci on 15-Nov-2017.
 */
public class ObjectTree {
    private static final Logger logger = LoggerFactory.getLogger(ObjectTree.class.getSimpleName());
    private static final XesXmlParser XES_PARSER = new XesXmlParser();
    private static final XesXmlSerializer XES_SERIALIZER = new XesXmlSerializer();
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
                menuItem.addActionListener(e -> UIUtility.executeInBackground(ObjectTree.this::saveSelected, toolkit.getProgressBar()));
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

    public Void openFiles(File[] files) {
        if (files != null) {
            for (File selectedFile : files) {
                switch (IOUtility.getFileType(selectedFile)) {
                    case ONTOLOGY:
                        try {
                            addObject(selectedFile.getName(), FileType.ONTOLOGY, OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(selectedFile));
                        } catch (Exception e) {
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

    public Void saveAll() {
        UIUtility.selectFileToSave(FileType.ONTOLOGY).ifPresent(selectedFile -> {
            String fileName = FilenameUtils.removeExtension(selectedFile.getAbsolutePath());
            for (int i = 0; i < objects.getCount(); i++) {
                TreeNode node = objects.getNode(i);
                saveObject(fileName, node.getType(), node.getUserObject());
            }
        });
        return null;
    }

    public Void saveSelected() {
        if (objects.getSelectionPaths() != null) {
            for (TreePath path : objects.getSelectionPaths()) {
                try {
                    TreeNode node = (TreeNode) path.getLastPathComponent();
                    if (node.getType() != FileType.OTHER) {
                        UIUtility.selectFileToSave(node.getType()).ifPresent(selectedFile -> saveObject(FilenameUtils.removeExtension(selectedFile.getAbsolutePath()), node.getType(), node.getUserObject()));
                    }
                } catch (Exception e) {
                    logError(e);
                }
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
                it.unibz.inf.kaos.ui.utility.IOUtility.exportJSON(new File(filePath), object);
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
                Integer i = Integer.parseInt(title);
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
