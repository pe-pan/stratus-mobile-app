package com.hp.dsg.stratus;

import com.hp.dsg.rest.AuthenticatedClient;
import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.entities.EntityHandler;
import com.hp.dsg.stratus.entities.MppOffering;
import com.hp.dsg.stratus.entities.MppOfferingHandler;
import com.hp.dsg.stratus.entities.MppRequest;
import com.hp.dsg.stratus.entities.MppRequestHandler;
import com.hp.dsg.stratus.entities.MppSubscriptionHandler;
import com.jayway.jsonpath.JsonPath;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by panuska on 6.1.2015.
 */
public class Mpp extends AuthenticatedClient {
    private static final String TAG = Mpp.class.getSimpleName();

    public static final String STRATUS_HOSTNAME = "https://csa4.hpswdemoportal.com/";
    public static final String REST_PATHNAME = "csa/api/mpp/";

    public static final String IDM_REST_URL = "idm-service/v2.0/tokens";
    public static final String TENANT_NAME = "CSADemo"; //todo should not be built-in

    private String username;
    private String password;

    public static final Mpp M_STRATUS = new Mpp(STRATUS_HOSTNAME);
    public Mpp(String hostName) {
        super(hostName);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String authenticate() {
        String json = "{\n" +
                "    \"passwordCredentials\":{\n" +
                "        \"username\":\""+username+"\",\n" +
                "        \"password\":\""+password+"\"\n" +
                "    },\n" +
                "    \"tenantName\":\""+TENANT_NAME+"\"\n" +
                "}";
        client.setCustomHeader("Authorization", "Basic "+"aWRtVHJhbnNwb3J0VXNlcjppZG1UcmFuc3BvcnRVc2Vy"); // "idmTransportUser:idmTransportUser" in Base64

        String response = client.doPost(IDM_REST_URL, json);
        String token = JsonPath.read(response, "$.token.id");
        setAuthenticationHeader(token);
        return token;
    }

    @Override
    public String getLoggedUserName() {
        return username;
    }

    public List<Entity> getSubscriptions(boolean enforce) {
        EntityHandler handler = MppSubscriptionHandler.INSTANCE;
        return handler.list(enforce);
    }

    public List<Entity> getOfferings(boolean enforce) {
        EntityHandler handler = MppOfferingHandler.INSTANCE;
        return handler.list(enforce);
    }

    public String createSubscription(MppOffering offering, String oppId, int days, String subscriptionName, String emailAddress) {

        EntityHandler reqHandler = MppRequestHandler.INSTANCE;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

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

        EntityHandler offHandler = MppOfferingHandler.INSTANCE;
        offHandler.loadDetails(offering);
        if (!"EXECUTIVE_DEMOS".equals(offering.getProperty("category.name"))) {
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
}
