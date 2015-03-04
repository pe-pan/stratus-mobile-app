package com.hp.dsg.stratus.entities;

import com.hp.dsg.stratus.Mpp;
import static com.hp.dsg.stratus.Mpp.M_STRATUS;

/**
 * Created by panuska on 6.1.15.
 */
public class MppSubscriptionHandler extends CsaEntityHandler {

    private MppSubscriptionHandler() {
        super();
        this.context = "mpp-subscription";
    }

    public static final MppSubscriptionHandler INSTANCE = new MppSubscriptionHandler();

    @Override
    protected Entity newEntity(String json) {
        return new MppSubscription(json);
    }

    @Override
    public Entity loadDetails(Entity entity) {
        String json = M_STRATUS.doGet(Mpp.REST_PATHNAME + "mpp-subscription/" + entity.getId());
        entity.init(json); //todo mark somewhere details have been loaded
        return entity;
    }

    public String delete(Entity entity) {
        return M_STRATUS.doDelete(Mpp.REST_PATHNAME + "mpp-subscription/" + entity.getId());
    }
}
