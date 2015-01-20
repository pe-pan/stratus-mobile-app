package com.hp.dsg.stratus.rest.entities;

import com.hp.dsg.stratus.entities.Entity;
import com.jayway.jsonpath.JsonPath;

/**
 * Created by panuska on 2.10.14.
 */
public class CsaEntity extends Entity {
    private String json;

    public CsaEntity(String json) {
        init(json);
    }

    @Override
    protected void init(String o) {
        this.json = o;
    }

    public String getId() {
        String self = getProperty("@self");
        int index = self.lastIndexOf('/');
        return self.substring(index+1);
    }

    @Override
    public String getProperty(String key) {
        if (key.equals("id")) {
            return getId();
        }
        Object value = JsonPath.read(json, "$."+key);
        return value == null ? "null" : value.toString().replace('\n', ' '); //todo remove new lines
    }

    @Override
    public void setProperty(String key, String value) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public String toJson() {
        return json;
    }
}
