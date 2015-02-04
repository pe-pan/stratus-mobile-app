package com.hp.dsg.stratus.entities;


import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.Mpp;

/**
 * Created by panuska on 6.1.15.
 */
public class MppInstanceHandler extends CsaEntityHandler {

    protected MppInstanceHandler() {
        super();
        this.context = "mpp-instance";
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
