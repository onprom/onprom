package it.unibz.inf.pm.ocel.entity;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class OcelEvent {
    @JSONField(name = "ocel:activity")
    private String activity;

    @JSONField(name = "ocel:timestamp")
    private String timestamp;

    @JSONField(name = "ocel:omap")
    private String omap;

    @JSONField(name = "ocel:vmap")
    private String vmap;
}
