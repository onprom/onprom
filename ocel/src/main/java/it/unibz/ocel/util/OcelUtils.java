/*
 * ocel
 *
 * OcelUtils.java
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
        return cloneAttributeWithChangedKeyWithFactory(oldAttribute, newKey, OcelFactoryRegistry.instance().currentDefault());
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

        for (OcelTrace t : traces) {
            for (OcelEvent e : t) {
                attributeKeys.addAll(e.getAttributes().keySet());
            }
        }

        return attributeKeys;
    }

    public static Map<String, Class<?>> getEventAttributeTypes(Iterable<OcelTrace> traces) {
        Map<String, Class<?>> attributeTypes = new HashMap();

        for (OcelTrace t : traces) {
            for (OcelEvent e : t) {
                for (OcelAttribute a : e.getAttributes().values()) {
                    fillAttributeType(attributeTypes, a);
                }
            }
        }

        return attributeTypes;
    }

    public static Set<String> getTraceAttributeKeys(Iterable<OcelTrace> traces) {
        Set<String> attributeKeys = new HashSet();

        for (OcelTrace t : traces) {
            attributeKeys.addAll(t.getAttributes().keySet());
        }

        return attributeKeys;
    }

    public static Map<String, Class<?>> getTraceAttributeTypes(Iterable<OcelTrace> traces) {
        Map<String, Class<?>> attributeTypes = new HashMap();

        for (OcelTrace t : traces) {
            for (OcelAttribute a : t.getAttributes().values()) {
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
