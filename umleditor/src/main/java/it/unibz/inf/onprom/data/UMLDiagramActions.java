/*
 * umleditor
 *
 * UMLDiagramActions.java
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

package it.unibz.inf.onprom.data;

import it.unibz.inf.onprom.interfaces.ActionType;

/**
 * @author T. E. Kalayci
 */
public enum UMLDiagramActions implements ActionType {
  //title are also name of images
  //so we use lowercase for the names and values
  open("open", 'o', "Open a File", "Open"),
  save("save", 's', "Save Changes to the File", "Save"),
  saveas("saveas", 'a', "Save Diagram to a new JSON File", "Save As..."),
  delete("delete", 'd', "Delete Selected Object", "Delete"),
  disable("disable", 'i', "Disable Selected Object", "Disable"),
  newdiagram("newdiagram", 'n', "New Diagram", "New"),
  print("print", 'p', "Send Diagram to Printer", "Print"),
  image("image", 'i', "Export Diagram as Image", "Export Image"),
  export("export", 'e', "Export Diagram", "Export"),
  close("close", 'c', "Exit Dialog", "Exit"),
  //edit menu
  undo("undo", 'u', "Undo last operation", "Undo"),
  redo("redo", 'r', "Redo last operation", "Redo"),
  grid("grid", 'g', "Enable or Disable Grid", "Toggle Grid"),
  layout("layout", 'l', "Automatically layout the objects", "Auto Layout"),
  objects("objects", 'j', "Show list of objects", "Objects"),
  zoomin("zoomin", 'z', "Zoom In Diagram", "Zoom In"),
  zoomout("zoomout", 'o', "Zoom Out Diagram", "Zoom Out"),
  resetzoom("resetzoom", 'r', "Reset Diagram", "Reset Zoom"),
  //editor specific actions
  select("select", 's', "Select Operation", "Select"),
  //UML editor actions
  umlclass("umlclass", 'c', "Add New UML Class", "Class"),
  isarelation("isarelation", 'i', "IS-A relation of two classes", "IS-A Association"),
  relation("relation", 'r', "Relate two classes", "Association"),
  disjoint("disjoint", 'd', "Disjoint two classes", "Disjoint"),
  association("association", 'a', "Add association class", "Association");

  private final String actionCommand;
  private final char mnemonic;
  private final String tooltip;
  private final String title;

  UMLDiagramActions(final String _actionCommand, char _mnemonic, String _tooltip, String _title) {
    this.actionCommand = _actionCommand;
    this.mnemonic = _mnemonic;
    this.tooltip = _tooltip;
    this.title = _title;
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
  public String getIcon() {
    return actionCommand;
  }

}