/*
 * onprom-toolkit
 *
 * ResourceConnection.java
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

import it.unibz.inf.kaos.ui.utility.DrawingConstants;

import java.awt.*;
import java.awt.geom.Path2D;

/**
 * @author T. E. Kalayci on 10-Jul-2017.
 */
public class ResourceConnection extends AbstractDiagramShape {
    private ResourceShape firstResource;
    private ResourceShape secondResource;

    public ResourceConnection() {
    }

    public ResourceConnection(ResourceShape _first, ResourceShape _second) {
        super("", "");
        setFirstResource(_first);
        setSecondResource(_second);
    }

    private ResourceShape getFirstResource() {
        return firstResource;
    }

    private void setFirstResource(ResourceShape firstResource) {
        this.firstResource = firstResource;
    }

    private ResourceShape getSecondResource() {
        return secondResource;
    }

    private void setSecondResource(ResourceShape secondResource) {
        this.secondResource = secondResource;
    }

    public String toString() {
        return getName();
    }

    public int getStartX() {
        return getFirstResource().getCenterX();
    }

    public int getStartY() {
        return getFirstResource().getCenterY();
    }

    @Override
    public int getCenterX() {
        return (getStartX() + getEndX()) / 2;
    }

    @Override
    public int getCenterY() {
        return (getStartY() + getEndY()) / 2;
    }

    public int getEndX() {
        return getSecondResource().getCenterX();
    }

    public int getEndY() {
        return getSecondResource().getCenterY();
    }

    @Override
    Shape getShape() {

        int xDiff = Math.abs(secondResource.getCenterX() - firstResource.getCenterX());
        int yDiff = Math.abs(secondResource.getCenterY() - firstResource.getCenterY());
        if (xDiff > 2 * yDiff) {
            if (secondResource.getStartX() > firstResource.getEndX()) {
                return drawArrow(firstResource.getCenterX(), firstResource.getCenterY(),
                        secondResource.getStartX(), secondResource.getCenterY());
            } else {
                return drawArrow(firstResource.getCenterX(), firstResource.getCenterY(),
                        secondResource.getEndX(), secondResource.getCenterY());
            }
        } else {
            if (secondResource.getStartY() > firstResource.getEndY()) {
                return drawArrow(firstResource.getCenterX(), firstResource.getCenterY(),
                        secondResource.getCenterX(), secondResource.getStartY());
            } else {
                return drawArrow(firstResource.getCenterX(), firstResource.getCenterY(),
                        secondResource.getCenterX(), secondResource.getEndY());
            }
        }
    }

    private Path2D drawArrow(int x1, int y1, int x2, int y2) {
        Path2D context = new Path2D.Float();
        int headlen = 10;   // length of head in pixels
        double angle = Math.atan2(y2 - y1, x2 - x1);
        context.moveTo(x1, y1);
        context.lineTo(x2, y2);
        context.lineTo(x2 - headlen * Math.cos(angle - Math.PI / 6), y2 - headlen * Math.sin(angle - Math.PI / 6));
        context.moveTo(x2, y2);
        context.lineTo(x2 - headlen * Math.cos(angle + Math.PI / 6), y2 - headlen * Math.sin(angle + Math.PI / 6));
        context.closePath();
        return context;
    }

    public void draw(Graphics2D g2d) {
        // store current stroke and color
        final Font oldFont = g2d.getFont();
        final Stroke oldStroke = g2d.getStroke();
        final Color oldColor = g2d.getColor();
        g2d.setFont(DrawingConstants.RELATION_FONT);
        // color according to state
        g2d.setColor(getState().getColor());
        g2d.setStroke(DrawingConstants.RELATION_STROKE);
        g2d.draw(getShape());

        //arrow
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
        g2d.setFont(oldFont);
    }

}
