/*
 * onprom-annoeditor
 *
 * ResourceAnnotation.java
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

import it.unibz.inf.kaos.data.query.old.V2.ResourceAnnotationQueryV2;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.form.ResourceForm;
import it.unibz.inf.kaos.ui.panel.AnnotationDiagramPanel;

/**
 * Resource annotation class
 * <p>
 * @author T. E. Kalayci on 09/11/16.
 */
@AnnotationProperties(type = "Resource", color = "#7ACEAA", action = AnnotationActionType.RESOURCE)
public class ResourceAnnotation extends AbstractAnnotation {
  /**
   * Could be name, role or group of RESOURCE
   */
  private StringAttribute resource;

  private ResourceAnnotation() {
  }

  public ResourceAnnotation(UMLClass relatedClass) {
    super(relatedClass);
  }

  public String toString() {
    return relatedClass.toString();
  }

  @Override
  public ResourceAnnotationQueryV2 getQuery() {
    if (getResource() != null) {
      return new ResourceAnnotationQueryV2(SimpleQueryExporter.getStringAttributeQuery(getResource(), getRelatedClass(), null), getRelatedClass().getCleanName(), "n", SimpleQueryExporter.getAttributeQueries(getAttributes()));
    }
    return null;
  }

  @Override
  public ResourceForm getForm(AnnotationDiagramPanel panel) {
    return new ResourceForm(panel, this);
  }

  public StringAttribute getResource() {
    return resource;
  }

  public void setResource(StringAttribute resource) {
    this.resource = resource;
  }
}
