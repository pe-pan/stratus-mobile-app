package com.hp.dsg.stratus.rest.entities;

import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.entities.EntityHandler;
import com.hp.dsg.stratus.rest.Csa;
import com.hp.dsg.stratus.rest.Mpp;

import java.util.List;

/**
 * Created by panuska on 2.10.14.
 */
public class PersonHandler extends CsaEntityHandler {

    public PersonHandler() {
        super();
        this.context = "person";

//        columns.add(new Column("userName"));
//        columns.add(new Column("ext.csa_active_subscription_count"));
    }

    private String organizationId;

    private Entity getOrganization(String name) {
        List<Entity> organizations = EntityHandler.getHandler("organizations").list(false);
        for (Entity o : organizations) {
            if (o.getProperty("name").equals(name)) return o;
        }
        return null;
    }

    private String getOrganizationId() {
        if (organizationId != null) {
            return organizationId;
        }
        Entity o = getOrganization(Mpp.TENANT_NAME);
        if (o == null) {
            throw new IllegalStateException("There is no organization called CSADemo"); //todo hack
        }
        organizationId = o.getId();
        return organizationId;
    }

    protected String getListJson() {
        return client.doGet(Csa.REST_API+"/person/organization/"+getOrganizationId(), ContentType.JSON_JSON).getResponse();
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
