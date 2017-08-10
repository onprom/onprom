/*
 * onprom-data
 *
 * EventAnnotationQuery.java
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
 * Class holding all queries of event annotations
 * <p>
 * Created by T. E. Kalayci on 14/12/16.
 * <p>
 * Modified by Ario Santoso (santoso.ario@gmail.com/santoso@inf.unibz.it) on 18/12/16 with the following modifications:
 * - Change the name of the class
 * - Adding answer variables information
 * - Change the way how we store the annotation queries for Trace, Timestamp, Resource, and Lifecycle
 */
public class EventAnnotationQuery extends AnnotationQuery {

  private EventTraceAnnotationQuery eventTrace;
  private EventTimestampAnnotationQuery eventTimestamp;
  private EventResourceAnnotationQuery eventResource;
  private EventLifecycleAnnotationQuery eventLifecycle;

  public EventAnnotationQuery() {
    //
  }

  public EventAnnotationQuery(String _name, String eventAnsVariable, String nameAnsVariable) {
    super(_name, eventAnsVariable, nameAnsVariable);
  }

  public EventAnnotationQuery(String _name, String eventAnsVariable, String nameAnsVariable,
                              EventTraceAnnotationQuery eventTrace,
                              EventTimestampAnnotationQuery eventTimestamp,
                              EventResourceAnnotationQuery eventResource,
                              EventLifecycleAnnotationQuery eventLifecycle) {

    this(_name, eventAnsVariable, nameAnsVariable);

    this.eventTrace = eventTrace;
    this.eventTimestamp = eventTimestamp;
    this.eventResource = eventResource;
    this.eventLifecycle = eventLifecycle;
  }

  public EventTraceAnnotationQuery getEventTrace() {
    return eventTrace;
  }

  public void setEventTrace(EventTraceAnnotationQuery eventTrace) {
    this.eventTrace = eventTrace;
  }

  public EventTimestampAnnotationQuery getEventTimestamp() {
    return eventTimestamp;
  }

  public void setEventTimestamp(EventTimestampAnnotationQuery eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }

  public EventResourceAnnotationQuery getEventResource() {
    return eventResource;
  }

  public void setEventResource(EventResourceAnnotationQuery eventResource) {
    this.eventResource = eventResource;
  }

  public EventLifecycleAnnotationQuery getEventLifecycle() {
    return eventLifecycle;
  }

  public void setEventLifecycle(EventLifecycleAnnotationQuery eventLifecycle) {
    this.eventLifecycle = eventLifecycle;
  }

  public String getEventAnsVariable() {
    return getFirstAnsVariable();
  }

  private void setEventAnsVariable(String eventAnsVariable) {
    setFirstAnsVariable(eventAnsVariable);
  }

  public String getNameAnsVariable() {
    return getSecondAnsVariable();
  }

  private void setNameAnsVariable(String nameAnsVariable) {
    setSecondAnsVariable(nameAnsVariable);
  }
}
