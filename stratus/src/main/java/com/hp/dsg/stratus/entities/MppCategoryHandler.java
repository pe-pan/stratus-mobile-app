package com.hp.dsg.stratus.entities;

import com.hp.dsg.stratus.Mpp;

import static com.hp.dsg.stratus.Mpp.M_STRATUS;

/**
 * Created by panuska on 7.4.15.
 */
public class MppCategoryHandler extends CsaEntityHandler {

    private MppCategoryHandler() {
        super();
        this.context = "mpp-category";
    }

    public static final MppCategoryHandler INSTANCE = new MppCategoryHandler();

    @Override
    protected Entity newEntity(String json) {
        return new MppCategory(json);
    }

    @Override
    protected String getListJson() {
        return M_STRATUS.doGet(Mpp.REST_PATHNAME + context + "/service");
    }
}
