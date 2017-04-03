package com.hp.dsg.stratus.entities;


import com.hp.dsg.stratus.Mpp;
import static com.hp.dsg.stratus.Mpp.M_STRATUS;

/**
 * Created by panuska on 6.1.15.
 */
public class MppInstanceHandler extends CsaEntityHandler {

    private  MppInstanceHandler() {
        super();
        this.context = "mpp-instance";
    }

    public static final MppInstanceHandler INSTANCE = new MppInstanceHandler();

    @Override
    protected Entity newEntity(String json) {
        return new MppInstance(json);
    }

    @Override
    public Entity loadDetails(Entity entity) {
        String instanceId = entity.getProperty("id");
        String catalogId = entity.getProperty("catalogId");

        String json = M_STRATUS.doGet(Mpp.REST_PATHNAME + "mpp-instance/"+instanceId+"?catalogId="+catalogId);
        if (json == null) { // no internet connection
            return null;
        }

        entity.init(json); //todo mark somewhere details have been loaded
        return entity;
    }
}
