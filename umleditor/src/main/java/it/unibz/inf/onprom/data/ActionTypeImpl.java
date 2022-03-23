/*
 * umleditor
 *
 * ActionTypeImpl.java
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
 * Created by T. E. Kalayci on 19-Dec-2017.
 */
public class ActionTypeImpl implements ActionType {

    private final String title;
    private final String tooltip;
    private final String icon;
    private final char mnemonic;

    public ActionTypeImpl(String title, String tooltip, String icon, char mnemonic) {
        this.title = title;
        this.tooltip = tooltip;
        this.icon = icon;
        this.mnemonic = mnemonic;
    }

    public ActionTypeImpl(String title, String tooltip, String icon) {
        this.title = title;
        this.tooltip = tooltip;
        this.icon = icon;
        this.mnemonic = title.charAt(0);
    }

    @Override
    public char getMnemonic() {
        return mnemonic;
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
        return icon;
    }
}
