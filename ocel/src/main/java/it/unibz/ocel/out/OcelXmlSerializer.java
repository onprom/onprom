/*
 * ocel
 *
 * OcelXmlSerializer.java
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

package it.unibz.ocel.out;

import it.unibz.ocel.classification.OcelEventAttributeClassifier;
import it.unibz.ocel.classification.OcelEventClassifier;
import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.logging.OcelLogging;
import it.unibz.ocel.model.*;
import it.unibz.ocel.util.OcelDateTimeConversion;
import it.unibz.ocel.util.OcelDateTimeConversionJava7;
import it.unibz.ocel.util.OcelTokenHelper;
import org.deckfour.spex.SXDocument;
import org.deckfour.spex.SXTag;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class OcelXmlSerializer implements OcelSerializer {
    protected OcelDateTimeConversion xsDateTimeConversion = new OcelDateTimeConversionJava7();

    public OcelXmlSerializer() {
    }

    public String getDescription() {
        return "Ocel XML Serialization";
    }

    public String getName() {
        return "Ocel XML";
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
//        doc.addComment("OCEL library version: 1.0RC7");
        doc.addComment("OCEL standard is available from http://ocel-standard.org/");
        SXTag logTag = doc.addNode("log");
//        logTag.addAttribute("ocel.version", "1.0");
//        logTag.addAttribute("ocel.features", "nested-attributes");
//        logTag.addAttribute("ocel.version", "1.0RC7");
        Iterator i$ = log.getExtensions().iterator();

        SXTag traceTag;
        while(i$.hasNext()) {
            OcelExtension extension = (OcelExtension)i$.next();
            traceTag = logTag.addChildNode("extension");
            traceTag.addAttribute("name", extension.getName());
            traceTag.addAttribute("prefix", extension.getPrefix());
            traceTag.addAttribute("uri", extension.getUri().toString());
        }

        this.addGlobalAttributes(logTag, "log", log.getGlobalLogAttributes());
        this.addGlobalAttributes(logTag, "event", log.getGlobalEventAttributes());
        this.addGlobalAttributes(logTag, "object", log.getGlobalObjectAttributes());
        i$ = log.getClassifiers().iterator();

        while(i$.hasNext()) {
            OcelEventClassifier classifier = (OcelEventClassifier)i$.next();
            if (classifier instanceof OcelEventAttributeClassifier) {
                OcelEventAttributeClassifier attrClass = (OcelEventAttributeClassifier)classifier;
                SXTag clsTag = logTag.addChildNode("classifier");
                clsTag.addAttribute("name", attrClass.name());
                clsTag.addAttribute("keys", OcelTokenHelper.formatTokenString(Arrays.asList(attrClass.getDefiningAttributeKeys())));
            }
        }

        this.addAttributes(logTag, log.getAttributes().values());
        i$ = log.iterator();

        while(i$.hasNext()) {
            OcelEvent event = (OcelEvent) i$.next();
            traceTag = logTag.addChildNode("event");
            this.addAttributes(traceTag, event.getAttributes().values());
//            Iterator<OcelEvent> iEvent = trace.iterator();

//            while(iEvent.hasNext()) {
//                OcelEvent event = (OcelEvent)iEvent.next();
//                SXTag eventTag = traceTag.addChildNode("event");
//                this.addAttributes(eventTag, event.getAttributes().values());
//            }
        }

        doc.close();
        String duration = " (" + (System.currentTimeMillis() - start) + " msec.)";
        OcelLogging.log("finished serializing log" + duration, OcelLogging.Importance.DEBUG);
    }

    protected void addGlobalAttributes(SXTag parent, String scope, List<OcelAttribute> attributes) throws IOException {
        if (attributes.size() > 0) {
            SXTag guaranteedNode = parent.addChildNode("global");
            guaranteedNode.addAttribute("scope", scope);
            this.addAttributes(guaranteedNode, attributes);
        }

    }

    protected void addAttributes(SXTag tag, Collection<OcelAttribute> attributes) throws IOException {

        for (OcelAttribute attribute : attributes) {
            SXTag attributeTag;
            if (attribute instanceof OcelAttributeList) {
                attributeTag = tag.addChildNode("list");
                attributeTag.addAttribute("key", attribute.getKey());
            } else if (attribute instanceof OcelAttributeContainer) {
                attributeTag = tag.addChildNode("container");
                attributeTag.addAttribute("key", attribute.getKey());
            } else if (attribute instanceof OcelAttributeLiteral) {
                attributeTag = tag.addChildNode("string");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            } else if (attribute instanceof OcelAttributeDiscrete) {
                attributeTag = tag.addChildNode("int");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            } else if (attribute instanceof OcelAttributeContinuous) {
                attributeTag = tag.addChildNode("float");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            } else if (attribute instanceof OcelAttributeTimestamp) {
                attributeTag = tag.addChildNode("date");
                attributeTag.addAttribute("key", attribute.getKey());
                Date timestamp = ((OcelAttributeTimestamp) attribute).getValue();
                attributeTag.addAttribute("value", this.xsDateTimeConversion.format(timestamp));
            } else if (attribute instanceof OcelAttributeBoolean) {
                attributeTag = tag.addChildNode("boolean");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            } else {
                if (!(attribute instanceof OcelAttributeID)) {
                    throw new IOException("Unknown attribute type!");
                }

                attributeTag = tag.addChildNode("id");
                attributeTag.addAttribute("key", attribute.getKey());
                attributeTag.addAttribute("value", attribute.toString());
            }

            if (attribute instanceof OcelAttributeCollection) {
                Collection<OcelAttribute> childAttributes = ((OcelAttributeCollection) attribute).getCollection();
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
