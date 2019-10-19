/*
 * onprom-annoeditor
 *
 * AnnotationEditorButtons.java
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

package it.unibz.inf.kaos.ui.utility;

import it.unibz.inf.kaos.ui.interfaces.Buttons;

/**
 * Enumeration for buttons used in annotation editor forms
 * <p>
 * @author T. E. Kalayci on 16/02/17.
 */
public enum AnnotationEditorButtons implements Buttons {
  CONTINUE("Continue", "Continue", 'c'),
    CANCEL("Cancel", "Cancel the operation", 'c'),
    SAVE("Save", "Save the attribute to the annotation", 's'),
  DIAGRAM("...", "Select the <u>v</u>alue from the diagram", 'v'),
  ADD("+", "<u>A</u>dd new attribute to the annotation", 'a'),
  REMOVE("-", "<u>R</u>emove the selected attribute of the annotation", 'r'),//"¤"
    ALL("All", "Select all items", 'a'),
    NONE("None", "Deselect all items", 'n'),
  ;

    private final String text;
    private final String tooltip;
    private final char mnemonic;

  AnnotationEditorButtons(final String _text, final String _tooltip, final char _mnemonic) {
    this.text = _text;
    this.tooltip = _tooltip;
    this.mnemonic = _mnemonic;
  }

  public String getText() {
    return this.text;
  }

  public String getTooltip() {
    return this.tooltip;
  }

  public char getMnemonic() {
    return this.mnemonic;
  }

  @Override
  public String toString() {
    return text;
  }
}
