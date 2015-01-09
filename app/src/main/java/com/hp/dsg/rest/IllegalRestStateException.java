package com.hp.dsg.rest;

/**
 * Created by panuska on 9.7.13.
 */
public class IllegalRestStateException extends IllegalStateException {
    private String errorStream;
    private int responseCode;

    public IllegalRestStateException(int responseCode, String message, String errorStream, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
        this.errorStream = errorStream;
    }

    public String getErrorStream() {
        return errorStream;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
