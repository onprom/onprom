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

package it.unibz.inf.kaos.data.query;

import java.util.List;

import org.semanticweb.owlapi.model.IRI;

/**
 * Super class for annotation queries
 * <p>
 * Created by T. E. Kalayci on 22-Jun-2017
 * 
 * <br />
 * Modified by Ario Santoso (santoso.ario@gmail.com/santoso@inf.unibz.it) on July 17 for the implementation of visitor design pattern.
 * 
 */
public abstract class AnnotationQuery {
  private List<AnnotationQuery> attributeQueries;
  private String query;
  private String targetURI;
  
  public AnnotationQuery(){}

  public AnnotationQuery(String query, IRI targetURI, List<AnnotationQuery> attributeQueries) {
    this.query = query;
    this.targetURI = targetURI.toString();
    this.attributeQueries = attributeQueries;
  }

  public AnnotationQuery(String query, IRI targetURI) {
    this.query = query;
    this.targetURI = targetURI.toString();
  }

  public AnnotationQuery(String query) {
    this.query = query;
  }

  public IRI getTargetURI() {
    return IRI.create(targetURI);
  }

  public void setTargetURI(IRI targetURI) {
    this.targetURI = targetURI.toString();
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public List<AnnotationQuery> getAttributeQueries() {
    return attributeQueries;
  }

  public void setAttributeQueries(List<AnnotationQuery> queries) {
    attributeQueries = queries;
  }

  public abstract void accept(AnnotationQueryVisitor visitor);
}

