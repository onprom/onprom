package it.unibz.inf.pm.ocel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
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
    private List<String> objectTypes;

    @JsonIgnore
    private Map<String, OcelAttribute> attributeMap;

    @JsonIgnore
    private List<String> timestamps;


    public OcelLog(Map<String, Object> globalLog, Map<String, OcelEvent> events, Map<String, OcelObject> objects) {
        attributeMap = new HashMap<>();
        this.globalLog = globalLog;
        this.events = events;
        this.objects = objects;
    }

    public OcelLog(Map<String, Object> globalLog, Map<String, OcelEvent> events, Map<String, OcelObject> objects, List<String> timestamps) {
        attributeMap = new HashMap<>();
        this.timestamps = timestamps;
        this.globalLog = globalLog;
        this.events = events;
        this.objects = objects;
    }

    public OcelLog(Map<String, Object> globalLog, Map<String, OcelEvent> events,
                   Map<String, OcelObject> objects, List<String> timestamps, List<String> objectTypes) {
        attributeMap = new HashMap<>();
        this.timestamps = timestamps;
        this.objectTypes = objectTypes;
        this.globalLog = globalLog;
        this.events = events;
        this.objects = objects;
    }

    public OcelLog(Map<String, Object> globalLog, Map<String, OcelEvent> events, Map<String, OcelObject> objects,
                   Map<String, OcelAttribute> attributes, List<String> timestamps, List<String> objectTypes) {
        this.attributeMap = attributes;
        this.timestamps = timestamps;
        this.objectTypes = objectTypes;
        this.globalLog = globalLog;
        this.events = events;
        this.objects = objects;
    }
}
