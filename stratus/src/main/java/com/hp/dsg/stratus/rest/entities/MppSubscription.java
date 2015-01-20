package com.hp.dsg.stratus.rest.entities;

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
}
