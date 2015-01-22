package com.hp.dsg.stratus.rest.entities;


import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.Mpp;

/**
 * Created by panuska on 6.1.15.
 */
public class MppOfferingHandler extends CsaEntityHandler {

    public MppOfferingHandler() {
        super();
        this.context = "mpp-offering";
    }

    @Override
    protected String getListJson() {
        return client.doPost(Mpp.REST_API+"/mpp/mpp-offering/filter", "{}").getResponse();
    }

    @Override
    protected Entity newEntity(String json) {
        return new MppOffering(json);
    }

    @Override
    public Entity loadDetails(Entity entity) {
        String offeringId = entity.getId();
        String catalogId = entity.getProperty("catalogId");
        String category = entity.getProperty("category.name");

        String json = client.doGet(Mpp.REST_API+"/mpp/mpp-offering/"+offeringId+"?catalogId="+catalogId+"&category="+category, ContentType.JSON_JSON).getResponse();
        entity.init(json); //todo mark somewhere details have been loaded
        return entity;
    }
}
