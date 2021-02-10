package ru.myitschool.normalplayer.playback;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import ru.myitschool.normalplayer.model.MusicProvider;
import ru.myitschool.normalplayer.ui.MainActivity;
import ru.myitschool.normalplayer.utils.MediaIDHelper;
import ru.myitschool.normalplayer.utils.QueueHelper;
import ru.myitschool.normalplayer.utils.Utils;

import static ru.myitschool.normalplayer.utils.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
import static ru.myitschool.normalplayer.utils.MediaIDHelper.MEDIA_ID_ROOT;

public class MusicService extends MediaBrowserServiceCompat {
    private static final String TAG = MusicService.class.getSimpleName();

    private static final String NP_USER_AGENT = "NP_USER_AGENT";

    public static final String MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms";

    private NPNotificationManager notificationManager;

    private MusicProvider musicProvider;

    protected MediaSessionCompat mediaSession;

    protected MediaSessionConnector mediaSessionConnector;

    private ArrayList<MediaMetadataCompat> currentPlaylistItems = new ArrayList<>();

    private PersistentStorage storage;

    private DefaultDataSourceFactory dataSourceFactory;

    private boolean isForegroundService;

    private final AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build();

    private final PlayerEventListener playerListener = new PlayerEventListener();

    private Player currentPlayer;

    private SimpleExoPlayer exoPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        musicProvider = new MusicProvider(this);
        musicProvider.retrieveMediaAsync(null);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setSessionActivity(pi);
        mediaSession.setActive(true);

        setSessionToken(mediaSession.getSessionToken());

        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, NP_USER_AGENT), null);

        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        exoPlayer.setAudioAttributes(audioAttributes);
        exoPlayer.setHandleAudioBecomingNoisy(true);
        exoPlayer.addListener(playerListener);

        notificationManager = new NPNotificationManager(this, mediaSession.getSessionToken(), new PlayerNotificationListener());


        notificationManager.showNotificationForPlayer(currentPlayer);

        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlaybackPreparer(new NPPlaybackPreparer());
        mediaSessionConnector.setQueueNavigator(new NPQueueNavigator(mediaSession));

        switchToPlayer(null, exoPlayer);

        storage = PersistentStorage.getInstance(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mediaSession.setActive(false);
        mediaSession.release();
        exoPlayer.removeListener(playerListener);
        exoPlayer.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "OnLoadChildren: parentMediaId=" + parentMediaId);
        if (MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<>());
        } else if (musicProvider.isInitialized()) {
            result.sendResult(musicProvider.getChildren(parentMediaId, getResources()));
        } else {
            result.detach();
            musicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    result.sendResult(musicProvider.getChildren(parentMediaId, getResources()));
                }
            });
        }
    }

    @Override
    public void onSearch(@NonNull String query, Bundle extras, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        super.onSearch(query, extras, result);
    }

    private void preparePlaylist(ArrayList<MediaMetadataCompat> metadataList, @Nullable MediaMetadataCompat itemToPlay, boolean playWhenReady, long playbackStartPositionMs) {
        int initialWindowIndex;
        if (itemToPlay == null) {
            initialWindowIndex = 0;
        } else {
            initialWindowIndex = metadataList.indexOf(itemToPlay);
        }
        currentPlaylistItems = metadataList;
        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.stop(true);
        if (currentPlayer == exoPlayer) {
            ConcatenatingMediaSource mediaSource = Utils.metadataListToMediaSource(metadataList, dataSourceFactory);
            exoPlayer.prepare(mediaSource);
            exoPlayer.seekTo(initialWindowIndex, playbackStartPositionMs);
        }
    }

    public void switchToPlayer(Player previousPlayer, Player newPlayer) {
        if (previousPlayer == newPlayer) {
            return;
        }
        currentPlayer = newPlayer;
        if (previousPlayer != null) {
            int playbackState = previousPlayer.getPlaybackState();
            if (currentPlaylistItems.isEmpty()) {
                currentPlayer.stop(true);
            } else if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                preparePlaylist(
                        currentPlaylistItems,
                        currentPlaylistItems.get(previousPlayer.getCurrentWindowIndex()),
                        previousPlayer.getPlayWhenReady(),
                        previousPlayer.getCurrentPosition()
                );
            }
        }
        mediaSessionConnector.setPlayer(newPlayer);
        if (previousPlayer != null) {
            previousPlayer.stop(true);
        }
    }

    private void saveRecentSongToStorage() {
        MediaDescriptionCompat description = currentPlaylistItems.get(currentPlayer.getCurrentWindowIndex()).getDescription();
        long position = currentPlayer.getCurrentPosition();
        storage.saveRecentSong(description, position);
    }

    private class NPQueueNavigator extends TimelineQueueNavigator {

        public NPQueueNavigator(MediaSessionCompat mediaSession) {
            super(mediaSession);
        }

        @Override
        public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
            return currentPlaylistItems.get(windowIndex).getDescription();
        }
    }

    private class NPPlaybackPreparer implements MediaSessionConnector.PlaybackPreparer {

        @Override
        public long getSupportedPrepareActions() {
            return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID |
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH |
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        }

        @Override
        public void onPrepare(boolean playWhenReady) {
            MediaItem recentSong = storage.loadRecentSong();
            if (recentSong == null) {
                return;
            }
            onPrepareFromMediaId(recentSong.getMediaId(), playWhenReady, recentSong.getDescription().getExtras());

        }

        @Override
        public void onPrepareFromMediaId(String mediaId, boolean playWhenReady, @Nullable Bundle extras) {
            MediaMetadataCompat itemToPlay = musicProvider.getMusic(MediaIDHelper.extractMusicIDFromMediaID(mediaId));
            if (itemToPlay == null) {
                Log.d(TAG, "onPrepareFromMediaId: not found");
            } else {
                long playbackStartPositionMs;
                if (extras != null) {
                    playbackStartPositionMs = extras.getLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, C.TIME_UNSET);
                } else {
                    playbackStartPositionMs = C.TIME_UNSET;
                }
                preparePlaylist(
                        buildPlaylist(itemToPlay),
                        itemToPlay,
                        playWhenReady,
                        playbackStartPositionMs
                );
            }
        }

        @Override
        public void onPrepareFromSearch(String query, boolean playWhenReady, @Nullable Bundle extras) {

        }

        @Override
        public void onPrepareFromUri(Uri uri, boolean playWhenReady, @Nullable Bundle extras) {

        }

        @Override
        public boolean onCommand(Player player, ControlDispatcher controlDispatcher, String command, @Nullable Bundle extras, @Nullable ResultReceiver cb) {
            return false;
        }

        private ArrayList<MediaMetadataCompat> buildPlaylist(MediaMetadataCompat item) {
            return QueueHelper.getPlayingQueue(item.getDescription().getMediaId(), musicProvider);
        }

    }

    private class PlayerNotificationListener implements PlayerNotificationManager.NotificationListener {
        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(), MusicService.class));
                startForeground(notificationId, notification);
                isForegroundService = true;
            }
        }

        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            stopForeground(true);
            isForegroundService = false;
            stopSelf();
        }
    }

    private class PlayerEventListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                case Player.STATE_READY:
                    notificationManager.showNotificationForPlayer(currentPlayer);
                    if (playbackState == Player.STATE_READY) {
                        saveRecentSongToStorage();
                        if (!playWhenReady) {
                            stopForeground(false);
                        }
                    }
                    break;
                default:
                    notificationManager.hideNotification();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG, "onPlayerError: " + error.getMessage());
        }
    }

}
