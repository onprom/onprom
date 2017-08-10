/*
 * onprom-umleditor
 *
 * InformationDialog.java
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

package it.unibz.inf.kaos.ui.form;

import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.UMLEditorButtons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

/**
 * Displays information to the user
 * <p>
 * @author T. E. Kalayci
 * Date: 27-Feb-17
 */
public class InformationDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(InformationDialog.class.getName());

    private final JEditorPane editorPane;

    private InformationDialog() {
        setModal(true);
        setTitle("Information");
        setLayout(null);

        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setBackground(UIManager.getColor("Panel.background"));

        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        });
        JScrollPane pane = new JScrollPane(editorPane);
        pane.setBorder(null);
      pane.setBounds(5, 5, 450, 350);
        add(pane);
        JButton ok = UIUtility.createButton(UMLEditorButtons.OK, e -> setVisible(false));
      ok.setBounds(175, 355, 100, 25);
        add(ok);
      setSize(450, 420);
        setLocationRelativeTo(null);
    }

    public static void display(String message) {
        InformationDialog dialog = new InformationDialog();
        dialog.editorPane.setText(message);
        dialog.editorPane.setCaretPosition(0);
        dialog.setVisible(true);
    }
}
