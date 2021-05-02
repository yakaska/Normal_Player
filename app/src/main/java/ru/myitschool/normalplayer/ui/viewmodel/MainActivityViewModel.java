package ru.myitschool.normalplayer.ui.viewmodel;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.myitschool.normalplayer.common.MusicServiceConnection;
import ru.myitschool.normalplayer.common.model.MusicProviderSource;
import ru.myitschool.normalplayer.common.playback.MusicService;
import ru.myitschool.normalplayer.ui.model.MediaItemData;
import ru.myitschool.normalplayer.utils.Event;
import ru.myitschool.normalplayer.utils.PlayerUtil;

public class MainActivityViewModel extends ViewModel {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();

    private final MusicServiceConnection connection;

    private final LiveData<String> rootMediaId;

    private final MutableLiveData<Event<String>> navigateToMediaItem;

    private final MutableLiveData<Event<FragmentNavigationRequest>> navigateToFragment;

    private final MutableLiveData<String> search;

    public MainActivityViewModel(MusicServiceConnection connection) {
        this.connection = connection;
        navigateToMediaItem = new MutableLiveData<>();
        navigateToFragment = new MutableLiveData<>();
        search = new MutableLiveData<>();
        rootMediaId = Transformations.map(connection.isConnected(), isConnected ->{
            if (isConnected) {
                Log.d(TAG, "MainActivityViewModel: connected");
                return connection.getBrowserRoot();
            } else {
                Log.d(TAG, "MainActivityViewModel: not connected");
                return null;
            }
        });
    }

    public void mediaItemClicked(MediaItemData clickedItem) {
        Log.d(TAG, "mediaItemClicked: " + clickedItem.getMediaUri());
        if (clickedItem.isBrowsable()) {
            browseToItem(clickedItem);
        } else {
            playMediaId(clickedItem.getMediaId());
            Log.d(TAG, "mediaItemClicked: " + clickedItem.getTitle());
        }
    }

    public void mediaItemMenuClicked(String action, MediaItemData clickedItem) {
        Bundle extras = new Bundle();
        Log.d(TAG, "mediaItemMenuClicked: " + clickedItem.getTitle());
        extras.putString(MusicService.MEDIA_EXTRA_FILE_URI, String.valueOf(clickedItem.getMediaUri()));
        extras.putString(MusicService.MEDIA_EXTRA_FILE_NAME, clickedItem.getTitle());
        sendAction(action, extras);
    }

    private void sendAction(String action, Bundle extras) {
        connection.sendAction(action, extras);
    }

    public void showFragment(Fragment fragment, boolean backStack, String tag) {
        Log.d(TAG, "showFragment: " + fragment.getClass());
        navigateToFragment.setValue(new Event<>(new FragmentNavigationRequest(fragment, backStack, tag)));
    }

    public LiveData<String> getRootMediaId() {
        return rootMediaId;
    }

    public void search(String query) {
        search.postValue(query);
    }

    public LiveData<String> getSearch() {
        return search;
    }

    public MutableLiveData<Event<FragmentNavigationRequest>> getNavigateToFragment() {
        return navigateToFragment;
    }

    public MutableLiveData<Event<String>> getNavigateToMediaItem() {
        return navigateToMediaItem;
    }

    public void playMediaId(String mediaId) {
        MediaMetadataCompat nowPlaying = connection.getNowPlaying().getValue();
        MediaControllerCompat.TransportControls transportControls = connection.getTransportControls();
        boolean isPrepared;
        if (connection.getPlaybackState().getValue() != null) {
            isPrepared = PlayerUtil.isPrepared(connection.getPlaybackState().getValue());
        } else {
            isPrepared = false;
        }
        if (isPrepared && mediaId.equals(nowPlaying.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))) {
            if (connection.getPlaybackState().getValue() != null) {
                if (PlayerUtil.isPlaying(connection.getPlaybackState().getValue())) {
                    transportControls.pause();
                } else if (PlayerUtil.isPlayEnabled(connection.getPlaybackState().getValue())) {
                    transportControls.play();
                } else {
                    Log.d(TAG, "playMediaId: пшлнхпдрс");
                }
            }
        } else {
            transportControls.playFromMediaId(mediaId, null);
        }
    }

    public void setMediaSource(MusicProviderSource.SourceType sourceType) {
        Bundle extra = new Bundle();
        extra.putLong(MusicProviderSource.SOURCE_TYPE_KEY, sourceType.getValue());
        sendAction(MusicService.ACTION_CHANGE_SOURCE, extra);
    }

    private void browseToItem(MediaItemData clickedItem) {
        Log.d(TAG, "browseToItem: " + clickedItem.getMediaId());
        Event<String> stringEvent = new Event<>(clickedItem.getMediaId());
        navigateToMediaItem.postValue(stringEvent);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final MusicServiceConnection connection;

        public Factory(MusicServiceConnection connection) {
            this.connection = connection;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainActivityViewModel(connection);
        }
    }

    public class FragmentNavigationRequest {
        private final Fragment fragment;
        private final boolean backStack;
        private final String tag;

        public FragmentNavigationRequest(Fragment fragment, boolean backStack, String tag) {
            this.fragment = fragment;
            this.backStack = backStack;
            this.tag = tag;
        }

        public Fragment getFragment() {
            return fragment;
        }

        public boolean isBackStack() {
            return backStack;
        }

        public String getTag() {
            return tag;
        }
    }

}
