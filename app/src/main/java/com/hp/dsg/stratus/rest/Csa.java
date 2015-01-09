package com.hp.dsg.stratus.rest;

import android.util.Log;

import com.hp.dsg.rest.AuthenticatedClient;
import com.hp.dsg.rest.HttpResponse;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.entities.EntityHandler;

import java.util.List;

/**
 * Created by panuska on 5.1.2015.
 */
public class Csa extends AuthenticatedClient {
    private static final String TAG = "CSA";
    public static final String STRATUS_URL = "https://csa4.hpswdemoportal.com";
    public static final String REST_URL = STRATUS_URL+"/csa";
    public static final String REST_URI = REST_URL +"/rest";
    public static final String REST_API = REST_URL +"/api";

    private String username;
    private String password;


    public final static Csa STRATUS = new Csa();
    private String loggedUserName;

    public Csa() {

    }

    public Csa(String username, String password) {
        this.username = username;
        this.password = password;
//        loggedUserName = null;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void authenticate() {
        final String[][] data = {
                { "j_username", username },
                { "j_password", password }
        };
        HttpResponse response = client.doPost(REST_URL +"/j_spring_security_check", data);
        if (response.getLocation().endsWith("login_error=1")) {
            throw new IllegalStateException("Cannot login to CSA");
        }
        loggedUserName = username;
        Log.d(TAG, "CSA user " + username + " logged in");

    }


    @Override
    public String getLoggedUserName() {
        return loggedUserName;
    }

//    private static final EntityHandler subscriptionHandler = new SubscriptionHandler();

    public List<Entity> getSubscriptions() {
        EntityHandler handler = EntityHandler.getHandler("subscriptions");
        return handler.list(false);
//        return subscriptionHandler.list(false);
    }

}
