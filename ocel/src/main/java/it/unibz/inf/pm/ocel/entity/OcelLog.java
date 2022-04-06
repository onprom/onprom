package it.unibz.inf.pm.ocel.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * an OCEL contains a global log, global event, and global object element.
 */

@Data
public class OcelLog {
    @JSONField(name = "ocel:global-log")
    private Object globalLog;

    @JSONField(name = "ocel:global-event")
    private Object globalEvent;

    @JSONField(name = "ocel:global-object")
    private Object globalObject;

    @JSONField(name = "ocel:events")
    private List<OcelEvent> events;

    @JSONField(name = "ocel:objects")
    private List<OcelObject> objects;

}
