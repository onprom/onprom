/*
 * onprom-data
 *
 * CaseAnnotationQuery.java
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
 * Class holding Trace annotation queries
 * <p>
 * Created by T. E. Kalayci on 14/12/16.
 */
public class CaseAnnotationQueryV2 extends AnnotationQueryV2 {

  public CaseAnnotationQueryV2() {
  }

  public CaseAnnotationQueryV2(String _name, String traceAnsVariable, String nameAnsVariable) {
    super(_name, traceAnsVariable, nameAnsVariable);
  }

  public CaseAnnotationQueryV2(String _name, String _firstAnsVariable, String _secondAnsVariable, List<AnnotationQueryV2> queries) {
    super(_name, _firstAnsVariable, _secondAnsVariable, queries);
  }

  public String getTraceAnsVariable() {
    return getFirstAnsVariable();
  }

  public String getNameAnsVariable() {
    return getSecondAnsVariable();
  }
}
