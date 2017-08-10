/*
 * onprom-umleditor
 *
 * State.java
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

package it.unibz.inf.kaos.data;

import java.awt.*;

/**
 * State of object. It is used for displaying object in different colors in the diagram.
 * <p>
 * @author T. E. Kalayci
 * 10-Nov-16
 */
public enum State {
    NORMAL(new Color(0, 0, 0)),
    SELECTED(new Color(228, 108, 59)),
    DISABLED(new Color(250, 250, 250)),
    HIGHLIGHTED(new Color(124, 168, 201));

    private final Color color;

    State(Color c) {
        this.color = c;
    }

    public Color getColor() {
        return color;
    }
}
