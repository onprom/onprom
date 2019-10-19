/*
 * onprom-umleditor
 *
 * ZoomUtility.java
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

import java.awt.*;

/**
 * This class contains fields and methods related to zoom capability of
 * drawing panel.
 * <p>
 * @author T. E. Kalayci
 * 29-Sep-16
 */
public class ZoomUtility {
    public static final Dimension INITIAL_SIZE = new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 4f;
    private static final float DEFAULT_SCALE = 1.0f;
    private static final float SCALE_INCREMENT = 0.1f;
    public static float ZOOMING_SCALE = DEFAULT_SCALE;

    /**
     * Method to get correct coordinate for the objects by calculating
     * based on zoom scale.
     * <p>
     * For possible zooming this method must be used always instead of
     * directly getting X coordinate from event object.
     * <p>
     *
     * @param x coordinate value of mouse event
     * @return calculated coordinate value based on zoom and click point on panel
     */
    public static int get(int x) {
        return (int) ((1 / ZOOMING_SCALE) * x);
    }

    public static void changeZoom(double ticks) {
        if (ticks < 0) {
            increaseZoom();
        } else {
            decreaseZoom();
        }
    }

    public static void increaseZoom() {
        if (Double.compare(ZOOMING_SCALE, MAX_SCALE) < 0)
            ZOOMING_SCALE += SCALE_INCREMENT;
    }

    public static void decreaseZoom() {
        if (Double.compare(ZOOMING_SCALE, MIN_SCALE) > 0)
            ZOOMING_SCALE -= SCALE_INCREMENT;
    }

    public static void resetZoom() {
        ZOOMING_SCALE = DEFAULT_SCALE;
    }

    public static int getWidth() {
        return Math.toIntExact(Math.round(INITIAL_SIZE.getWidth()
                * ZOOMING_SCALE));
    }

    public static int getHeight() {
        return Math.toIntExact(Math.round(INITIAL_SIZE.getHeight()
                * ZOOMING_SCALE));
    }
}
