package com.hp.dsg.stratus.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by panuska on 2.10.14.
 */
public abstract class EntityHandler {

    protected List<Entity> lastEntities = null;
    protected List<Entity> filteredEntities = null;
    protected long lastRefresh;

    protected EntityHandler() {
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    public List<Entity> getFilteredEntities() {
        return filteredEntities;
    }

    public void setFilteredEntities(List<Entity> filteredEntities) {
        this.filteredEntities = filteredEntities;
    }

    public void resetFilteredEntities() {
        if (lastEntities == null) return;
        this.filteredEntities = new LinkedList<>(this.lastEntities);
    }

    protected abstract Entity newEntity(String json);

    protected String context = "";
    public String getContext() {
        return context;
    }

    public abstract List<Entity> list(boolean enforce);

    public abstract String create(Entity entity);

    public abstract Entity update(Entity entity);
    public abstract String delete(Entity entity);

    public Entity get(String id) {
        throw new IllegalStateException("Not implemented");
    }

    public abstract Entity loadDetails(Entity entity);
}
