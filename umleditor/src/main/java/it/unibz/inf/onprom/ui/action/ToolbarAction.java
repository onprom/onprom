/*
 * umleditor
 *
 * ToolbarAction.java
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

package it.unibz.inf.onprom.ui.action;

import it.unibz.inf.onprom.interfaces.ActionType;

/**
 * Abstract class for toolbar actions
 * <p>
 * @author T. E. Kalayci
 * Date: 28-Oct-16
 */
public abstract class ToolbarAction {
    final ActionType actionType;

    protected ToolbarAction(ActionType _action) {
        actionType = _action;
    }

    public String getIcon() {
        return actionType.getIcon();
    }

    public char getMnemonic() {
        return actionType.getMnemonic();
    }

    public String getTooltip() {
        return actionType.getTooltip();
    }

    public String getTitle() {
        return actionType.getTitle();
    }

    public abstract void execute();
}
