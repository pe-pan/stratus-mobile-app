package com.hp.dsg.rest;

import android.util.Log;

import com.hp.dsg.stratus.BuildConfig;
import com.hp.dsg.utils.IOUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by panuska on 10/26/12.
 */
public class RestClient {

    private static final String TAG = RestClient.class.getName();

    private final HashMap<String, String> cookies = new HashMap<>();
//    private String oldLocation = "";

    private void addCookieList(List<String> cookieList) {
        synchronized (cookies) {
            if (cookieList == null) {
                return;
            }
            Log.d(TAG, "Adding cookies:");
            for (String cookie : cookieList) {
                Log.d(TAG, cookie);
                String key = cookie.substring(0, cookie.indexOf('='));
                String value = cookie.substring(key.length()+1, cookie.indexOf(";", key.length()));
                cookies.put(key, value);
            }
            Log.d(TAG, "New cookies: "+cookies.toString());
        }
    }

    private String getCookieList() {
        synchronized (cookies) {
            if (cookies.size() == 0) {
                return "";
            }
            Set<String> keys = cookies.keySet();
            StringBuilder cookieList = new StringBuilder();
            for (String key : keys) {
                String value = cookies.get(key);
                cookieList.append(key).append('=').append(value).append(';');
            }
            return cookieList.substring(0, cookieList.length() - 1); // remove the last ';'
        }
    }

    /**
     * Posts given data to the given address and collects (re-send) cookies.
     * Also handles redirects; only first time it does POST, then it does GET.
     *
     * @param urlAddress where the request is being sent
     * @param formData if null, GET method is used; POST otherwise
     * @param method which method will be used
     * @return response of the request
     */
    public String doRequest(String urlAddress, String formData, Method method, ContentType contentType) {
        HttpURLConnection conn = null;
        try {
            boolean redirect = false;
            do {
                if (method == Method.GET && formData != null) {
                    urlAddress = urlAddress + "?"+formData;
                    formData = null;
                }
                Log.d(TAG, "At: "+urlAddress);
                URL url = new URL(urlAddress);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(formData != null);
                conn.setDoInput(true);
                conn.setAllowUserInteraction(false);
                conn.setInstanceFollowRedirects(false);
                conn.setReadTimeout(120*1000);          //todo shouldn't be just 10*1000 -> offerings need longer timeout
                conn.setConnectTimeout(120*1000);
                String methodName = redirect ? "GET" : method.toString();
                Log.d(TAG, "Doing "+methodName);
                conn.setRequestMethod(methodName);
                switch (contentType) {
                    case JSON_JSON: {
                        Log.d(TAG, "JSON_JSON documents");
                        conn.setRequestProperty("Content-type", "application/json;type=collection");
                        conn.setRequestProperty("Accept", "application/json");
                        break;
                    }
                    case FORM_JSON: {      //todo because of generating requirements using apmuiservices
                        Log.d(TAG, "FORM_JSON documents");
                        conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("Accept", "application/json");
                        break;
                    }
                    case JSON_MULTI: {
                        Log.d(TAG, "JSON_MULTI documents");
                        conn.setRequestProperty("Content-type", "multipart/form-data; boundary=----WebKitFormBoundaryPxXpP71FeJBaYBqa");
                        conn.setRequestProperty("Accept", "application/json");
                        break;
                    }
                    case XML_XML: {
                        Log.d(TAG, "XML_XML documents");
                        conn.setRequestProperty("Content-type", "application/xml; charset=UTF-8");
                        conn.setRequestProperty("Accept", "application/xml");
                        break;
                    }
                }
                if (headerName != null && headerValue != null) {
                    conn.setRequestProperty(headerName, headerValue);
                    Log.d(TAG, "Setting "+headerName+": "+headerValue);
                }
                conn.setRequestProperty("User-Agent", "Stratus Mobile Application/1.0; (c) SDG");

                String cookieList = getCookieList();
                Log.d(TAG, "Sending cookies: "+cookieList);
                conn.setRequestProperty("Cookie", cookieList);

                // write the data
                if (!redirect && formData != null) {
                    if (contentType == ContentType.JSON_MULTI) {  //todo hack
                        formData =  "------WebKitFormBoundaryPxXpP71FeJBaYBqa\n" +
                                    "Content-Disposition: form-data; name=\"requestForm\"\n\n" +
                                    formData +
                                    "------WebKitFormBoundaryPxXpP71FeJBaYBqa\n";
                    }
                    Log.d(TAG, "Data size: " + formData.length());
                    conn.setRequestProperty("Content-Length", Integer.toString(formData.length()));
                    Log.d(TAG, "Posting: " + formData);
                    IOUtils.write(formData, conn.getOutputStream());
                    conn.getOutputStream().flush();
                    conn.getOutputStream().close();
                }
                Log.d(TAG, "Code: "+conn.getResponseCode()+"; Message: "+conn.getResponseMessage());

                if (conn.getResponseCode() == 301 || conn.getResponseCode() == 302) {
                    urlAddress = conn.getHeaderField("Location");
                    Log.d(TAG, "Redirect to: " + urlAddress);
                    redirect = true;
                    conn.disconnect();
                } else {
                    redirect = false;
                }
                addCookieList(conn.getHeaderFields().get("Set-Cookie"));
            } while (redirect);

            // Get the response

            Log.d(TAG, "Receiving:");
            String response = IOUtils.toString(conn.getInputStream());
            conn.getInputStream().close();
            Log.d(TAG, response);
            return response;
        } catch (IOException e) {
            Log.d(TAG, "Exception caught", e);
            String errorStream = null;
            int responseCode = 0;
            try {
                if (conn != null && conn.getErrorStream() != null) {
                    responseCode = conn.getResponseCode();
                    Log.d(TAG, "Response Code: "+ responseCode);
                    errorStream = IOUtils.toString(conn.getErrorStream());
                    Log.d(TAG, "Error stream: "+ errorStream);
                }
            } catch (IOException e1) {
                Log.d(TAG, "Cannot convert error stream to string");
            }
            if (e.getMessage().equals("Received authentication challenge is null")) {
                responseCode = HttpURLConnection.HTTP_UNAUTHORIZED; // todo hack, FMI, see http://stackoverflow.com/questions/1357372
            }
            throw new IllegalRestStateException(responseCode, errorStream == null ? e.getMessage() : errorStream, errorStream, e);
        } finally {
            if (conn != null) {  // close the connection
                conn.disconnect();
            }
        }
    }

