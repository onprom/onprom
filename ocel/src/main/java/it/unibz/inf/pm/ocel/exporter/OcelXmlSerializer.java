package it.unibz.inf.pm.ocel.exporter;

import it.unibz.inf.pm.ocel.entity.OcelAttribute;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;
import it.unibz.inf.pm.ocel.entity.OcelObject;
import it.unibz.inf.pm.ocel.logging.OcelLogging;
import org.deckfour.xes.util.XsDateTimeConversion;
import org.deckfour.xes.util.XsDateTimeConversionJava7;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OcelXmlSerializer {
    protected XsDateTimeConversion xsDateTimeConversion = new XsDateTimeConversionJava7();
    private String prefix = "ocel:";

    public OcelXmlSerializer() {
    }

    public String getDescription() {
        return "OCEL XML Serialization";
    }

    public String getName() {
        return "OCEL XML";
    }

    public String getAuthor() {
        return "Unibz";
    }

    public String[] getSuffices() {
        return new String[]{"ocel"};
    }

    public String removePrefix(String str, String prefix) {
        int i = str.indexOf(prefix);
        return str.substring(i + prefix.length());
    }


    public void serialize(OcelLog log, OutputStream out) throws IOException, DocumentException {
        OcelLogging.log("start serializing log to OCEL.XML", OcelLogging.Importance.DEBUG);
        long start = System.currentTimeMillis();

        String prefix = "ocel:";
        Document document = DocumentHelper.createDocument();
        document.addComment("This file has been generated with the Ocel library. It conforms");
        document.addComment("to the XML serialization of the OCEl standard for log storage and");
        document.addComment("management.");
        document.addComment("OCEL standard version: 1.0");
        document.addComment("OCEL standard is available from http://ocel-standard.org/");
        //create root node
        Element root = document.addElement("log");

        //create <global scope="log"> node
        Element global_log = root.addElement("global");
        global_log.addAttribute("scope", "log");
        global_log.setName("global");

        //create <global scope="event"> node
        Element global_event = root.addElement("global");
        global_event.addAttribute("scope", "event");
        global_event.setName("global");

        //create <global scope="object"> node
        Element global_object = root.addElement("global");
        global_object.addAttribute("scope", "object");
        global_object.setName("global");

        //create <global scope="log"> <list key="attribute-names"></global>
        Element vesionInfo = global_log.addElement("string");
        vesionInfo.addAttribute("key", "version");
        vesionInfo.addAttribute("value", "1.0");
        vesionInfo.setName("string");

        //create <global scope="log"> <list key="attribute-names"></global>
        Element attribute_names = global_log.addElement("list");
        attribute_names.addAttribute("key", "attribute-names");
        attribute_names.setName("list");

        //create <global scope="log"> <list key="object-types"></global>
        Element object_types = global_log.addElement("list");
        object_types.addAttribute("key", "object-types");
        object_types.setName("list");


        //loop for global tag
        Map<String, Object> global_logMap = log.getGlobalLog();

        for (String stringkey : global_logMap.keySet()) {
//            if ((prefix + "version").equals(stringkey)) {
//                Element global_log_string = global_log.addElement("string");
//                global_log_string.addAttribute("key", "version");
//                global_log_string.addAttribute("value", global_logMap.get(stringkey).toString());
//                global_log_string.setName("string");
//            }
            if ((prefix + "attribute-names").equals(stringkey)) {
                List attributeNames = (ArrayList) global_logMap.get(stringkey);
                for (Object attr : attributeNames) {
                    Element global_log_string = attribute_names.addElement("string");
                    global_log_string.addAttribute("key", "attribute-name");
                    global_log_string.addAttribute("value", attr.toString());
                    global_log_string.setName("string");
                }
            }
            if ((prefix + "object-types").equals(stringkey)) {
                List objectTypes = (ArrayList) global_logMap.get(stringkey);
                for (Object attr : objectTypes) {
                    Element global_log_string = object_types.addElement("string");
                    global_log_string.addAttribute("key", "object-type");
                    global_log_string.addAttribute("value", attr.toString());
                    global_log_string.setName("string");
                }
            }
            if ((prefix + "global-event").equals(stringkey)) {
                writeXMLElementsFromMap(global_event, global_logMap, stringkey);
            }
            if ((prefix + "global-object").equals(stringkey)) {
                writeXMLElementsFromMap(global_object, global_logMap, stringkey);
            }
        }

        //create <events> node
        Element events = root.addElement("events");
        events.setName("events");

        //loop for events tag
        Map<String, OcelEvent> eventsMap = log.getEvents();
        for (String s : eventsMap.keySet()) {
            OcelEvent ocelEvent = eventsMap.get(s);
            Element event = events.addElement("event");
            event.setName("event");
            //write eventId
            Element eventId = event.addElement("string");
            eventId.setName("string");
            eventId.addAttribute("key", "id");
            eventId.addAttribute("value", ocelEvent.getId());
            //write eventActivity
            Element eventActivity = event.addElement("string");
            eventActivity.setName("string");
            eventActivity.addAttribute("key", "activity");
            eventActivity.addAttribute("value", ocelEvent.getActivity());
            //write eventTimestamp
            Element eventTimestamp = event.addElement("date");
            eventTimestamp.setName("date");
            eventTimestamp.addAttribute("key", "timestamp");
            eventTimestamp.addAttribute("value", ocelEvent.getTimestamp());
            //write omap
            List<String> omap = ocelEvent.getOmap();
            //create <event></list> node
            Element eventOmapList = event.addElement("list");
            eventOmapList.addAttribute("key", "omap");
            eventOmapList.setName("list");
//            List objectIds = (ArrayList) eventMap.get(stringkey);
            for (Object attr : omap) {
                Element event_omaplist_string = eventOmapList.addElement("string");
                event_omaplist_string.addAttribute("key", "object-id");
                event_omaplist_string.addAttribute("value", attr.toString());
                event_omaplist_string.setName("string");
            }

            Map<String, OcelAttribute> vmap = ocelEvent.getVmap();
            //create <event></list> node
            Element eventVmapList = event.addElement("list");
            eventVmapList.addAttribute("key", "vmap");
            eventVmapList.setName("list");
            for (String itemkey : vmap.keySet()) {
                OcelAttribute ocelAttribute = vmap.get(itemkey);
                Element event_vmaplist_string = eventVmapList.addElement("string");
                event_vmaplist_string.addAttribute("key", itemkey);
                event_vmaplist_string.addAttribute("value", ocelAttribute.getValue());
                event_vmaplist_string.setName("string");
            }

        }

        //create <objects> node
        Element objects = root.addElement("objects");
        objects.setName("objects");
        //loop for objects tag
        Map<String, OcelObject> objectsMap = log.getObjects();
        for (String s : objectsMap.keySet()) {
            OcelObject ocelObject = objectsMap.get(s);
            Element object = objects.addElement("object");
            object.setName("object");
            //write objectId
            Element objectId = object.addElement("string");
            objectId.setName("string");
            objectId.addAttribute("key", "id");
            objectId.addAttribute("value", ocelObject.getId());
            //write objectType
            Element objectType = object.addElement("string");
            objectType.setName("string");
            objectType.addAttribute("key", "type");
            objectType.addAttribute("value", ocelObject.getType());

            //write ovmap
            Map<String, OcelAttribute> ovmap = ocelObject.getOvmap();

            Element objectOVmapList = object.addElement("list");
            objectOVmapList.addAttribute("key", "ovmap");
            objectOVmapList.setName("list");
            for (String itemkey : ovmap.keySet()) {
                OcelAttribute ocelAttribute = ovmap.get(itemkey);
                Element object_list_string = objectOVmapList.addElement("string");
                object_list_string.addAttribute("key", itemkey);
                object_list_string.addAttribute("value", ocelAttribute.getValue());
                object_list_string.setName("string");
            }
        }

        try {
//            File file = new File(output_path);
            OutputFormat format = new OutputFormat("\t", true);
            format.setTrimText(false);
            //format.setLineSeparator("\\r\\n");
            XMLWriter xmlWriter = new XMLWriter(out, format);
            xmlWriter.write(document);
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String duration = " (" + (System.currentTimeMillis() - start) + " msec.)";
        OcelLogging.log("finished serializing log" + duration, OcelLogging.Importance.DEBUG);
    }

    static void writeXMLElementsFromMap(Element xmlElement, Map<String, Object> map, String stringkey) {
        Map<String, String> tmpMap = (Map<String, String>) map.get(stringkey);
        for (String key : tmpMap.keySet()) {
            String value = tmpMap.get(key);
            Element elm_string = xmlElement.addElement("string");
            elm_string.addAttribute("key", key);
            elm_string.addAttribute("value", value);
            elm_string.setName("string");
        }
    }


    public String toString() {
        return this.getName();
    }
}
