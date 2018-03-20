package com.hp.dsg.rest;

import android.util.Log;

import com.hp.dsg.stratus.BuildConfig;
import com.hp.dsg.stratus.Mpp;
import com.hp.dsg.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by panuska on 10/26/12.
 */
public class RestClient {

    private final String hostName;

    public RestClient(String hostName) {
        this.hostName = hostName;
        trustAllCertificates(); //todo breaking security!
    }

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
     * @param pathName where the request is being sent (URL = hostName + pathName); hostName = http(s)://domain.domain.com/; pathName = /context/context/context
     * @param formData if null, GET method is used; POST otherwise
     * @param method which method will be used
     * @return response of the request
     */
    public InputStream doRequest(String pathName, String formData, Method method, ContentType contentType, CacheListener cacheListener) {
        HttpsURLConnection conn = null;
        try {
            if (cacheListener != null) {
                InputStream stream = cacheListener.onRequest(pathName, method);
                if (stream != null) return stream;
            }
            boolean redirect = false;
            do {
                if (method == Method.GET && formData != null) {
                    pathName = pathName + "?"+formData;
                    formData = null;
                }
                Log.d(TAG, "At: "+pathName);
                URL url = new URL(hostName+pathName);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setHostnameVerifier(NO_HOSTNAME_VERIFIER);
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
                    pathName = conn.getHeaderField("Location");
                    Log.d(TAG, "Redirect to: " + pathName);
                    if (pathName.startsWith(hostName)) pathName = pathName.substring(hostName.length());
                    redirect = true;
                    conn.disconnect();
                } else {
                    redirect = false;
                }
                addCookieList(conn.getHeaderFields().get("Set-Cookie"));
            } while (redirect);

            // Get the response

            Log.d(TAG, "Receiving:");
            InputStream response = new ConnectionInputStream(conn);

            if (cacheListener != null) {
                return cacheListener.onResponse(pathName, response);
            } else {
                return response;
            }
        } catch (IOException e) {
            handleIOException(conn, e);
            return null; // will never get executed
        }
    }

    /**
     * Implementation of an {@link InputStream} coming from an {@link HttpURLConnection} that closes
     * this connection upon InputStream is closed.
     */
    private class ConnectionInputStream extends InputStream {
        private HttpURLConnection connection;
        private InputStream inputStream;

        private ConnectionInputStream(HttpURLConnection connection) throws IOException {
            this.connection = connection;
            this.inputStream = connection.getInputStream();
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public void close() throws IOException {
            connection.disconnect();
            super.close();
        }

        public HttpURLConnection getConnection() {
            return connection;
        }

        public InputStream getInputStream() {
            return inputStream;
        }
    }

    private void handleIOException(HttpURLConnection conn, IOException e) {
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
        if (conn != null) {
            conn.disconnect();
        }
        throw new IllegalRestStateException(responseCode, errorStream == null ? e.getMessage() : errorStream, errorStream, e);
    }

    public String doRequest(String urlAddress, String formData, Method method, ContentType contentType) {
        InputStream response = null;
        try {
            response = doRequest(urlAddress, formData, method, contentType, null);
            Log.d(TAG, "Receiving:");
            String responseString = IOUtils.toString(response);
            response.close();
            Log.d(TAG, responseString);
            return responseString;
        } catch (IOException e) {
            if (response instanceof ConnectionInputStream) {
                handleIOException(((ConnectionInputStream) response).getConnection(), e);
                return null; // will never get executed
            } else {
                Log.e(TAG, "Exception when doing "+method.toString()+" at "+urlAddress, e);
                throw new IllegalStateException("Exception when doing "+method.toString()+" at "+urlAddress, e);
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

    public InputStream doGet(String pathName, ContentType contentType, CacheListener cacheListener) {
        return doRequest(pathName, null, Method.GET, contentType, cacheListener);
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

    protected static boolean hasActiveInternetConnection() {
        try {
            HttpsURLConnection urlc = (HttpsURLConnection) (new URL(Mpp.STRATUS_HOSTNAME).openConnection());
            urlc.setHostnameVerifier(NO_HOSTNAME_VERIFIER);
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return (urlc.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            return false;
        }
    }

    //todo breaks security!
    public static void trustAllCertificates() {
        TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {

            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{trm}, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "When registering TrustManager to trust all certificates");
        } catch (KeyManagementException e) {
            Log.e(TAG, "When registering TrustManager to trust all certificates");
        }
    }

    //todo breaks security!
    private static HostnameVerifier NO_HOSTNAME_VERIFIER = new HostnameVerifier () {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
