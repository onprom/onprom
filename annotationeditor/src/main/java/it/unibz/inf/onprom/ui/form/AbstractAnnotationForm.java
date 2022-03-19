/*
 * annotationeditor
 *
 * AbstractAnnotationForm.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
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

package it.unibz.inf.onprom.ui.form;

import it.unibz.inf.onprom.data.Annotation;
import it.unibz.inf.onprom.data.Attribute;
import it.unibz.inf.onprom.data.UMLClass;
import it.unibz.inf.onprom.interfaces.AnnotationDiagram;
import it.unibz.inf.onprom.interfaces.DiagramShape;
import it.unibz.inf.onprom.interfaces.NavigationListener;
import it.unibz.inf.onprom.ui.component.UpdateListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * This class provides a skeletal implementation of the AnnotationForm interface,
 * to minimize the effort required to implement this interface.
 * <p>
 * @author T. E. Kalayci on 13/12/16.
 */
public abstract class AbstractAnnotationForm extends JPanel implements NavigationListener {
    public static final Dimension BTN_SIZE = new Dimension(85, 25);
    public static final Dimension CHK_SIZE = new Dimension(125, 25);
    public static final Dimension TXT_SIZE = new Dimension(175, 25);
    public static final Dimension DOUBLE_TXT_SIZE = new Dimension(350, 25);
    final AnnotationDiagram drawingPanel;
    final Annotation annotation;
    private UpdateListener updateListener;

    AbstractAnnotationForm(AnnotationDiagram _drawingPanel, Annotation _annotation) {
        drawingPanel = _drawingPanel;
        annotation = _annotation;
    }

  void startNavigation(UpdateListener _updateListener, boolean functional) {
    updateListener = _updateListener;
    drawingPanel.resetAttributeStates();
    drawingPanel.highlightAttribute(annotation.getRelatedClass(), functional, updateListener.getDataType());
    drawingPanel.startNavigation(this);
  }

  @Override
  public void navigationComplete(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
    updateListener.updateAttribute(path, selectedClass, selectedAttribute);
  }

  protected abstract void populateForm();

  void addTabbedPane(JPanel mainPanel, JPanel attributeForm) {
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("Main", mainPanel);
    tabbedPane.setMnemonicAt(0, KeyEvent.VK_M);
    tabbedPane.add("Additional Attributes", attributeForm);
    tabbedPane.setMnemonicAt(1, KeyEvent.VK_A);
    add(tabbedPane);
  }

  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    if (aFlag) {
      populateForm();
    } else {
      drawingPanel.resetAttributeStates();
      drawingPanel.resetNavigation();
    }
  }
}
