package com.hp.dsg.stratus.rest.entities;

import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;


/**
 * Created by panuska on 6.1.15.
 */
public class MppInstance extends CsaEntity {
    public MppInstance(String json) {
        super(json);
    }

    @Override
    public String toString() {
        return getProperty("name");
    }

    public String getComponentProperty(String name) {
        return JsonPath.read(json, "$.components[*].properties[?(@.name == '"+name+"')].value[0]").toString();
    }

    public String[] getComponentProperties(String name) {
        JSONArray a = JsonPath.read(json, "$.components[*].properties[?(@.name == '"+name+"')].value");
        return a.toArray(new String[a.size()]);
    }

    public Boolean[] getBooleanComponentProperties(String name) {
        JSONArray a = JsonPath.read(json, "$.components[*].properties[?(@.name == '"+name+"')].value");
        return a.toArray(new Boolean[a.size()]);
    }
}
