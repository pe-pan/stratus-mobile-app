package com.hp.dsg.stratus.entities;

import android.util.Log;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by panuska on 6.1.15.
 */
public class MppInstance extends CsaEntity {
    private static final String TAG = MppInstance.class.getSimpleName();

    public MppInstance(String json) {
        super(json);
    }

    @Override
    public String toString() {
        return getProperty("name");
    }

    private Server[] cachedServers;

    public Server[] getServers() {
        if (cachedServers != null) return cachedServers;
        List<JSONObject> components = JsonPath.read(json, "$.components[*]");
        if (components.size() == 0) return null;
        Server[] servers = new Server[components.size()];
        for (int j = 0; j < components.size(); j++) {
            JSONObject component = components.get(j);
            Object id = component.get("id");

            JSONArray properties = JsonPath.read(json, "$.components[?(@.id == '" + id + "')].properties[*]");
            ServerProperty[] sps;
            if (component.get("name").equals("INFRASTRUCTURE_SERVICE__Thu Oct 10 11:51:57 UTC 2013")) {
                sps = new ServerProperty[properties.size() + 1];  // give Infrastructure service the DEMO_NAME property
                sps[properties.size()] = ServerProperty.INFRASTRUCTURE_SERVICE;
            } else {
                sps = new ServerProperty[properties.size()];
            }
            for (int i = 0; i < properties.size(); i++) {
                ServerProperty sp = new ServerProperty((JSONObject) properties.get(i));
                sps[i] = sp;
            }
            Arrays.sort(sps);
            String serviceSubscriptionId;
            ServiceAction[] sas ;
            try {
                serviceSubscriptionId = JsonPath.read(json, "$.components[?(@.id == '" + id + "')].resourceSubscription[*].id[0]");
                JSONArray actions = JsonPath.read(json, "$.components[?(@.id == '" + id + "')].resourceSubscription[*].serviceAction");
                sas = new ServiceAction[actions.size()];
                for (int i = 0; i < actions.size(); i++) {
                    ServiceAction sa = new ServiceAction((JSONObject) actions.get(i));
                    sas[i] = sa;
                }
                Arrays.sort(sas);
            } catch (Exception e) {
                serviceSubscriptionId = null;
                sas = null;
                Log.d(TAG, "Actions cannot be parsed from: "+json, e);
            }
            Server server = new Server(sps, serviceSubscriptionId, sas);
            servers[j] = server;
        }
        Arrays.sort(servers);
        cachedServers = servers;
        return servers;
    }

    private String cachedShareServiceId;
    public String getShareServiceId() {
        if (cachedShareServiceId != null) {
            return cachedShareServiceId.length() == 0 ? null : cachedShareServiceId;
        }
        try {
            cachedShareServiceId = JsonPath.read(json, "$.components[*].resourceSubscription[?(@.name == '361196ac-5614-4781-a7f8-d552877ef1da')].id[0]");
            //todo this should be done differently; e.g. to have a structure returning both parameters at once; but not to have a single method parsing 2 strings and returning them one upon another method
            cachedShareActionName = JsonPath.read(json,"$.components[*].resourceSubscription[?(@.name == '361196ac-5614-4781-a7f8-d552877ef1da')].serviceAction[?(@.displayName == 'Share Demo Access')].name[0]");
            return cachedShareServiceId;
        } catch (Exception e) {
            Log.d(TAG, "No share service found: "+json, e);
            cachedShareServiceId = "";  // mark the json has been already parsed but no service ID has been found
            return null;
        }
    }

    private String cachedShareActionName;
    public String getShareActionName() {
        return cachedShareActionName;
    }
}
