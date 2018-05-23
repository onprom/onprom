/*
 * onprom-umleditor
 *
 * Association.java
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

import it.unibz.inf.kaos.interfaces.UMLDiagram;
import it.unibz.inf.kaos.ui.form.RelationForm;
import it.unibz.inf.kaos.ui.utility.DrawingUtility;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * @author T. E. Kalayci
 */
public class Association extends Relationship {
  private AssociationClass associationClass;
  private Cardinality firstMultiplicity = Cardinality.C0_1;
  private Cardinality secondMultiplicity = Cardinality.C0_1;

  public Association() {
  }

  public Association(String name, String longName) {
    this(name, longName, null, null);
  }

  public Association(String name, UMLClass firstClass, UMLClass secondClass) {
    this(name, null, firstClass, secondClass);
  }

  public Association(String name, String longName, UMLClass firstClass, UMLClass secondClass) {
    super(name, longName);
    setFirstClass(firstClass);
    setSecondClass(secondClass);
  }

  public void removeAssociationClass(AssociationClass aClass) {
    if (associationClass.equals(aClass)) {
      associationClass = null;
    }
  }

  public AssociationClass getAssociationClass() {
    return associationClass;
  }

  public void setAssociationClass(AssociationClass aClass) {
    associationClass = aClass;
  }

  public boolean hasAssociation(UMLClass aClass) {
      return (aClass instanceof AssociationClass) && aClass.equals(associationClass);
  }

  public boolean hasAssociation() {
    return associationClass != null;
  }

  private String getFirstMultiplicityString() {
    return firstMultiplicity.toString();
  }

  public Cardinality getFirstMultiplicity() {
    return firstMultiplicity;
  }

  public void setFirstMultiplicity(Cardinality _multiplicity) {
    this.firstMultiplicity = _multiplicity;
  }

  public Cardinality getSecondMultiplicity() {
    return secondMultiplicity;
  }

  public void setSecondMultiplicity(Cardinality _multiplicity) {
    this.secondMultiplicity = _multiplicity;
  }

  private String getSecondMultiplicityString() {
    return secondMultiplicity.toString();
  }

  public String getDisplayString() {
      if (associationClass == null) {
          return getName() + getDirectionTriangle();
      } else {
      return "";
      }
  }

  @Override
  public void draw(Graphics2D g2d) {
    super.draw(g2d);
      final Font oldFont = g2d.getFont();
      final Stroke oldStroke = g2d.getStroke();
      final Color oldColor = g2d.getColor();
    g2d.setFont(DrawingUtility.RELATION_FONT);
    g2d.setColor(getState().getColor());
    g2d.setStroke(DrawingUtility.RELATION_STROKE);
      final int[] m1Position = getM1Position(g2d);
      final int[] m2Position = getM2Position(g2d);
    drawLabel(g2d, getFirstMultiplicityString(), m1Position[0], m1Position[1] - DrawingUtility.MARGIN, true);
    drawLabel(g2d, getSecondMultiplicityString(), m2Position[0], m2Position[1] - DrawingUtility.MARGIN, true);
    g2d.setColor(oldColor);
    g2d.setStroke(oldStroke);
    g2d.setFont(oldFont);
  }

  @Override
  public boolean isFirstFunctional() {
    return firstMultiplicity.isFunctional();
  }

  @Override
  public boolean isSecondFunctional() {
    return secondMultiplicity.isFunctional();
  }

