package ru.myitschool.normalplayer.model;

import android.support.v4.media.MediaMetadataCompat;

import java.util.Iterator;

public interface MusicProviderSource {

    Iterator<MediaMetadataCompat> iterator();
}
