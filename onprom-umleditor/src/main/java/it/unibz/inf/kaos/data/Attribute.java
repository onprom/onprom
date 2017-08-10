/*
 * onprom-umleditor
 *
 * Attribute.java
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

/**
 * @author T. E. Kalayci
 */
public class Attribute implements Cloneable {
  private String name;
  private String longName;
  private DataType type = DataType.STRING;
  private Cardinality multiplicity = Cardinality.C1_1;
  @JsonIgnore
  private java.awt.Rectangle bounds;
  @JsonIgnore
  private State state = State.NORMAL;

  public Attribute() {
  }

  public Attribute(String _name) {
    this.name = _name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLongName() {
    if (longName == null || longName.isEmpty())
      return getName();
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  public DataType getType() {
    return type;
  }

  public void setType(DataType type) {
    this.type = type;
  }

  public Cardinality getMultiplicity() {
    return multiplicity;
  }

  public void setMultiplicity(Cardinality multiplicity) {
    this.multiplicity = multiplicity;
  }

  public boolean isFunctional() {
    return multiplicity.isFunctional();
  }

  public boolean isExistential() {
    return multiplicity.isExistential();
  }

  void setBounds(java.awt.Rectangle _bounds) {
    bounds = _bounds;
  }

  public boolean contains(int x, int y) {
    return bounds.contains(x, y);
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public String toString() {
    String result = name + ": " + type;
    if (!multiplicity.equals(Cardinality.C1_1))
      return result + " [" + multiplicity + "]";
    return result;
  }

  public Attribute getClone() {
    try {
      return (Attribute) super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
}