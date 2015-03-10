package com.hp.dsg.stratus.cache;

import android.graphics.drawable.Drawable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by panuska on 10.3.2015.
 */
public class MemoryCache {
    private static final Map<String, Drawable> images = new WeakHashMap<>();

    public static void putImage(String url, Drawable image) {
        images.put(url, image);
    }

    public static Drawable getImage(String url) {
        return images.get(url);
    }
}
