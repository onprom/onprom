package it.unibz.ocel.util;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.factory.OcelFactory;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.id.OcelIDFactory;
import it.unibz.ocel.model.*;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class OcelAttributeUtils {
    public OcelAttributeUtils() {
    }

    public static Class<? extends OcelAttribute> getType(OcelAttribute attribute) {
        if (attribute instanceof OcelAttributeList) {
            return OcelAttributeList.class;
        } else if (attribute instanceof OcelAttributeContainer) {
            return OcelAttributeContainer.class;
        } else if (attribute instanceof OcelAttributeLiteral) {
            return OcelAttributeLiteral.class;
        } else if (attribute instanceof OcelAttributeBoolean) {
            return OcelAttributeBoolean.class;
        } else if (attribute instanceof OcelAttributeContinuous) {
            return OcelAttributeContinuous.class;
        } else if (attribute instanceof OcelAttributeDiscrete) {
            return OcelAttributeDiscrete.class;
        } else if (attribute instanceof OcelAttributeTimestamp) {
            return OcelAttributeTimestamp.class;
        } else if (attribute instanceof OcelAttributeID) {
            return OcelAttributeID.class;
        } else {
            throw new AssertionError("Unexpected attribute type!");
        }
    }

    public static String getTypeString(OcelAttribute attribute) {
        if (attribute instanceof OcelAttributeList) {
            return "LIST";
        } else if (attribute instanceof OcelAttributeContainer) {
            return "CONTAINER";
        } else if (attribute instanceof OcelAttributeLiteral) {
            return "LITERAL";
        } else if (attribute instanceof OcelAttributeBoolean) {
            return "BOOLEAN";
        } else if (attribute instanceof OcelAttributeContinuous) {
            return "CONTINUOUS";
        } else if (attribute instanceof OcelAttributeDiscrete) {
            return "DISCRETE";
        } else if (attribute instanceof OcelAttributeTimestamp) {
            return "TIMESTAMP";
        } else if (attribute instanceof OcelAttributeID) {
            return "ID";
        } else {
            throw new AssertionError("Unexpected attribute type!");
        }
    }

    public static OcelAttribute derivePrototype(OcelAttribute instance) {
        OcelAttribute prototype = (OcelAttribute)instance.clone();
        if (!(prototype instanceof OcelAttributeList) && !(prototype instanceof OcelAttributeContainer)) {
            if (prototype instanceof OcelAttributeLiteral) {
                ((OcelAttributeLiteral)prototype).setValue("DEFAULT");
            } else if (prototype instanceof OcelAttributeBoolean) {
                ((OcelAttributeBoolean)prototype).setValue(true);
            } else if (prototype instanceof OcelAttributeContinuous) {
                ((OcelAttributeContinuous)prototype).setValue(0.0D);
            } else if (prototype instanceof OcelAttributeDiscrete) {
                ((OcelAttributeDiscrete)prototype).setValue(0L);
            } else if (prototype instanceof OcelAttributeTimestamp) {
                ((OcelAttributeTimestamp)prototype).setValueMillis(0L);
            } else {
                if (!(prototype instanceof OcelAttributeID)) {
                    throw new AssertionError("Unexpected attribute type!");
                }

                ((OcelAttributeID)prototype).setValue(OcelIDFactory.instance().createId());
            }
        }

        return prototype;
    }

    public static OcelAttribute composeAttribute(OcelFactory factory, String key, String value, String type, OcelExtension extension) {
        type = type.trim();
        if (type.equalsIgnoreCase("LIST")) {
            OcelAttributeList attr = factory.createAttributeList(key, extension);
            return attr;
        } else if (type.equalsIgnoreCase("CONTAINER")) {
            OcelAttributeContainer attr = factory.createAttributeContainer(key, extension);
            return attr;
        } else if (type.equalsIgnoreCase("LITERAL")) {
            OcelAttributeLiteral attr = factory.createAttributeLiteral(key, value, extension);
            return attr;
        } else if (type.equalsIgnoreCase("BOOLEAN")) {
            OcelAttributeBoolean attr = factory.createAttributeBoolean(key, Boolean.parseBoolean(value), extension);
            return attr;
        } else if (type.equalsIgnoreCase("CONTINUOUS")) {
            OcelAttributeContinuous attr = factory.createAttributeContinuous(key, Double.parseDouble(value), extension);
            return attr;
        } else if (type.equalsIgnoreCase("DISCRETE")) {
            OcelAttributeDiscrete attr = factory.createAttributeDiscrete(key, Long.parseLong(value), extension);
            return attr;
        } else if (type.equalsIgnoreCase("TIMESTAMP")) {
            try {
                synchronized(OcelAttributeTimestamp.FORMATTER) {
                    OcelAttributeTimestamp attr = factory.createAttributeTimestamp(key, OcelAttributeTimestamp.FORMATTER.parseObject(value), extension);
                    return attr;
                }
            } catch (ParseException var9) {
                throw new AssertionError("OpenXES: could not parse date-time attribute. Value: " + value);
            }
        } else if (type.equalsIgnoreCase("ID")) {
            OcelAttributeID attr = factory.createAttributeID(key, OcelID.parse(value), extension);
            return attr;
        } else {
            throw new AssertionError("Ocel: could not parse attribute type!");
        }
    }

    public static Set<OcelExtension> extractExtensions(Map<String, OcelAttribute> attributeMap) {
        HashSet<OcelExtension> extensions = new HashSet();
        Iterator i$ = attributeMap.values().iterator();

        while(i$.hasNext()) {
            OcelAttribute attribute = (OcelAttribute)i$.next();
            OcelExtension extension = attribute.getExtension();
            if (extension != null) {
                extensions.add(extension);
            }
        }

        return extensions;
    }
}
