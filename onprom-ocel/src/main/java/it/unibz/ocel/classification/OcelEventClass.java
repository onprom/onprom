package it.unibz.ocel.classification;

public class OcelEventClass implements Comparable<OcelEventClass> {
    protected int index;
    protected String id;
    protected int size;

    public OcelEventClass(String id, int index) {
        this.id = id;
        this.index = index;
        this.size = 0;
    }

    public String getId() {
        return this.id;
    }

    public int getIndex() {
        return this.index;
    }

    public int size() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void incrementSize() {
        ++this.size;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return o instanceof OcelEventClass ? this.id.equals(((OcelEventClass)o).id) : false;
        }
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return this.id;
    }

    public int compareTo(OcelEventClass o) {
        return this.id.compareTo(o.getId());
    }
}
