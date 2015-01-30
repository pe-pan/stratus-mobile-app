package com.hp.dsg.stratus.rest;

import com.hp.dsg.rest.AuthenticatedClient;
import com.hp.dsg.rest.HttpResponse;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.entities.EntityHandler;
import com.hp.dsg.stratus.rest.entities.MppInstance;
import com.hp.dsg.stratus.rest.entities.MppOffering;
import com.hp.dsg.stratus.rest.entities.MppRequest;
import com.hp.dsg.stratus.rest.entities.MppRequestHandler;
import com.hp.dsg.stratus.rest.entities.MppSubscription;
import com.jayway.jsonpath.JsonPath;

import java.text.SimpleDateFormat;
import java.util.Date;
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
        return username;
    }

    public List<Entity> getSubscriptions(boolean enforce) {
        EntityHandler handler = EntityHandler.getHandler("mpp-subscriptions");
        return handler.list(enforce);
    }

    public List<Entity> getOfferings(boolean enforce) {
        EntityHandler handler = EntityHandler.getHandler("mpp-offerings");
        return handler.list(enforce);
    }

    public String createSubscription(MppOffering offering, String oppId, int days, String subscriptionName, String emailAddress) {

        EntityHandler reqHandler = EntityHandler.getHandler("mpp-requests");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        MppRequest req = new MppRequest(null);
        req.setProperty("action", "ORDER");
        req.setProperty(MppRequestHandler.SERVICE_ID_KEY, offering.getId());       // todo hack; these properties are not being sent in json but are part of URL
        req.setProperty(MppRequestHandler.CATALOG_ID_KEY, offering.getProperty("catalogId"));
        req.setProperty("offeringName", offering.getProperty("displayName"));
        req.setProperty("categoryName", offering.getProperty("category.name"));
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime()+ days * 24 * 60 * 60 * 1000);
        req.setProperty("subscriptionName", subscriptionName);
        req.setProperty("subscriptionDescription", subscriptionName);
        req.setProperty("startDate", sdf.format(startDate));
        req.setProperty("endDate", sdf.format(endDate));

        EntityHandler offHandler = EntityHandler.getHandler("mpp-offerings");
        offHandler.loadDetails(offering);
        if (!offering.getProperty("category.name").equals("EXECUTIVE_DEMOS")) {
            String checkBoxId = offering.getProperty("field_FDABAA51_D5B2_0A11_2B13_19D86153F685");
            String oppDetailsId = offering.getProperty("field_OPPDETAILS");
            req.setObjectProperty(checkBoxId, true);
            req.setProperty(oppDetailsId, oppId);
        } else {
            String checkBoxId = offering.getProperty("field_E3332EC1_DFF5_3D5D_34A6_6F767683E54A");
            String emailAddressId = offering.getProperty("field_EmailAddress");
            req.setObjectProperty(checkBoxId, true);
            req.setProperty(emailAddressId, emailAddress);
        }
        return reqHandler.create(req);

    }

    public MppInstance getInstance(MppSubscription subscription) {
        EntityHandler subHandler = EntityHandler.getHandler("mpp-subscriptions");
        subscription = (MppSubscription) subHandler.loadDetails(subscription);

        String catalogId = subscription.getProperty("catalogId");
        String instanceId = subscription.getProperty("instanceId");

        EntityHandler instanceHandler = EntityHandler.getHandler("mpp-instances");
        MppInstance instance = new MppInstance("{ \"catalogId\" : \""+catalogId+"\", \"id\" : \""+instanceId+"\" }");
        instanceHandler.loadDetails(instance);
        return instance;
    }
}
