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
    private List<OcelEvent> events;

    @JSONField(name = "ocel:objects")
    private List<OcelObject> objects;

    private Map<String, OcelAttribute> attributeMap;

    public OcelLog() {
        attributeMap = createInternalMap();
    }

    private Map<String, OcelAttribute> createInternalMap() {
        return new Object2ObjectOpenHashMap<>(4);
    }

    public void addAttributes(Map<String, OcelAttribute> attributes) {
    }

    public void addEvents(Map<String, OcelEvent> events) {
    }

    public void addObjects(Collection<OcelObject> objects) {
    }

    /**
     *
     * Reuse some properties of XES XAttribute
     */
    public List<XAttribute> getGlobalLogAttributes() {
        return new ArrayList<>();
    }

    public List<XEventClassifier> getClassifiers() {
        return null;
    }

    public List<XAttribute> getGlobalEventAttributes() {
        return new ArrayList<>();
    }

    public List<XAttribute> getGlobalObjectAttributes() {
        return new ArrayList<>();
    }

    public Map<String, XAttribute> getAttributes() {
        return new HashMap<>();
    }
}
