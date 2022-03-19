/*
 * ocel
 *
 * OcelFactoryNaiveImpl.java
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

package it.unibz.ocel.factory;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import it.unibz.ocel.extension.OcelExtension;
import it.unibz.ocel.id.OcelID;
import it.unibz.ocel.model.*;
import it.unibz.ocel.model.impl.*;

import java.net.URI;
import java.util.Date;


public class OcelFactoryNaiveImpl implements OcelFactory {
    private final Interner<String> interner = Interners.newWeakInterner();
    private boolean useInterner = true;

    public OcelFactoryNaiveImpl() {
    }

    private String intern(String s) {
        return this.useInterner ? this.interner.intern(s) : s;
    }

    public String getAuthor() {
        return "Process and Data Science Group (PADS)";
    }

    public String getDescription() {
        return "Creates naive implementations for all available model hierarchy elements, i.e., no optimizations will be employed.";
    }

    public String getName() {
        return "Standard / naive";
    }

    public URI getUri() {
        return URI.create("http://ocel-standard.org/");
    }

    public String getVendor() {
        return "ocel-standard.org";
    }

    public OcelLog createLog() {
        return new OcelLogImpl(new OcelAttributeMapLazyImpl(OcelAttributeMapImpl.class));
    }

    public OcelLog createLog(OcelAttributeMap attributes) {
        return new OcelLogImpl(attributes);
    }

    public OcelTrace createTrace() {
        return new OcelTraceImpl(new OcelAttributeMapLazyImpl(OcelAttributeMapImpl.class));
    }

    public OcelTrace createTrace(OcelAttributeMap attributes) {
        return new OcelTraceImpl(attributes);
    }

    public OcelEvent createEvent() {
        return new OcelEventImpl();
    }

    public OcelEvent createEvent(OcelAttributeMap attributes) {
        return new OcelEventImpl(attributes);
    }

    public OcelEvent createEvent(OcelID id, OcelAttributeMap attributes) {
        return new OcelEventImpl(id, attributes);
    }

    public OcelAttributeMap createAttributeMap() {
        return new OcelAttributeMapImpl();
    }

    public OcelAttributeBoolean createAttributeBoolean(String key, boolean value, OcelExtension extension) {
        return new OcelAttributeBooleanImpl(this.intern(key), value, extension);
    }

    public OcelAttributeContinuous createAttributeContinuous(String key, double value, OcelExtension extension) {
        return new OcelAttributeContinuousImpl(this.intern(key), value, extension);
    }

    public OcelAttributeDiscrete createAttributeDiscrete(String key, long value, OcelExtension extension) {
        return new OcelAttributeDiscreteImpl(this.intern(key), value, extension);
    }

    public OcelAttributeLiteral createAttributeLiteral(String key, String value, OcelExtension extension) {
        return new OcelAttributeLiteralImpl(this.intern(key), this.intern(value), extension);
    }

    public OcelAttributeTimestamp createAttributeTimestamp(String key, Date value, OcelExtension extension) {
        return new OcelAttributeTimestampImpl(this.intern(key), value, extension);
    }

    public OcelAttributeTimestamp createAttributeTimestamp(String key, long millis, OcelExtension extension) {
        return new OcelAttributeTimestampImpl(this.intern(key), millis, extension);
    }

    public OcelAttributeID createAttributeID(String key, OcelID value, OcelExtension extension) {
        return new OcelAttributeIDImpl(this.intern(key), value, extension);
    }

    public OcelAttributeList createAttributeList(String key, OcelExtension extension) {
        return new OcelAttributeListImpl(this.intern(key), extension);
    }

    public OcelAttributeContainer createAttributeContainer(String key, OcelExtension extension) {
        return new OcelAttributeContainerImpl(this.intern(key), extension);
    }

    public boolean isUseInterner() {
        return this.useInterner;
    }

    public void setUseInterner(boolean useInterner) {
        this.useInterner = useInterner;
    }
}

