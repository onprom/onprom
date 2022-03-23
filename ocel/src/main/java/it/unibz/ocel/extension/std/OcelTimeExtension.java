/*
 * ocel
 *
 * OcelTimeExtension.java
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

package it.unibz.ocel.extension.std;


import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.factory.OcelFactory;
import it.unibz.ocel.factory.OcelFactoryRegistry;
import it.unibz.ocel.info.OcelGlobalAttributeNameMap;
import it.unibz.ocel.model.OcelAttribute;
import it.unibz.ocel.model.OcelAttributeTimestamp;
import it.unibz.ocel.model.OcelEvent;

import java.net.URI;
import java.util.Date;

public class OcelTimeExtension extends OcelExtension {
    public static final URI EXTENSION_URI = URI.create("http://www.ocel-standard.org");
    public static final String KEY_TIMESTAMP = "timestamp";
    private static final long serialVersionUID = -3632061569016038500L;
    public static OcelAttributeTimestamp ATTR_TIMESTAMP;
    private static OcelTimeExtension singleton = new OcelTimeExtension();

    private OcelTimeExtension() {
        super("Time", "time", EXTENSION_URI);
        OcelFactory factory = OcelFactoryRegistry.instance().currentDefault();
        ATTR_TIMESTAMP = factory.createAttributeTimestamp("timestamp", 0L, this);
        this.eventAttributes.add((OcelAttribute)ATTR_TIMESTAMP.clone());
        OcelGlobalAttributeNameMap.instance().registerMapping("EN", "timestamp", "Timestamp");
        OcelGlobalAttributeNameMap.instance().registerMapping("DE", "timestamp", "Zeitstempel");
        OcelGlobalAttributeNameMap.instance().registerMapping("FR", "timestamp", "Horodateur");
        OcelGlobalAttributeNameMap.instance().registerMapping("ES", "timestamp", "Timestamp");
        OcelGlobalAttributeNameMap.instance().registerMapping("PT", "timestamp", "Timestamp");
    }

    public static OcelTimeExtension instance() {
        return singleton;
    }

    private Object readResolve() {
        return singleton;
    }

    public Date extractTimestamp(OcelEvent event) {
        OcelAttributeTimestamp timestampAttribute = (OcelAttributeTimestamp)event.getAttributes().get("timestamp");
        return timestampAttribute == null ? null : timestampAttribute.getValue();
    }

    public void assignTimestamp(OcelEvent event, Date timestamp) {
        this.assignTimestamp(event, timestamp.getTime());
    }

    public void assignTimestamp(OcelEvent event, long time) {
        OcelAttributeTimestamp attr = (OcelAttributeTimestamp)ATTR_TIMESTAMP.clone();
        attr.setValueMillis(time);
        event.getAttributes().put("timestamp", attr);
    }
}

