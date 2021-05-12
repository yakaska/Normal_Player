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
import java.util.List;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.common.MusicServiceConnection;
import ru.myitschool.normalplayer.common.model.MusicProvider;
import ru.myitschool.normalplayer.common.model.MusicProviderSource;
import ru.myitschool.normalplayer.ui.model.MediaItemData;
import ru.myitschool.normalplayer.utils.PlayerUtil;

public class MediaItemFragmentViewModel extends ViewModel {

    private static final String TAG = MediaItemFragmentViewModel.class.getSimpleName();

    private static final int NO_RES_ID = 0;

    private final String mediaId;

    private final MusicServiceConnection connection;

    public MutableLiveData<List<MediaItemData>> mediaItems = new MutableLiveData<>();

    private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
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
                        item.getDescription().getMediaUri(),
                        item.getDescription().getIconUri(),
                        duration,
                        item.isBrowsable(),
                        getResourceForMediaId(mediaId),
                        item.getDescription().getExtras().getLong(MusicProviderSource.SOURCE_TYPE_KEY, -1)
                ));
            }
            mediaItems.postValue(itemList);
        }
    };


    public MediaItemFragmentViewModel(String mediaId, MusicServiceConnection connection) {
        this.connection = connection;
        this.mediaId = mediaId;
        this.connection.subscribe(mediaId, subscriptionCallback);
        this.connection.getPlaybackState().observeForever(playbackStateObserver);
        this.connection.getNowPlaying().observeForever(mediaMetadataObserver);
    }

    private final Observer<PlaybackStateCompat> playbackStateObserver = new Observer<PlaybackStateCompat>() {
        @Override
        public void onChanged(PlaybackStateCompat playbackStateCompat) {
            Log.d(TAG, "onChanged: " + playbackStateCompat);
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
        public void onChanged(MediaMetadataCompat metadataCompat) {
            Log.d(TAG, "onChanged: " + metadataCompat);
            PlaybackStateCompat playbackState = connection.getPlaybackState().getValue();
            if (playbackState == null) {
                playbackState = MusicServiceConnection.EMPTY_PLAYBACK_STATE;
            }
            MediaMetadataCompat metadata = metadataCompat;
            if (metadata == null) {
                metadata = MusicServiceConnection.NOTHING_PLAYING;
            }
            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
                mediaItems.postValue(updateState(playbackState, metadata));
            }
        }
    };

    private List<MediaItemData> updateState(PlaybackStateCompat playbackState, MediaMetadataCompat metadata) {
        int newResId = NO_RES_ID;
        if (PlayerUtil.isPlaying(playbackState)) {
            newResId = R.drawable.ic_pause_24;
        } else {
            newResId = R.drawable.ic_play_24;
        }
        List<MediaItemData> resultList = new ArrayList<>();

        if (mediaItems.getValue() != null) {
            for (MediaItemData item : mediaItems.getValue()) {
                int useResId = NO_RES_ID;
                if (item.getMediaId().equals(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))) {
                    useResId = newResId;
                }
                //TODO copy!!!!!!!
                item = new MediaItemData(
                        item.getMediaId(),
                        item.getTitle(),
                        item.getSubtitle(),
                        item.getMediaUri(),
                        item.getAlbumArtUri(),
                        item.getDuration(),
                        item.isBrowsable(),
                        useResId,
                        item.getSourceType());
                resultList.add(item);
            }
        }
        return resultList;
    }

    private int getResourceForMediaId(String mediaId) {
        boolean isActive;
        boolean isPlaying;
        if (connection.getNowPlaying().getValue() != null) {
            isActive = mediaId.equals(connection.getNowPlaying().getValue().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        } else {
            isActive = false;
        }
        if (connection.getPlaybackState().getValue() != null) {
            isPlaying = PlayerUtil.isPlaying(connection.getPlaybackState().getValue());
        } else {
            isPlaying = false;
        }
        if (!isActive) {
            return NO_RES_ID;
        } else if (isPlaying) {
            return R.drawable.ic_pause_24;
        } else {
            return R.drawable.ic_play_24;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        connection.unsubscribe(mediaId, subscriptionCallback);
        connection.getPlaybackState().removeObserver(playbackStateObserver);
        connection.getNowPlaying().removeObserver(mediaMetadataObserver);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final String mediaId;
        private final MusicServiceConnection connection;

        public Factory(String mediaId, MusicServiceConnection connection) {
            this.mediaId = mediaId;
            this.connection = connection;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MediaItemFragmentViewModel(mediaId, connection);
        }
    }

}
