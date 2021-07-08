package it.unibz.inf.pm.ocel.importer;


import it.unibz.inf.pm.ocel.util.DateFormatUtil;
import it.unibz.inf.pm.ocel.util.XmlUtil;
import org.dom4j.*;


import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

public class Ocelxml {
    public static Map apply(String input_path, String... parameters) throws DocumentException
    {
        XmlUtil xmlUtil = new XmlUtil();
        Element root = xmlUtil.read(new File(input_path));
        Document dom = xmlUtil.getDocument();

        HashMap<String, Object> logMap = new HashMap<String,Object>();

        List<Element> globalNodes = dom.selectNodes("//global");
        List<Attribute> globalNodeAttributes = dom.selectNodes("//global/@scope");

        // parse <global scope="event">
        Element globalEventNode = XmlUtil.parse(root, "scope", "event");
        HashMap<String, String> globalEventMap = new HashMap<String,String>();
        List<Element> subGlobalEventNodes = globalEventNode.elements();
        for(Element emt : subGlobalEventNodes)
        {
            globalEventMap.put(emt.attribute("key").getValue(),emt.attribute("value").getValue());
        }
        logMap.put("ocel:global-event",globalEventMap);

        // parse <global scope="object">
        Element globalObjectNode = XmlUtil.parse(root, "scope", "object");
        HashMap<String, String> globalObjectMap = new HashMap<String,String>();
        List<Element> subGlobalObjectNodes = globalObjectNode.elements();
        for(Element emt : subGlobalObjectNodes)
        {
            globalObjectMap.put(emt.attribute("key").getValue(),emt.attribute("value").getValue());
        }
        logMap.put("ocel:global-object",globalObjectMap);

        // parse <global scope="log">
        Element globalLogNode = XmlUtil.parse(root, "scope", "log");
        HashMap<String, Object> globalLogMap = new HashMap<String,Object>();
        List<Element> subGlobalLogNodes = globalLogNode.elements();
        for(Element emt : subGlobalLogNodes)
        {
            String value = emt.attribute("key").getValue();
            List<Element> attributNamesElments = emt.elements();
            List<String> attributeValueList = new ArrayList<>();
            List<String> objTpyeValueList = new ArrayList<>();
            if(value.equals("attribute-names"))
            {
                for(Element attrElm : attributNamesElments)
                {
                    attributeValueList.add(attrElm.attribute("value").getValue());
                    globalLogMap.put("ocel:attribute-names",attributeValueList);
                }
            }
            else if(value.equals("object-types"))
            {
                for(Element attrElm : attributNamesElments)
                {
                    objTpyeValueList.add(attrElm.attribute("value").getValue());
                    globalLogMap.put("ocel:object-types",objTpyeValueList);
                }
            }
            else if(value.equals("version"))
            {
                globalLogMap.put("ocel:version",emt.attribute("value").getValue());
            }
            else if(value.equals("ordering"))
            {
                globalLogMap.put("ocel:ordering",emt.attribute("value").getValue());
            }
        }
        logMap.put("ocel:global-log",globalLogMap);

        //parse the elements of <event>
        List<Element> eventsNodes = dom.selectNodes("//event");
        List<HashMap> eventMapList = new ArrayList<>();
        HashMap<String, Object> eventMapInOne = new HashMap<String,Object>();
        for (Element entElm: eventsNodes) {
            String eventKeyName = "";
            List<Element> eventElmt = entElm.elements();
            HashMap<String, Object> eventMap = new HashMap<String,Object>();
            for (Element event: eventElmt ) {
                String keyStr = event.attribute("key").getValue();
                if("id".equals(keyStr))
                {
                    eventKeyName = event.attribute("value").getValue();
                }else if("timestamp".equals(keyStr)){
                    try {
                        eventMap.put("ocel:timestamp", DateFormatUtil.dealDateFormat(event.attribute("value").getValue()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }else if("activity".equals(keyStr)){
                    eventMap.put("ocel:activity",event.attribute("value").getValue());
                }else if("omap".equals(keyStr)){
                    List<Element>  omapElements = event.elements();
                    List<String> ompValueList = new ArrayList<>();
                    for (Element ompElm :omapElements ) {
                        ompValueList.add(ompElm.attribute("value").getValue());
                        eventMap.put("ocel:omap",ompValueList);
                    }
                }else if("vmap".equals(keyStr)){
                    HashMap<String, Object> vmapMap = new HashMap<String,Object>();
                    List<Element>  vmapElements = event.elements();
                    for (Element vmpElm : vmapElements ) {
                        vmapMap.put(vmpElm.attribute("key").getValue(), parse_xml(vmpElm.attribute("value").getValue(),"vmpElm.getName()"));
                    }
                    eventMap.put("ocel:vmap",vmapMap);
                }
                eventMapInOne.put(eventKeyName,eventMap);
            }
            logMap.put("ocel:events",eventMapInOne);
        }

        //parse the elements of <event>
        List<Element> objectsNodes = dom.selectNodes("//object");
        HashMap<String, Object> objectMapInOne = new HashMap<String,Object>();
        for (Element objElm: objectsNodes) {
            String objectKeyName = "";
            List<Element> objElmt = objElm.elements();
            HashMap<String, Object> objectMap = new HashMap<String,Object>();
            for (Element obj: objElmt ) {
                String keyStr = obj.attribute("key").getValue();
                if("id".equals(keyStr))
                {
                    objectKeyName = obj.attribute("value").getValue();
                }else if("type".equals(keyStr)){
                    objectMap.put("ocel:type", obj.attribute("value").getValue());
                }else if("ovmap".equals(keyStr)){
                    HashMap<String, Object> ovmapMap = new HashMap<String,Object>();
                    List<Element>  ovmapElements = obj.elements();
                    for (Element ovmpElm : ovmapElements ) {
                        ovmapMap.put(ovmpElm.attribute("key").getValue(), parse_xml(ovmpElm.attribute("value").getValue(),"vmpElm.getName()"));
                    }
                    objectMap.put("ocel:ovmap",ovmapMap);
                }
                objectMapInOne.put(objectKeyName,objectMap);
            }
            logMap.put("ocel:objects",objectMapInOne);
        }
        return logMap;
    }


    public static Object parse_xml(String value, String tag_str_lower)
    {
        if(tag_str_lower.indexOf("float") != -1)
        {
            return Float.parseFloat(value);
        }else if(tag_str_lower.indexOf("date") != -1)
        {
            Date date = new Date(value);
            Timestamp timestamp = new Timestamp(date.getTime());
            return timestamp.toString();
        }
        return value;
    }

    public static void main(String[] args) {
        try {
            System.out.println(Ocelxml.apply("minimal.xmlocel"));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


}
