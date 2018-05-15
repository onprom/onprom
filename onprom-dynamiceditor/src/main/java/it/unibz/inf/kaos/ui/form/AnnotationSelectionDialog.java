/*
 * onprom-dynamiceditor
 *
 * AnnotationSelectionDialog.java
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

package it.unibz.inf.kaos.ui.form;

import com.google.common.collect.Sets;
import it.unibz.inf.kaos.data.UMLClass;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import it.unibz.inf.kaos.ui.utility.UMLEditorButtons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Set;
import java.util.stream.Stream;

public class AnnotationSelectionDialog extends JDialog {
    private Set<UMLClass> classes = Sets.newLinkedHashSet();

    public AnnotationSelectionDialog(Stream<UMLClass> classStream) {
        setModal(true);
        setTitle("Select concepts to use as an annotation type");
        setLayout(new BorderLayout());

        JPanel pnlConcepts = new JPanel(null);
        pnlConcepts.setLayout(new BoxLayout(pnlConcepts, BoxLayout.Y_AXIS));
        pnlConcepts.setPreferredSize(new Dimension(500, 250));

        classStream.forEach(umlClass -> {
            pnlConcepts.add(UIUtility.createCheckBox(umlClass.getLongName(), "Use <u>" + umlClass.getName() + "</u> as an annotation type", true, e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    classes.add(umlClass);
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    classes.remove(umlClass);
                }
            }));
            classes.add(umlClass);
        });
        add(new JScrollPane(pnlConcepts), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel();

        pnlButtons.add(UIUtility.createButton(UMLEditorButtons.OK, e -> dispose(), AbstractAnnotationForm.BTN_SIZE));
        pnlButtons.add(UIUtility.createButton(AnnotationEditorButtons.ALL, e -> {
            for (Component component : pnlConcepts.getComponents()) {
                if (component instanceof JCheckBox) {
                    ((JCheckBox) component).setSelected(true);
                }
            }
        }, AbstractAnnotationForm.BTN_SIZE));
        pnlButtons.add(UIUtility.createButton(AnnotationEditorButtons.NONE, e -> {
            for (Component component : pnlConcepts.getComponents()) {
                if (component instanceof JCheckBox) {
                    ((JCheckBox) component).setSelected(false);
                }
            }
        }, AbstractAnnotationForm.BTN_SIZE));

        add(pnlButtons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public Set<UMLClass> getSelectedClasses() {
        return classes;
    }
}