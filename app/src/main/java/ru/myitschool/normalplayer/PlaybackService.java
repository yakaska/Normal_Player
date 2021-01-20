package ru.myitschool.normalplayer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.myitschool.normalplayer.model.MusicProvider;
import ru.myitschool.normalplayer.playback.LocalPlayback;
import ru.myitschool.normalplayer.playback.PlaybackManager;
import ru.myitschool.normalplayer.playback.QueueManager;
import ru.myitschool.normalplayer.ui.MainActivity;
import ru.myitschool.normalplayer.utils.PackageValidator;

import static ru.myitschool.normalplayer.utils.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
import static ru.myitschool.normalplayer.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;

public class PlaybackService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback {
    private static final String TAG = PlaybackService.class.getSimpleName();

    public static final String ACTION_STOP_SERVICE = "ru.myitschool.ACTION_STOP_SERVICE";

    private MusicProvider mMusicProvider;
    private PlaybackManager mPlaybackManager;

    private MediaSessionCompat mSession;
    private MediaNotificationManager mMediaNotificationManager;
    private Bundle mSessionExtras;
    private PackageValidator mPackageValidator;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mMusicProvider = new MusicProvider(this);

        // To make the app more responsive, fetch and cache catalog information now.
        // This can help improve the response time in the method
        // {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}.
        mMusicProvider.retrieveMediaAsync(null /* Callback */);

        mPackageValidator = new PackageValidator(this);

        QueueManager queueManager = new QueueManager(mMusicProvider, getResources(),
                new QueueManager.MetadataUpdateListener() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        mSession.setMetadata(metadata);
                    }

                    @Override
                    public void onMetadataRetrieveError() {
                        mPlaybackManager.updatePlaybackState(
                                getString(R.string.error_no_metadata));
                    }

                    @Override
                    public void onCurrentQueueIndexUpdated(int queueIndex) {
                        mPlaybackManager.handlePlayRequest();
                    }

                    @Override
                    public void onQueueUpdated(String title,
                                               List<MediaSessionCompat.QueueItem> newQueue) {
                        mSession.setQueue(newQueue);
                        mSession.setQueueTitle(title);
                    }
                });

        LocalPlayback playback = new LocalPlayback(this, mMusicProvider);
        mPlaybackManager = new PlaybackManager(this, getResources(), mMusicProvider, queueManager,
                playback);

        // Start a new MediaSession
        mSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        mSessionExtras = new Bundle();
        mSession.setExtras(mSessionExtras);

        mPlaybackManager.updatePlaybackState(null);

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            if (startIntent.getAction() != null) {
                Log.d(TAG, "onStartCommand: " + startIntent.getAction());
                if (startIntent.getAction().equals(ACTION_STOP_SERVICE)) {
                    mPlaybackManager.handleStopRequest(null);
                    stopForeground(true);
                    stopSelf();
                }
            }
            MediaButtonReceiver.handleIntent(mSession, startIntent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mPlaybackManager.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        //og.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName + "; clientUid=" + clientUid + " ; rootHints=" + rootHints);
        /// To ensure you are not allowing any arbitrary app to browse your app's contents, you
        /// need to check the origin:
        //f (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
        //   // If the request comes from an untrusted package, return an empty browser root.
        //   // If you return null, then the media browser will not be able to connect and
        //   // no further calls will be made to other media browsing methods.
        //   Log.i(TAG, "OnGetRoot: Browsing NOT ALLOWED for unknown caller. " + "Returning empty browser root so all apps can use MediaController." + clientPackageName);
        //   return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        //
        return new BrowserRoot(MEDIA_ID_MUSICS_BY_GENRE + "/Not implemented yet", null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "OnLoadChildren: parentMediaId=" + parentMediaId);
        if (MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            // if music library is ready, return immediately
            result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
        } else {
            // otherwise, only return results when the music library is retrieved
            result.detach();
            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
                }
            });
        }
    }

    @Override
    public void onPlaybackStart() {
        Log.d(TAG, "onPlaybackStart: ");
        mSession.setActive(true);
        startService(new Intent(getApplicationContext(), PlaybackService.class));
    }


    @Override
    public void onPlaybackStop() {
        Log.d(TAG, "onPlaybackStop: ");
        mSession.setActive(false);
        //stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        Log.d(TAG, "onNotificationRequired: ");
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        Log.d(TAG, "onPlaybackStateUpdated: ");
        mSession.setPlaybackState(newState);
    }

    private static class DelayedStopHandler extends Handler {
        private final WeakReference<PlaybackService> mWeakReference;

        private DelayedStopHandler(PlaybackService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            PlaybackService service = mWeakReference.get();
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    Log.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Log.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }
}
