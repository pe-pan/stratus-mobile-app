package com.hp.dsg.stratus.rest.entities;


import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.Mpp;

/**
 * Created by panuska on 6.1.15.
 */
public class MppInstanceHandler extends CsaEntityHandler {

    public MppInstanceHandler() {
        super();
        this.context = "mpp-instance";
    }

    @Override
    protected String getListJson() {
        return client.doPost(Mpp.REST_API+"/mpp/mpp-instance/filter", "{}").getResponse();
    }

    @Override
    protected Entity newEntity(String json) {
        return new MppInstance(json);
    }

    @Override
    public Entity loadDetails(Entity entity) {
        String instanceId = entity.getProperty("id");
        String catalogId = entity.getProperty("catalogId");

        String json = client.doGet(Mpp.REST_API+"/mpp/mpp-instance/"+instanceId+"?catalogId="+catalogId, ContentType.JSON_JSON).getResponse();
        entity.init(json); //todo mark somewhere details have been loaded
        return entity;
    }
}
