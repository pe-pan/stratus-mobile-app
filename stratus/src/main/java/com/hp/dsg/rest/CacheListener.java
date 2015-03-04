package com.hp.dsg.rest;

import java.io.InputStream;

/**
 * Created by panuska on 3.3.2015.
 */
public interface CacheListener {
    public InputStream onRequest(String pathName, Method method);
    public InputStream onResponse(String pathName, InputStream inner);
}
