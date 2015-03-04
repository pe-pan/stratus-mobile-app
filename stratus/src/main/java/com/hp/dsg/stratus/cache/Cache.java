package com.hp.dsg.stratus.cache;

import android.app.Activity;
import android.util.Log;

import com.hp.dsg.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * Created by panuska on 2.3.2015.
 */
public class Cache {
    private static final String TAG = Cache.class.getSimpleName();

    private static String mapUrlToFileName(String url) {
        return URLEncoder.encode(url);
    }

    public static InputStream getFileInputStream(Activity context, String pathName) {
        String fileName = mapUrlToFileName(pathName);
        File file = new File(context.getCacheDir(), fileName);
        try {
            return file.exists() ? new FileInputStream(file) : null;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception when opening an input stream "+file.getAbsolutePath());
            return null;
        }
    }

    public static void writeFileInputStream(Activity context, String pathName, InputStream stream) {
        String fileName = mapUrlToFileName(pathName);
        File file = new File(context.getCacheDir(), fileName);
        try {
            OutputStream out = new FileOutputStream(file);
            IOUtils.copy(stream, out);
            stream.close();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception when writing the file " + file.getAbsolutePath());
        }
    }
}
