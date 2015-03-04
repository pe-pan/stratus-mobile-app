package com.hp.dsg.stratus.entities;

import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.Mpp;
import com.jayway.jsonpath.JsonPath;
import static com.hp.dsg.stratus.Mpp.M_STRATUS;

/**
 * Created by panuska on 6.1.15.
 */
public class MppRequestHandler extends CsaEntityHandler {

    private MppRequestHandler() {
        super();
        this.context = "mpp-request";
    }

    public static final MppRequestHandler INSTANCE = new MppRequestHandler();

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
        String response = M_STRATUS.doPost(Mpp.REST_PATHNAME + "mpp-request/"+serviceId+"?"+CATALOG_ID_KEY+"="+catalogId, entity.toJson(), ContentType.JSON_MULTI);
        String id = JsonPath.read(response, "$.id");
        return id;
    }
}
