/*
 * onprom-annoeditor
 *
 * AnnotationEditorMessages.java
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

import it.unibz.inf.kaos.ui.interfaces.Messages;

/**
 * Messages shown on annotation editor
 * <p>
 *
 * @author T. E. Kalayci on 16-Feb-17.
 */
public enum AnnotationEditorMessages implements Messages {
    EVENT_NAME_ERROR("Event name is Missing", "Please enter a <em>name</em> for the Event"),
    TIMESTAMP_ERROR("Timestamp is Missing", "Please select a <em>timestamp</em> for the Event"),
    CASE_NAME_ERROR("Please enter a <em>case name</em> for the Case", "Case Name is Missing"),
    DELETE_CONFIRMATION("Delete Confirmation", "Are you sure you want to delete selected annotation?"),
    CASE_DELETE_CONFIRMATION("Delete Confirmation", "There are resources and events related with this <em>Case</em> and deleting it may cause inconsistencies, are you sure you want to delete?"),
    CHANGE_CASE("New Case", "You already have a <em>Case</em>. Do you want to <em>add</em> another <em>Case</em> annotation?"),
    SELECT_CASE("Selecting Case", "Please select a <em>Case</em> first"),;

    private final String title;
    private final String message;

    AnnotationEditorMessages(String _title, String _message) {
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
