package ru.myitschool.normalplayer.common.playback;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import ru.myitschool.normalplayer.common.model.InternalSource;
import ru.myitschool.normalplayer.common.model.MusicProvider;
import ru.myitschool.normalplayer.common.model.MusicProviderSource;
import ru.myitschool.normalplayer.common.model.VkSource;
import ru.myitschool.normalplayer.ui.activity.MainActivity;
import ru.myitschool.normalplayer.utils.CacheUtil;
import ru.myitschool.normalplayer.utils.MediaIDUtil;
import ru.myitschool.normalplayer.utils.PlayerUtil;
import ru.myitschool.normalplayer.utils.QueueUtil;

import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_EMPTY_ROOT;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_ROOT;

public class MusicService extends MediaBrowserServiceCompat {

    private static final String TAG = MusicService.class.getSimpleName();

    public static final String ACTION_CHANGE_SOURCE = "ru.myitschool.normalplayer.ACTION_CHANGE_SOURCE";
    public static final String ACTION_DETAILS = "ru.myitschool.normalplayer.ACTION_DETAILS";
    public static final String ACTION_SHARE = "ru.myitschool.normalplayer.ACTION_SHARE";
    public static final String ACTION_DOWNLOAD = "ru.myitschool.normalplayer.ACTION_DOWNLOAD";

    public static final String MEDIA_EXTRA_START_PLAYBACK_POSITION = "extra_playback_start_position";
    public static final String MEDIA_EXTRA_FILE_URI = "extra_file_uri";
    public static final String MEDIA_EXTRA_FILE_NAME = "extra_file_name";

    private static final String NP_USER_AGENT = "NP_USER_AGENT";

    private final AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build();
    private final PlayerEventListener playerListener = new PlayerEventListener();

    private NPNotificationManager notificationManager;
    private MusicProvider musicProvider;
    private ArrayList<MediaMetadataCompat> currentPlaylistItems = new ArrayList<>();
    private CacheDataSourceFactory cacheDataSourceFactory;
    private boolean isForegroundService;
    private SimpleExoPlayer exoPlayer;

