/*
 * onprom-umleditor
 *
 * DrawingConstants.java
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

import java.awt.*;

/**
 * This class contains static drawing methods
 * <p>
 *
 * @author T. E. Kalayci
 * Date: 28-Sep-16
 */
public class DrawingConstants {

    //contants
    //45 degree
    public static final double D45 = Math.PI / 4;
    //135 degree
    public static final double D135 = 3 * D45;
    //variables
    public static final int GAP = 5;
    public static final int GRID_SIZE = 25;
    public static final float ANCHOR_RADIUS = 12;
    public static final float HALF_ANCHOR_RADIUS = ANCHOR_RADIUS / 2;
    //fonts
    public final static Font CLASS_NAME_FONT = new Font(Font.DIALOG, Font.BOLD, 15);
    public final static Font RELATION_FONT = new Font(Font.DIALOG, Font.ITALIC, 12);
    public final static Font ATTRIBUTE_NAME_FONT = new Font(Font.DIALOG, Font.PLAIN, 13);
    //colors
    public static final Color BACKGROUND = new Color(240, 248, 250);

    //strokes
    private static final float NORMAL_SIZE = 2f;
    public final static Stroke NORMAL_STROKE = new BasicStroke(NORMAL_SIZE);
    public final static Stroke DISJOINT_STROKE = new BasicStroke(NORMAL_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{10}, 0);
    public final static Stroke RELATION_STROKE = new BasicStroke(NORMAL_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
}
