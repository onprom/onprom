/*
 * onprom-annoeditor
 *
 * AddDeleteAnnotationEdit.java
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

package it.unibz.inf.onprom.ui.edit;

import it.unibz.inf.onprom.data.DynamicAnnotation;
import it.unibz.inf.onprom.interfaces.AnnotationDiagram;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Annotation addition and removal undo-redo edit
 * <p>
 * @author T. E. Kalayci on 16/11/16.
 */
public class AddDeleteAnnotationEdit extends AbstractUndoableEdit {
  private final AnnotationDiagram drawingPanel;
  private final DynamicAnnotation cls;
  // true for adding, false for deletion
  private final boolean adding;

  public AddDeleteAnnotationEdit(AnnotationDiagram _panel, DynamicAnnotation _cls, boolean _adding) {
    this.drawingPanel = _panel;
    this.cls = _cls;
    this.adding = _adding;
  }

  @Override
  public void undo() {
    super.undo();
    if (adding) {
      delete();
    } else {
      add();
    }
  }

  @Override
  public void redo() {
    super.redo();
    if (adding) {
      add();
    } else {
      delete();
    }
  }

  @Override
  public String getPresentationName() {
    return String.format("AddDeleteAnnotationEdit_%s_%s", cls.toString(), adding);
  }

  private void delete() {
    drawingPanel.removeAnnotation(cls);
  }

  private void add() {
    drawingPanel.addAnnotation(cls);
  }
}
