package it.unibz.inf.pm.ocel.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class OcelElement {
    @JSONField(name = "ocel:global-log")
    private String globalLog;

    @JSONField(name = "ocel:global-event")
    private Object globalEvent;

    @JSONField(name = "ocel:global-object")
    private Object globalObject;

    @JSONField(name = "ocel:events")
    private Object events;

    @JSONField(name = "ocel:objects")
    private Object objects;

}
