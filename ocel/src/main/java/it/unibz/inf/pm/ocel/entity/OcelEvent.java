package it.unibz.inf.pm.ocel.entity;

import com.alibaba.fastjson.annotation.JSONField;
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
}
