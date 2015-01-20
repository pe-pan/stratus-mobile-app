package com.hp.dsg.stratus.rest;

import com.hp.dsg.rest.AuthenticatedClient;
import com.hp.dsg.rest.HttpResponse;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.entities.EntityHandler;
import com.jayway.jsonpath.JsonPath;

import java.util.List;

/**
 * Created by panuska on 6.1.2015.
 */
public class Mpp extends AuthenticatedClient {
    private static final String TAG = Mpp.class.getSimpleName();

    public static final String STRATUS_URL = "https://csa4.hpswdemoportal.com";
    public static final String REST_URL = STRATUS_URL+"/csa";
    public static final String REST_API = REST_URL +"/api";
    public static final String REST_URI = REST_URL +"/rest";

    public static final String IDM_REST_URL = STRATUS_URL+"/idm-service/v2.0/tokens";
    public static final String TENANT_NAME = "CSADemo"; //todo should not be built-in

    private String username;
    private String password;

    public final static Mpp M_STRATUS = new Mpp();
    public Mpp() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void authenticate() {
        String json = "{\n" +
                "    \"passwordCredentials\":{\n" +
                "        \"username\":\""+username+"\",\n" +
                "        \"password\":\""+password+"\"\n" +
                "    },\n" +
                "    \"tenantName\":\""+TENANT_NAME+"\"\n" +
                "}";
        client.setCustomHeader("Authorization", "Basic "+"aWRtVHJhbnNwb3J0VXNlcjppZG1UcmFuc3BvcnRVc2Vy");

        HttpResponse response = client.doPost(IDM_REST_URL, json);
        String token = JsonPath.read(response.getResponse(), "$.token.id");
        client.clearCustomHeader();
        client.setCustomHeader("X-Auth-Token", token);
    }

    @Override
    public String getLoggedUserName() {
        return null;
    }

    public List<Entity> getSubscriptions(boolean enforce) {
        EntityHandler handler = EntityHandler.getHandler("mpp-subscriptions");
        return handler.list(enforce);
    }

    public List<Entity> getOfferings(boolean enforce) {
        EntityHandler handler = EntityHandler.getHandler("mpp-offerings");
        return handler.list(enforce);
    }

}
