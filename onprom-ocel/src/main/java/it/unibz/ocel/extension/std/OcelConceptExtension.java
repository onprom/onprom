package it.unibz.ocel.extension.std;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.factory.OcelFactory;
import it.unibz.ocel.factory.OcelFactoryRegistry;
import it.unibz.ocel.info.OcelGlobalAttributeNameMap;
import it.unibz.ocel.model.OcelAttributable;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeLiteral;
import it.unibz.ocel.model.OcelEvent;

import java.net.URI;

public class OcelConceptExtension extends OcelExtension {
    private static final long serialVersionUID = 6604751608301985546L;
    public static final URI EXTENSION_URI = URI.create("http://ocel-standard.org/");
    public static final String KEY_NAME = "concept:name";
    public static final String KEY_INSTANCE = "concept:instance";
    public static OcelAttributeLiteral ATTR_NAME;
    public static OcelAttributeLiteral ATTR_INSTANCE;
    private static transient OcelConceptExtension singleton = new OcelConceptExtension();

    public static OcelConceptExtension instance() {
        return singleton;
    }

    private Object readResolve() {
        return singleton;
    }

    private OcelConceptExtension() {
        super("Concept", "concept", EXTENSION_URI);
        OcelFactory factory = (OcelFactory) OcelFactoryRegistry.instance().currentDefault();
        ATTR_NAME = factory.createAttributeLiteral("concept:name", "__INVALID__", this);
        ATTR_INSTANCE = factory.createAttributeLiteral("concept:instance", "__INVALID__", this);
        this.logAttributes.add((OcelAttribute)ATTR_NAME.clone());
        this.traceAttributes.add((OcelAttribute)ATTR_NAME.clone());
        this.eventAttributes.add((OcelAttribute)ATTR_NAME.clone());
        this.eventAttributes.add((OcelAttribute)ATTR_INSTANCE.clone());
        OcelGlobalAttributeNameMap.instance().registerMapping("EN", "concept:name", "Name");
        OcelGlobalAttributeNameMap.instance().registerMapping("EN", "concept:instance", "Instance");
        OcelGlobalAttributeNameMap.instance().registerMapping("DE", "concept:name", "Name");
        OcelGlobalAttributeNameMap.instance().registerMapping("DE", "concept:instance", "Instanz");
        OcelGlobalAttributeNameMap.instance().registerMapping("FR", "concept:name", "Appellation");
        OcelGlobalAttributeNameMap.instance().registerMapping("FR", "concept:instance", "Entité");
        OcelGlobalAttributeNameMap.instance().registerMapping("ES", "concept:name", "Nombre");
        OcelGlobalAttributeNameMap.instance().registerMapping("ES", "concept:instance", "Instancia");
        OcelGlobalAttributeNameMap.instance().registerMapping("PT", "concept:name", "Nome");
        OcelGlobalAttributeNameMap.instance().registerMapping("PT", "concept:instance", "Instância");
    }

    public String extractName(OcelAttributable element) {
        OcelAttribute attribute = (OcelAttribute)element.getAttributes().get("concept:name");
        return attribute == null ? null : ((OcelAttributeLiteral)attribute).getValue();
    }

    public void assignName(OcelAttributable element, String name) {
        if (name != null && name.trim().length() > 0) {
            OcelAttributeLiteral attr = (OcelAttributeLiteral)ATTR_NAME.clone();
            attr.setValue(name);
            element.getAttributes().put("concept:name", attr);
        }

    }

    public String extractInstance(OcelEvent event) {
        OcelAttribute attribute = (OcelAttribute)event.getAttributes().get("concept:instance");
        return attribute == null ? null : ((OcelAttributeLiteral)attribute).getValue();
    }

    public void assignInstance(OcelEvent event, String instance) {
        if (instance != null && instance.trim().length() > 0) {
            OcelAttributeLiteral attr = (OcelAttributeLiteral)ATTR_INSTANCE.clone();
            attr.setValue(instance);
            event.getAttributes().put("concept:instance", attr);
        }

    }
}

