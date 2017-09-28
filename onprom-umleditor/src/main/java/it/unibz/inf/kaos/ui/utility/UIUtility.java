/*
 * onprom-umleditor
 *
 * UIUtility.java
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

package it.unibz.inf.kaos.ui.utility;

import it.unibz.inf.kaos.ui.action.ToolbarAction;
import it.unibz.inf.kaos.ui.component.WidePopupComboBox;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.interfaces.Buttons;
import it.unibz.inf.kaos.ui.interfaces.Labels;
import it.unibz.inf.kaos.ui.interfaces.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * This class contains static variables and methods which are used by UI classes
 *
 * @author T. E. Kalayci
 */
public class UIUtility {
    private static final Logger logger = LoggerFactory.getLogger(UIUtility.class.getName());
    //set for storing class and relation names
    private static final Set<String> names = new HashSet<>();
    private static final String HTML_STRING = "<html>%s</html>";
    private static final Dimension SMALL_BUTTON_DIMENSION = new Dimension(45, 25);
    private static final Set<SwingWorker> BACKGROUND_WORKERS = new LinkedHashSet<>();
    private static BufferedImage LOGO = null;

    public static boolean isNameExist(String name) {
        return names.contains(name.trim());
    }

    public static void addName(String name) {
        names.add(name);
    }

    public static void removeName(String name) {
        names.remove(name);
    }

    public static void clearNames() {
        names.clear();
    }

