package ru.myitschool.normalplayer.ui.viewmodel;

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
import ru.myitschool.normalplayer.ui.model.MediaItemData;
import ru.myitschool.normalplayer.utils.Event;
import ru.myitschool.normalplayer.utils.PlayerUtil;

public class MainActivityViewModel extends ViewModel {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();

    private final MusicServiceConnection connection;

    private final LiveData<String> rootMediaId;

    private final MutableLiveData<Event<String>> navigateToMediaItem;

    private final MutableLiveData<Event<FragmentNavigationRequest>> navigateToFragment;

    public MainActivityViewModel(MusicServiceConnection connection) {
        this.connection = connection;
        navigateToMediaItem = new MutableLiveData<>();
        navigateToFragment = new MutableLiveData<>();
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
        Log.d(TAG, "mediaItemClicked: ");
        if (clickedItem.isBrowsable()) {
            browseToItem(clickedItem);
        } else {
            playMediaId(clickedItem.getMediaId());
            Log.d(TAG, "mediaItemClicked: " + clickedItem.getMediaId());
        }
    }

    public void showFragment(Fragment fragment, boolean backStack, String tag) {
        Log.d(TAG, "showFragment: ");
        navigateToFragment.setValue(new Event<>(new FragmentNavigationRequest(fragment, backStack, tag)));
    }

    public LiveData<String> getRootMediaId() {
        return rootMediaId;
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

    //TODO
    public void playMedia(MediaItemData clickedItem, boolean pauseAllowed) {
        MediaMetadataCompat metadata = connection.getNowPlaying().getValue();
        MediaControllerCompat.TransportControls transportControls = connection.getTransportControls();

        boolean isPrepared;
        if (connection.getPlaybackState().getValue() != null) {
            isPrepared = PlayerUtil.isPrepared(connection.getPlaybackState().getValue());
        } else {
            isPrepared = false;
        }
        if (isPrepared && clickedItem.getMediaId().equals(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))) {
            if (connection.getPlaybackState().getValue() != null) {
                if (PlayerUtil.isPlaying(connection.getPlaybackState().getValue())) {
                    if (pauseAllowed) transportControls.pause();
                } else if (PlayerUtil.isPlayEnabled(connection.getPlaybackState().getValue())) {
                    transportControls.play();
                } else {
                    Log.d(TAG, "playMedia: ПОШЕЛ НАХУЙ ПИДОРАС");
                }
            }
        } else {
            transportControls.playFromMediaId(clickedItem.getMediaId(), null);
        }

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
