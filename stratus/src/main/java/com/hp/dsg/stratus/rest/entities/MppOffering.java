package com.hp.dsg.stratus.rest.entities;

/**
 * Created by panuska on 6.1.15.
 */
public class MppOffering extends CsaEntity {
    public MppOffering(String json) {
        super(json);
    }

    @Override
    public String toString() {
        return getProperty("displayName");
    }

}
