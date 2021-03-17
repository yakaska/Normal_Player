package ru.myitschool.normalplayer.utils;

import android.content.Context;

import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class CacheUtil {

    public static synchronized Cache getPlayerCache(Context context) {
        File cacheDirectory = new File(context.getCacheDir().getAbsolutePath() + "/NormalPlayer");
        return new SimpleCache(cacheDirectory, new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100));
    }
}
