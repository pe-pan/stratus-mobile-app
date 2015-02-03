package com.hp.dsg.stratus.rest.entities;

import net.minidev.json.JSONObject;

/**
 * Created by panuska on 30.1.2015.
 */
public class ServiceAction {
    public final String name;
    public final String displayName;

    protected ServiceAction(JSONObject action) {
        this.name = (String)action.get("name");
        this.displayName = (String)action.get("displayName");
    }
}
