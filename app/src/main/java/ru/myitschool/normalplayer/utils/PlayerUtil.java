package ru.myitschool.normalplayer.utils;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.ShuffleOrder;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayerUtil {

    @SuppressLint("DefaultLocale")
    public static String convertMs(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

    public static ConcatenatingMediaSource metadataListToMediaSource(ArrayList<MediaMetadataCompat> metadataList, DefaultHlsDataSourceFactory dataSourceFactory) {
        ConcatenatingMediaSource mediaSource = new ConcatenatingMediaSource(false, true, new ShuffleOrder.UnshuffledShuffleOrder(500));
        for (MediaMetadataCompat metadata : metadataList) {
            Log.d("TAG", "metadataListToMediaSource: " + metadata.getDescription().getMediaUri());

            mediaSource.addMediaSource(toMediaSource(metadata, dataSourceFactory));
        }
        return mediaSource;
    }

    public static MediaSource toMediaSource(MediaMetadataCompat metadataCompat, DefaultHlsDataSourceFactory dataSourceFactory) {
        return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
    }


    public static boolean isPlaying(PlaybackStateCompat playbackState) {
        return (playbackState.getState() == PlaybackStateCompat.STATE_BUFFERING) ||
                (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    public static boolean isPrepared(PlaybackStateCompat playbackState) {
        return (playbackState.getState() == PlaybackStateCompat.STATE_BUFFERING) ||
                (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) ||
                (playbackState.getState() == PlaybackStateCompat.STATE_PAUSED);
    }

    public static boolean isPlayEnabled(PlaybackStateCompat playbackState) {
        return (((playbackState.getActions() & PlaybackStateCompat.ACTION_PLAY) != 0L)
                || ((playbackState.getActions() & PlaybackStateCompat.ACTION_PLAY_PAUSE) != 0L))
                && (playbackState.getState() == PlaybackStateCompat.STATE_PAUSED);
    }

    public static long getCurrentPosition(PlaybackStateCompat playbackState) {
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            long timeDelta = SystemClock.elapsedRealtime() - playbackState.getLastPositionUpdateTime();
            return (long) (playbackState.getPosition() + (timeDelta * playbackState.getPlaybackSpeed()));
        } else {
            return playbackState.getPosition();
        }
    }

}
