/*
 * ocel
 *
 * OcelEventAndClassifier.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
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

package it.unibz.inf.pm.ocel.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


public class OcelEventAndClassifier extends OcelEventAttributeClassifier {
    public OcelEventAndClassifier(OcelEventClassifier... comparators) {
        super("");
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(comparators[0].name());
        Collection<String> keys = new ArrayList(Arrays.asList(comparators[0].getDefiningAttributeKeys()));

        for(int i = 1; i < comparators.length; ++i) {
            sb.append(" AND ");
            sb.append(comparators[i].name());
            keys.addAll(Arrays.asList(comparators[i].getDefiningAttributeKeys()));
        }

        sb.append(")");
        this.name = sb.toString();
        this.keys = keys.toArray(new String[0]);
    }
}

