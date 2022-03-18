package it.unibz.ocel.classification;

import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelEvent;
import it.unibz.ocel.model.OcelLog;
import it.unibz.ocel.model.OcelVisitor;

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
                OcelAttribute attr = (OcelAttribute)event.getAttributes().get(this.keys[0]);
                if (attr != null) {
                    return attr.toString();
                }

                return "";
            case 2:
                OcelAttribute attr1 = (OcelAttribute)event.getAttributes().get(this.keys[0]);
                OcelAttribute attr2 = (OcelAttribute)event.getAttributes().get(this.keys[1]);
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
                    OcelAttribute attribute = (OcelAttribute)event.getAttributes().get(this.keys[i]);
                    if (attribute != null) {
                        sb.append(attribute.toString());
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

