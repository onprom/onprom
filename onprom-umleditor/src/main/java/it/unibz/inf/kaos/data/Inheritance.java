/*
 * onprom-umleditor
 *
 * Inheritance.java
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

package it.unibz.inf.kaos.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.*;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.Path2D;

/**
 * @author T. E. Kalayci
 */
public class Inheritance extends Relationship {
    @JsonIgnore
    private Position trianglePosition = Position.LEFT;

  private static final double d30 = 0.53;// 30°
  private static final int lineSize = 25;
  private static final int cos30 = (int) (lineSize * Math.cos(d30));
  private static final int sin30 = (int) (lineSize * Math.sin(d30));
  private static final int sinNeg30 = (int) (lineSize * Math.sin(-d30));
  private static final int cosNeg30 = (int) (lineSize * Math.cos(-d30));

  public Inheritance() {
    }

    public Inheritance(UMLClass superClass, UMLClass subClass) {
        super("", null, superClass, subClass);
    }

    public UMLClass getSubclass() {
        return getSecondClass();
    }

    public UMLClass getSuperclass() {
        return getFirstClass();
    }

    @Override
    public String getDisplayString() {
        return "";
    }

    public int getStartX() {
        if (trianglePosition == Position.LEFT) {
            return getSuperclass().getStartX() - cos30;
        } else if (trianglePosition == Position.RIGHT) {
            return getSuperclass().getEndX() + cos30;
        }
        return getSuperclass().getCenterX();
    }

    public int getStartY() {
        switch (trianglePosition) {
            case TOP:
              return getSuperclass().getStartY() - cos30;
            case BOTTOM:
              return getSuperclass().getEndY() + cos30;
        }
        return getSuperclass().getCenterY();
    }

  //TODO a different triangle and line drawn for each subclass
  @Override
  public Shape getShape() {
    Path2D shape = new Path2D.Float();
    int xDiff = Math.abs(getSuperclass().getCenterX() - getSubclass().getCenterX());
    int yDiff = Math.abs(getSuperclass().getCenterY() - getSubclass().getCenterY());
    if (xDiff > 2 * yDiff) {
      if (getSuperclass().getStartX() > getSubclass().getEndX()) {
        trianglePosition = Position.LEFT;
        shape.moveTo(getSuperclass().getStartX(), getSuperclass().getCenterY());
        shape.lineTo(getSuperclass().getStartX() - cos30,
          getSuperclass().getCenterY() - sin30);
        shape.lineTo(getSuperclass().getStartX() - cos30,
          getSuperclass().getCenterY() - sinNeg30);
        shape.lineTo(getSuperclass().getStartX(), getSuperclass().getCenterY());
      } else if (getSuperclass().getEndX() < getSubclass().getStartX()) {
        trianglePosition = Position.RIGHT;
        shape.moveTo(getSuperclass().getEndX(), getSuperclass().getCenterY());
        shape.lineTo(getSuperclass().getEndX() + cos30,
          getSuperclass().getCenterY() + sin30);
        shape.lineTo(getSuperclass().getEndX() + cos30,
          getSuperclass().getCenterY() + sinNeg30);
        shape.lineTo(getSuperclass().getEndX(), getSuperclass().getCenterY());
      }
    } else {
      if (getSuperclass().getStartY() > getSubclass().getEndY()) {
        trianglePosition = Position.TOP;
        shape.moveTo(getSuperclass().getCenterX(), getSuperclass().getStartY());
        shape.lineTo(getSuperclass().getCenterX() - sin30,
          getSuperclass().getStartY() - cos30);// 30°
        shape.lineTo(getSuperclass().getCenterX() - sinNeg30,
          getSuperclass().getStartY() - cosNeg30);// 30°
        shape.lineTo(getSuperclass().getCenterX(), getSuperclass().getStartY());
      } else {
        trianglePosition = Position.BOTTOM;
        shape.moveTo(getSuperclass().getCenterX(), getSuperclass().getEndY());
        shape.lineTo(getSuperclass().getCenterX() + sin30,
          getSuperclass().getEndY() + cos30);// 30°
        shape.lineTo(getSuperclass().getCenterX() + sinNeg30,
          getSuperclass().getEndY() + cosNeg30);// 30°
        shape.lineTo(getSuperclass().getCenterX(), getSuperclass().getEndY());
      }
    }
    try {
      shape.closePath();
    } catch (IllegalPathStateException e) {
      return super.getShape();
    }
    shape.append(super.getShape(), false);
    return shape;
  }

    @Override
    public String getName() {
        return String.format("ISA (%s,%s)", getFirstClass().getName(), getSecondClass().getName());
    }

    public enum Position {
        TOP, BOTTOM, RIGHT, LEFT
    }

}
