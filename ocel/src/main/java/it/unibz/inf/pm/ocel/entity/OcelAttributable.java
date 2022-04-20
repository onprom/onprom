package it.unibz.inf.pm.ocel.entity;




import java.util.Set;

public interface OcelAttributable {
    OcelAttributeMap getAttributes();

    void setAttributes(OcelAttributeMap var1);

    boolean hasAttributes();

    Set<OcelExtension> getExtensions();
}
