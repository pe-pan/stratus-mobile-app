package com.hp.dsg.stratus.entities;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panuska on 6.1.15.
 */
public class MppRequest extends CsaEntity {
    public MppRequest(String json) {
        super(json);
    }

    @Override
    protected String propertiesToJson() {
        Map<String, Object> fields = new HashMap<>();
        StringBuilder b = new StringBuilder();
        for (String key : properties.keySet()) {
            Object value = properties.get(key);
            if (key.startsWith("field_")) {
                fields.put(key, value);
            } else {
                b.append("\"").append(key).append("\" : ").append(toJsonString(value)).append(", ");
            }
        }
        b.deleteCharAt(b.length()-1);  // remove the very last comma
        b.deleteCharAt(b.length()-1);

        if (!fields.isEmpty()) {
            b.append(", \"fields\" : {");
            for (String key : fields.keySet()) {
                Object value = fields.get(key);
                b.append("\"").append(key.substring("field_".length())).append("\" : ").append(toJsonString(value)).append(", ");
            }
            b.deleteCharAt(b.length()-1);  // remove the very last comma
            b.deleteCharAt(b.length()-1);
            b.append("}");
        }

        return b.toString();
    }

}
