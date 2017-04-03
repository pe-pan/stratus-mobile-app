package com.hp.dsg.stratus.entities;

/**
 * Created by panuska on 6.1.15.
 */
public class MppSubscription extends CsaEntity {
    public MppSubscription(String json) {
        super(json);
    }

    @Override
    public String toString() {
        return getProperty("name");
    }

    public MppInstance getInstance() {
        EntityHandler subHandler = MppSubscriptionHandler.INSTANCE;
        subHandler.loadDetails(this);

        String catalogId = getProperty("catalogId");
        String instanceId = getProperty("instanceId");

        EntityHandler instanceHandler = MppInstanceHandler.INSTANCE;
        MppInstance instance = new MppInstance("{ \"catalogId\" : \""+catalogId+"\", \"id\" : \""+instanceId+"\" }");
        if (instanceHandler.loadDetails(instance) == null) { // no internet connection
            return null;
        }
        return instance;
    }

    public String delete() {
        EntityHandler handler = MppSubscriptionHandler.INSTANCE;
        return handler.delete(this);
    }
}