    public static GridBagConstraints getGridBagConstraints() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        return gridBagConstraints;
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }

    public static void executeInBackground(Callable method, JProgressBar progressBar) {
        SwingWorker worker = new SwingWorker() {
            public Void doInBackground() {
                if (progressBar != null) {
                    progressBar.setIndeterminate(true);
                    Component glassPane = ((RootPaneContainer) progressBar.getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    glassPane.setVisible(true);
                }
                try {
                    method.call();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    UIUtility.error(e.getMessage());
                }
                if (progressBar != null) {
                    progressBar.setIndeterminate(false);
                    progressBar.setString(null);
                    Component glassPane = ((RootPaneContainer) progressBar.getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getDefaultCursor());
                    glassPane.setVisible(false);
                }
                return null;
            }

            @Override
            protected void done() {
                super.done();
                BACKGROUND_WORKERS.remove(this);
            }
        };
        worker.execute();
        BACKGROUND_WORKERS.add(worker);
    }

    public static void stopWorkers() {
        BACKGROUND_WORKERS.forEach(sw -> sw.cancel(true));
    }

    public static String input(String message, String defaultValue) {
        return JOptionPane.showInputDialog(null, message, defaultValue);
    }

    public static boolean deleteConfirm() {
        return confirm(UMLEditorMessages.DELETE_CONFIRM);
    }

    public static boolean confirm(Messages message) {
        int confirmation = JOptionPane.showConfirmDialog(null, message.getMessage(), message.getTitle(), JOptionPane.YES_NO_OPTION);
        return confirmation == JOptionPane.YES_OPTION;
    }

    public static void error(String message) {
        error(message, "Error");
    }

    public static void error(Messages message) {
        error(message.getMessage(), message.getTitle());
    }

    private static void error(String message, String title) {
        JOptionPane.showMessageDialog(null, String.format(HTML_STRING, message), title, JOptionPane.ERROR_MESSAGE);
    }

    public static BufferedImage getLogo() {
        if (LOGO == null) {
            try {
                LOGO = ImageIO.read(IOUtility.getImageURL("onprom"));
            } catch (IOException e) {
                logger.error("Couldn't load logo", e);
            }
        }
        return LOGO;
    }

    public static Cursor getCursorImage(String imageName) {
        return Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage(IOUtility.getImageURL(imageName)),
                new Point(0, 0), "img");
    }

    public static <E> WidePopupComboBox createWideComboBox(Dimension dimension, ItemListener listener, boolean editable, boolean withEmpty) {
        WidePopupComboBox<E> cmb = new WidePopupComboBox<>();
        setupWidePopupComboBox(cmb, dimension, listener, editable, withEmpty);
        return cmb;
    }

    public static <E> WidePopupComboBox<E> createWideComboBox(Set<E> values, Dimension dimension, ItemListener listener, boolean editable, boolean withEmpty) {
        WidePopupComboBox<E> cmb = new WidePopupComboBox<>(values);
        setupWidePopupComboBox(cmb, dimension, listener, editable, withEmpty);
        return cmb;
    }

    public static <E> WidePopupComboBox<E> createWideComboBox(E[] values, Dimension dimension, ItemListener listener, boolean editable, boolean withEmpty) {
        WidePopupComboBox<E> cmb = new WidePopupComboBox<>(values);
        setupWidePopupComboBox(cmb, dimension, listener, editable, withEmpty);
        return cmb;
    }

    private static <E> void setupWidePopupComboBox(WidePopupComboBox<E> cmb, Dimension dimension, ItemListener listener, boolean editable, boolean withEmpty) {
        cmb.setPreferredSize(dimension);
        cmb.setEditable(editable);
        cmb.setEnabled(editable);
        if (listener != null) {
            cmb.addItemListener(listener);
        }
        if (withEmpty) {
            cmb.insertItemAt(null, 0);
        }
        if (cmb.getItemCount() > 0) cmb.setSelectedIndex(0);
    }

    private static JButton createButton(String text, char mnemonic, String tooltip, ActionListener actionListener, Dimension preferredSize) {
        JButton button = new JButton(text);
        button.setToolTipText(String.format(HTML_STRING, tooltip));
        button.setMnemonic(mnemonic);
        button.addActionListener(actionListener);
        button.setPreferredSize(preferredSize);
        return button;
    }

    public static JButton createSmallButton(Buttons button, ActionListener actionListener) {
        return createButton(button.getText(), button.getMnemonic(), button.getTooltip(), actionListener, SMALL_BUTTON_DIMENSION);
    }

    public static JButton createButton(Buttons button, ActionListener actionListener) {
        return createButton(button.getText(), button.getMnemonic(), button.getTooltip(), actionListener, null);
    }

    public static JButton createButton(Buttons button, ActionListener actionListener, Dimension preferredSize) {
        return createButton(button.getText(), button.getMnemonic(), button.getTooltip(), actionListener, preferredSize);
    }

    public static JLabel createLabel(Labels label, Dimension preferredSize) {
        JLabel lbl = new JLabel(String.format(HTML_STRING, label.getLabel()));
        lbl.setToolTipText(String.format(HTML_STRING, label.getTooltip()));
        lbl.setPreferredSize(preferredSize);
        if (label.isClickable()) {
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    InformationDialog.display(label.getTooltip());
                }
            });
        }
        return lbl;
    }

    public static JLabel createLabel(String text, Dimension preferredSize) {
        JLabel lbl = new JLabel(String.format(HTML_STRING, text));
        lbl.setPreferredSize(preferredSize);
        return lbl;
    }

    public static JTextField createTextField(Dimension preferredSize) {
        return createTextField(null, preferredSize, null, true);
    }

    public static JTextField createTextField(String tooltip, Dimension preferredSize) {
        return createTextField(tooltip, preferredSize, null, true);
    }

    public static JTextField createTextField(String tooltip, Dimension preferredSize, ActionListener listener) {
        return createTextField(tooltip, preferredSize, listener, true);
    }

    public static JTextField createTextField(String tooltip, Dimension preferredSize, ActionListener actionListener, boolean editable) {
        JTextField txt = new JTextField();
        if (tooltip != null && !tooltip.isEmpty()) {
            txt.setToolTipText(String.format(HTML_STRING, tooltip));
        }
        if (actionListener != null)
            txt.addActionListener(actionListener);
        txt.setPreferredSize(preferredSize);
        txt.setEditable(editable);
        return txt;
    }

    public static boolean isCTRLPressed(ActionEvent e) {
        return (e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK;
    }

    public static JButton createToolbarButton(ToolbarAction action) {
        JButton button = new JButton();
        button.setToolTipText(String.format(HTML_STRING, action.getTooltip()));
        button.addActionListener(e -> action.execute());
        button.setMnemonic(action.getMnemonic());
      URL imageURL = IOUtility.getImageURL(action.getActionName().toLowerCase());
        if (imageURL != null) {
            ImageIcon imageIcon = new ImageIcon(imageURL, action.getTitle());
            button.setIcon(imageIcon);
        } else {
            button.setText(String.format(HTML_STRING, action.getTitle()));
        }
        return button;
    }

    public static JMenuItem createMenuItem(ToolbarAction action) {
        JMenuItem menuItem = new JMenuItem(action.getTitle(), action.getMnemonic());
        menuItem.getAccessibleContext().setAccessibleDescription(action.getTooltip());
        menuItem.addActionListener(e -> action.execute());
        URL imageURL = IOUtility.getImageURL(action.getActionName());
        if (imageURL != null)
            menuItem.setIcon(new ImageIcon(imageURL));
        return menuItem;
    }

    public static <T> void loadItems(JComboBox<T> comboBox, Set<T> items) {
        comboBox.removeAllItems();
        for (T item : items) {
            comboBox.addItem(item);
        }
    }
}