package com.hp.dsg.stratus.rest.entities;

import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.Mpp;

/**
 * Created by panuska on 6.1.15.
 */
public class MppSubscriptionHandler extends CsaEntityHandler {

    public MppSubscriptionHandler() {
        super();
        this.context = "mpp-subscription";
    }

    @Override
    protected String getListJson() {
        return client.doPost(Mpp.REST_API + "/mpp/mpp-subscription/filter", "{}").getResponse();
    }

    @Override
    protected Entity newEntity(String json) {
        return new MppSubscription(json);
    }

}
