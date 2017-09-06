/*
 * onprom-data
 *
 * ResourceAnnotationQuery.java
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
 * Class holding all queries related with resource annotation
 * <p>
 * Created by T. E. Kalayci on 14/12/16.
 */
public class ResourceAnnotationQueryV2 extends AnnotationQueryV2 {

  public ResourceAnnotationQueryV2() {
    //
  }

  public ResourceAnnotationQueryV2(String _name, String resourceAnnotationName, String valueAnsName) {
    super(_name, resourceAnnotationName, valueAnsName);
  }

  public ResourceAnnotationQueryV2(String _name, String _firstAnsVariable, String _secondAnsVariable, List<AnnotationQueryV2> queries) {
    super(_name, _firstAnsVariable, _secondAnsVariable, queries);
  }

  public String getResourceAnsName() {
    return getFirstAnsVariable();
  }

  public void setResourceAnsName(String _name) {
    setFirstAnsVariable(_name);
  }

  public String getValueAnsName() {
    return getSecondAnsVariable();
  }

  public void setValueAnsName(String _name) {
    setSecondAnsVariable(_name);
  }
}
