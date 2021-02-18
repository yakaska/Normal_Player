package ru.myitschool.normalplayer.playback;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import ru.myitschool.normalplayer.R;

public class NPNotificationManager {

    private static final String TAG = NPNotificationManager.class.getSimpleName();

    private static final String NOW_PLAYING_CHANNEL_ID = "ru.myitschool.normalplayer.NOW_PLAYING";
    private static final int NOW_PLAYING_NOTIFICATION_ID = 30399;

    private static final int NOTIFICATION_LARGE_ICON_SIZE = 144;

    private final PlayerNotificationManager notificationManager;

    public NPNotificationManager(Context context, MediaSessionCompat.Token sessionToken, PlayerNotificationManager.NotificationListener notificationListener) {

        MediaControllerCompat mediaController = null;
        try {
            mediaController = new MediaControllerCompat(context, sessionToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context,
                NOW_PLAYING_CHANNEL_ID,
                R.string.notification_channel_name,
                R.string.notification_channel_description,
                NOW_PLAYING_NOTIFICATION_ID,
                new DescriptionAdapter(mediaController),
                notificationListener
        );

        notificationManager.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
        notificationManager.setColorized(true);
        notificationManager.setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.setSmallIcon(R.drawable.ic_notification);
        notificationManager.setFastForwardIncrementMs(0);
        notificationManager.setRewindIncrementMs(0);
        notificationManager.setMediaSessionToken(sessionToken);
    }

    public void hideNotification() {
        Log.d(TAG, "hideNotification: ");
        notificationManager.setPlayer(null);
    }

    public void showNotificationForPlayer(Player player) {
        notificationManager.setPlayer(player);
    }

    private static class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        private final MediaControllerCompat controller;

        private Uri currentIconUri;

        private Bitmap currentBitmap;

        public DescriptionAdapter(MediaControllerCompat controller) {
            this.controller = controller;
        }

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            Log.d(TAG, "getCurrentContentTitle: " + controller.getMetadata().getDescription().getTitle());
            return controller.getMetadata().getDescription().getTitle();
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            Log.d(TAG, "createCurrentContentIntent: ");
            return controller.getSessionActivity();
        }



        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            Log.d(TAG, "getCurrentContentText: " + controller.getMetadata().getDescription().getSubtitle());
            return controller.getMetadata().getDescription().getSubtitle();
        }

        @Nullable
        @Override
        public CharSequence getCurrentSubText(Player player) {
            return controller.getMetadata().getDescription().getSubtitle();
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            return controller.getMetadata().getDescription().getIconBitmap();
        }

        private Bitmap resolveUriAsBitmap(Uri uri) {
            return null;//Picasso.get().load(uri).get();
        }

    }

}