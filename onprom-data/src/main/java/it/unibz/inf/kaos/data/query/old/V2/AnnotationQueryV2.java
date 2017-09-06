/*
 * onprom-data
 *
 * AnnotationQuery.java
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

package it.unibz.inf.kaos.data.query.old.V2;

import java.util.List;

/**
 * Super class for annotation queries
 * <p>
 * Created by T. E. Kalayci on 14/12/16.
 * <p>
 * Modified by Ario Santoso (santoso.ario@gmail.com/santoso@inf.unibz.it) on 18/12/16 with the following modification:
 * - change the query of the class
 */
public class AnnotationQueryV2 {

  private List<AnnotationQueryV2> attributeQueries;
  private String query;

  //answer variables
  private String firstAnsVariable;
  private String secondAnsVariable;

  AnnotationQueryV2() {
    //
  }

  AnnotationQueryV2(String _name) {
    this.query = _name;
  }

  public AnnotationQueryV2(String _name, String _firstAnsVariable, String _secondAnsVariable) {
    this.query = _name;
    this.firstAnsVariable = _firstAnsVariable;
    this.secondAnsVariable = _secondAnsVariable;
  }

  public AnnotationQueryV2(String _name, String _firstAnsVariable, String _secondAnsVariable, List<AnnotationQueryV2> queries) {
    this(_name, _firstAnsVariable, _secondAnsVariable);
    this.attributeQueries = queries;
  }

  String getFirstAnsVariable() {
    return firstAnsVariable;
  }

  void setFirstAnsVariable(String firstAnsVariable) {
    this.firstAnsVariable = firstAnsVariable;
  }

  String getSecondAnsVariable() {
    return secondAnsVariable;
  }

  void setSecondAnsVariable(String secondAnsVariable) {
    this.secondAnsVariable = secondAnsVariable;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String toString() {
    return getClass().getSimpleName() + " (" + getFirstAnsVariable() + ", " + getSecondAnsVariable() + ")";
  }

  public List<AnnotationQueryV2> getAttributeQueries() {
    return attributeQueries;
  }

  public void setAttributeQueries(List<AnnotationQueryV2> queries) {
    attributeQueries = queries;
  }
}
