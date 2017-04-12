package com.hp.dsg.stratus.entities;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by panuska on 30.1.2015.
 */
public class ServerProperty implements Comparable<ServerProperty> {
    public final String name;
    public final String displayName;
    public final Object value;

    public static final String DEMO_NAME = "DEMONAME";
    public static final String ACTIVATED = "ACTIVATED";
    public static final ServerProperty INFRASTRUCTURE_SERVICE = new ServerProperty(DEMO_NAME, DEMO_NAME, "Infrastructure");

    protected ServerProperty(String name, String displayName, Object value) {
        this.name = name;
        this.displayName = displayName;
        this.value = value;
    }

    protected ServerProperty(JSONObject property) {
        this.name = (String)property.get("name");
        String newDisplayName = dictionary.get(name);
        String displayName = ((String)property.get("displayName")).trim();
        this.displayName = newDisplayName != null ? newDisplayName : displayName;
        this.value = ((JSONArray)property.get("value")).get(0);
    }

    // translates property names (the ones not listed here remain in original form)
    private static final Map<String, String> dictionary = new HashMap<>() ;
    static {
        dictionary.put("PRIVATEIP", "Private IP");
        dictionary.put("vpninfo.txt", "VPN");
        dictionary.put("PublicIPAddress", "Public IP");
        dictionary.put("VNC Console URL", "VNC");
        dictionary.put("RDPUSER", "User");
        dictionary.put("RDP_PASSWORD", "Password");

    }

    @Override
    public int compareTo(ServerProperty another) {
        return this.displayName.compareTo(another.displayName);
    }
}
