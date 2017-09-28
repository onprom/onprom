/*
 * onprom-toolkit
 *
 * CustomTree.java
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

package it.unibz.inf.kaos.ui.component;

import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.ui.utility.IOUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * @author T. E. Kalayci on 28-Apr-2017
 */
public class CustomTree<T> extends JTree {
    public final static DataFlavor INT_ARRAY_FLAVOR = new DataFlavor(int[].class, "Int Array");
    private static final Logger logger = LoggerFactory.getLogger(CustomTree.class.getSimpleName());
    private final TreeNode<T> root;
    private JPopupMenu menu;
    private Callable action;

    public CustomTree(TreeNode<T> _root) {
        super(new DefaultTreeModel(_root));
        this.setDragEnabled(true);
        this.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport info) {
                return false;
            }

            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }

            protected Transferable createTransferable(JComponent c) {
                return new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{INT_ARRAY_FLAVOR};
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor.equals(INT_ARRAY_FLAVOR);
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        return getSelectionRows();
                    }
                };
            }
        });
        root = _root;
        setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                          boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                TreeNode node = (TreeNode) value;
                if (node.getIcon() != null) {
                    URL imageUrl = IOUtility.getImageURL(node.getIcon());
                    if (imageUrl != null) {
                        setIcon(new ImageIcon(imageUrl));
                    }
                }
                return this;
            }
        });
    }

    public void setDoubleClickAction(Callable _action) {
        action = _action;
    }

    public void setPopMenu(JPopupMenu _menu) {
        menu = _menu;
        if (menu != null) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        setSelectionRow(getClosestRowForLocation(e.getX(), e.getY()));
                        if (getSelectionCount() > 0 && isRootNotSelected()) {
                            menu.show(CustomTree.this, e.getX(), e.getY());
                        }
                    } else if (e.getClickCount() == 2) {
                        try {
                            if (action != null)
                                action.call();
                        } catch (Exception ex) {
                            logger.warn(ex.getMessage(), ex);
                        }
                    }
                }
            });
        }
    }

    public int getCount() {
        return root.getChildCount();
    }

    public boolean isRootNotSelected() {
        return !getLastSelectedPathComponent().equals(root);
    }

    public TreeNode<T> getSelectedNode() {
        return (TreeNode<T>) getLastSelectedPathComponent();
    }

    public T getSelectedObject() {
        TreeNode<T> node = getSelectedNode();
        if (node != null)
            return node.getUserObject();
        return null;
    }

    private void reload() {
        ((DefaultTreeModel) super.getModel()).reload();
    }

    public void removeAll() {
        root.removeAllChildren();
        reload();
    }

    public TreeNode<T> getNode(int i) {
        return root.getChildAt(i);
    }

    public void add(String title, FileType type, T object) {
        root.add(new TreeNode<>(root.getChildCount(), title, type, object));
        reload();
    }

    public void removeNodeWithObject(Object toRemove) {
        for (int i = 0; i < getCount(); i++) {
            TreeNode childAt = getNode(i);
            if (childAt.getUserObject().equals(toRemove)) {
                removeNode(i);
                return;
            }
        }
    }

    public void removeSelected() {
        if (isRootNotSelected()) {
            root.remove((TreeNode<T>) getLastSelectedPathComponent());
            reload();
        }
    }

    public void removeNode(int i) {
        root.remove(i);
        reload();
    }
}