package it.unibz.inf.pm.ocel.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
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

    public void addAttributes(Map<String, OcelAttribute> attributes) {
    }

    public void addEvents(Map<String, OcelEvent> events) {
    }

    public void addObjects(Collection<OcelObject> objects) {
    }

    public List<XEventClassifier> getClassifiers() {
        return null;
    }

    /**
     *
     * Reuse some properties of XES XAttribute
     */
    public List<XAttribute> getGlobalLogAttributes() {
        return new ArrayList<>();
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
