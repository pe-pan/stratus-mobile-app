package com.hp.dsg.stratus.entities;

import java.util.Date;

/**
 * Created by panuska on 24.9.14.
 */
public abstract class Entity {
    protected String context;

    protected Entity() {
    }

    public abstract void init(String o);

    public abstract String getId();
    public abstract String getProperty(String key);
    public abstract void setProperty(String key, String value);
    public abstract String removeProperty(String key);
    public abstract Object getObjectProperty(String key);
    public abstract void setObjectProperty(String key, Object value);
    public abstract Date getDateProperty(String key);
    public abstract Boolean getBooleanProperty(String key);
    public abstract String toJson();

}
