package it.unibz.ocel.util;

import it.unibz.ocel.extension.std.OcelConceptExtension;
import it.unibz.ocel.factory.OcelFactory;
import it.unibz.ocel.factory.OcelFactoryRegistry;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.model.*;

import java.util.*;


public final class OcelUtils {
    private OcelUtils() {
    }

    public static String getConceptName(OcelAttributable element) {
        return OcelConceptExtension.instance().extractName(element);
    }

    public static OcelAttribute cloneAttributeWithChangedKey(OcelAttribute oldAttribute, String newKey) {
        return cloneAttributeWithChangedKeyWithFactory(oldAttribute, newKey, (OcelFactory) OcelFactoryRegistry.instance().currentDefault());
    }

    public static OcelAttribute cloneAttributeWithChangedKeyWithFactory(OcelAttribute oldAttribute, String newKey, OcelFactory factory) {
        Iterator var4;
        OcelAttribute a;
        if (oldAttribute instanceof OcelAttributeList) {
            OcelAttributeList newAttribute = factory.createAttributeList(newKey, oldAttribute.getExtension());
            var4 = ((OcelAttributeList)oldAttribute).getCollection().iterator();

            while(var4.hasNext()) {
                a = (OcelAttribute)var4.next();
                newAttribute.addToCollection(a);
            }

            return newAttribute;
        } else if (!(oldAttribute instanceof OcelAttributeContainer)) {
            if (oldAttribute instanceof OcelAttributeLiteral) {
                return factory.createAttributeLiteral(newKey, ((OcelAttributeLiteral)oldAttribute).getValue(), oldAttribute.getExtension());
            } else if (oldAttribute instanceof OcelAttributeBoolean) {
                return factory.createAttributeBoolean(newKey, ((OcelAttributeBoolean)oldAttribute).getValue(), oldAttribute.getExtension());
            } else if (oldAttribute instanceof OcelAttributeContinuous) {
                return factory.createAttributeContinuous(newKey, ((OcelAttributeContinuous)oldAttribute).getValue(), oldAttribute.getExtension());
            } else if (oldAttribute instanceof OcelAttributeDiscrete) {
                return factory.createAttributeDiscrete(newKey, ((OcelAttributeDiscrete)oldAttribute).getValue(), oldAttribute.getExtension());
            } else if (oldAttribute instanceof OcelAttributeTimestamp) {
                return factory.createAttributeTimestamp(newKey, ((OcelAttributeTimestamp)oldAttribute).getValue(), oldAttribute.getExtension());
            } else if (oldAttribute instanceof OcelAttributeID) {
                return factory.createAttributeID(newKey, ((OcelAttributeID)oldAttribute).getValue(), oldAttribute.getExtension());
            } else {
                throw new IllegalArgumentException("Unexpected attribute type!");
            }
        } else {
            OcelAttributeContainer newAttribute = factory.createAttributeContainer(newKey, oldAttribute.getExtension());
            var4 = ((OcelAttributeContainer)oldAttribute).getCollection().iterator();

            while(var4.hasNext()) {
                a = (OcelAttribute)var4.next();
                newAttribute.addToCollection(a);
            }

            return newAttribute;
        }
    }

    public static Class<?> getAttributeClass(OcelAttribute attribute) {
        if (attribute instanceof OcelAttributeLiteral) {
            return String.class;
        } else if (attribute instanceof OcelAttributeBoolean) {
            return Boolean.class;
        } else if (attribute instanceof OcelAttributeContinuous) {
            return Double.class;
        } else if (attribute instanceof OcelAttributeDiscrete) {
            return Long.class;
        } else if (attribute instanceof OcelAttributeTimestamp) {
            return Date.class;
        } else if (attribute instanceof OcelAttributeID) {
            return OcelID.class;
        } else {
            throw new IllegalArgumentException("Unexpected attribute type!");
        }
    }

    public static Set<String> getEventAttributeKeys(Iterable<OcelTrace> traces) {
        Set<String> attributeKeys = new HashSet();
        Iterator var2 = traces.iterator();

        while(var2.hasNext()) {
            OcelTrace t = (OcelTrace)var2.next();
            Iterator var4 = t.iterator();

            while(var4.hasNext()) {
                OcelEvent e = (OcelEvent)var4.next();
                attributeKeys.addAll(e.getAttributes().keySet());
            }
        }

        return attributeKeys;
    }

    public static Map<String, Class<?>> getEventAttributeTypes(Iterable<OcelTrace> traces) {
        Map<String, Class<?>> attributeTypes = new HashMap();
        Iterator var2 = traces.iterator();

        while(var2.hasNext()) {
            OcelTrace t = (OcelTrace)var2.next();
            Iterator var4 = t.iterator();

            while(var4.hasNext()) {
                OcelEvent e = (OcelEvent)var4.next();
                Iterator var6 = e.getAttributes().values().iterator();

                while(var6.hasNext()) {
                    OcelAttribute a = (OcelAttribute)var6.next();
                    fillAttributeType(attributeTypes, a);
                }
            }
        }

        return attributeTypes;
    }

    public static Set<String> getTraceAttributeKeys(Iterable<OcelTrace> traces) {
        Set<String> attributeKeys = new HashSet();
        Iterator var2 = traces.iterator();

        while(var2.hasNext()) {
            OcelTrace t = (OcelTrace)var2.next();
            attributeKeys.addAll(t.getAttributes().keySet());
        }

        return attributeKeys;
    }

    public static Map<String, Class<?>> getTraceAttributeTypes(Iterable<OcelTrace> traces) {
        Map<String, Class<?>> attributeTypes = new HashMap();
        Iterator var2 = traces.iterator();

        while(var2.hasNext()) {
            OcelTrace t = (OcelTrace)var2.next();
            Iterator var4 = t.getAttributes().values().iterator();

            while(var4.hasNext()) {
                OcelAttribute a = (OcelAttribute)var4.next();
                fillAttributeType(attributeTypes, a);
            }
        }

        return attributeTypes;
    }

    private static void fillAttributeType(Map<String, Class<?>> attributeTypes, OcelAttribute attribute) {
        if (!attributeTypes.containsKey(attribute.getKey())) {
            attributeTypes.put(attribute.getKey(), getAttributeClass(attribute));
        }

    }
}
