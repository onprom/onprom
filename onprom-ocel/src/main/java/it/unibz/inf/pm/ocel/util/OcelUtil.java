package it.unibz.inf.pm.ocel.util;

import java.util.List;
import java.util.Map;

public class OcelUtil {
    public static Map getEvents(Map log) {
        Map<String, Object> eventsMap = (Map<String, Object>) log.get("ocel:events");
        return eventsMap;
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
