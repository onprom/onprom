/*
 * onprom-toolkit
 *
 * CustomTree.java
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

import it.unibz.inf.onprom.data.FileType;
import it.unibz.inf.onprom.ui.utility.IOUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author T. E. Kalayci on 28-Apr-2017
 */
public class CustomTree<T> extends JTree {
    public static final DataFlavor INT_ARRAY_FLAVOR = new DataFlavor(int[].class, "Int Array");
    private static final Logger logger = LoggerFactory.getLogger(CustomTree.class.getSimpleName());
    private final TreeNode<T> root;
    private JPopupMenu menu;
    private Callable action;

    CustomTree(TreeNode<T> _root) {
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
                    @Nonnull
                    public int[] getTransferData(DataFlavor flavor) {
                        int[] selectionRows = getSelectionRows();
                        return (selectionRows != null) ? selectionRows : new int[0];
                    }
                };
            }
        });
        root = _root;
        setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                IOUtility.getImageURL(((TreeNode) value).getIcon()).ifPresent(iconUrl -> setIcon(new ImageIcon(iconUrl)));
                return component;
            }
        });
    }

    void setDoubleClickAction(Callable _action) {
        action = _action;
    }

    void setPopMenu(JPopupMenu _menu) {
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
                            if (action != null) {
                                action.call();
                            }
                        } catch (Exception ex) {
                            logger.warn(ex.getMessage(), ex);
                        }
                    }
                }
            });
        }
    }

    int getCount() {
        return root.getChildCount();
    }

    boolean isRootNotSelected() {
        return !getLastSelectedPathComponent().equals(root);
    }

    @Override
    public TreeNode<T> getLastSelectedPathComponent() {
        return (TreeNode<T>) super.getLastSelectedPathComponent();
    }

    Optional<TreeNode<T>> getSelectedNode() {
        return Optional.ofNullable(getLastSelectedPathComponent());
    }

    Optional<T> getSelectedObject() {
        return getSelectedNode().flatMap(TreeNode::getUserObjectProvider);
    }

    @Nonnull
    Set<TreeNode<T>> getAllNodes() {
        return root.getChildren();
    }

    private void reload() {
        ((DefaultTreeModel) super.getModel()).reload();
    }

    public void removeAll() {
        root.removeAllChildren();
        reload();
    }

    TreeNode<T> getNode(int i) {
        return root.getChildAt(i);
    }

    void add(String title, FileType type, T object) {
        root.add(new TreeNode<>(root.getChildCount(), title, type, object));
        reload();
    }

    void removeNodeWithObject(T toRemove) {
        if (root.removeChild(toRemove)) {
            reload();
        }
    }

    void removeSelected() {
        if (isRootNotSelected()) {
            root.remove(getLastSelectedPathComponent());
            reload();
        }
    }

    void removeNode(int i) {
        root.remove(i);
        reload();
    }
}