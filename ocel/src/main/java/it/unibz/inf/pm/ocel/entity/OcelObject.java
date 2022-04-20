package it.unibz.inf.pm.ocel.entity;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

@Data
public class OcelObject {
    @JSONField(name = "ocel:id")
    private String id;

    @JSONField(name = "ocel:type")
    private String type;

    @JSONField(name = "ocel:ovmap")
    private Map<String, String> ovmap; //map with its child elements having a string value type. not required


    private Map<String, OcelAttribute> attributes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getOvmap() {
        return ovmap;
    }

    public void setOvmap(Map<String, String> ovmap) {
        this.ovmap = ovmap;
    }

    public Map<String, OcelAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, OcelAttribute> attributes) {
        this.attributes = attributes;
    }
}
