package com.hp.dsg.stratus.entities;

/**
 * Created by panuska on 7.4.15.
 */
public class MppCategory extends CsaEntity {
    public MppCategory(String json) {
        super(json);
    }

    @Override
    public String toString() {
        return getProperty("displayName");
    }
}
