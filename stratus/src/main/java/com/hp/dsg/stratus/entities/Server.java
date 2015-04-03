package com.hp.dsg.stratus.entities;

/**
 * Created by panuska on 30.1.2015.
 */
public class Server implements Comparable<Server>{
    public final ServerProperty[] properties;
    public final String serviceSubscriptionId;
    public final ServiceAction[] actions;

    protected Server(ServerProperty[] properties, String serviceSubscriptionId, ServiceAction[] actions) {
        this.properties = properties;
        this.serviceSubscriptionId = serviceSubscriptionId;
        this.actions = actions;
    }

    public ServerProperty getProperty(String name) { //todo as DEMO_NAME property is not being cached,
        for (ServerProperty property : properties) { //this will be called over and over when comparing
            if (property.name.equals(name)) return property;
        }
        return null;
    }

    @Override
    public int compareTo(Server another) {
        return ((String)this.getProperty(ServerProperty.DEMO_NAME).value).compareTo((String)another.getProperty(ServerProperty.DEMO_NAME).value);
    }
}
