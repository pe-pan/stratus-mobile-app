package com.hp.dsg.stratus.entities;

import com.hp.dsg.stratus.rest.entities.MppInstanceHandler;
import com.hp.dsg.stratus.rest.entities.MppOfferingHandler;
import com.hp.dsg.stratus.rest.entities.MppRequestHandler;
import com.hp.dsg.stratus.rest.entities.MppSubscriptionHandler;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by panuska on 2.10.14.
 */
public abstract class EntityHandler {

    private static Map<String, EntityHandler> handlers;
    public static void initHandlers() {
        handlers = new LinkedHashMap<>();
        final Class[] entityClasses = new Class[] {
                MppSubscriptionHandler.class, MppOfferingHandler.class, MppRequestHandler.class, MppInstanceHandler.class};
        for (Class entityClass : entityClasses) {
            try {
                EntityHandler handler = (EntityHandler) entityClass.newInstance();
                handlers.put(handler.getContexts(), handler);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static EntityHandler getHandler(String contexts) {
        return handlers.get(contexts);
    }
    public static Set<String> getHandlerNames() {
        return handlers.keySet();
    }

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

    public String getContexts() {
        return context+"s";
    }

    public abstract List<Entity> list(boolean enforce);

    public abstract String create(Entity entity);

    public abstract Entity update(Entity entity);
    public abstract void delete(Entity entity);

    public Entity get(String id) {
        throw new IllegalStateException("Not implemented");
    }

    public abstract Entity loadDetails(Entity entity);
}
