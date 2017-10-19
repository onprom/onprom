/*
 * onprom-annoeditor
 *
 * AbstractAnnotation.java
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.AnnotationForm;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import it.unibz.inf.kaos.ui.utility.DrawingConstants;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import java.awt.*;
import java.util.List;

/**
 * This class provides a skeletal implementation of the Annotation interface,
 * to minimize the effort required to implement it.
 * <p>
 *
 * @author T. E. Kalayci on 03/11/16.
 * @see AbstractDiagramShape
 */
public abstract class AbstractAnnotation extends AbstractDiagramShape implements Annotation {
    @JsonIgnore
    private final static Font ANNOTATION_FONT = new Font(Font.DIALOG, Font.PLAIN, 14);
    @JsonIgnore
    AnnotationProperties properties = getClass().getAnnotation(AnnotationProperties.class);

    UMLClass relatedClass;
    private String label;
    private List<AnnotationAttribute> attributes;

    AbstractAnnotation() {
    }

    AbstractAnnotation(UMLClass _relatedClass) {
        relatedClass = _relatedClass;
    }

    public String getName() {
        return label;
    }

    public AnnotationProperties getAnnotationProperties() {
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
        final String typeLabel = getAnnotationProperties().label();
        final Color bgColor = Color.decode(getAnnotationProperties().color());
        final String label = getLabel();
        //calculate required width and height
        int rectangleWidth = g2d.getFontMetrics().stringWidth(typeLabel) + 2 * DrawingConstants.GAP;
        int rectangleHeight = fontHeight * 2;
        if (label != null && !label.isEmpty()) {
            if (label.length() > typeLabel.length()) {
                rectangleWidth = g2d.getFontMetrics().stringWidth(label) + 2 * DrawingConstants.GAP;
            }
            rectangleHeight = fontHeight * 3;
        }
        g2d.setFont(ANNOTATION_FONT);
        //set background color of the rectangle
        g2d.setColor(bgColor);
        //draw filled rectangle with background color of the annotation
        g2d.fillRect(startX, startY, rectangleWidth, rectangleHeight);
        //draw line between the class and the annotation
        g2d.drawLine(startX + rectangleWidth / 2, startY + rectangleHeight / 2, getRelatedClass().getCenterX(),
                getRelatedClass().getCenterY());
        //set color of rectangle outline according to annotation state
        g2d.setColor(getState().getColor());
        //draw outline rectangle
        g2d.drawRect(startX, startY, rectangleWidth, rectangleHeight);
        //draw type of the annotation
        int fontWidth = g2d.getFontMetrics().stringWidth(typeLabel);
        int typeCoord = startX + (rectangleWidth - fontWidth) / 2;
        if (UIUtility.isDark(bgColor)) {
            g2d.setColor(Color.WHITE);
        }
        g2d.drawString(typeLabel, typeCoord, startY + fontHeight + DrawingConstants.GAP);
        if (label != null && !label.isEmpty()) {
            //draw label of the annotation if it exists
            fontWidth = g2d.getFontMetrics().stringWidth(label);
            typeCoord = startX + (rectangleWidth - fontWidth) / 2;
            g2d.drawString(label, typeCoord, startY + 2 * fontHeight);
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
        return relatedClass.toString();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public List<AnnotationAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AnnotationAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public UMLClass getRelatedClass() {
        return relatedClass;
    }

    @Override
    public abstract List<AnnotationQuery> getQuery();

    @Override
    public abstract AnnotationForm getForm(AnnotationDiagramPanel panel);

}