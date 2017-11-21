/*
 * onprom-umleditor
 *
 * WidePopupComboBox.java
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

package it.unibz.inf.kaos.ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * @author T. E. Kalayci
 * Date: 03-Feb-17
 */
public class WidePopupComboBox<E> extends JComboBox<E> {
    private boolean layouting;

    public WidePopupComboBox(java.util.stream.Stream<E> items) {
        this(items::iterator);
    }

    public WidePopupComboBox(Iterable<E> items) {
        super();
        if (items != null) {
            items.forEach(this::addItem);
        }
    }

    public WidePopupComboBox(E[] items) {
        super(items);
    }

    public Dimension getSize() {
        Dimension dim = super.getSize();
        if (!layouting)
            dim.width = Math.max(getWidestItemWidth(), dim.width);
        return dim;
    }

    private int getWidestItemWidth() {
        int numOfItems = this.getItemCount();
        if (numOfItems > 0) {
            Font font = this.getFont();
            FontMetrics metrics = this.getFontMetrics(font);
            int widest = 0;
            int lineWidth;
            for (int i = 0; i < numOfItems; i++) {
                Object item = this.getItemAt(i);
                if (item != null) {
                    lineWidth = metrics.stringWidth(item.toString());
                    widest = Math.max(widest, lineWidth);
                }
            }
            return widest + 5;
        }
        return super.getWidth();
    }

    public void doLayout() {
        try {
            layouting = true;
            super.doLayout();
        } finally {
            layouting = false;
        }
    }
}
