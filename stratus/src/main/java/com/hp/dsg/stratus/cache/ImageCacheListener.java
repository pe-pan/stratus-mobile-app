package com.hp.dsg.stratus.cache;

import android.app.Activity;

import com.hp.dsg.rest.CacheListener;
import com.hp.dsg.rest.Method;

import java.io.InputStream;

/**
 * Created by panuska on 3.3.2015.
 */
public class ImageCacheListener implements CacheListener {

    private Activity context;
    public ImageCacheListener(Activity context) {
        this.context = context;
    }

    @Override
    public InputStream onRequest(String pathName, Method method) {
        return Cache.getFileInputStream(context, pathName);
    }

    @Override
    public InputStream onResponse(String pathName, InputStream inner) {
        Cache.writeFileInputStream(context, pathName, inner);  // first write the file down
        return Cache.getFileInputStream(context, pathName);  // then returns the file content
    }
}
