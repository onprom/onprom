/*
 * onprom-data
 *
 * AnnotationQueries.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
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

package it.unibz.inf.onprom.data.query;

import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Container class for all annotation queries
 * <p>
 * Created by T. E. Kalayci on 14/12/16.
 * <p>
 * Modified by Ario Santoso (santoso.ario@gmail.com/santoso@inf.unibz.it) on 18/12/16 with the following modification:
 * Modified by T. E. Kalayci on 19/07/2017 with single query set to simplify change and transfer
 * - change the name of the class
 */
public class AnnotationQueries {
    private final Set<AnnotationQuery> queries = Sets.newLinkedHashSet();

  public AnnotationQueries() {
    //
  }

  public Set<AnnotationQuery> getAllQueries() {
    return queries;
  }

  public <T extends AnnotationQuery> Set<T> getQueries(Class<T> type) {
    return queries.stream()
            .filter(type::isInstance)
            .map(type::cast)
            .collect(Collectors.toSet());
  }

  public <T extends AnnotationQuery> T getQuery(Class<T> type) {
    return queries.stream()
            .filter(type::isInstance)
            .findFirst()
            .map(type::cast)
            .orElse(null);
  }

  public void addQuery(AnnotationQuery query) {
    queries.add(query);
  }

  public void addQuery(List<AnnotationQuery> _queries) {
    queries.addAll(_queries);
  }

  public int getQueryCount() {
    return queries.size();
  }
}
