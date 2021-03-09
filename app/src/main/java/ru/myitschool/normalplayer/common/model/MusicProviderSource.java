package ru.myitschool.normalplayer.common.model;

import android.support.v4.media.MediaMetadataCompat;

import java.util.Iterator;

public interface MusicProviderSource {

    Iterator<MediaMetadataCompat> iterator();
}
