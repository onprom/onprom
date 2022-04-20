package it.unibz.inf.pm.ocel.info;



import it.unibz.inf.pm.ocel.entity.OcelAttribute;
import it.unibz.inf.pm.ocel.entity.OcelExtension;

import java.util.Collection;

public interface OcelAttributeInfo {
    Collection<OcelAttribute> getAttributes();

    Collection<String> getAttributeKeys();

    int getFrequency(String var1);

    int getFrequency(OcelAttribute var1);

    double getRelativeFrequency(String var1);

    double getRelativeFrequency(OcelAttribute var1);

    Collection<OcelAttribute> getAttributesForType(Class<? extends OcelAttribute> var1);

    Collection<String> getKeysForType(Class<? extends OcelAttribute> var1);

    Collection<OcelAttribute> getAttributesForExtension(OcelExtension var1);

    Collection<String> getKeysForExtension(OcelExtension var1);

    Collection<OcelAttribute> getAttributesWithoutExtension();

    Collection<String> getKeysWithoutExtension();
}

