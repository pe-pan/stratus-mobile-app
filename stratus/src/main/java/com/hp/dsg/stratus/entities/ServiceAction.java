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
    public final String emailProperty;

    protected ServiceAction(JSONObject action) {
        this.name = (String)action.get("name");
        this.displayName = ((String)action.get("displayName")).trim();
        this.emailProperty = evaluateEmailProperty(action);
    }

    private String evaluateEmailProperty(JSONObject action) { //todo hack -> it should find all possible properties and serialize all necessary (currently; there is only one known property)
        JSONArray properties = (JSONArray) action.get("properties");
        if (properties != null && properties.size() > 0 ) {
            for (int i = 0; i < properties.size(); i++) {
                JSONObject property = (JSONObject)properties.get(i);
                if ("EMAIL_CONF".equals(property.get("name"))) {
                    return Mpp.M_STRATUS.getLoggedUserName();
                }
            }
        }
        return null;
    }

    @Override
    public int compareTo(ServiceAction another) {
        return this.displayName.compareTo(another.displayName);
    }
}
