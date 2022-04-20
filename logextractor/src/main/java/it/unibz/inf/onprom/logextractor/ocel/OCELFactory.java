/*
 * onprom-logextractor
 *
 * SimpleXESFactory.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
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

package it.unibz.inf.onprom.logextractor.ocel;

import it.unibz.inf.pm.ocel.entity.OcelAttribute;
import it.unibz.inf.pm.ocel.entity.OcelLog;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OCELFactory {
    SimpleDateFormat WITH_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    SimpleDateFormat WITHOUT_T = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat ONLY_DATE = new SimpleDateFormat("yyyy-MM-dd");

    public OcelAttribute createAttribute(String type, String key, String value) throws ParseException {

        if (type != null && key != null && value != null) {
            if (type.equalsIgnoreCase("timestamp")) {
                // we assume that the timestamp is in format yyyy-[m]m-[d]d hh:mm:ss[.f...].
                // The fractional seconds may be omitted. The leading zero for mm and dd may also be omitted.
                Date date;
                try {
                    date = WITH_T.parse(value);
                } catch (ParseException e0) {
                    try {
                        date = WITHOUT_T.parse(value);
                    } catch (ParseException e1) {
                        date = ONLY_DATE.parse(value);
                    }
                }
                //return createAttributeTimestamp(key, date);
                return new OcelAttribute(key, date.toString());
            } else {
                return new OcelAttribute(key, value);
            }
        }
        return null;
    }

    public OcelAttribute createAttribute(String type, String key, String value, XExtension extension ) throws ParseException {

        if (type != null && key != null && value != null) {
            if (type.equalsIgnoreCase("timestamp")) {
                // we assume that the timestamp is in format yyyy-[m]m-[d]d hh:mm:ss[.f...].
                // The fractional seconds may be omitted. The leading zero for mm and dd may also be omitted.
                Date date;
                try {
                    date = WITH_T.parse(value);
                } catch (ParseException e0) {
                    try {
                        date = WITHOUT_T.parse(value);
                    } catch (ParseException e1) {
                        date = ONLY_DATE.parse(value);
                    }
                }
                //return createAttributeTimestamp(key, date);
                return new OcelAttribute(key, date.toString());
            } else {
                return new OcelAttribute(key, value);
            }
        }
        return null;
    }

    public OcelLog createLog() {
        return new OcelLog();
    }

    public XExtension getPredefinedExtension(String key) {
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

//    public void addDefaultExtensions(OcelLog ocelLog) {
//        try {
//            ocelLog.getGlobalLogAttributes().add(createAttributeLiteral("version", "0.1", null));
//            ocelLog.getGlobalLogAttributes().add(createAttributeLiteral("ordering", "timestamp", null));
//            ocelLog.getGlobalLogAttributes().add(createAttributeLiteral("attribute-names", "list", null));
//
//            ocelLog.getGlobalEventAttributes().add(createAttributeLiteral("id", "__INVALID__", null));
//            ocelLog.getGlobalEventAttributes().add(createAttributeLiteral("activity", "__INVALID__", null));
//            ocelLog.getGlobalEventAttributes().add(createAttributeTimestamp("timestamp", Timestamp.valueOf("1970-01-01 01:00:00").getTime(), null));
//            ocelLog.getGlobalEventAttributes().add(createAttributeLiteral("omap", "__INVALID__", null));
//
//            ocelLog.getGlobalObjectAttributes().add(createAttributeLiteral("id", "__INVALID__", null));
//            ocelLog.getGlobalObjectAttributes().add(createAttributeLiteral("type", "__INVALID__", null));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
