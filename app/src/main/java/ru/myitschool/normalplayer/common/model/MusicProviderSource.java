package ru.myitschool.normalplayer.common.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class MusicProviderSource {

    public static final String SOURCE_TYPE_KEY = "ru.myitschool.normalplayer.SOURCE_TYPE_KEY";

    protected Context context;

    protected Bitmap defaultBitmap;

    protected SOURCE_TYPE sourceType;

    public enum SOURCE_TYPE {
        INTERNAL(0),
        VK(1);

        private final long value;

        private static Map<Long, SOURCE_TYPE> map = new HashMap<>();

        SOURCE_TYPE(long value) {
            this.value = value;
        }

        static {
            for (SOURCE_TYPE source_type : SOURCE_TYPE.values()) {
                map.put(source_type.value, source_type);
            }
        }

        public long getValue() {
            return value;
        }

        public static SOURCE_TYPE valueOf(long sourceType) {
            return map.get(sourceType);
        }

    }

    public MusicProviderSource(Context context, SOURCE_TYPE sourceType) {
        this.context = context;
        this.sourceType = sourceType;
    }

    abstract Iterator<MediaMetadataCompat> iterator();

}
