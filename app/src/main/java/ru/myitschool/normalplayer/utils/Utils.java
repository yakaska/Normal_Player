package ru.myitschool.normalplayer.utils;

import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static String convertMs(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

    public static ConcatenatingMediaSource metadataListToMediaSource(ArrayList<MediaMetadataCompat> metadataList, DefaultDataSourceFactory dataSourceFactory) {
        ConcatenatingMediaSource mediaSource = new ConcatenatingMediaSource();
        for (MediaMetadataCompat metadata : metadataList) {
            mediaSource.addMediaSource(toMediaSource(metadata, dataSourceFactory));
        }
        return mediaSource;
    }

    public static MediaSource toMediaSource(MediaMetadataCompat metadataCompat, DefaultDataSourceFactory dataSourceFactory) {
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
    }

    public static <E> Collection<E> makeCollectionFromIterable(Iterable<E> iterable) {
        Collection<E> list = new ArrayList<E>();
        for (E item : iterable) {
            list.add(item);
        }
        return list;
    }

    public static boolean isPlaying(PlaybackStateCompat playbackState) {
        return (playbackState.getState() == PlaybackStateCompat.STATE_BUFFERING) ||
                (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) ||
                (playbackState.getState() == PlaybackStateCompat.STATE_PAUSED);
    }
}
