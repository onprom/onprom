package it.unibz.inf.pm.ocel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * an OCEL contains a global log, global event, and global object element.
 */

@Data
public class OcelLog {
    @JsonProperty("ocel:global-log")
    private Map<String, Object> globalLog;

    @JsonProperty("ocel:events")
    private Map<String, OcelEvent> events;

    @JsonProperty("ocel:objects")
    private Map<String, OcelObject> objects;

    @JsonIgnore
    private Map<String, OcelAttribute> attributeMap;

    public OcelLog(Map<String, Object> globalLog, Map<String, OcelEvent> events, Map<String, OcelObject> objects) {
        attributeMap = new HashMap<>();
        this.globalLog = globalLog;
        this.events = events;
        this.objects = objects;
    }
}
