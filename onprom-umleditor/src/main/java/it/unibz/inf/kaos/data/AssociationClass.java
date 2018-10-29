/*
 * onprom-umleditor
 *
 * AssociationClass.java
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

import it.unibz.inf.kaos.ui.utility.DrawingUtility;

import java.awt.*;

/**
 * Association class
 * <p>
 * @author T. E. Kalayci
 * 07-Mar-17
 */
public class AssociationClass extends UMLClass {
  private Association association;

  public AssociationClass() {
  }

    public AssociationClass(Association association, int x, int y) {
        this(association);
        setStartX(x);
        setStartY(y);
    }

  public AssociationClass(Association association) {
    this.association = association;
    association.setAssociationClass(this);
    this.setAssociation(association);
  }

  public String getName() {
    return association.getName();
  }

  public void setName(String _name) {
    if (association != null)
      association.setName(_name);
  }

  public String getLongName() {
    return association.getLongName();
  }

  public Association getAssociation() {
    return association;
  }

  public void setAssociation(Association association) {
    this.association = association;
  }

  public void draw(Graphics2D g2d) {
      final Stroke oldStroke = g2d.getStroke();
      final int[] coordinates = association.getNamePosition();
      g2d.setStroke(DrawingUtility.DISJOINT_STROKE);
    g2d.drawLine(getCenterX(), getCenterY(), coordinates[0], coordinates[1]);
    g2d.setStroke(oldStroke);
    super.draw(g2d);
  }
}
