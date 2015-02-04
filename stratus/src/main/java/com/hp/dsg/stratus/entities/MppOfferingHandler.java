package com.hp.dsg.stratus.entities;


import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.Mpp;

/**
 * Created by panuska on 6.1.15.
 */
public class MppOfferingHandler extends CsaEntityHandler {

    protected MppOfferingHandler() {
        super();
        this.context = "mpp-offering";
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
