/*
 * onprom-data
 *
 * UnaryAnnotationQuery.java
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

import org.semanticweb.owlapi.model.IRI;

/**
 * Unary annotation query class
 * <p>
 * Created by T. E. Kalayci on 22-Jun-2017
 * 
 * <br />
 * Modified by Ario Santoso (santoso.ario@gmail.com/santoso@inf.unibz.it) on July 17 for the implementation of visitor design pattern.
 * 
 */
public class UnaryAnnotationQuery extends AnnotationQuery {

  private String[] component;

  UnaryAnnotationQuery() {
    //
  }

  public UnaryAnnotationQuery(String _name, IRI _targetURI, String[] _firstAnsVariable) {
    super(_name, _targetURI);
    this.component = _firstAnsVariable;
  }

  public UnaryAnnotationQuery(String _query, IRI _targetURI, String[] _firstAnsVariable, List<AnnotationQuery> queries) {
    super(_query,_targetURI,queries);
    this.component = _firstAnsVariable;
  }

  public String[] getComponent() {
    return component;
  }

  void setComponent(String[] component) {
    this.component = component;
  }

  public String toString() {
    return String.format("%s %s", getTargetURI(), Arrays.toString(getComponent()));
  }

  public void accept(AnnotationQueryVisitor aqv){
	  aqv.visit(this);
  }

}
