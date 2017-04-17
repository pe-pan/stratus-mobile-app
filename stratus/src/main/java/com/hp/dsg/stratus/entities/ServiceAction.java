package com.hp.dsg.stratus.entities;

import com.hp.dsg.stratus.Mpp;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * Created by panuska on 30.1.2015.
 */
public class ServiceAction implements Comparable<ServiceAction> {
    public final String name;
    public final String displayName;
    public final String propertyName;
    public final String propertyValue;

    protected ServiceAction(JSONObject action) {
        this.name = (String)action.get("name");
        this.displayName = ((String)action.get("displayName")).trim();
        JSONArray properties = (JSONArray) action.get("properties");
        if (properties != null && properties.size() > 0) {
            JSONObject property = (JSONObject) properties.get(0);                // todo we expect to have one property only
            propertyName = (String) property.get("name");
            propertyValue = (String) ((JSONArray)property.get("value")).get(0);  // todo we expect to have one default value only
        } else {
            propertyName = null;
            propertyValue = null;
        }
    }

    @Override
    public int compareTo(ServiceAction another) {
        return this.displayName.compareTo(another.displayName);
    }
}
