package it.unibz.inf.pm.ocel.entity;

import com.alibaba.fastjson.annotation.JSONField;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 *
 * an event contains the id, activity, timestamp, omap, and vmap elements.
 */
public class OcelEvent {
    @JSONField(name = "ocel:id")
    private String id;

    @JSONField(name = "ocel:activity")
    private String activity;

    @JSONField(name = "ocel:timestamp")
    private DateTime timestamp;

    @JSONField(name = "ocel:omap")
    private List<String> omap;

    @JSONField(name = "ocel:vmap")
    private Map<String, String> vmap; //map with its child elements having a string value type. not required

    private Map<String, OcelAttribute> attributes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getOmap() {
        return omap;
    }

    public void setOmap(List<String> omap) {
        this.omap = omap;
    }

    public Map<String, String> getVmap() {
        return vmap;
    }

    public void setVmap(Map<String, String> vmap) {
        this.vmap = vmap;
    }

    public Map<String, OcelAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, OcelAttribute> attributes) {
        this.attributes = attributes;
    }
}
