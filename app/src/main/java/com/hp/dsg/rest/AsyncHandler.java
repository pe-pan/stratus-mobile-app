package com.hp.dsg.rest;

import java.net.HttpURLConnection;

/**
 * Created by panuska on 2/13/13.
 */
public interface AsyncHandler extends Runnable {
    public void setConnection(HttpURLConnection conn);
}
