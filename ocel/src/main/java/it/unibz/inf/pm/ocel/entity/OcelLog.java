package it.unibz.inf.pm.ocel.entity;

import com.alibaba.fastjson.annotation.JSONField;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XLogImpl;
import org.python.apache.commons.compress.archivers.zip.X000A_NTFS;

import java.util.*;

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
    private Map<String, OcelEvent> events;

    @JSONField(name = "ocel:objects")
    private Map<String, OcelObject> objects;

    private Map<String, OcelAttribute> attributeMap;

    public OcelLog(Map<String, OcelEvent> events, Map<String, OcelObject> objects) {
        attributeMap = new HashMap<>();
        this.events = events;
        this.objects = objects;
    }
}
