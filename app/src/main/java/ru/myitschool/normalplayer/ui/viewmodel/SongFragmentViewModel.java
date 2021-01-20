package ru.myitschool.normalplayer.ui.viewmodel;

import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ru.myitschool.normalplayer.ui.MusicServiceConnection;

public class SongFragmentViewModel extends ViewModel {

    private static String TAG = SongFragmentViewModel.class.getSimpleName();

    private final String mediaId;
    private final MusicServiceConnection connection;

    public MutableLiveData<List<MediaBrowserCompat.MediaItem>> mediaItems = new MutableLiveData<>();

    private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            Log.d(TAG, "onChildrenLoaded: " + children.toString());
            mediaItems.postValue(children);
        }
    };

    public SongFragmentViewModel(String mediaId, MusicServiceConnection connection) {
        this.connection = connection;
        this.mediaId = mediaId;
        this.connection.subscribe(mediaId, subscriptionCallback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        connection.unsubscribe(mediaId, subscriptionCallback);
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