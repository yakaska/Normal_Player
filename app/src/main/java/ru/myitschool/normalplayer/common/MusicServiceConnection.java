package ru.myitschool.normalplayer.common;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class MusicServiceConnection {

    private static MusicServiceConnection instance = null;

    public static PlaybackStateCompat EMPTY_PLAYBACK_STATE = new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
            .build();

    public static final MediaMetadataCompat NOTHING_PLAYING = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "")
            .build();

    private final MediaBrowserCompat mediaBrowser;
    private final MediaBrowserConnectionCallback connectionCallback;
    private MediaControllerCompat mediaController;

    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    private final MutableLiveData<MediaMetadataCompat> nowPlaying = new MutableLiveData<>(NOTHING_PLAYING);
    private final MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>(EMPTY_PLAYBACK_STATE);

    public MutableLiveData<MediaMetadataCompat> getNowPlaying() {
        return nowPlaying;
    }

    public MutableLiveData<PlaybackStateCompat> getPlaybackState() {
        return playbackState;
    }

    public static synchronized MusicServiceConnection getInstance(Context context, ComponentName serviceComponent) {
        if (instance == null) {
            instance = new MusicServiceConnection(context, serviceComponent);
        }
        return instance;
    }

    private MusicServiceConnection(Context context, ComponentName serviceComponent) {
        connectionCallback = new MediaBrowserConnectionCallback(context);
        mediaBrowser = new MediaBrowserCompat(context, serviceComponent, connectionCallback, null);
        mediaBrowser.connect();
    }

    public void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback subscriptionCallback) {
        mediaBrowser.subscribe(parentId, subscriptionCallback);
    }

    public void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback subscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, subscriptionCallback);
    }

    public void search(String query, Bundle extras, MediaBrowserCompat.SearchCallback searchCallback) {
        mediaBrowser.search(query, extras, searchCallback);
    }

    public MutableLiveData<Boolean> isConnected() {
        return isConnected;
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return mediaController.getTransportControls();
    }

    public int getShuffleMode() {
        return mediaController.getShuffleMode();
    }

    public int getRepeatMode() {
        return mediaController.getRepeatMode();
    }

    public String getBrowserRoot() {
        return mediaBrowser.getRoot();
    }

    private class MediaSearchCallback extends MediaBrowserCompat.SearchCallback {
        @Override
        public void onSearchResult(@NonNull String query, Bundle extras, @NonNull List<MediaBrowserCompat.MediaItem> items) {
            super.onSearchResult(query, extras, items);
        }

        @Override
        public void onError(@NonNull String query, Bundle extras) {
            super.onError(query, extras);
        }
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        private final Context context;

        public MediaBrowserConnectionCallback(Context context) {
            this.context = context;
        }

        @Override
        public void onConnected() {
            super.onConnected();

            mediaController = new MediaControllerCompat(context, mediaBrowser.getSessionToken());
            mediaController.registerCallback(new MediaControllerCallback());
            isConnected.postValue(true);
        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
            isConnected.postValue(false);
        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            isConnected.postValue(false);
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null) {
                playbackState.postValue(EMPTY_PLAYBACK_STATE);
            } else {
                playbackState.postValue(state);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata.getDescription().getMediaId() == null) {
                nowPlaying.postValue(NOTHING_PLAYING);
            } else {
                nowPlaying.postValue(metadata);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            connectionCallback.onConnectionSuspended();
        }
    }

}
