package ru.myitschool.normalplayer.playback;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.model.MusicProvider;
import ru.myitschool.normalplayer.utils.MediaIDHelper;

public class PlaybackManager implements Playback.Callback {

    private static final String TAG = PlaybackManager.class.getSimpleName();
    // Action to thumbs up a media item
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.example.android.uamp.THUMBS_UP";

    private MusicProvider musicProvider;
    private QueueManager queueManager;
    private Resources resources;
    private Playback playback;
    private PlaybackServiceCallback serviceCallback;
    private MediaSessionCallback mediaSessionCallback;

    public PlaybackManager(PlaybackServiceCallback serviceCallback, Resources resources, MusicProvider musicProvider, QueueManager queueManager, Playback playback) {
        this.musicProvider = musicProvider;
        this.serviceCallback = serviceCallback;
        this.resources = resources;
        this.queueManager = queueManager;
        mediaSessionCallback = new MediaSessionCallback();
        this.playback = playback;
        this.playback.setCallback(this);
    }

    public Playback getPlayback() {
        return playback;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mediaSessionCallback;
    }

    /**
     * Handle a request to play music
     */
    public void handlePlayRequest() {
        Log.d(TAG, "handlePlayRequest: mState=" + playback.getState());
        MediaSessionCompat.QueueItem currentMusic = queueManager.getCurrentMusic();
        if (currentMusic != null) {
            serviceCallback.onPlaybackStart();
            playback.play(currentMusic);
        }
    }

    /**
     * Handle a request to pause music
     */
    public void handlePauseRequest() {
        Log.d(TAG, "handlePauseRequest: mState=" + playback.getState());
        if (playback.isPlaying()) {
            playback.pause();
            serviceCallback.onPlaybackStop();
        }
    }

    public void handleStopRequest(String withError) {
        Log.d(TAG, "handleStopRequest: ");
        Log.d(TAG, "handleStopRequest: mState=" + playback.getState() + " error=" + withError);
        playback.stop(true);
        serviceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }


    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    public void updatePlaybackState(String error) {
        Log.d(TAG, "updatePlaybackState, playback state=" + playback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (playback != null && playback.isConnected()) {
            position = playback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        setCustomAction(stateBuilder);
        int state = playback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        MediaSessionCompat.QueueItem currentMusic = queueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        serviceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            serviceCallback.onNotificationRequired();
        }
    }

    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        MediaSessionCompat.QueueItem currentMusic = queueManager.getCurrentMusic();
        if (currentMusic == null) {
            return;
        }
        // Set appropriate "Favorite" icon on Custom action:
        String mediaId = currentMusic.getDescription().getMediaId();
        if (mediaId == null) {
            return;
        }
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        int favoriteIcon = musicProvider.isFavorite(musicId) ?
                android.R.drawable.star_on : android.R.drawable.star_off;
        Log.d(TAG, "updatePlaybackState, setting Favorite custom action of music " + musicId + " current favorite=" + musicProvider.isFavorite(musicId));
        Bundle customActionExtras = new Bundle();
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_THUMBS_UP, resources.getString(R.string.favorite), favoriteIcon)
                .setExtras(customActionExtras)
                .build());
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (playback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    /**
     * Implementation of the Playback.Callback interface
     */
    @Override
    public void onCompletion() {
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        if (queueManager.skipQueuePosition(1)) {
            handlePlayRequest();
            queueManager.updateMetadata();
        } else {
            // If skipping was not possible, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        Log.d(TAG, "setCurrentMediaId" + mediaId);
        queueManager.setQueueFromMusic(mediaId);
    }


    /**
     * Switch to a different Playback instance, maintaining all playback state, if possible.
     *
     * @param playback switch to this playback
     */
    public void switchToPlayback(Playback playback, boolean resumePlaying) {
        if (playback == null) {
            throw new IllegalArgumentException("Playback cannot be null");
        }
        // suspend the current one.
        int oldState = this.playback.getState();
        int pos = this.playback.getCurrentStreamPosition();
        String currentMediaId = this.playback.getCurrentMediaId();
        this.playback.stop(false);
        playback.setCallback(this);
        playback.setCurrentStreamPosition(pos < 0 ? 0 : pos);
        playback.setCurrentMediaId(currentMediaId);
        playback.start();
        // finally swap the instance
        this.playback = playback;
        switch (oldState) {
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_PAUSED:
                this.playback.pause();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                MediaSessionCompat.QueueItem currentMusic = queueManager.getCurrentMusic();
                if (resumePlaying && currentMusic != null) {
                    this.playback.play(currentMusic);
                } else if (!resumePlaying) {
                    this.playback.pause();
                } else {
                    this.playback.stop(true);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            default:
                Log.d(TAG, "Default called. Old state is " + oldState);
        }
    }


    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            Log.d(TAG, "play");
            if (queueManager.getCurrentMusic() == null) {
                queueManager.setRandomQueue();
            }
            handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            Log.d(TAG, "OnSkipToQueueItem:" + queueId);
            queueManager.setCurrentQueueItem(queueId);
            queueManager.updateMetadata();
        }

        @Override
        public void onSeekTo(long position) {
            Log.d(TAG, "onSeekTo:" + position);
            playback.seekTo((int) position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "playFromMediaId mediaId:" + mediaId + "  extras=" + extras);
            queueManager.setQueueFromMusic(mediaId);
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            Log.d(TAG, "pause. current state=" + playback.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            Log.d(TAG, "stop. current state=" + playback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "skipToNext");
            if (queueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            queueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            if (queueManager.skipQueuePosition(-1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            queueManager.updateMetadata();
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
                Log.i(TAG, "onCustomAction: favorite for current track");
                MediaSessionCompat.QueueItem currentMusic = queueManager.getCurrentMusic();
                if (currentMusic != null) {
                    String mediaId = currentMusic.getDescription().getMediaId();
                    if (mediaId != null) {
                        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
                        musicProvider.setFavorite(musicId, !musicProvider.isFavorite(musicId));
                    }
                }
                // playback state needs to be updated because the "Favorite" icon on the
                // custom action will change to reflect the new favorite state.
                updatePlaybackState(null);
            } else {
                Log.e(TAG, "Unsupported action: " + action);
            }
        }

        /**
         * Handle free and contextual searches.
         * <p/>
         * All voice searches on Android Auto are sent to this method through a connected
         * {@link android.support.v4.media.session.MediaControllerCompat}.
         * <p/>
         * Threads and async handling:
         * Search, as a potentially slow operation, should run in another thread.
         * <p/>
         * Since this method runs on the main thread, most apps with non-trivial metadata
         * should defer the actual search to another thread (for example, by using
         * an {@link AsyncTask} as we do here).
         **/
        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            Log.d(TAG, "playFromSearch  query=" + query + " extras=" + extras);

            playback.setState(PlaybackStateCompat.STATE_CONNECTING);
            boolean successSearch = queueManager.setQueueFromSearch(query, extras);
            if (successSearch) {
                handlePlayRequest();
                queueManager.updateMetadata();
            } else {
                updatePlaybackState("Could not find music");
            }
        }
    }


    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }

}
