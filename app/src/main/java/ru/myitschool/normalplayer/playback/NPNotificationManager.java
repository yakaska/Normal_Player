package ru.myitschool.normalplayer.playback;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import ru.myitschool.normalplayer.R;

public class NPNotificationManager {

    private static final String NOW_PLAYING_CHANNEL_ID = "ru.myitschool.normalplayer.NOW_PLAYING";
    private static final int NOW_PLAYING_NOTIFICATION_ID = 30399;

    private static final int NOTIFICATION_LARGE_ICON_SIZE = 144;


    private Player player;
    private final PlayerNotificationManager notificationManager;
    private final NotificationManager platformNotificationManager;

    public NPNotificationManager(Context context, MediaSessionCompat.Token sessionToken, PlayerNotificationManager.NotificationListener notificationListener) {
        platformNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

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

        notificationManager.setMediaSessionToken(sessionToken);
        notificationManager.setColorized(true);
        notificationManager.setSmallIcon(R.drawable.ic_notification);
        notificationManager.setFastForwardIncrementMs(0);
        notificationManager.setRewindIncrementMs(0);
    }

    public void hideNotification() {
        notificationManager.setPlayer(null);
    }

    public void showNotificationForPlayer(Player player) {
        notificationManager.setPlayer(player);
    }

    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        private MediaControllerCompat controller;

        private Uri currentIconUri;

        private Bitmap currentBitmap;

        public DescriptionAdapter(MediaControllerCompat controller) {
            this.controller = controller;
        }

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return controller.getMetadata().getDescription().getTitle();
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            return controller.getSessionActivity();
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return controller.getMetadata().getDescription().getSubtitle();
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            Uri iconUri = controller.getMetadata().getDescription().getIconUri();
            if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri;
                currentBitmap = resolveUriAsBitmap(iconUri);
                callback.onBitmap(currentBitmap);
                return null;
            } else {
                return currentBitmap;
            }
        }

        private Bitmap resolveUriAsBitmap(Uri uri) {

            return null;//Picasso.get().load(uri).resize(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE).get();

        }

    }

}
