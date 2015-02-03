package com.hp.dsg.stratus.rest.entities;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by panuska on 30.1.2015.
 */
public class ServerProperty {
    public final String name;
    public final String displayName;
    public final Object value;

    protected ServerProperty(JSONObject property) {
        this.name = (String)property.get("name");
        String newDisplayName = dictionary.get(name);
        String displayName = (String)property.get("displayName");
        this.displayName = newDisplayName != null ? newDisplayName : displayName;
        this.value = ((JSONArray)property.get("value")).get(0);
    }

    // translates property names (the ones not listed here remain in original form)
    private static final Map<String, String> dictionary = new HashMap<>() ;
    {
        dictionary.put("PRIVATEIP", "Private IP");
        dictionary.put("vpninfo.txt", "VPN");
        dictionary.put("PublicIPAddress", "Public IP");
        dictionary.put("VNC Console URL", "VNC");

    }
}
