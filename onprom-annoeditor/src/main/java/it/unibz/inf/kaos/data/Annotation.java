/*
 * onprom-annoeditor
 *
 * Annotation.java
 *
 * Copyright (C) 2016-2018 Free University of Bozen-Bolzano
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

package it.unibz.inf.kaos.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.AnnotationProperties;
import it.unibz.inf.kaos.ui.utility.DrawingUtility;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;

/**
 * This class provides a skeletal implementation of the Annotation interface,
 * to minimize the effort required to implement it.
 * <p>
 *
 * @author T. E. Kalayci on 03/11/16.
 * @see AbstractDiagramShape
 */
public abstract class Annotation extends AbstractDiagramShape<AnnotationDiagram> {
    @JsonIgnore
    private static final Font ANNOTATION_FONT = DrawingUtility.getFont("Prompt-Regular", Font.PLAIN, 14f);
    @JsonIgnore
    AnnotationProperties properties = getClass().getAnnotation(AnnotationProperties.class);

    UMLClass relatedClass;
    private String label;
    private List<AnnotationAttribute> attributes;

    Annotation() {
    }

    Annotation(UMLClass _relatedClass) {
        relatedClass = _relatedClass;
    }

    public String getName() {
        return label;
    }

    AnnotationProperties getAnnotationProperties() {
        return properties;
    }

    @Override
    public String getLongName() {
        return relatedClass.getLongName();
    }

    public void draw(Graphics2D g2d) {
        final Color oldColor = g2d.getColor();
        final Stroke oldStroke = g2d.getStroke();
        final Font oldFont = g2d.getFont();

        final int fontHeight = g2d.getFontMetrics().getHeight();
        final int startX = getStartX();
        final int startY = getStartY();
        final String type = getAnnotationProperties().title();
        final Color bgColor = Color.decode(getAnnotationProperties().color());
        final String label = getLabel();
        //calculate box height and width
        g2d.setFont(ANNOTATION_FONT);
        int rectangleHeight = fontHeight * 2;
        int rectangleWidth = g2d.getFontMetrics().stringWidth(type);
        if (label != null && !label.isEmpty()) {
            rectangleHeight += fontHeight;
            if (label.length() > type.length()) {
                rectangleWidth = g2d.getFontMetrics().stringWidth(label);
            }
        }
        rectangleWidth += 3 * DrawingUtility.MARGIN;
        //draw a rectangle for the box using font and background color
        g2d.setColor(bgColor);
        g2d.fillRect(startX, startY, rectangleWidth, rectangleHeight);
        //draw line between the class and the annotation
        g2d.drawLine(startX + rectangleWidth / 2, startY + rectangleHeight / 2, getRelatedClass().getCenterX(),
                getRelatedClass().getCenterY());
        //draw outline rectangle according to the state of the shape
        g2d.setColor(getState().getColor());
        g2d.drawRect(startX, startY, rectangleWidth, rectangleHeight);
        //draw type of the annotation
        int fontWidth = g2d.getFontMetrics().stringWidth(type);
        int typeCoord = startX + (rectangleWidth - fontWidth) / 2;
        if (UIUtility.isDark(bgColor)) {
            g2d.setColor(Color.WHITE);
        }
        g2d.drawString(type, typeCoord, startY + fontHeight + DrawingUtility.MARGIN);
        if (label != null && !label.isEmpty()) {
            //draw label of the annotation if it exists
            fontWidth = g2d.getFontMetrics().stringWidth(label);
            typeCoord = startX + (rectangleWidth - fontWidth) / 2;
            g2d.drawString(label, typeCoord, startY + 2 * fontHeight + DrawingUtility.MARGIN);
        }
        //set end coordinates of the annotation
        setEndX(startX + rectangleWidth);
        setEndY(startY + rectangleHeight);
        //load previous properties again
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
        g2d.setFont(oldFont);
    }

    public String toString() {
        return getAnnotationProperties().title() + (label!=null ? " "+label:" ") + " (" + relatedClass.toString() + ")";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<AnnotationAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AnnotationAttribute> attributes) {
        this.attributes = attributes;
    }

    public UMLClass getRelatedClass() {
        return relatedClass;
    }

    public String getVarName(){
        return relatedClass.getCleanName();
    }

    @Override
    public String getCleanName(){
        return relatedClass.getCleanName();
    }

    public abstract List<AnnotationQuery> getQuery();

    @Override
    public abstract java.util.Optional<? extends JPanel> getForm(AnnotationDiagram panel);
}