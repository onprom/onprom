package it.unibz.inf.pm.ocel.exporter;

import it.unibz.inf.pm.ocel.entity.OcelAttribute;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelLog;
import it.unibz.inf.pm.ocel.logging.OcelLogging;
import org.deckfour.spex.SXDocument;
import org.deckfour.spex.SXTag;
import org.deckfour.xes.model.*;
import org.deckfour.xes.util.XsDateTimeConversion;
import org.deckfour.xes.util.XsDateTimeConversionJava7;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class OcelXmlSerializer {
    protected XsDateTimeConversion xsDateTimeConversion = new XsDateTimeConversionJava7();

    public OcelXmlSerializer(){}

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

    public void serialize(OcelLog log, OutputStream out) throws IOException {
        OcelLogging.log("start serializing log to OCEL.XML", OcelLogging.Importance.DEBUG);
        long start = System.currentTimeMillis();
        SXDocument doc = new SXDocument(out);
        doc.addComment("This file has been generated with the Ocel library. It conforms");
        doc.addComment("to the XML serialization of the OCEl standard for log storage and");
        doc.addComment("management.");
        doc.addComment("OCEL standard version: 1.0");
        doc.addComment("OCEL standard is available from http://ocel-standard.org/");
        SXTag logTag = doc.addNode("log");
//        Iterator i$ = log.getExtensions().iterator();
//
//        SXTag traceTag;
//        while(i$.hasNext()) {
//            OcelExtension extension = (OcelExtension)i$.next();
//            traceTag = logTag.addChildNode("extension");
//            traceTag.addAttribute("name", extension.getName());
//            traceTag.addAttribute("prefix", extension.getPrefix());
//            traceTag.addAttribute("uri", extension.getUri().toString());
//        }

//        this.addGlobalAttributes(logTag, "log", log.getGlobalLogAttributes());
//        this.addGlobalAttributes(logTag, "event", log.getGlobalEventAttributes());
//        this.addGlobalAttributes(logTag, "object", log.getGlobalObjectAttributes());
//        i$ = log.getClassifiers().iterator();
//
//        while(i$.hasNext()) {
//            OcelEventClassifier classifier = (OcelEventClassifier)i$.next();
//            if (classifier instanceof OcelEventAttributeClassifier) {
//                OcelEventAttributeClassifier attrClass = (OcelEventAttributeClassifier)classifier;
//                SXTag clsTag = logTag.addChildNode("classifier");
//                clsTag.addAttribute("name", attrClass.name());
//                clsTag.addAttribute("keys", OcelTokenHelper.formatTokenString(Arrays.asList(attrClass.getDefiningAttributeKeys())));
//            }
//        }

        //this.addAttributes(logTag, log.getAttributes().values());
//        i$ = log.iterator();
//
//        while(i$.hasNext()) {
//            OcelEvent event = (OcelEvent) i$.next();
//            eventTag = logTag.addChildNode("event");
//            this.addAttributes(eventTag, event.getAttributes().values());
//            Iterator<OcelEvent> iEvent = trace.iterator();
//
//            while(iEvent.hasNext()) {
//                OcelEvent event = (OcelEvent)iEvent.next();
//                SXTag eventTag = traceTag.addChildNode("event");
//                this.addAttributes(eventTag, event.getAttributes().values());
//            }
//        }

        doc.close();
        String duration = " (" + (System.currentTimeMillis() - start) + " msec.)";
        OcelLogging.log("finished serializing log" + duration, OcelLogging.Importance.DEBUG);
    }

    protected void addGlobalAttributes(SXTag parent, String scope, List<XAttribute> attributes) throws IOException {
        if (attributes.size() > 0) {
            SXTag guaranteedNode = parent.addChildNode("global");
            guaranteedNode.addAttribute("scope", scope);
            this.addAttributes(guaranteedNode, attributes);
        }

    }

    protected void addAttributes(SXTag tag, Collection<XAttribute> attributes) throws IOException {
        Iterator i$ = attributes.iterator();

        while(i$.hasNext()) {
            XAttribute attribute = (XAttribute)i$.next();
            SXTag attributeTag;
            if (attribute instanceof XAttributeList) {
                attributeTag = tag.addChildNode("list");
                attributeTag.addAttribute("key", attribute.getKey());
            } else if (attribute instanceof XAttributeContainer) {
                attributeTag = tag.addChildNode("container");
                attributeTag.addAttribute("key", attribute.getKey());
            } else if (attribute instanceof XAttributeLiteral) {
                attributeTag = tag.addChildNode("string");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            } else if (attribute instanceof XAttributeDiscrete) {
                attributeTag = tag.addChildNode("int");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            } else if (attribute instanceof XAttributeContinuous) {
                attributeTag = tag.addChildNode("float");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            } else if (attribute instanceof XAttributeTimestamp) {
                attributeTag = tag.addChildNode("date");
                attributeTag.addAttribute("key", attribute.getKey());
                Date timestamp = ((XAttributeTimestamp)attribute).getValue();
                attributeTag.addAttribute("value", this.xsDateTimeConversion.format(timestamp));
            } else if (attribute instanceof XAttributeBoolean) {
                attributeTag = tag.addChildNode("boolean");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            } else {
                if (!(attribute instanceof XAttributeID)) {
                    throw new IOException("Unknown attribute type!");
                }

                attributeTag = tag.addChildNode("id");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            }

            if (attribute instanceof XAttributeCollection) {
                Collection<XAttribute> childAttributes = ((XAttributeCollection)attribute).getCollection();
                this.addAttributes(attributeTag, childAttributes);
            } else if (attribute.hasAttributes()) {
                this.addAttributes(attributeTag, attribute.getAttributes().values());
            }
        }
    }

    public String toString() {
        return this.getName();
    }
}
