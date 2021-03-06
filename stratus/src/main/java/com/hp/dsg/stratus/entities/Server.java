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
        String thisDemoName = (String)this.getProperty(ServerProperty.DEMO_NAME).value;
        String thatDemoName = (String)another.getProperty(ServerProperty.DEMO_NAME).value;
        if (thisDemoName == ServerProperty.INFRASTRUCTURE_SERVICE.value) return -1;  // make sure that Infrastructure service is always on top in the list
        if (thatDemoName == ServerProperty.INFRASTRUCTURE_SERVICE.value) return 1;
        return (thisDemoName).compareTo(thatDemoName);
    }
}
