package ru.myitschool.normalplayer.ui.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.ui.MusicServiceConnection;
import ru.myitschool.normalplayer.ui.NowPlayingMetadata;
import ru.myitschool.normalplayer.utils.Utils;

public class NowPlayingViewModel extends AndroidViewModel {

    private static final long POSITION_UPDATE_INTERVAL_MILLIS = 100L;

    private final Application app;

    private final MusicServiceConnection connection;

    private PlaybackStateCompat playbackState = MusicServiceConnection.EMPTY_PLAYBACK_STATE;

    private final MutableLiveData<NowPlayingMetadata> mediaMetadata = new MutableLiveData<>();

    private final MutableLiveData<Long> mediaPosition = new MutableLiveData<>();

    private final MutableLiveData<Integer> mediaButtonRes = new MutableLiveData<>();

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
            Log.d("Ass", "onChanged: " + metadataCompat.getDescription().getIconUri());
            updateState(playbackState, metadataCompat);
        }
    };

    public NowPlayingViewModel(@NonNull Application app, MusicServiceConnection connection) {
        super(app);

        this.app = app;

        this.connection = connection;

        handler = new Handler(Looper.getMainLooper());

        mediaPosition.postValue(0L);

        mediaButtonRes.postValue(R.drawable.ic_play_arrow_white_24);

        this.connection.getPlaybackState().observeForever(playbackStateObserver);

        this.connection.getNowPlaying().observeForever(mediaMetadataObserver);

        checkPlaybackPosition();
    }

    public void checkPlaybackPosition() {
        handler.postDelayed(() -> {
            long currentPos = Utils.getCurrentPosition(playbackState);
            if (mediaPosition.getValue() != currentPos) {
                mediaPosition.postValue(currentPos);
                Log.d("Posted", "run: posted");
            }
            if (updatePosition) {
                checkPlaybackPosition();
            }
        }, POSITION_UPDATE_INTERVAL_MILLIS);
    }

    private void updateState(PlaybackStateCompat playbackState, MediaMetadataCompat metadata) {

        if (metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) != 0 && metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {

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

        if (Utils.isPlaying(playbackState)) {
            buttonRes = R.drawable.ic_pause_white_24;
        } else {
            buttonRes = R.drawable.ic_play_arrow_white_24;
        }

        mediaButtonRes.postValue(buttonRes);

    }

    public MutableLiveData<Integer> getMediaButtonRes() {
        return mediaButtonRes;
    }

    public MutableLiveData<Long> getMediaPosition() {
        return mediaPosition;
    }

    public MutableLiveData<NowPlayingMetadata> getMediaMetadata() {
        return mediaMetadata;
    }

    public void skipToNext() {
        connection.getTransportControls().skipToNext();
    }

    public void skipToPrevious() {
        connection.getTransportControls().skipToPrevious();
    }

    public void seekTo(long position) {
        connection.getTransportControls().seekTo(position);
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
