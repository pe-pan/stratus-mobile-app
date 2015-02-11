package com.hp.dsg.stratus.entities;

import com.hp.dsg.rest.AuthenticatedClient;
import com.hp.dsg.stratus.Mpp;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panuska on 2.10.14.
 */
public abstract class CsaEntityHandler extends EntityHandler {
    protected static AuthenticatedClient client;

    public static void setClient(AuthenticatedClient client) {
        CsaEntityHandler.client = client;
    }

    protected CsaEntityHandler() {
        super();

    }

    protected String getListJson() {
        return client.doPost(Mpp.REST_API+"/mpp/"+context+"/filter", "{}").getResponse();
    }

    public List<Entity> list(boolean enforce) {
        if (lastEntities != null && !enforce) {
            resetFilteredEntities();
            return lastEntities;
        }

        String json = getListJson();
        JSONArray array = JsonPath.read(json, "$.members");
        List<Entity> returnValue = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            JSONObject o = (JSONObject) array.get(i);
            returnValue.add(newEntity(o.toJSONString()));
        }

        lastRefresh = System.currentTimeMillis();
        lastEntities = returnValue;
        resetFilteredEntities();// every list resets also the filter
        return returnValue;
    }

    public String create(Entity entity) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Entity update(Entity entity) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public void delete(Entity entity) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public Entity loadDetails(Entity entity) {
        throw new IllegalStateException("Not implemented");
    }
}