package com.hp.dsg.stratus.rest.entities;


import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.Csa;

/**
 * Created by panuska on 2.10.14.
 */
public class OfferingHandler extends CsaEntityHandler {

    public OfferingHandler() {
        super();
        this.context = "offering";

//        columns.add(new Column("name"));
//        columns.add(new Column("description"));
//        columns.add(new Column("state"));
//        columns.add(new Column("@deleted"));
    }

    protected String getListJson() {
        return client.doGet(Csa.REST_API+"/service/offering/", ContentType.JSON_JSON).getResponse();
    }

    public Entity create(Object o) {
        return new Offering(o);
    }

    @Override
    public Entity update(Entity entity) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public void delete(Entity entity) {
        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public Entity get(String id) {
        String json = client.doGet(Csa.REST_API+"/service/offering/"+id, ContentType.JSON_JSON).getResponse();
        lastRefresh = System.currentTimeMillis(); // todo kind of hack (it assumes get() method is called when refreshing the list...)
        return create(json);
    }
}
