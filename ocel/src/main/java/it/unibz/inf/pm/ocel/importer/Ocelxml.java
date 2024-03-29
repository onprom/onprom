/*
 * ocel
 *
 * Ocelxml.java
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

package it.unibz.inf.pm.ocel.importer;


import it.unibz.inf.pm.ocel.util.DateFormatUtil;
import it.unibz.inf.pm.ocel.util.XmlUtil;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

public class Ocelxml {
    public static Map apply(String input_path, String... parameters) throws DocumentException {
        XmlUtil xmlUtil = new XmlUtil();
        Element root = xmlUtil.read(new File(input_path));
        Document dom = xmlUtil.getDocument();

        HashMap<String, Object> logMap = new HashMap<>();

        List<Element> globalNodes = dom.selectNodes("//global");
        List<Attribute> globalNodeAttributes = dom.selectNodes("//global/@scope");

        // parse <global scope="event">
        Element globalEventNode = XmlUtil.parse(root, "scope", "event");
        HashMap<String, String> globalEventMap = new HashMap<>();
        List<Element> subGlobalEventNodes = globalEventNode.elements();
        for(Element emt : subGlobalEventNodes) {
            globalEventMap.put(emt.attribute("key").getValue(),emt.attribute("value").getValue());
        }
        logMap.put("ocel:global-event",globalEventMap);

        // parse <global scope="object">
        Element globalObjectNode = XmlUtil.parse(root, "scope", "object");
        HashMap<String, String> globalObjectMap = new HashMap<>();
        List<Element> subGlobalObjectNodes = globalObjectNode.elements();
        for(Element emt : subGlobalObjectNodes) {
            globalObjectMap.put(emt.attribute("key").getValue(),emt.attribute("value").getValue());
        }
        logMap.put("ocel:global-object",globalObjectMap);

        // parse <global scope="log">
        Element globalLogNode = XmlUtil.parse(root, "scope", "log");
        HashMap<String, Object> globalLogMap = new HashMap<>();
        List<Element> subGlobalLogNodes = globalLogNode.elements();
        for (Element emt : subGlobalLogNodes) {
            String value = emt.attribute("key").getValue();
            List<Element> attributNamesElments = emt.elements();
            List<String> attributeValueList = new ArrayList<>();
            List<String> objTpyeValueList = new ArrayList<>();
            switch (value) {
                case "attribute-names":
                    for (Element attrElm : attributNamesElments) {
                        attributeValueList.add(attrElm.attribute("value").getValue());
                        globalLogMap.put("ocel:attribute-names", attributeValueList);
                    }
                    break;
                case "object-types":
                    for (Element attrElm : attributNamesElments) {
                        objTpyeValueList.add(attrElm.attribute("value").getValue());
                        globalLogMap.put("ocel:object-types", objTpyeValueList);
                    }
                    break;
                case "version":
                    globalLogMap.put("ocel:version", emt.attribute("value").getValue());
                    break;
                case "ordering":
                    globalLogMap.put("ocel:ordering", emt.attribute("value").getValue());
                    break;
            }
        }
        logMap.put("ocel:global-log",globalLogMap);

        //parse the elements of <event>
        List<Element> eventsNodes = dom.selectNodes("//event");
        List<HashMap> eventMapList = new ArrayList<>();
        HashMap<String, Object> eventMapInOne = new HashMap<>();
        for (Element entElm: eventsNodes) {
            String eventKeyName = "";
            List<Element> eventElmt = entElm.elements();
            HashMap<String, Object> eventMap = new HashMap<>();
            for (Element event: eventElmt ) {
                String keyStr = event.attribute("key").getValue();
                if("id".equals(keyStr)) {
                    eventKeyName = event.attribute("value").getValue();
                }else if("timestamp".equals(keyStr)){
                    try {
                        eventMap.put("ocel:timestamp", DateFormatUtil.dealDateFormatReverse(event.attribute("value").getValue()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }else if("activity".equals(keyStr)){
                    eventMap.put("ocel:activity",event.attribute("value").getValue());
                }else if ("omap".equals(keyStr)) {
                    List<Element> omapElements = event.elements();
                    List<String> ompValueList = new ArrayList<>();
                    for (Element ompElm : omapElements) {
                        ompValueList.add(ompElm.attribute("value").getValue());
                        eventMap.put("ocel:omap", ompValueList);
                    }
                } else if ("vmap".equals(keyStr)) {
                    HashMap<String, Object> vmapMap = new HashMap<>();
                    List<Element> vmapElements = event.elements();
                    for (Element vmpElm : vmapElements) {
                        vmapMap.put(vmpElm.attribute("key").getValue(), parse_xml(vmpElm.attribute("value").getValue(), "vmpElm.getName()"));
                    }
                    eventMap.put("ocel:vmap", vmapMap);
                }
                eventMapInOne.put(eventKeyName,eventMap);
            }
            logMap.put("ocel:events",eventMapInOne);
        }

        //parse the elements of <event>
        List<Element> objectsNodes = dom.selectNodes("//object");
        HashMap<String, Object> objectMapInOne = new HashMap<>();
        for (Element objElm: objectsNodes) {
            String objectKeyName = "";
            List<Element> objElmt = objElm.elements();
            HashMap<String, Object> objectMap = new HashMap<>();
            for (Element obj: objElmt ) {
                String keyStr = obj.attribute("key").getValue();
                if("id".equals(keyStr)) {
                    objectKeyName = obj.attribute("value").getValue();
                }else if ("type".equals(keyStr)) {
                    objectMap.put("ocel:type", obj.attribute("value").getValue());
                } else if ("ovmap".equals(keyStr)) {
                    HashMap<String, Object> ovmapMap = new HashMap<>();
                    List<Element> ovmapElements = obj.elements();
                    for (Element ovmpElm : ovmapElements) {
                        ovmapMap.put(ovmpElm.attribute("key").getValue(), parse_xml(ovmpElm.attribute("value").getValue(), "vmpElm.getName()"));
                    }
                    objectMap.put("ocel:ovmap", ovmapMap);
                }
                objectMapInOne.put(objectKeyName,objectMap);
            }
            logMap.put("ocel:objects",objectMapInOne);
        }
        return logMap;
    }


    public static Object parse_xml(String value, String tag_str_lower) {
        if (tag_str_lower.contains("float")) {
            return Float.parseFloat(value);
        } else if (tag_str_lower.contains("date")) {
            Date date = new Date(value);
            Timestamp timestamp = new Timestamp(date.getTime());
            return timestamp.toString();
        }
        return value;
    }

//    public static void main(String[] args) {
//        try {
//            System.out.println(Ocelxml.apply("minimal.xmlocel"));
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
//    }


}
