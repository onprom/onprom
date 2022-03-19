/*
 * onprom-umleditor
 *
 * Disjoint.java
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

package it.unibz.inf.onprom.data;

import it.unibz.inf.onprom.ui.utility.DrawingUtility;

import java.awt.*;

/**
 * @author T. E. Kalayci
 */
public class Disjoint extends Relationship {
    private static final String DISJOINT = "{disjoint}";

  public Disjoint() {
  }

  public Disjoint(UMLClass firstClass, UMLClass secondClass) {
    super(DISJOINT, null, firstClass, secondClass);
  }

  @Override
  public String getDisplayString() {
    return DISJOINT;
  }

  @Override
  public Stroke getStroke() {
      return DrawingUtility.DISJOINT_STROKE;
  }

  @Override
  public String getName() {
    return String.format("DISJOINT (%s,%s)", getFirstClass().getName(), getSecondClass().getName());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Disjoint) {
      Disjoint other = (Disjoint) obj;
        return other.getFirstClass().equals(getFirstClass()) && other.getSecondClass().equals(getSecondClass()) || other.getFirstClass().equals(getSecondClass()) && other.getSecondClass().equals(getFirstClass());
    }
    return false;
  }
}
