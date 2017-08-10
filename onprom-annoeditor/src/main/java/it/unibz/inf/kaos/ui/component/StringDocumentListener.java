/*
 * onprom-annoeditor
 *
 * StringDocumentListener.java
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

import it.unibz.inf.kaos.data.StringAttribute;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Document listener for updating StringAttribute objects using JTextField
 * <p>
 * @author T. E. Kalayci on 15/02/17.
 */
public class StringDocumentListener implements DocumentListener {
  final private JTextField textField;
  private StringAttribute attribute;

  public StringDocumentListener(StringAttribute _attribute, JTextField _textField) {
    this.attribute = _attribute;
    this.textField = _textField;
  }

  public void updateAttribute(StringAttribute _attribute) {
    attribute = _attribute;
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    if (attribute != null) {
      attribute.setValue(textField.getText());
    }
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    if (attribute != null) {
      attribute.setValue(textField.getText());
    }
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
  }
}
