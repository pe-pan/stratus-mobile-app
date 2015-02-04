package com.hp.dsg.stratus.entities;

import android.util.Log;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

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

    public Server[] getServers() {
        Filter fooOrBar = Filter.filter(Criteria.where("name").regex(Pattern.compile("^HPCSSERVER.*")));
        List<JSONObject> components = JsonPath.read(json, "$.components[?]", fooOrBar);
        if (components.size() == 0) return null;
        Server[] servers = new Server[components.size()];
        for (int j = 0; j < components.size(); j++) {
            JSONObject component = components.get(j);
            Object id = component.get("id");

            JSONArray properties = JsonPath.read(json, "$.components[?(@.id == '" + id + "')].properties[*]");
            ServerProperty[] sps = new ServerProperty[properties.size()];
            for (int i = 0; i < properties.size(); i++) {
                ServerProperty sp = new ServerProperty((JSONObject) properties.get(i));
                sps[i] = sp;
            }
            String serviceSubscriptionId;
            ServiceAction[] sas ;
            try {
                serviceSubscriptionId = JsonPath.read(json, "$.components[?(@.id == '" + id + "')].resourceSubscription[?(@.name == 'cc41303b-f6e8-40f2-b335-30404a2a3c63')].id[0]");
                JSONArray actions = JsonPath.read(json, "$.components[?(@.id == '" + id + "')].resourceSubscription[?(@.name == 'cc41303b-f6e8-40f2-b335-30404a2a3c63')].serviceAction");
                sas = new ServiceAction[actions.size()];
                for (int i = 0; i < actions.size(); i++) {
                    ServiceAction sa = new ServiceAction((JSONObject) actions.get(i));
                    sas[i] = sa;
                }
            } catch (Exception e) {
                serviceSubscriptionId = null;
                sas = null;
                Log.d(TAG, "Actions cannot be parsed from: "+json, e);
            }
            Server server = new Server(sps, serviceSubscriptionId, sas);
            servers[j] = server;
        }
        return servers;
    }
}
