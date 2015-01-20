package com.hp.dsg.stratus.entities;

/**
 * Created by panuska on 24.9.14.
 */
public abstract class Entity {
    protected String context;

    protected Entity() {
    }

    protected abstract void init(String o);

    public abstract String getId();
    public abstract String getProperty(String key);
    public abstract void setProperty(String key, String value);
    public abstract String toJson();

}
