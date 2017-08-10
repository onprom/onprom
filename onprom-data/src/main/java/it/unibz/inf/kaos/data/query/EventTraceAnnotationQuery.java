/*
 * onprom-data
 *
 * EventTraceAnnotationQuery.java
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

/**
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class EventTraceAnnotationQuery extends AnnotationQuery {

  public EventTraceAnnotationQuery() {
    //
  }

  public EventTraceAnnotationQuery(String query, String eventAnsVariable, String traceAnsVariable) {
    super(query);
    setFirstAnsVariable(eventAnsVariable);
    setSecondAnsVariable(traceAnsVariable);
  }

  public String getEventAnsVariable() {
    return getFirstAnsVariable();
  }

  public void setEventAnsVariable(String eventAnsVariable) {
    setFirstAnsVariable(eventAnsVariable);
  }

  public String getTraceAnsVariable() {
    return getSecondAnsVariable();
  }

  public void setTraceAnsVariable(String traceAnsVariable) {
    setSecondAnsVariable(traceAnsVariable);
  }

}
