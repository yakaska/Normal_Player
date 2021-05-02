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

    protected SourceType sourceType;

    public enum SourceType {
        INTERNAL(0),
        VK(1);

        private final long value;

        private static Map<Long, SourceType> map = new HashMap<>();

        SourceType(long value) {
            this.value = value;
        }

        static {
            for (SourceType source_type : MusicProviderSource.SourceType.values()) {
                map.put(source_type.value, source_type);
            }
        }

        public long getValue() {
            return value;
        }

        public static SourceType valueOf(long sourceType) {
            return map.get(sourceType);
        }

    }

    public MusicProviderSource(Context context, SourceType sourceType) {
        this.context = context;
        this.sourceType = sourceType;
    }

    abstract Iterator<MediaMetadataCompat> iterator();

}
