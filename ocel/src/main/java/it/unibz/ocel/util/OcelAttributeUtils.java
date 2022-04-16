/*
 * ocel
 *
 * OcelAttributeUtils.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.ocel.util;

import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.factory.OcelFactory;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.id.OcelIDFactory;
import it.unibz.ocel.model.*;

import java.text.ParseException;
import java.util.HashSet;
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
            return factory.createAttributeList(key, extension);
        } else if (type.equalsIgnoreCase("CONTAINER")) {
            return factory.createAttributeContainer(key, extension);
        } else if (type.equalsIgnoreCase("LITERAL")) {
            return factory.createAttributeLiteral(key, value, extension);
        } else if (type.equalsIgnoreCase("BOOLEAN")) {
            return factory.createAttributeBoolean(key, Boolean.parseBoolean(value), extension);
        } else if (type.equalsIgnoreCase("CONTINUOUS")) {
            return factory.createAttributeContinuous(key, Double.parseDouble(value), extension);
        } else if (type.equalsIgnoreCase("DISCRETE")) {
            return factory.createAttributeDiscrete(key, Long.parseLong(value), extension);
        } else if (type.equalsIgnoreCase("TIMESTAMP")) {
            try {
                synchronized(OcelAttributeTimestamp.FORMATTER) {
                    return factory.createAttributeTimestamp(key, OcelAttributeTimestamp.FORMATTER.parseObject(value), extension);
                }
            } catch (ParseException var9) {
                throw new AssertionError("Ocel: could not parse date-time attribute. Value: " + value);
            }
        } else if (type.equalsIgnoreCase("ID")) {
            return factory.createAttributeID(key, OcelID.parse(value), extension);
        } else {
            throw new AssertionError("Ocel: could not parse attribute type!");
        }
    }

    public static Set<OcelExtension> extractExtensions(Map<String, OcelAttribute> attributeMap) {
        HashSet<OcelExtension> extensions = new HashSet();

        for (OcelAttribute attribute : attributeMap.values()) {
            OcelExtension extension = attribute.getExtension();
            if (extension != null) {
                extensions.add(extension);
            }
        }

        return extensions;
    }
}
