package it.unibz.inf.kaos.logextractor;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.xeslite.lite.factory.XFactoryLiteImpl;

import java.sql.Timestamp;

public class SimpleXESFactory extends XFactoryLiteImpl {
    public XAttribute createXAttribute(String type, String key, String value, XExtension extension) {

        if (type != null && key != null && value != null) {
            if (type.toLowerCase().equals("timestamp")) {
                // we assume that the timestamp is in format yyyy-[m]m-[d]d hh:mm:ss[.f...].
                // The fractional seconds may be omitted. The leading zero for mm and dd may also be omitted.
                return createAttributeTimestamp(key, Timestamp.valueOf(value).getTime(), extension);
            } else {
                return createAttributeLiteral(key, value, extension);
            }
        }
        return null;
    }

    public XExtension getPredefinedXExtension(String key) {
        if (key != null) {
            switch (key.toLowerCase()) {
                case "time:timestamp":
                    return XTimeExtension.instance();
                case "concept:name":
                    return XConceptExtension.instance();
                case "lifecycle:transition":
                    return XLifecycleExtension.instance();
                case "org:resource":
                    return XOrganizationalExtension.instance();
            }
        }
        return null;
    }

    public void addDefaultExtensions(XLog xlog) {
        try {
            xlog.getGlobalTraceAttributes().add(createAttributeLiteral("concept:name", "DEFAULT", null));

            xlog.getGlobalEventAttributes().add(createAttributeTimestamp("time:timestamp", Timestamp.valueOf("1970-01-01 01:00:00").getTime(), null));
            xlog.getGlobalEventAttributes().add(createAttributeLiteral("lifecycle:transition", "complete", null));
            xlog.getGlobalEventAttributes().add(createAttributeLiteral("concept:name", "DEFAULT", null));

            xlog.getClassifiers().add(new XEventAttributeClassifier("Time timestamp", "time:timestamp"));
            xlog.getClassifiers().add(new XEventLifeTransClassifier());
            xlog.getClassifiers().add(new XEventNameClassifier());
            xlog.getClassifiers().add(new XEventResourceClassifier());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