  /**
   * Calculates position of first multiplicity using distance and
   * trigonometry based on class shape positions
   *
   * @return position of first multiplicity
   */
  private int[] getM1Position(Graphics2D g2d) {
    UMLClass firstClass = getFirstClass();
    UMLClass secondClass = getSecondClass();
    //use local variables
    final int centerX = firstClass.getCenterX();
    final int startX = firstClass.getStartX();
    final int centerY = firstClass.getCenterY();
    final int startY = firstClass.getStartY();
    // get distance from center to start point to estimate the position
    int distance = Math.max(centerX - startX, centerY - startY);
    // getCardinality slope between class and reference point
    int deltaX, deltaY;
    if (getAnchorCount() == 0) {
      // if we don't have any anchors, we are going to use second class as
      // reference point
      deltaX = centerX - secondClass.getCenterX();
      deltaY = secondClass.getCenterY() - centerY;
    } else {
      // if we have any anchor we are going to use first anchor as
      // reference point
      deltaX = centerX - getFirstAnchor().getX();
      deltaY = getFirstAnchor().getY() - centerY;
    }
    float angle = -(float) Math.atan2(deltaY, deltaX);
    // get new position using trigonometric functions isOver calculated distance
    //adjacent edge - for x axis
    int a = (int) (Math.cos(angle) * distance);
    //adjacent edge - for y axis
    int b = (int) (Math.sin(angle) * distance);
    // get coordinates of position using distance from center
    int x = centerX - a;
    int y = centerY - b;
    // arrange coordinates according size of string
    //check the angle to find correct position of label
    if (angle >= -DrawingUtility.D45 && angle < DrawingUtility.D45) {
        x = startX - g2d.getFontMetrics().stringWidth(getFirstMultiplicityString()) - DrawingUtility.MARGIN;
    } else if (angle >= DrawingUtility.D45 && angle < DrawingUtility.D135) {
      y = startY - DrawingUtility.MARGIN;
    } else if (angle <= -DrawingUtility.D45 && angle > -DrawingUtility.D135) {
      y = firstClass.getEndY() + g2d.getFontMetrics().getHeight() + DrawingUtility.MARGIN;
    } else {
      x = firstClass.getEndX() + DrawingUtility.MARGIN;
    }
    // return calculated position
    return new int[]{x, y};
  }

  /**
   * Calculates position of second multiplicity using distance and
   * trigonometry based on class shape positions
   *
   * @return position of second multiplicity
   */
  private int[] getM2Position(Graphics2D g2d) {
    UMLClass firstClass = getFirstClass();
    UMLClass secondClass = getSecondClass();
    //use local variables
    final int centerX = secondClass.getCenterX();
    final int startX = secondClass.getStartX();
    final int centerY = secondClass.getCenterY();
    final int startY = secondClass.getStartY();
    // get distance from center to start point to estimate the position
    int distance = Math.max(centerX - startX, centerY - startY);
    // getCardinality slope between class and reference point
    int deltaX, deltaY;
    if (getAnchorCount() == 0) {
      // if we don't have any anchors, we are going to second class as
      // reference point
      deltaX = firstClass.getCenterX() - centerX;
      deltaY = centerY - firstClass.getCenterY();
    } else {
      // if we have any anchor we are going to use first anchor as
      // reference point
      deltaX = getLastAnchor().getX() - centerX;
      deltaY = centerY - getLastAnchor().getY();
    }
    float angle = -(float) Math.atan2(deltaY, deltaX);
    // get new position using trigonometric functions isOver
    // calculated distance
    //adjacent edge - for x axis
    int a = (int) (Math.cos(angle) * distance);
    //opposite edge - for y axis
    int b = (int) (Math.sin(angle) * distance);
    // get coordinates of position using distance from center
    int x = centerX + a;
    int y = centerY + b;
    //check the angle to find correct position of label
    if (angle > -DrawingUtility.D45 && angle < DrawingUtility.D45) {
      x = secondClass.getEndX() + DrawingUtility.MARGIN;
    } else if (angle > DrawingUtility.D45 && angle < DrawingUtility.D135) {
      y = secondClass.getEndY() + g2d.getFontMetrics().getHeight() + DrawingUtility.MARGIN;
    } else if (angle < -DrawingUtility.D45 && angle > -DrawingUtility.D135) {
      y = startY - DrawingUtility.MARGIN;
    } else {
        x = startX - g2d.getFontMetrics().stringWidth(getSecondMultiplicityString()) - DrawingUtility.MARGIN;
    }
    //return calculated position
    return new int[]{x, y};
  }

  private String getDirectionTriangle() {
    if (getFirstClass().equals(getSecondClass())) {
      return "";
    }
    int diffX = getFirstClass().getCenterX() - getSecondClass().getCenterX();
    int diffY = getFirstClass().getCenterY() - getSecondClass().getCenterY();
    if (Math.abs(diffX) > Math.abs(diffY)) {
      if (diffX > 0) {
        //left pointing triangle
        return " ◁";
      }
      //right pointing triangle
      return " ▷";
    }
    if (diffY < 0) {
      //down pointing triangle
      return " ▽";
    }
    //up pointing triangle
    return " △";
  }

  @Override
  public java.util.Optional<RelationForm> getForm(UMLDiagram panel) {
      return java.util.Optional.of(new RelationForm(panel, this));
  }

}