    protected String headerName = null;
    protected String headerValue = null;
    public void setCustomHeader(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    private String serializeParameters(String [][]data) {
        if (data == null) return null;
        StringBuilder returnValue = new StringBuilder();
        for (String[] parameter : data) {
            if (BuildConfig.DEBUG && parameter.length != 2) throw new AssertionError();
            String key = parameter[0];
            String value  = parameter[1];
            if (value == null) {
                value = "";
            }
            try {
                returnValue.
                        append('&').                    // even the very first parameter starts with '&'
                        append(key).append('=').
                        append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
        return returnValue.substring(1);                // remove the starting '&' character
    }

    public String doRequest(String urlAddress, String[][] formData, Method method, ContentType contentType) {
        return doRequest(urlAddress, serializeParameters(formData), method, contentType);
    }

    public String doGet(String url) {
        return doRequest(url, (String) null, Method.GET, ContentType.XML_XML);
    }

    public String doGet(String url, ContentType contentType) {
        return doRequest(url, (String) null, Method.GET, contentType);
    }

    public String doPost(String url, String data) {
        return doRequest(url, data, Method.POST, ContentType.JSON_JSON);
    }

    public String doPost(String url, String[][] data) {
        return doRequest(url, serializeParameters(data), Method.POST, ContentType.NONE);
    }

    public String doPost(String url, String data, ContentType type) {
        return doRequest(url, data, Method.POST, type);
    }

    public String doPut(String url, String data, ContentType type) {
        return doRequest(url, data, Method.PUT, type);
    }

    public String doPut(String url, String data) {
        return doRequest(url, data, Method.PUT, ContentType.JSON_JSON);
    }

    public String doPut(String url, String[][] data) {
        return doRequest(url, serializeParameters(data), Method.PUT, ContentType.JSON_JSON);
    }

    public String doDelete(String url) {
        return doRequest(url, (String) null, Method.DELETE, ContentType.JSON_JSON);
    }

}
