package com.hp.dsg.stratus.rest.entities;


import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.Csa;

/**
 * Created by panuska on 2.10.14.
 */
public class OrganizationHandler extends CsaEntityHandler {

    public OrganizationHandler() {
        super();
        this.context = "organization";

//        columns.add(new Column("name"));
//        columns.add(new Column("description"));
    }

    protected String getListJson() {
        return client.doGet(Csa.REST_API+"/"+context, ContentType.JSON_JSON).getResponse();
    }

    @Override
    protected Entity create(Object object) {
        return new Organization(object);
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
