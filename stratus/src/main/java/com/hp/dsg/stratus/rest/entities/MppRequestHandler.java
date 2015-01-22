package com.hp.dsg.stratus.rest.entities;

import com.hp.dsg.rest.ContentType;
import com.hp.dsg.rest.HttpResponse;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.Mpp;
import com.jayway.jsonpath.JsonPath;

/**
 * Created by panuska on 6.1.15.
 */
public class MppRequestHandler extends CsaEntityHandler {

    public MppRequestHandler() {
        super();
        this.context = "mpp-request";
    }

    @Override
    protected String getListJson() {
        return client.doPost(Mpp.REST_API + "/mpp/mpp-request", "{}").getResponse();
    }

    @Override
    protected Entity newEntity(String json) {
        return new MppSubscription(json);
    }

    @Override
    public String create (Entity entity) {
        String offeringId = entity.removeProperty("offeringId");
        String catalogId = entity.removeProperty("catalogId");
        HttpResponse response = client.doPost(Mpp.REST_API + "/mpp/mpp-request/"+offeringId+"?catalogId="+catalogId, entity.toJson(), ContentType.JSON_MULTI);
        String id = JsonPath.read(response.getResponse(), "$.id");
        return id;
    }

}
