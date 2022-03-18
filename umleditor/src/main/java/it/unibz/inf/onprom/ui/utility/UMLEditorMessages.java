/*
 * onprom-umleditor
 *
 * UMLEditorMessages.java
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

package it.unibz.inf.onprom.ui.utility;

import it.unibz.inf.onprom.ui.interfaces.Messages;

/**
 * Messages shown on annotation editor
 * <p>
 * @author T. E. Kalayci
 * 16-Feb-17
 */
public enum UMLEditorMessages implements Messages {
    CLASS_NAME_ERROR("Class name is Missing", "Please enter a <em>name</em> for the Class"),
    CLASS_NAME_DUPLICATE_ERROR("Class Name is Not Unique", "Please change <em>name</em> of the class, it is already <em>used</em> in the diagram"),
    DELETE_CONFIRM("Deleting Item", "Are you sure you want to delete selected item?"),
    CLEAR_DIAGRAM("Clearing Diagram", "Are you sure you want to clear diagram? You can't take back it."),
    EMPTY_DIAGRAM("Empty Diagram", "Diagram is empty!"),
    LAYOUT_DIAGRAM("Diagram Layout", "Are you sure you want to layout the diagram, you'll lose the current layout?"),
    SAVE_FILE("Save File", "Would you like to save as file also?"),
    FILE_EXISTS("File Exists", "Are you sure you want to overwrite existing file?"),
    CLOSE_EDITOR("Closing Editor", "Are you sure you want to close this dialog? Your changes that you didn't save will be lost!");

    private final String title;
    private final String message;

    UMLEditorMessages(String _title, String _message) {
        this.title = _title;
        this.message = _message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

}
