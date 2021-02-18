package ru.myitschool.normalplayer.utils;

import android.content.Context;

import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class CacheUtil {

    private static Cache cache;

    public static synchronized Cache getCache(Context context) {
        if (cache == null) {
            File cacheDirectory = new File(context.getExternalFilesDir(null), "downloads");
            cache = new SimpleCache(cacheDirectory, new NoOpCacheEvictor());
        }
        return cache;
    }


}
