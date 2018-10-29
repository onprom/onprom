package it.unibz.inf.kaos.ui.component;

import it.unibz.inf.kaos.data.FileType;

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
