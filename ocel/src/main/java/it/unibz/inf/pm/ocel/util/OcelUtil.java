/*
 * ocel
 *
 * OcelUtil.java
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

package it.unibz.inf.pm.ocel.util;

import java.util.List;
import java.util.Map;

public class OcelUtil {
    public static Map getEvents(Map log) {
        return (Map<String, Object>) log.get("ocel:events");
    }

    public static Map getObjects(Map log) {
        return (Map<String, Object>) log.get("ocel:objects");
    }

    public static List getAttributeNames(Map log) {
        Map<String,Object> globalLog = getGlobalLog(log);
        return (List) globalLog.get("ocel:attribute-names");
    }

    public static List getObjectTypes(Map log) {
        Map<String,Object> globalLog = getGlobalLog(log);
        return (List) globalLog.get("ocel:object-types");
    }

    public static String getVersion(Map log) {
        Map<String,Object> globalLog = getGlobalLog(log);
        return globalLog.get("ocel:version").toString();
    }

    public static Map getGlobalLog(Map log) {
        return (Map<String, Object>) log.get("ocel:global-log");
    }

    public static Map getGlobalEvent(Map log) {
        return (Map<String, Object>) log.get("ocel:global-event");
    }

    public static Map getGlobalObject(Map log) {
        return (Map<String, Object>) log.get("ocel:global-object");
    }
}
