package com.hp.dsg.stratus.entities;

import android.util.Log;

import com.hp.dsg.utils.StringUtils;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by panuska on 2.10.14.
 */
public class CsaEntity extends Entity {
    private static final String TAG = CsaEntity.class.getSimpleName();
    protected Map<String, Object> properties;
    private boolean isDirty;
    protected String json;

    public CsaEntity(String json) {
        init(json);
    }

    @Override
    public void init(String o) {
        this.json = o;
        properties = new HashMap<>();
        isDirty = false;
    }

    public String getId() {
        String self = getProperty("@self");
        int index = self.lastIndexOf('/');
        return self.substring(index+1);
    }

    @Override
    public String getProperty(String key) {
        Object value = getObjectProperty(key);
//        return value == null ? "null" : value.toString().replace('\n', ' '); //todo new lines should be removed differently
        return StringUtils.nullifyNullObject(value);
    }

    public Object getObjectProperty(String key) {
        Object value = properties.get(key);
        if (value == null) {
            try {
                value = JsonPath.read(json, "$." + key);
            } catch (InvalidPathException e) {
                Log.d(TAG, "Invalid Path $." + key);
            }
            if (value != null) {
                properties.put(key, value);
            }
        }
        return value;
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public Date getDateProperty(String key) {
        Object value = getObjectProperty(key);
        if (value == null) return null;
        try {
            return sdf.parse((String)value);
        } catch (ParseException e) {
            Log.d(TAG, "Cannot parse property "+key+": "+value, e);
        }
        return null;
    }

    public String removeProperty(String key) {
        String value = properties.remove(key).toString();
        if (value != null) isDirty = true;
        return value;
    }

    @Override
    public void setProperty(String key, String value) {
        setObjectProperty(key, value);
    }

    public void setObjectProperty(String key, Object value) {
        properties.put(key, value);
        isDirty = true;
    }


    @Override
    public String toJson() {
        if (isDirty) {
            json = "{ "
                    +propertiesToJson()
                    + " }";
            isDirty = false;
        }
        return json;
    }

    protected String propertiesToJson() {
        StringBuilder b = new StringBuilder();
        for (String key : properties.keySet()) {
            Object value = properties.get(key);
            b.append("\"").append(key).append("\" : ").append(StringUtils.toJsonString(value)).append(", ");
        }
        b.deleteCharAt(b.length()-1);  // remove the very last comma
        b.deleteCharAt(b.length()-1);

        return b.toString();
    }

}
