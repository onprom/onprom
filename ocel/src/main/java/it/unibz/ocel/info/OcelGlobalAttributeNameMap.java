package it.unibz.ocel.info;

import it.unibz.ocel.info.impl.OcelAttributeNameMapImpl;
import it.unibz.ocel.model.OcelAttribute;

import java.util.*;


public class OcelGlobalAttributeNameMap implements OcelAttributeNameMap {
    public static final String MAPPING_STANDARD = "EN";
    public static final String MAPPING_ENGLISH = "EN";
    public static final String MAPPING_GERMAN = "DE";
    public static final String MAPPING_DUTCH = "NL";
    public static final String MAPPING_FRENCH = "FR";
    public static final String MAPPING_ITALIAN = "IT";
    public static final String MAPPING_SPANISH = "ES";
    public static final String MAPPING_PORTUGUESE = "PT";
    private static final OcelGlobalAttributeNameMap singleton = new OcelGlobalAttributeNameMap();
    private HashMap<String, OcelAttributeNameMapImpl> mappings = new HashMap();
    private OcelAttributeNameMapImpl standardMapping = new OcelAttributeNameMapImpl("EN");

    private OcelGlobalAttributeNameMap() {
        this.mappings.put("EN", this.standardMapping);
    }

    public static OcelGlobalAttributeNameMap instance() {
        return singleton;
    }

    public Collection<String> getAvailableMappingNames() {
        return Collections.unmodifiableCollection(this.mappings.keySet());
    }

    public Collection<OcelAttributeNameMap> getAvailableMappings() {
        HashSet<OcelAttributeNameMap> result = new HashSet();
        result.addAll(this.mappings.values());
        return Collections.unmodifiableCollection(result);
    }

    public OcelAttributeNameMap getMapping(String name) {
        OcelAttributeNameMapImpl mapping = (OcelAttributeNameMapImpl)this.mappings.get(name);
        if (mapping == null) {
            mapping = new OcelAttributeNameMapImpl(name);
            this.mappings.put(name, mapping);
        }

        return mapping;
    }

    public OcelAttributeNameMap getStandardMapping() {
        return this.standardMapping;
    }

    public String mapSafely(OcelAttribute attribute, OcelAttributeNameMap mapping) {
        return this.mapSafely(attribute.getKey(), mapping);
    }

    public String mapSafely(String attributeKey, OcelAttributeNameMap mapping) {
        String alias = null;
        if (mapping != null) {
            alias = mapping.map(attributeKey);
        }

        if (alias == null) {
            alias = this.standardMapping.map(attributeKey);
        }

        if (alias == null) {
            alias = attributeKey;
        }

        return alias;
    }

    public String mapSafely(OcelAttribute attribute, String mappingName) {
        return this.mapSafely(attribute, (OcelAttributeNameMap)this.mappings.get(mappingName));
    }

    public String mapSafely(String attributeKey, String mappingName) {
        return this.mapSafely(attributeKey, (OcelAttributeNameMap)this.mappings.get(mappingName));
    }

    public void registerMapping(String mappingName, String attributeKey, String alias) {
        OcelAttributeNameMapImpl mapping = (OcelAttributeNameMapImpl)this.getMapping(mappingName);
        mapping.registerMapping(attributeKey, alias);
    }

    public String getMappingName() {
        return "EN";
    }

    public String map(OcelAttribute attribute) {
        return this.standardMapping.map(attribute);
    }

    public String map(String attributeKey) {
        return this.standardMapping.map(attributeKey);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Global attribute name map.\n\nContained maps:\n\n");
        Iterator i$ = this.mappings.values().iterator();

        while(i$.hasNext()) {
            OcelAttributeNameMapImpl map = (OcelAttributeNameMapImpl)i$.next();
            sb.append(map.toString());
            sb.append("\n\n");
        }

        return sb.toString();
    }
}
