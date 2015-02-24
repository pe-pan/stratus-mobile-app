package com.hp.dsg.stratus.entities;


import com.hp.dsg.rest.ContentType;
import com.hp.dsg.stratus.Mpp;
import static com.hp.dsg.stratus.Mpp.M_STRATUS;

/**
 * Created by panuska on 6.1.15.
 */
public class MppOfferingHandler extends CsaEntityHandler {

    private MppOfferingHandler() {
        super();
        this.context = "mpp-offering";
    }

    public static final MppOfferingHandler INSTANCE = new MppOfferingHandler();

    @Override
    protected Entity newEntity(String json) {
        return new MppOffering(json);
    }

    @Override
    public Entity loadDetails(Entity entity) {
        String offeringId = entity.getId();
        String catalogId = entity.getProperty("catalogId");
        String category = entity.getProperty("category.name");

        String json = M_STRATUS.doGet(Mpp.REST_API+"/mpp/mpp-offering/"+offeringId+"?catalogId="+catalogId+"&category="+category, ContentType.JSON_JSON).getResponse();
        entity.init(json); //todo mark somewhere details have been loaded
        return entity;
    }
}
