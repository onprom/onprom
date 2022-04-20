/*
 * ocel
 *
 * OcelEventAttributeClassifier.java
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


import it.unibz.inf.pm.ocel.entity.OcelAttribute;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;
import it.unibz.inf.pm.ocel.entity.OcelVisitor;

import java.io.Serializable;
import java.util.Arrays;

public class OcelEventAttributeClassifier implements OcelEventClassifier, Comparable<OcelEventAttributeClassifier>, Serializable {
    private static final String CONCATENATION_SYMBOL = "+";
    private static final long serialVersionUID = -2697438317286269727L;
    protected String[] keys;
    protected String name;

    public OcelEventAttributeClassifier(String name, String... keys) {
        this.name = name;
        this.keys = keys;
    }

    public String getClassIdentity(OcelEvent event) {
        switch(this.keys.length) {
            case 1:
                OcelAttribute attr = event.getAttributes().get(this.keys[0]);
                if (attr != null) {
                    return attr.toString();
                }

                return "";
            case 2:
                OcelAttribute attr1 = event.getAttributes().get(this.keys[0]);
                OcelAttribute attr2 = event.getAttributes().get(this.keys[1]);
                if (attr1 != null && attr2 != null) {
                    String val1 = attr1.toString();
                    String val2 = attr2.toString();
                    return val1.concat("+").concat(val2);
                } else if (attr1 != null) {
                    return attr1.toString().concat("+");
                } else {
                    if (attr2 != null) {
                        return "+".concat(attr2.toString());
                    }

                    return "+";
                }
            default:
                StringBuilder sb = new StringBuilder();

                for(int i = 0; i < this.keys.length; ++i) {
                    OcelAttribute attribute = event.getAttributes().get(this.keys[i]);
                    if (attribute != null) {
                        sb.append(attribute);
                    }

                    if (i < this.keys.length - 1) {
                        sb.append("+");
                    }
                }

                return sb.toString();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public boolean sameEventClass(OcelEvent eventA, OcelEvent eventB) {
        return this.getClassIdentity(eventA).equals(this.getClassIdentity(eventB));
    }

    public String toString() {
        return this.name();
    }

    public String[] getDefiningAttributeKeys() {
        return this.keys;
    }

    public int compareTo(OcelEventAttributeClassifier o) {
        if (!o.name.equals(this.name)) {
            return this.name.compareTo(o.name);
        } else if (this.keys.length != o.keys.length) {
            return this.keys.length - o.keys.length;
        } else {
            for(int i = 0; i < this.keys.length; ++i) {
                if (!this.keys[i].equals(o.keys[i])) {
                    return this.keys[i].compareTo(o.keys[i]);
                }
            }

            return 0;
        }
    }

    public int hashCode() {
        boolean prime = true;
        int result = 1;
        result = 31 * result + Arrays.hashCode(this.keys);
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        return result;
    }

    public boolean equals(Object o) {
        if (!(o instanceof OcelEventAttributeClassifier)) {
            return false;
        } else {
            return this.compareTo((OcelEventAttributeClassifier)o) == 0;
        }
    }

    public void accept(OcelVisitor visitor, OcelLog log) {
        visitor.visitClassifierPre(this, log);
        visitor.visitClassifierPost(this, log);
    }
}

