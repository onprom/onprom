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

package it.unibz.inf.pm.ocel.exporter;


import it.unibz.inf.pm.ocel.util.DateFormatUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Ocelxml {

    public static String getType(String type) {
        String typeLower = type.toLowerCase();
        if (typeLower.contains("float") || typeLower.contains("double")) {
            return "Float";
        } else if (typeLower.contains("object")) {
            return "String";
        } else {
            return "String";
        }
    }


    public static void apply(Map<String,Object> log, String output_path, String ... parameters) throws DocumentException {
        String prefix = "ocel:";
        Document document = DocumentHelper.createDocument();
        //create root node
        Element root = document.addElement("log");

        //create <global scope="event"> node
        Element global_event = root.addElement("global");
        global_event.addAttribute("scope", "event");
        global_event.setName("global");

        //create <global scope="object"> node
        Element global_object = root.addElement("global");
        global_object.addAttribute("scope", "object");
        global_object.setName("global");

        //create <global scope="log"> node
        Element global_log = root.addElement("global");
        global_log.addAttribute("scope", "log");
        global_log.setName("global");

        //create <global scope="log"> <list key="attribute-names"></global>
        Element attribute_names = global_log.addElement("list");
        attribute_names.addAttribute("key", "attribute-names");
        attribute_names.setName("list");

        //create <global scope="log"> <list key="object-types"></global>
        Element object_types = global_log.addElement("list");
        object_types.addAttribute("key", "object-types");
        object_types.setName("list");

        //create <events> node
        Element events = root.addElement("events");
        events.setName("events");

        //create <objects> node
        Element objects = root.addElement("objects");
        objects.setName("objects");

        for(String key : log.keySet()) {
            if((prefix+"global-event").equals(key)) {
                Map<String,String> global_eventMap = (Map<String, String>) log.get(key);
                for (String stringkey : global_eventMap.keySet()) {
                    String value = global_eventMap.get(stringkey);
                    Element global_event_string = global_event.addElement("string");
                    global_event_string.addAttribute("key", stringkey);
                    global_event_string.addAttribute("value", value);
                    global_event_string.setName("string");
                }
            }

            if((prefix + "global-object").equals(key)) {
                Map<String, String> global_objectMap = (Map<String, String>) log.get(key);
                for (String stringkey : global_objectMap.keySet()) {
                    String value = global_objectMap.get(stringkey);
                    Element global_object_string = global_object.addElement("string");
                    global_object_string.addAttribute("key", stringkey);
                    global_object_string.addAttribute("value", value);
                    global_object_string.setName("string");
                }
            }

            if ((prefix + "global-log").equals(key)) {
                Map<String, Object> global_logMap = (Map<String, Object>) log.get(key);
                for (String stringkey : global_logMap.keySet()) {
                    if ((prefix + "attribute-names").equals(stringkey)) {
                        List attributeNames = (ArrayList) global_logMap.get(stringkey);
                        for (Object attr : attributeNames) {
                            Element global_log_string = attribute_names.addElement("string");
                            global_log_string.addAttribute("key", "attribute-name");
                            global_log_string.addAttribute("value", attr.toString());
                            global_log_string.setName("string");
                        }
                    } else if ((prefix + "object-types").equals(stringkey)) {
                        List objectTypes = (ArrayList) global_logMap.get(stringkey);
                        for (Object attr : objectTypes) {
                            Element global_log_string = object_types.addElement("string");
                            global_log_string.addAttribute("key", "object-type");
                            global_log_string.addAttribute("value", attr.toString());
                            global_log_string.setName("string");
                        }
                    } else {
                        String value = (String) global_logMap.get(stringkey);
                        stringkey = stringkey.substring(stringkey.indexOf(prefix) + prefix.length());
                        Element global_log_string = global_log.addElement("string");
                        global_log_string.addAttribute("key", stringkey);
                        global_log_string.addAttribute("value", value);
                        global_log_string.setName("string");
                    }
                }
            }

            if((prefix + "events").equals(key)) {
                Map<String,Object> eventsMap = (Map<String, Object>) log.get(key);
                for (String s : eventsMap.keySet()) {
                    //create <event> node
                    Element event = events.addElement("event");
                    event.setName("event");
                    Element eventId = event.addElement("string");
                    eventId.setName("string");
                    eventId.addAttribute("key", "id");
                    eventId.addAttribute("value", s);
                    Map<String, Object> eventMap = (Map<String, Object>) eventsMap.get(s);
                    for (String stringkey : eventMap.keySet()) {
                        if ((prefix + "omap").equals(stringkey)) {
                            //create <event></list> node
                            Element eventList = event.addElement("list");
                            eventList.addAttribute("key", "omap");
                            eventList.setName("list");
                            List objectIds = (ArrayList) eventMap.get(stringkey);
                            for (Object attr : objectIds) {
                                Element event_list_string = eventList.addElement("string");
                                event_list_string.addAttribute("key", "object-id");
                                event_list_string.addAttribute("value", attr.toString());
                                event_list_string.setName("string");
                            }
                        } else if ((prefix + "vmap").equals(stringkey)) {
                            //create <event></list> node
                            Element eventList = event.addElement("list");
                            eventList.addAttribute("key", "vmap");
                            eventList.setName("list");
                            Map<String, String> vMap = (Map<String, String>) eventMap.get(stringkey);
                            for (String itemkey : vMap.keySet()) {
                                Object value = vMap.get(itemkey);
                                Element event_list_string = eventList.addElement("string");
                                event_list_string.addAttribute("key", itemkey);
                                event_list_string.addAttribute("value", value.toString());
                                event_list_string.setName("string");
                            }
                        } else if ((prefix + "timestamp").equals(stringkey)) {
                            //create <event></list> node
                            Element timestamp = event.addElement("date");
                            timestamp.addAttribute("key", "timestamp");
                            try {
                                timestamp.addAttribute("value", DateFormatUtil.dealDateFormatReverse(eventMap.get(stringkey).toString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            timestamp.setName("date");
                        } else {
                            String value = (String) eventMap.get(stringkey);
                            stringkey = stringkey.substring(stringkey.indexOf(prefix) + prefix.length());
                            Element orther_string = event.addElement("string");
                            orther_string.addAttribute("key", stringkey);
                            orther_string.addAttribute("value", value);
                            orther_string.setName("string");
                        }
                    }
                }
            }

            if((prefix + "objects").equals(key)) {
                Map<String,Object> objectsMap = (Map<String, Object>) log.get(key);
                for (String s : objectsMap.keySet()) {
                    //create <event> node
                    Element object = objects.addElement("object");
                    object.setName("object");
                    Element eventId = object.addElement("string");
                    eventId.setName("string");
                    eventId.addAttribute("key", "id");
                    eventId.addAttribute("value", s);
                    Map<String, Object> eventMap = (Map<String, Object>) objectsMap.get(s);
                    for (String stringkey : eventMap.keySet()) {
                        if ((prefix + "ovmap").equals(stringkey)) {
                            //create <event></list> node
                            Element eventList = object.addElement("list");
                            eventList.addAttribute("key", "ovmap");
                            eventList.setName("list");
                            Map<String, String> vMap = (Map<String, String>) eventMap.get(stringkey);
                            for (String itemkey : vMap.keySet()) {
                                String value = String.valueOf(vMap.get(itemkey));
                                Element event_list_string = eventList.addElement("string");
                                event_list_string.addAttribute("key", itemkey);
                                event_list_string.addAttribute("value", value);
                                event_list_string.setName("string");
                            }
                        } else if ((prefix + "timestamp").equals(stringkey)) {
                            //create <event></list> node
                            Element timestamp = object.addElement("date");
                            timestamp.addAttribute("key", "timestamp");
                            try {
                                timestamp.addAttribute("value", DateFormatUtil.dealDateFormatReverse(eventMap.get(stringkey).toString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            timestamp.setName("date");
                        } else {
                            String value = (String) eventMap.get(stringkey);
                            stringkey = stringkey.substring(stringkey.indexOf(prefix) + prefix.length());
                            Element orther_string = object.addElement("string");
                            orther_string.addAttribute("key", stringkey);
                            orther_string.addAttribute("value", value);
                            orther_string.setName("string");
                        }
                    }
                }
            }
        }

        try {
            File file = new File(output_path);
            OutputFormat format = new OutputFormat("\t",true);
            format.setTrimText(false);
            //format.setLineSeparator("\\r\\n");
            XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(file),format);
            xmlWriter.write(document);
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
