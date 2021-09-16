package it.unibz.ocel.model;


import it.unibz.ocel.extension.OcelExtension;

import java.util.Set;

public interface OcelAttributable {
    OcelAttributeMap getAttributes();

    void setAttributes(OcelAttributeMap var1);

    boolean hasAttributes();

    Set<OcelExtension> getExtensions();
}
