/*
 * onprom-data
 *
 * BinaryAnnotationQuery.java
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

package it.unibz.inf.kaos.data.query;

import java.util.Arrays;
import java.util.List;

/**
 * Binary annotation query class
 * <p>
 * Created by T. E. Kalayci on 22-Jun-2017
 * 
 * <br />
 * Modified by Ario Santoso (santoso.ario@gmail.com/santoso@inf.unibz.it) on July 17 for the implementation of visitor design pattern.
 * 
 */
public class BinaryAnnotationQuery extends AnnotationQuery {

  private String[] firstComponent;
  private String[] secondComponent;

  BinaryAnnotationQuery() {
    //
  }

    public BinaryAnnotationQuery(String _query, String _targetURI, String[] _firstAnsVariable, String[] _secondAnsVariable) {
    super(_query,_targetURI);
    this.firstComponent = _firstAnsVariable;
    this.secondComponent = _secondAnsVariable;
  }

    public BinaryAnnotationQuery(String _query, String _targetURI, String[] _firstAnsVariable, String[] _secondAnsVariable, List<AnnotationQuery> queries) {
    this(_query,_targetURI, _firstAnsVariable, _secondAnsVariable);
    setAttributeQueries(queries);
  }

  public String[] getFirstComponent() {
    return firstComponent;
  }

  void setFirstComponent(String[] firstComponent) {
    this.firstComponent = firstComponent;
  }

  public String[] getSecondComponent() {
    return secondComponent;
  }

  void setSecondComponent(String[] secondComponent) {
    this.secondComponent = secondComponent;
  }

  public String toString() {
      return String.format("%s %s %s", getTargetIRI(), Arrays.toString(getFirstComponent()), Arrays.toString(getSecondComponent()));
  }

  public void accept(AnnotationQueryVisitor aqv){
	  aqv.visit(this);
  }

}
