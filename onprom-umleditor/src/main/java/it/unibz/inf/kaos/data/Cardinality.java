/*
 * onprom-umleditor
 *
 * Cardinality.java
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

package it.unibz.inf.kaos.data;

/**
 * Cardinality class which is used to stored multiplicities both for attributes
 * and relations.
 * <p>
 * @author T. E. Kalayci
 * 06-Oct-16
 */
public enum Cardinality {
    C1_1("1..1", true, true),
    C0_1("0..1", false, true),
    C0_S("0..*", false, false),
    C1_S("1..*", true, false);

    private final String representation;
    private final boolean existential;
  private final boolean functional;

    Cardinality(final String _representation, final boolean _existential, final boolean _functional) {
        representation = _representation;
        existential = _existential;
      functional = _functional;
    }

    public static Cardinality get(boolean existential, boolean functional) {
        if (existential && functional) {
            return C1_1;
        }
        if (existential) {
            return C1_S;
        }
        if (functional) {
            return C0_1;
        }
        return C0_S;
    }

    public boolean isFunctional() {
        return functional;
    }

    public boolean isExistential() {
        return existential;
    }

    @Override
    public String toString() {
        return representation;
    }
}
