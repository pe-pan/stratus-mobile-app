package com.hp.dsg.stratus.entities;

import com.hp.dsg.stratus.Mpp;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import static com.hp.dsg.stratus.Mpp.M_STRATUS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panuska on 2.10.14.
 */
public abstract class CsaEntityHandler extends EntityHandler {

    protected CsaEntityHandler() {
        super();

    }

    static final String NO_FILTER = "{}";

    private String filter = NO_FILTER;

    protected String getListJson() {
        return M_STRATUS.doPost(Mpp.REST_PATHNAME +context+"/filter", filter);
    }

    public void setFilter(String filter) {
        this.filter = filter == null ? NO_FILTER : filter;
    }

    public synchronized List<Entity> list(boolean enforce) {
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
    public String delete(Entity entity) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public Entity loadDetails(Entity entity) {
        throw new IllegalStateException("Not implemented");
    }
}
