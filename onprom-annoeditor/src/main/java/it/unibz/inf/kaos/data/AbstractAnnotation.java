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

import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueryV2;
import it.unibz.inf.kaos.interfaces.Annotation;
import it.unibz.inf.kaos.interfaces.AnnotationForm;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;
import it.unibz.inf.kaos.ui.utility.DrawingConstants;

import java.awt.*;
import java.util.LinkedList;

/**
 * Abstract class to store various annotations.
 * <p>
 * @author T. E. Kalayci on 03/11/16.
 *
 * @see AbstractDiagramShape
 */
public abstract class AbstractAnnotation extends AbstractDiagramShape implements Annotation {
  @JsonIgnore
  private final static Font ANNOTATION_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
  @JsonIgnore
  private final AnnotationProperties properties = getClass().getAnnotation(AnnotationProperties.class);

  UMLClass relatedClass;
  private String label;
  private LinkedList<AnnotationAttribute> attributes;

  AbstractAnnotation() {
  }

  AbstractAnnotation(UMLClass _relatedClass) {
    relatedClass = _relatedClass;
  }

  public String getName() {
    return label;
  }

  public void draw(Graphics2D g2d) {
    Color oldColor = g2d.getColor();
    Stroke oldStroke = g2d.getStroke();
    Font oldFont = g2d.getFont();
    g2d.setFont(ANNOTATION_FONT);
    int fontHeight = g2d.getFontMetrics().getHeight();
    int startX = getStartX();
    int startY = getStartY();
    String typeLabel = properties.type();
    String label = getLabel();
    //draw shapes, string
    int rectangleWidth = g2d.getFontMetrics().stringWidth(typeLabel) + 2 * DrawingConstants.GAP;
    int rectangleHeight = fontHeight * 2;
    if (label != null && !label.isEmpty()) {
      if (label.length() > typeLabel.length()) {
        rectangleWidth = g2d.getFontMetrics().stringWidth(label) + 2 * DrawingConstants.GAP;
      }
      rectangleHeight = fontHeight * 3;
    }
    //rectangle background color
    g2d.setColor(Color.decode(properties.color()));
    //filled rectangle background of annotation
    g2d.fillRect(startX, startY, rectangleWidth, rectangleHeight);
    //line between class and annotation
    g2d.drawLine(startX + rectangleWidth / 2, startY + rectangleHeight / 2, getRelatedClass().getCenterX(),
      getRelatedClass().getCenterY());
    //set color of rectangle outline according to annotation state
    g2d.setColor(getState().getColor());
    //draw rectangle outline
    g2d.drawRect(startX, startY, rectangleWidth, rectangleHeight);
    //draw type label of annotation
    int fontWidth = g2d.getFontMetrics().stringWidth(typeLabel);
    int typeCoord = startX + (rectangleWidth - fontWidth) / 2;
    g2d.drawString(typeLabel, typeCoord, startY + fontHeight + DrawingConstants.GAP);
    if (label != null && !label.isEmpty()) {
      //draw label of annotation if it exists
      fontWidth = g2d.getFontMetrics().stringWidth(label);
      typeCoord = startX + (rectangleWidth - fontWidth) / 2;
      g2d.drawString(label, typeCoord, startY + 2 * fontHeight);
    }
    //set end coordinates for annotation
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

  public int getAttributeCount() {
    if (attributes != null) {
      return attributes.size();
    }
    return 0;
  }

  public void setCoordinates(int x, int y) {
    setStartX(x);
    setStartY(y);
  }

  public LinkedList<AnnotationAttribute> getAttributes() {
    return attributes;
  }

  public UMLClass getRelatedClass() {
    return relatedClass;
  }

  @Override
  public abstract <T extends AnnotationQueryV2> T getQuery();

  public abstract AnnotationForm getForm(AnnotationDiagramPanel panel);

  public void setAttributes(LinkedList<AnnotationAttribute> attributes) {
    this.attributes = attributes;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

}