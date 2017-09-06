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

package it.unibz.inf.kaos.data.query.old.V2;

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
public class EventAnnotationQueryV2 extends AnnotationQueryV2 {

  private EventTraceAnnotationQueryV2 eventTrace;
  private EventTimestampAnnotationQueryV2 eventTimestamp;
  private EventResourceAnnotationQueryV2 eventResource;
  private EventLifecycleAnnotationQueryV2 eventLifecycle;

  public EventAnnotationQueryV2() {
    //
  }

  public EventAnnotationQueryV2(String _name, String eventAnsVariable, String nameAnsVariable) {
    super(_name, eventAnsVariable, nameAnsVariable);
  }

  public EventAnnotationQueryV2(String _name, String eventAnsVariable, String nameAnsVariable,
                              EventTraceAnnotationQueryV2 eventTrace,
                              EventTimestampAnnotationQueryV2 eventTimestamp,
                              EventResourceAnnotationQueryV2 eventResource,
                              EventLifecycleAnnotationQueryV2 eventLifecycle) {

    this(_name, eventAnsVariable, nameAnsVariable);

    this.eventTrace = eventTrace;
    this.eventTimestamp = eventTimestamp;
    this.eventResource = eventResource;
    this.eventLifecycle = eventLifecycle;
  }

  public EventTraceAnnotationQueryV2 getEventTrace() {
    return eventTrace;
  }

  public void setEventTrace(EventTraceAnnotationQueryV2 eventTrace) {
    this.eventTrace = eventTrace;
  }

  public EventTimestampAnnotationQueryV2 getEventTimestamp() {
    return eventTimestamp;
  }

  public void setEventTimestamp(EventTimestampAnnotationQueryV2 eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }

  public EventResourceAnnotationQueryV2 getEventResource() {
    return eventResource;
  }

  public void setEventResource(EventResourceAnnotationQueryV2 eventResource) {
    this.eventResource = eventResource;
  }

  public EventLifecycleAnnotationQueryV2 getEventLifecycle() {
    return eventLifecycle;
  }

  public void setEventLifecycle(EventLifecycleAnnotationQueryV2 eventLifecycle) {
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
