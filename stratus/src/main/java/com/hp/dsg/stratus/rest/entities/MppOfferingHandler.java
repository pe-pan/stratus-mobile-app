package com.hp.dsg.stratus.rest.entities;


import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.Csa;

/**
 * Created by panuska on 6.1.15.
 */
public class MppOfferingHandler extends CsaEntityHandler {

    public MppOfferingHandler() {
        super();
        this.context = "mpp-offering";
    }

    @Override
    protected String getListJson() {
        return client.doPost(Csa.REST_API+"/mpp/mpp-offering/filter", "{}").getResponse();
    }

    @Override
    protected Entity newEntity(Object object) {
        return new MppOffering(object);
    }

    @Override
    public Entity update(Entity entity) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public void delete(Entity entity) {
        throw new IllegalStateException("Not implemented!");
    }
}
