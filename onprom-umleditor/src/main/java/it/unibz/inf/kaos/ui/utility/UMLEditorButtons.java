/*
 * onprom-umleditor
 *
 * UMLEditorButtons.java
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

package it.unibz.inf.kaos.ui.utility;

import it.unibz.inf.kaos.ui.interfaces.Buttons;

/**
 * Enumeration for UMLEditorButtons of the forms
 * <p>
 * @author T. E. Kalayci
 * 16-Feb-17
 */
public enum UMLEditorButtons implements Buttons {
    OK("OK", "Close the dialog", 'o'),
    CANCEL("Cancel", "Cancel the operation", 'c'),
    SAVE("Save", "<u>S</u>ave the changes", 's'),
    EXPORT("Export", "Start <u>e</u>xport operation", 'e'),
    EXPORT_OBDA("Generate OBDA", "Generate intermediate <u>O</u>BDA", 'o'),
    RESET("Reset", "<u>R</u>eset the form", 'r'),
    ADD("+", "<u>A</u>dd new attribute to the Class", 'a'),
    REMOVE("-", "<u>R</u>emove selected attribute from Class", 'r'),
    UP("↑", "Move selected attribute <u>u</u>p", 'u'),
    DOWN("↓", "Move selected attribute <u>d</u>own", 'd'),
    CLOSE("×", "Close the form", 'x');

    private final String text;
    private final String tooltip;
    private final char mnemonic;


    UMLEditorButtons(final String _text, final String _tooltip, final char _mnemonic) {
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
