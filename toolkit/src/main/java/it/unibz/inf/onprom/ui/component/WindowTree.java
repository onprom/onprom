/*
 * onprom-toolkit
 *
 * WindowTree.java
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by T. E. Kalayci on 15-Nov-2017.
 */
public class WindowTree {
    private final CustomTree<JInternalFrame> windows = new CustomTree<>(new TreeNode<>(-1, "Windows", FileType.OTHER, null));

    public WindowTree() {
        windows.setPopMenu(new JPopupMenu() {
            {
                JMenuItem menuItem = new JMenuItem("Close", KeyEvent.VK_C);
                menuItem.addActionListener(e -> closeSelectedWindow());
                add(menuItem);
            }
        });
        windows.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    closeSelectedWindow();
                }
            }
        });
        windows.addTreeSelectionListener(e -> windows.getSelectedObject().ifPresent(JInternalFrame::toFront));
    }

    private void closeSelectedWindow() {
        if (windows.isRootNotSelected()) {
            windows.getSelectedObject().ifPresent(JInternalFrame::dispose);
        }
    }

    public Component getTreeComponent() {
        return windows;
    }

    public void removeNodeWithObject(final JInternalFrame internalFrame) {
        windows.removeNodeWithObject(internalFrame);
    }

    public void add(final String title, final FileType other, final JInternalFrame editor) {
        windows.add(title, other, editor);
    }
}
