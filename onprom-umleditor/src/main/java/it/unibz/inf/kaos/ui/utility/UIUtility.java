/*
 * onprom-umleditor
 *
 * UIUtility.java
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

package it.unibz.inf.kaos.ui.utility;

import com.google.common.collect.Sets;
import it.unibz.inf.kaos.data.FileType;
import it.unibz.inf.kaos.ui.action.ToolbarAction;
import it.unibz.inf.kaos.ui.component.WidePopupComboBox;
import it.unibz.inf.kaos.ui.filter.FileTypeFilter;
import it.unibz.inf.kaos.ui.form.InformationDialog;
import it.unibz.inf.kaos.ui.interfaces.Buttons;
import it.unibz.inf.kaos.ui.interfaces.Labels;
import it.unibz.inf.kaos.ui.interfaces.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * This class contains static variables and methods which are used by UI classes
 *
 * @author T. E. Kalayci
 */
public class UIUtility {
    private static final Logger logger = LoggerFactory.getLogger(UIUtility.class.getName());
    private static final JFileChooser FILE_CHOOSER = new JFileChooser();
    //set for storing class and relation names
    private static final HashSet<String> names = Sets.newHashSet();
    private static final String HTML_STRING = "<html>%s</html>";
    private static final Dimension SMALL_BUTTON_DIMENSION = new Dimension(45, 25);
    private static final HashSet<SwingWorker> BACKGROUND_WORKERS = Sets.newHashSet();

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
        gridBagConstraints.insets = new Insets(1, 1, 1, 1);
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

    public static String strToHexColor(String str) {
        return String.format("#%06X", (0xFFFFFF & str.hashCode()));
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

    public static void warning(String message) {
        warning(message, "Warning");
    }

    public static void warning(String message, String title) {
        JOptionPane.showMessageDialog(null, String.format(HTML_STRING, message), title, JOptionPane.WARNING_MESSAGE);
    }

    public static <E> WidePopupComboBox<E> createWideComboBox(Dimension dimension, ItemListener listener, boolean editable, boolean withEmpty) {
        return createWideComboBox(Collections.emptyList(), dimension, listener, editable, withEmpty);
    }

    public static <E> WidePopupComboBox<E> createWideComboBox(Iterable<E> values, Dimension dimension, ItemListener listener, boolean editable, boolean withEmpty) {
        WidePopupComboBox<E> cmb = new WidePopupComboBox<>(values);
        setupWidePopupComboBox(cmb, dimension, listener, editable, withEmpty);
        return cmb;
    }

    public static <E> WidePopupComboBox<E> createWideComboBox(Stream<E> values, Dimension dimension, ItemListener listener, boolean editable, boolean withEmpty) {
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

    public static JCheckBox createCheckBox(String tooltip) {
        return createCheckBox("", tooltip);
    }

    public static JCheckBox createCheckBox(String text, String tooltip) {
        return createCheckBox(text, tooltip, false, null);
    }

    public static JCheckBox createCheckBox(String text, String tooltip, boolean selected) {
        return createCheckBox(text, tooltip, selected, null);
    }

    public static JCheckBox createCheckBox(String text, String tooltip, boolean selected, ItemListener listener) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setToolTipText(String.format(HTML_STRING, tooltip));
        checkBox.setSelected(selected);
        if (listener != null) {
            checkBox.addItemListener(listener);
        }
        return checkBox;
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

    public static JLabel createLabel(String text, Dimension preferredSize, final boolean isHTML) {
        if (isHTML) {
            text = String.format(HTML_STRING, text);
        }
        JLabel lbl = new JLabel(text);
        lbl.setToolTipText(text);
        lbl.setPreferredSize(preferredSize);
        return lbl;
    }

    public static JLabel createLabel(String text, Dimension preferredSize, MouseListener mouseListener) {
        JLabel lbl = createLabel(text, preferredSize);
        lbl.addMouseListener(mouseListener);
        return lbl;
    }

    public static JLabel createLabel(String text, Dimension preferredSize) {
        return createLabel(text, preferredSize, true);
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

    public static JTextField createTextField(String tooltip, Dimension preferredSize, boolean editable) {
        return createTextField(tooltip, preferredSize, e -> {
        }, editable);
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
            button.setText(String.format(HTML_STRING, Character.toUpperCase(action.getMnemonic())));
            button.setFont(new Font("Monospaced", Font.PLAIN, 30));
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

    public static <T> void loadItems(JComboBox<T> comboBox, Iterable<T> items) {
        comboBox.removeAllItems();
        for (T item : items) {
            comboBox.addItem(item);
        }
    }

    public static boolean isDark(Color color) {
        return color != null && ((30 * color.getRed() + 59 * color.getGreen() + 11 * color.getBlue()) / 100) < 128;
    }

    public static File[] selectFiles(FileType... allowedFileType) {
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FILE_CHOOSER.setMultiSelectionEnabled(true);
        FILE_CHOOSER.setFileFilter(FileTypeFilter.get(allowedFileType));
        int returnVal = FILE_CHOOSER.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return FILE_CHOOSER.getSelectedFiles();
        }
        return null;
    }

    @Nonnull
    public static Optional<File> selectFileToOpen(FileType... allowedFileType) {
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FILE_CHOOSER.setFileFilter(FileTypeFilter.get(allowedFileType));
        FILE_CHOOSER.setMultiSelectionEnabled(false);
        int returnVal = FILE_CHOOSER.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return Optional.of(FILE_CHOOSER.getSelectedFile());
        }
        return Optional.empty();
    }

    @Nonnull
    public static Optional<File> selectFileToSave(FileType fileType) {
        FILE_CHOOSER.setFileFilter(FileTypeFilter.get(fileType));
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FILE_CHOOSER.setSelectedFile(new File(""));
        int returnVal = FILE_CHOOSER.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = FILE_CHOOSER.getSelectedFile();
            if (IOUtility.getFileExtension(selectedFile).isEmpty()) {
                //set default extension if doesn't exist
                selectedFile = new File(selectedFile.getAbsolutePath() + "." + fileType.getDefaultExtension());
            }
            return Optional.of(selectedFile);
        }
        return Optional.empty();
    }
}