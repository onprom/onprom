package it.unibz.ocel.extension.std;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.factory.OcelFactory;
import it.unibz.ocel.factory.OcelFactoryRegistry;
import it.unibz.ocel.info.OcelGlobalAttributeNameMap;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeLiteral;
import it.unibz.ocel.model.OcelEvent;

import java.net.URI;

public class OcelOrganizationalExtension extends OcelExtension {
    private static final long serialVersionUID = -8578385457800103461L;
    public static final URI EXTENSION_URI = URI.create("http://www.ocel-standard.org");
    public static final String KEY_RESOURCE = "org:resource";
    public static final String KEY_ROLE = "org:role";
    public static final String KEY_GROUP = "org:group";
    public static OcelAttributeLiteral ATTR_RESOURCE;
    public static OcelAttributeLiteral ATTR_ROLE;
    public static OcelAttributeLiteral ATTR_GROUP;
    private static OcelOrganizationalExtension singleton = new OcelOrganizationalExtension();

    public static OcelOrganizationalExtension instance() {
        return singleton;
    }

    private Object readResolve() {
        return singleton;
    }

    private OcelOrganizationalExtension() {
        super("Organizational", "org", EXTENSION_URI);
        OcelFactory factory = (OcelFactory) OcelFactoryRegistry.instance().currentDefault();
        ATTR_RESOURCE = factory.createAttributeLiteral("org:resource", "__INVALID__", this);
        ATTR_ROLE = factory.createAttributeLiteral("org:role", "__INVALID__", this);
        ATTR_GROUP = factory.createAttributeLiteral("org:group", "__INVALID__", this);
        this.eventAttributes.add((OcelAttribute)ATTR_RESOURCE.clone());
        this.eventAttributes.add((OcelAttribute)ATTR_ROLE.clone());
        this.eventAttributes.add((OcelAttribute)ATTR_GROUP.clone());
        OcelGlobalAttributeNameMap.instance().registerMapping("EN", "org:resource", "Resource");
        OcelGlobalAttributeNameMap.instance().registerMapping("EN", "org:role", "Role");
        OcelGlobalAttributeNameMap.instance().registerMapping("EN", "org:group", "Group");
        OcelGlobalAttributeNameMap.instance().registerMapping("DE", "org:resource", "Akteur");
        OcelGlobalAttributeNameMap.instance().registerMapping("DE", "org:role", "Rolle");
        OcelGlobalAttributeNameMap.instance().registerMapping("DE", "org:group", "Gruppe");
        OcelGlobalAttributeNameMap.instance().registerMapping("FR", "org:resource", "Agent");
        OcelGlobalAttributeNameMap.instance().registerMapping("FR", "org:role", "Rôle");
        OcelGlobalAttributeNameMap.instance().registerMapping("FR", "org:group", "Groupe");
        OcelGlobalAttributeNameMap.instance().registerMapping("ES", "org:resource", "Recurso");
        OcelGlobalAttributeNameMap.instance().registerMapping("ES", "org:role", "Papel");
        OcelGlobalAttributeNameMap.instance().registerMapping("ES", "org:group", "Grupo");
        OcelGlobalAttributeNameMap.instance().registerMapping("PT", "org:resource", "Recurso");
        OcelGlobalAttributeNameMap.instance().registerMapping("PT", "org:role", "Papel");
        OcelGlobalAttributeNameMap.instance().registerMapping("PT", "org:group", "Grupo");
    }

    public String extractResource(OcelEvent event) {
        OcelAttribute attribute = (OcelAttribute)event.getAttributes().get("org:resource");
        return attribute == null ? null : ((OcelAttributeLiteral)attribute).getValue();
    }

    public void assignResource(OcelEvent event, String resource) {
        if (resource != null && resource.trim().length() > 0) {
            OcelAttributeLiteral attr = (OcelAttributeLiteral)ATTR_RESOURCE.clone();
            attr.setValue(resource.trim());
            event.getAttributes().put("org:resource", attr);
        }

    }

    public String extractRole(OcelEvent event) {
        OcelAttribute attribute = (OcelAttribute)event.getAttributes().get("org:role");
        return attribute == null ? null : ((OcelAttributeLiteral)attribute).getValue();
    }

    public void assignRole(OcelEvent event, String role) {
        if (role != null && role.trim().length() > 0) {
            OcelAttributeLiteral attr = (OcelAttributeLiteral)ATTR_ROLE.clone();
            attr.setValue(role.trim());
            event.getAttributes().put("org:role", attr);
        }

    }

    public String extractGroup(OcelEvent event) {
        OcelAttribute attribute = (OcelAttribute)event.getAttributes().get("org:group");
        return attribute == null ? null : ((OcelAttributeLiteral)attribute).getValue();
    }

    public void assignGroup(OcelEvent event, String group) {
        if (group != null && group.trim().length() > 0) {
            OcelAttributeLiteral attr = (OcelAttributeLiteral)ATTR_GROUP.clone();
            attr.setValue(group.trim());
            event.getAttributes().put("org:group", attr);
        }

    }
}

