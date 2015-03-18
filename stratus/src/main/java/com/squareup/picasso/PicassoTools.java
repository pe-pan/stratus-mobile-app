package com.squareup.picasso;

import android.content.Context;

import java.io.File;

/**
 * Created by panuska on 13.3.2015.
 * Copied from http://stackoverflow.com/questions/22016382/invalidate-cache-in-picasso
 */
public class PicassoTools {

    public static void clearCache (Picasso p) {
        p.cache.clear();

        File cacheDir = Utils.createDefaultCacheDir(p.context);
        for (File file : cacheDir.listFiles()) {
            file.delete();
        }
    }

    public static long getDiskCacheSize(Context c) {
        File cacheDir = Utils.createDefaultCacheDir(c);
        long size = 0;
        for (File file : cacheDir.listFiles()) {
            size += file.length();
        }
        return size;
    }

    public static int getMemoryCacheSize(Picasso p) {
        return p.cache.size();
    }
}
