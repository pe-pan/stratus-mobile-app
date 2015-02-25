package com.hp.dsg.utils;

/**
 * Created by panuska on 11.2.2015.
 */
public class TimeUtils {
    public static String getPeriod (long past, long now) {
        long diff = past - now;
        if (diff <= 1000) {       // less than a second or already expired
            return ("Expired");
        } else {
            diff = diff / 1000;
            if (diff < 2*60) {
                return (diff+" seconds");
            } else {
                diff = diff / 60;
                if (diff < 2*60) {
                    return (diff+" minutes");
                } else {
                    diff = diff / 60;
                    if (diff < 2*24) {
                        return (diff+" hours");
                    } else {
                        diff = diff / 24;
                        if (diff < 2*7) {
                            return (diff+ " days");
                        } else {
                            diff = diff / 7;
                            return (diff+ " weeks");
                        }
                    }
                }
            }
        }
    }

    public static String getPeriod(long past) {
        long now = System.currentTimeMillis();
        return getPeriod(past, now);
    }
}
