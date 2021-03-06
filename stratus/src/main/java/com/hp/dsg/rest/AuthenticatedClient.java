package com.hp.dsg.rest;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by panuska on 14.10.14.
 */
public abstract class AuthenticatedClient  {
    protected RestClient client;

    public abstract String authenticate();

    public abstract String getLoggedUserName();

    protected AuthenticatedClient(String hostName) {
        this.client = new RestClient(hostName);
    }

    public boolean isAuthenticated() {
        return client.headerValue != null;
    }

    public void setAuthenticationHeader(String headerValue) {
        client.setCustomHeader("X-Auth-Token", headerValue);
        client.headerValue = headerValue;
    }

    public interface AuthenticationListener {
        boolean onNoAuthentication();
    }

    private AuthenticationListener listener;

    public void setAuthenticationListener(AuthenticationListener listener) {
        this.listener = listener;
    }

    public String doGet(String url) {
        return doGet(url, ContentType.JSON_JSON);
    }

    public String doGet(String url, ContentType type) {
        try {
            return client.doGet(url, type);
        } catch (IllegalRestStateException e) {
            if (!client.hasActiveInternetConnection()) return null;
            if (e.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED || e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                if (listener != null) listener.onNoAuthentication();
                return client.doGet(url, type);
            }
            throw e;
        }
    }

    public InputStream doGet(String pathName, ContentType type, CacheListener cacheListener) {
        try {
            return client.doGet(pathName, type, cacheListener);
        } catch (IllegalRestStateException e) {
            if (!client.hasActiveInternetConnection()) return null;
            if (e.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED || e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                if (listener != null) listener.onNoAuthentication();
                return client.doGet(pathName, type, cacheListener);
            }
            throw e;
        }
    }

    public String doPut(String url, String data) {
        try {
            return client.doPut(url, data);
        } catch (IllegalRestStateException e) {
            if (!client.hasActiveInternetConnection()) return null;
            if (e.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED || e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                if (listener != null) listener.onNoAuthentication();
                return client.doPut(url, data);
            }
            throw e;
        }
    }

    public String doDelete(String url) {
        try {
            return client.doDelete(url);
        } catch (IllegalRestStateException e) {
            if (!client.hasActiveInternetConnection()) return null;
            if (e.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED || e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                if (listener != null) if (!listener.onNoAuthentication()) ;
                return client.doDelete(url);
            }
            throw e;
        }
    }

    public String doPost(String url, String data) {
        try {
            return client.doPost(url, data);
        } catch (IllegalRestStateException e) {
            if (!client.hasActiveInternetConnection()) return null;
            if (e.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED || e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                if (listener != null) listener.onNoAuthentication();
                return client.doPost(url, data);
            }
            throw e;
        }
    }

    public String doPost(String url, String data, ContentType type) {
        try {
            return client.doPost(url, data, type);
        } catch (IllegalRestStateException e) {
            if (!client.hasActiveInternetConnection()) return null;
            if (e.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED || e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                if (listener != null) listener.onNoAuthentication();
                return client.doPost(url, data);
            }
            throw e;
        }
    }
}
