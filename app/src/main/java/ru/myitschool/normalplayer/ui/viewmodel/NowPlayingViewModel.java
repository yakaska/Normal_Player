package ru.myitschool.normalplayer.ui.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.common.MusicServiceConnection;
import ru.myitschool.normalplayer.ui.model.NowPlayingMetadata;
import ru.myitschool.normalplayer.utils.PlayerUtil;

public class NowPlayingViewModel extends AndroidViewModel {

    private static final long POSITION_UPDATE_INTERVAL_MILLIS = 1000L;

    private final Application app;

    private final MusicServiceConnection connection;

    private PlaybackStateCompat playbackState = MusicServiceConnection.EMPTY_PLAYBACK_STATE;

    private final MutableLiveData<NowPlayingMetadata> mediaMetadata = new MutableLiveData<>();

    private final MutableLiveData<Long> mediaPosition = new MutableLiveData<>();

    private final MutableLiveData<Integer> playButtonRes = new MutableLiveData<>();

    private final MutableLiveData<Integer> shuffleButtonRes = new MutableLiveData<>();

    private final MutableLiveData<Integer> repeatButtonRes = new MutableLiveData<>();

    private boolean updatePosition = true;

    private final Handler handler;

    private final Observer<PlaybackStateCompat> playbackStateObserver = new Observer<PlaybackStateCompat>() {
        @Override
        public void onChanged(PlaybackStateCompat playbackStateCompat) {
            playbackState = playbackStateCompat;
            if (playbackState == null) {
                playbackState = MusicServiceConnection.EMPTY_PLAYBACK_STATE;
            }
            MediaMetadataCompat metadata = connection.getNowPlaying().getValue();
            if (metadata == null) {
                metadata = MusicServiceConnection.NOTHING_PLAYING;
            }
            updateState(playbackState, metadata);
        }
    };

    private final Observer<MediaMetadataCompat> mediaMetadataObserver = new Observer<MediaMetadataCompat>() {
        @Override
        public void onChanged(MediaMetadataCompat metadataCompat) {
            updateState(playbackState, metadataCompat);
        }
    };

    public NowPlayingViewModel(@NonNull Application app, MusicServiceConnection connection) {
        super(app);

        this.app = app;

        this.connection = connection;

        handler = new Handler(Looper.getMainLooper());

        mediaPosition.postValue(0L);

        playButtonRes.postValue(R.drawable.ic_play_24);

        this.connection.getPlaybackState().observeForever(playbackStateObserver);

        this.connection.getNowPlaying().observeForever(mediaMetadataObserver);

        checkPlaybackPosition();
    }

    public void checkPlaybackPosition() {
        handler.postDelayed(() -> {
            long currentPos = PlayerUtil.getCurrentPosition(playbackState);
            if (mediaPosition.getValue() != currentPos) {
                mediaPosition.postValue(currentPos);
            }
            if (updatePosition) {
                checkPlaybackPosition();
            }
        }, POSITION_UPDATE_INTERVAL_MILLIS);
    }

    private void updateState(PlaybackStateCompat playbackState, MediaMetadataCompat metadata) {

        String mediaId = "";
        if (mediaMetadata.getValue() != null) {
            mediaId = mediaMetadata.getValue().getMediaId();
        }

        if (metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) > 0
                && metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null
                && !metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).equals(mediaId)) {

            NowPlayingMetadata nowPlayingMetadata = new NowPlayingMetadata(
                    metadata.getDescription().getMediaId(),
                    metadata.getDescription().getIconUri(),
                    metadata.getDescription().getTitle().toString(),
                    metadata.getDescription().getSubtitle().toString(),
                    NowPlayingMetadata.timestampToMSS(app, metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)),
                    metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            );
            this.mediaMetadata.postValue(nowPlayingMetadata);
        }

        int buttonRes = 0;

        if (PlayerUtil.isPlaying(playbackState)) {
            buttonRes = R.drawable.ic_pause_24;
        } else {
            buttonRes = R.drawable.ic_play_24;
        }

        playButtonRes.postValue(buttonRes);

    }

    public MutableLiveData<Integer> getPlayButtonRes() {
        return playButtonRes;
    }

    public MutableLiveData<Long> getMediaPosition() {
        return mediaPosition;
    }

    public MutableLiveData<NowPlayingMetadata> getMediaMetadata() {
        return mediaMetadata;
    }

    public MutableLiveData<Integer> getShuffleButtonRes() {
        return shuffleButtonRes;
    }

    public MutableLiveData<Integer> getRepeatButtonRes() {
        return repeatButtonRes;
    }

    public void skipToNext() {
        if (mediaMetadata.getValue() != null) {
            connection.getTransportControls().skipToNext();
        }
    }

    public void skipToPrevious() {
        if (mediaMetadata.getValue() != null) {
            connection.getTransportControls().skipToPrevious();
        }
    }

    public void seekTo(long position) {
        if (mediaMetadata.getValue() != null) {
            connection.getTransportControls().seekTo(position);
        }
    }

    public void toggleShuffleMode() {
        if (mediaMetadata.getValue() != null) {
            if (connection.getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                connection.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                shuffleButtonRes.postValue(R.drawable.ic_shuffle_on_24);
            } else {
                connection.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                shuffleButtonRes.postValue(R.drawable.ic_shuffle_24);
            }
        }
    }

    public void toggleRepeatMode() {
        if (mediaMetadata.getValue() != null) {
            if (connection.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_NONE) {
                connection.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                repeatButtonRes.postValue(R.drawable.ic_repeat_one_on_24);
            } else if (connection.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE) {
                connection.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                repeatButtonRes.postValue(R.drawable.ic_repeat_on_24);
            } else if (connection.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ALL) {
                connection.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                repeatButtonRes.postValue(R.drawable.ic_repeat_24);
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        connection.getNowPlaying().removeObserver(mediaMetadataObserver);
        connection.getPlaybackState().removeObserver(playbackStateObserver);
        updatePosition = false;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final Application app;
        private final MusicServiceConnection connection;

        public Factory(Application app, MusicServiceConnection connection) {
            this.app = app;
            this.connection = connection;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new NowPlayingViewModel(app, connection);
        }
    }

}
