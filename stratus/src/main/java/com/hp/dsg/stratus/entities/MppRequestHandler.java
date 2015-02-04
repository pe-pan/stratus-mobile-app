package com.hp.dsg.stratus.entities;

import com.hp.dsg.rest.ContentType;
import com.hp.dsg.rest.HttpResponse;
import com.hp.dsg.stratus.Mpp;
import com.jayway.jsonpath.JsonPath;

/**
 * Created by panuska on 6.1.15.
 */
public class MppRequestHandler extends CsaEntityHandler {

    protected MppRequestHandler() {
        super();
        this.context = "mpp-request";
    }

    @Override
    protected Entity newEntity(String json) {
        return new MppSubscription(json);
    }

    public static final String SERVICE_ID_KEY = "serviceId";
    public static final String CATALOG_ID_KEY = "catalogId";

    @Override
    public String create (Entity entity) {
        String serviceId = entity.removeProperty(SERVICE_ID_KEY);
        String catalogId = entity.removeProperty(CATALOG_ID_KEY);
        HttpResponse response = client.doPost(Mpp.REST_API + "/mpp/mpp-request/"+serviceId+"?"+CATALOG_ID_KEY+"="+catalogId, entity.toJson(), ContentType.JSON_MULTI);
        String id = JsonPath.read(response.getResponse(), "$.id");
        return id;
    }

}
