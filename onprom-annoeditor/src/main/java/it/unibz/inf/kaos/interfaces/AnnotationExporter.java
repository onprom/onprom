/*
 * onprom-annoeditor
 *
 * AnnotationExporter.java
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

package it.unibz.inf.kaos.interfaces;

import it.unibz.inf.kaos.data.query.old.V2.AnnotationQueriesV2;
import it.unibz.inf.kaos.ui.utility.UIUtility;
import org.apache.jena.query.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * To support different exporters
 * <p>
 * @author T. E. Kalayci on 04/04/17.
 */
public interface AnnotationExporter {
  Logger logger = LoggerFactory.getLogger(AnnotationExporter.class.getName());

  default String checkQuery(String queryStr) {
    try {
      // attempt to create the query from the string using Apache Jena
      // query factory and return the result if successful
      return QueryFactory.create(queryStr).toString();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      UIUtility.error(e.getMessage());
    }
    // in case of parsing error, return the original string
    return queryStr;
  }

  AnnotationQueriesV2 getQueries(Set<Annotation> annotations);

  //LinkedList<AnnotationQuery> getAttributeQueries(LinkedList<AnnotationAttribute> attributes);

  //void addJoin(SelectBuilder builder, Set<DiagramShape> path);

  //String getStringAttributeQuery(StringAttribute name, UMLClass relatedClass, Set<DiagramShape> casePath);
}
