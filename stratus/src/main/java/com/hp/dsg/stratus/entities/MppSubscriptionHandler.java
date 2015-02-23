package com.hp.dsg.stratus.entities;

import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.Mpp;

/**
 * Created by panuska on 6.1.15.
 */
public class MppSubscriptionHandler extends CsaEntityHandler {

    protected MppSubscriptionHandler() {
        super();
        this.context = "mpp-subscription";
    }

    @Override
    protected Entity newEntity(String json) {
        return new MppSubscription(json);
    }

    @Override
    public Entity loadDetails(Entity entity) {
        String subscriptionId = entity.getId();

        String json = client.doGet(Mpp.REST_API+"/mpp/mpp-subscription/"+subscriptionId, ContentType.JSON_JSON).getResponse();
        entity.init(json); //todo mark somewhere details have been loaded
        return entity;
    }

    public String delete(Entity entity) {
        return client.doDelete(Mpp.REST_API+"/mpp/mpp-subscription/"+entity.getId()).getResponse();
    }
}
