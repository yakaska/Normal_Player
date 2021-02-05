package ru.myitschool.normalplayer.ui.viewmodel;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.model.MusicProvider;
import ru.myitschool.normalplayer.ui.MediaItemData;
import ru.myitschool.normalplayer.ui.MusicServiceConnection;
import ru.myitschool.normalplayer.utils.MediaIDHelper;

public class SongFragmentViewModel extends ViewModel {

    private static String TAG = SongFragmentViewModel.class.getSimpleName();

    private static final int NO_RES_ID = 0;

    private final String mediaId;
    private final MusicServiceConnection connection;

    public MutableLiveData<List<MediaItemData>> mediaItems = new MutableLiveData<>();

    private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            Log.d(TAG, "onChildrenLoaded: " + children.toString());
            List<MediaItemData> itemList = new ArrayList<>();
            for (MediaBrowserCompat.MediaItem item : children) {
                long duration;
                if (item.getDescription().getExtras() == null) {
                    duration = 0;
                } else {
                    duration = item.getDescription().getExtras().getLong(MusicProvider.EXTRA_DURATION);
                }
                itemList.add(new MediaItemData(
                        item.getMediaId(),
                        item.getDescription().getTitle().toString(),
                        item.getDescription().getSubtitle().toString(),
                        item.getDescription().getIconUri(),
                        duration,
                        item.isBrowsable(),
                        getResourceForMediaId(item.getMediaId())
                ));
            }
            mediaItems.postValue(itemList);
        }
    };

    private final Observer<PlaybackStateCompat> playbackStateObserver = new Observer<PlaybackStateCompat>() {
        @Override
        public void onChanged(PlaybackStateCompat playbackStateCompat) {
            //TODO maybe bugs
            PlaybackStateCompat playbackState = playbackStateCompat;
            if (playbackState == null) {
                playbackState = MusicServiceConnection.EMPTY_PLAYBACK_STATE;
            }

            MediaMetadataCompat metadata = connection.getNowPlaying().getValue();
            if (metadata == null) {
                metadata = MusicServiceConnection.NOTHING_PLAYING;
            }
            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
                mediaItems.postValue(updateState(playbackState, metadata));
            }
        }
    };

    private final Observer<MediaMetadataCompat> mediaMetadataObserver = new Observer<MediaMetadataCompat>() {
        @Override
        public void onChanged(MediaMetadataCompat mediaMetadataCompat) {
            PlaybackStateCompat playbackState = connection.getPlaybackState().getValue();
            if (playbackState == null) {
                playbackState = MusicServiceConnection.EMPTY_PLAYBACK_STATE;
            }

            MediaMetadataCompat metadata = connection.getNowPlaying().getValue();
            if (metadata == null) {
                metadata = MusicServiceConnection.NOTHING_PLAYING;
            }

            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
                mediaItems.postValue(updateState(playbackState, metadata));
            }
        }
    };

    public SongFragmentViewModel(String mediaId, MusicServiceConnection connection) {
        this.connection = connection;
        this.mediaId = mediaId;
        this.connection.subscribe(mediaId, subscriptionCallback);
        this.connection.getPlaybackState().observeForever(playbackStateObserver);
        this.connection.getNowPlaying().observeForever(mediaMetadataObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        connection.getPlaybackState().removeObserver(playbackStateObserver);
        connection.getNowPlaying().removeObserver(mediaMetadataObserver);
        connection.unsubscribe(mediaId, subscriptionCallback);
    }

    private int getResourceForMediaId(String mediaId) {
        Log.d(TAG, "getResourceForMediaId: " + connection.getNowPlaying().getValue().getDescription().getMediaId());
        Log.d(TAG, "getResourceForMediaId2: " + mediaId);
        boolean isActive = MediaIDHelper.isMediaItemPlaying(connection.getNowPlaying().getValue(), mediaId);
        boolean isPlaying = connection.getPlaybackState().getValue().getState() == PlaybackStateCompat.STATE_BUFFERING
                || connection.getPlaybackState().getValue().getState() == PlaybackStateCompat.STATE_PLAYING;
        if (!isActive) {
            return NO_RES_ID;
        } else if (isPlaying) {
            return R.drawable.ic_pause_black_24;
        } else {
            return R.drawable.ic_play_arrow_black_24;
        }
    }

    private List<MediaItemData> updateState(PlaybackStateCompat playbackState, MediaMetadataCompat mediaMetadata) {
        int newResId;
        if (connection.getPlaybackState().getValue().getState() == PlaybackStateCompat.STATE_BUFFERING
                || connection.getPlaybackState().getValue().getState() == PlaybackStateCompat.STATE_PLAYING) {
            newResId = R.drawable.ic_pause_black_24;
        } else {
            newResId = R.drawable.ic_play_arrow_black_24;
        }
        if (mediaItems.getValue() != null) {
            List<MediaItemData> items = new ArrayList();
            for (MediaItemData item : mediaItems.getValue()) {
                int useResId;
                if (item.getMediaId().equals(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))) {
                    useResId = newResId;
                } else {
                    useResId = NO_RES_ID;
                }
                item.setPlaybackRes(useResId);
                items.add(item);
            }
            return items;
        } else {
            return Collections.emptyList();
        }
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private String mediaId;
        private MusicServiceConnection connection;

        public Factory(String mediaId, MusicServiceConnection connection) {
            this.mediaId = mediaId;
            this.connection = connection;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SongFragmentViewModel(mediaId, connection);
        }
    }

}
