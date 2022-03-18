/*
 * onprom-umleditor
 *
 * UMLEditorLabels.java
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

import it.unibz.inf.onprom.ui.interfaces.Labels;

/**
 * Labels used in UML Editor
 * <p>
 * @author T. E. Kalayci
 * Date: 16-Feb-17
 */
public enum UMLEditorLabels implements Labels {
    CLASS_NAME("Class Name", "Please enter name of the class"),
    RELATION_NAME("Name", "Please enter name of the relation"),
    FROM_TO("From → To", "Please select classes for the relation"),
    CARDINALITY("Cardinalities", "Please select cardinalities of the relation"),
    RELATION("Association", "Please select relation"),;

    private final String label;
    private final String tooltip;
    private final boolean clickable;

    UMLEditorLabels(String _label, String _tooltip) {
        this.label = _label;
        this.tooltip = _tooltip;
        this.clickable = false;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public boolean isClickable() {
        return clickable;
    }


}
