package it.unibz.inf.pm.ocel.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OcelObject {
    @JsonProperty("ocel:id")
    private String id;

    @JsonProperty("ocel:type")
    private String type;


    @JsonProperty("ocel:ovmap")
    private Map<String, OcelAttribute> ovmap; //map with its child elements having a string value type. not required

    public OcelObject(String id) {
        this.id = id;
        this.ovmap = new HashMap<>();
    }

    @Deprecated
    public OcelObject() {
        this.ovmap = new HashMap<>();
    }

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

    public Map<String, OcelAttribute> getOvmap() {
        return ovmap;
    }
}