    protected MediaSessionCompat mediaSession;
    protected MediaSessionConnector mediaSessionConnector;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        Bundle ex = new Bundle();
        ex.putLong("ass", MusicProviderSource.SourceType.INTERNAL.getValue());
        musicProvider = new MusicProvider(new InternalSource(getApplicationContext()));
        musicProvider.retrieveMediaAsync(success -> {
            if (success) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mediaSession = new MediaSessionCompat(getApplicationContext(), "MusicService");
                mediaSession.setSessionActivity(pi);
                mediaSession.setActive(true);
                setSessionToken(mediaSession.getSessionToken());
                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), NP_USER_AGENT), null);
                cacheDataSourceFactory = new CacheDataSourceFactory(CacheUtil.getPlayerCache(getApplicationContext()), dataSourceFactory, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
                exoPlayer = new SimpleExoPlayer.Builder(getApplicationContext()).build();
                exoPlayer.setAudioAttributes(audioAttributes, true);
                exoPlayer.setHandleAudioBecomingNoisy(true);
                exoPlayer.addListener(playerListener);
                mediaSessionConnector = new MediaSessionConnector(mediaSession);
                mediaSessionConnector.setPlaybackPreparer(new NPPlaybackPreparer());
                mediaSessionConnector.setQueueNavigator(new NPQueueNavigator(mediaSession));
                mediaSessionConnector.setPlayer(exoPlayer);
                notificationManager = new NPNotificationManager(getApplicationContext(), mediaSession, new PlayerNotificationListener());
                notificationManager.showNotificationForPlayer(exoPlayer);
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mediaSession.setActive(false);
        mediaSession.release();
        notificationManager.hideNotification();
        exoPlayer.removeListener(playerListener);
        exoPlayer.release();
    }

    @Override
    public void onCustomAction(@NonNull String action, Bundle extras, @NonNull Result<Bundle> result) {
        Log.d(TAG, "onCustomAction: " + action);
        result.detach();

        switch (action) {
            case ACTION_CHANGE_SOURCE:
                if (extras.getLong(MusicProviderSource.SOURCE_TYPE_KEY) == MusicProviderSource.SourceType.INTERNAL.getValue()) {
                    musicProvider = new MusicProvider(new InternalSource(getApplicationContext()));
                } else {
                    musicProvider = new MusicProvider(new VkSource(getApplicationContext()));
                }
                break;
            case ACTION_DETAILS:
                Toast.makeText(this, "DETAILS", Toast.LENGTH_SHORT).show();
                break;
            case ACTION_SHARE:
                Toast.makeText(this, "SHARE", Toast.LENGTH_SHORT).show();
                break;
            case ACTION_DOWNLOAD:
                Toast.makeText(this, "DOWNLOAD", Toast.LENGTH_SHORT).show();
                download(extras.getString(MEDIA_EXTRA_FILE_URI), extras.getString(MEDIA_EXTRA_FILE_NAME));
                break;
            default:
                break;
        }

        //if (action.equals(SOURCE_VK)) musicProvider = new MusicProvider(new VkSource(getApplicationContext()));
        //else musicProvider = new MusicProvider(new InternalSource(getApplicationContext()));
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        exoPlayer.stop(true);
        super.onTaskRemoved(rootIntent);
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
        List<MediaBrowserCompat.MediaItem> resultList = new ArrayList<>();
        for (MediaMetadataCompat metadata : musicProvider.searchMusicBySongTitle(query)) {
            resultList.add(new MediaBrowserCompat.MediaItem(metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        result.sendResult(resultList);
    }

    private void download(String url, String fileName) {
        Log.d(TAG, "download: " + fileName);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setVisibleInDownloadsUi(true)
                .setTitle(fileName)
                .setDescription("Downloading")
                .setMimeType("audio/MP3")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".mp3");
        request.allowScanningByMediaScanner();
        downloadManager.enqueue(request);
    }

    private void preparePlaylist(ArrayList<MediaMetadataCompat> metadataList, @Nullable String mediaIdToPlay, boolean playWhenReady, long playbackStartPositionMs) {
        int initialWindowIndex = 0;
        if (mediaIdToPlay == null) {
            initialWindowIndex = 0;
        } else {
            for (MediaMetadataCompat metadata : metadataList) {
                if (metadata.getDescription().getMediaId().equals(mediaIdToPlay)) {
                    initialWindowIndex = metadataList.indexOf(metadata);
                }
            }
        }
        currentPlaylistItems = metadataList;
        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.stop(true);
        ConcatenatingMediaSource mediaSource = PlayerUtil.metadataListToMediaSource(metadataList, cacheDataSourceFactory);
        exoPlayer.prepare(mediaSource);
        Log.d(TAG, "start:" + playbackStartPositionMs + " window:" + initialWindowIndex);
        exoPlayer.seekTo(initialWindowIndex, playbackStartPositionMs);
    }

    private class NPQueueNavigator extends TimelineQueueNavigator {

        public NPQueueNavigator(MediaSessionCompat mediaSession) {
            super(mediaSession);
        }

        @Override
        public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
            Bundle extras = new Bundle();
            MediaDescriptionCompat oldDesc = currentPlaylistItems.get(windowIndex).getDescription();

            extras.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, String.valueOf(oldDesc.getSubtitle()));
            extras.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, String.valueOf(oldDesc.getIconUri()));
            extras.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, String.valueOf(oldDesc.getIconUri()));

            return new MediaDescriptionCompat.Builder()
                    .setMediaId(oldDesc.getMediaId())
                    .setMediaUri(oldDesc.getMediaUri())
                    .setIconUri(oldDesc.getIconUri())
                    .setIconBitmap(oldDesc.getIconBitmap())
                    .setTitle(oldDesc.getTitle())
                    .setSubtitle(oldDesc.getSubtitle())
                    .setExtras(extras)
                    .build();
        }
    }

    private class NPPlaybackPreparer implements MediaSessionConnector.PlaybackPreparer {

        @Override
        public long getSupportedPrepareActions() {
            return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID |
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH |
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                    PlaybackStateCompat.ACTION_SET_REPEAT_MODE |
                    PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE;
        }

        @Override
        public void onPrepare(boolean playWhenReady) {
        }

        @Override
        public void onPrepareFromMediaId(String mediaId, boolean playWhenReady, @Nullable Bundle extras) {
            MediaMetadataCompat itemToPlay = musicProvider.getMusic(MediaIDUtil.extractMusicIDFromMediaID(mediaId));
            if (itemToPlay == null) {
                Log.d(TAG, "onPrepareFromMediaId: not found");
            } else {
                long playbackStartPositionMs;
                if (extras != null) {
                    playbackStartPositionMs = extras.getLong(MEDIA_EXTRA_START_PLAYBACK_POSITION, 0);
                } else {
                    playbackStartPositionMs = 0;
                }
                preparePlaylist(
                        buildPlaylist(mediaId),
                        mediaId,
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

        private ArrayList<MediaMetadataCompat> buildPlaylist(String mediaId) {
            return QueueUtil.getPlayingQueue(mediaId, musicProvider);
        }

    }

    private class PlayerNotificationListener implements PlayerNotificationManager.NotificationListener {
        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            if (ongoing && !isForegroundService) {
                Log.d(TAG, "onNotificationPosted: ");
                ContextCompat.startForegroundService(
                        getApplicationContext(),
                        new Intent(getApplicationContext(), MusicService.class)
                );

                startForeground(notificationId, notification);
                isForegroundService = true;
            }
        }

        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            Log.d(TAG, "onNotificationCancelled: ");
            stopForeground(true);
            isForegroundService = false;
            stopSelf();
        }
    }

    private class PlayerEventListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY) {
                notificationManager.showNotificationForPlayer(exoPlayer);
                if (playbackState == Player.STATE_READY) {
                    if (!playWhenReady) {
                        stopForeground(false);
                    }
                }
            } else {
                notificationManager.hideNotification();
            }
        }


        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG, "onPlayerError: " + error.getMessage());
        }
    }

}
