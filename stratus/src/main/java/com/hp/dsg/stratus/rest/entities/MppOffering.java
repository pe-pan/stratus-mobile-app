package com.hp.dsg.stratus.rest.entities;

import android.util.Log;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

/**
 * Created by panuska on 6.1.15.
 */
public class MppOffering extends CsaEntity {
    private static final String TAG = MppOffering.class.getSimpleName();

    public MppOffering(String json) {
        super(json);
    }

    @Override
    public String toString() {
        return getProperty("displayName");
    }

    public Object getObjectProperty(String key) {
        Object value = properties.get(key);
        if (value == null) {
                if (key.startsWith("field_")) {
                    String key2 = key.substring(6); //remove the field_ prefix
                    try {
                        value = JsonPath.read(json, "$.fields[?(@.name=='"+key2+"')].id[0]");
                    } catch (InvalidPathException e) {
                        Log.d(TAG, "Invalid path $.fields[?(@.name=='"+key2+"')].id[0]", e);
                    }
                } else {
                    try {
                        value = JsonPath.read(json, "$." + key);
                    } catch (InvalidPathException e) {
                        Log.d(TAG, "Invalid path $." + key, e);
                    }
                }
            if (value != null) {
                properties.put(key, value);
            }
        }
        return value;
    }

}
