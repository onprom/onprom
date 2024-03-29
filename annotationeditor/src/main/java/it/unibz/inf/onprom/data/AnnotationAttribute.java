/*
 * onprom-annoeditor
 *
 * AnnotationAttribute.java
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

/**
 * Class to store additional attribute information for annotations
 * <p>
 * @author T. E. Kalayci on 15/02/17.
 */
public class AnnotationAttribute {
  private String name;
  private String type;
  private StringAttribute value;

  AnnotationAttribute() {

  }

  public AnnotationAttribute(String _name, StringAttribute _value) {
    this.name = _name;
    this.value = _value;
    this.type = "literal";
  }

  public String getName() {
    return name;
  }

  public StringAttribute getValue() {
    return value;
  }

  public String getType() {
    return type;
  }

  public void setType(String _type) {
    type = _type;
  }

  public String toString() {
    return "name:" + name + " value:" + value;
  }
}
