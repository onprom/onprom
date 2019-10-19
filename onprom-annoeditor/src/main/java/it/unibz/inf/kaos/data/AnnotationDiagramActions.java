/*
 * onprom-annoeditor
 *
 * AnnotationDiagramActions.java
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

package it.unibz.inf.kaos.data;

import it.unibz.inf.kaos.interfaces.ActionType;

/**
 * Actions for specific annotations
 * <p>
 * @author T. E. Kalayci on 25-May-2017.
 */
public enum AnnotationDiagramActions implements ActionType {
  NAVIGATE("navigate", 'n', "Navigate the diagram", "Navigate");

  private final String actionCommand;
  private final char mnemonic;
  private final String tooltip;
  private final String title;

  AnnotationDiagramActions(final String text, char _mnemonic, String _tooltip, String _text) {
    this.actionCommand = text;
    this.mnemonic = _mnemonic;
    this.tooltip = _tooltip;
    this.title = _text;
  }

  @Override
  public char getMnemonic() {
    return this.mnemonic;
  }

  @Override
  public String getTooltip() {
    return tooltip;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    return actionCommand;
  }

}
