package com.hp.dsg.stratus.rest.entities;

/**
 * Created by panuska on 30.1.2015.
 */
public class Server {
    public final ServerProperty[] properties;
    public final String serviceSubscriptionId;
    public final ServiceAction[] actions;

    protected Server(ServerProperty[] properties, String serviceSubscriptionId, ServiceAction[] actions) {
        this.properties = properties;
        this.serviceSubscriptionId = serviceSubscriptionId;
        this.actions = actions;
    }

}
