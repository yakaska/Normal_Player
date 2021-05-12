package ru.myitschool.normalplayer.common.playback;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import ru.myitschool.normalplayer.App;
import ru.myitschool.normalplayer.R;

public class NPNotificationManager {

    private static final int NOW_PLAYING_NOTIFICATION_ID = 0xb339;

    private final PlayerNotificationManager notificationManager;

    public NPNotificationManager(Context context, MediaSessionCompat mediaSession, PlayerNotificationManager.NotificationListener notificationListener) {

        MediaControllerCompat mediaController = new MediaControllerCompat(context, mediaSession);

        notificationManager = new PlayerNotificationManager(
                context,
                App.NOW_PLAYING_CHANNEL_ID,
                NOW_PLAYING_NOTIFICATION_ID,
                new DescriptionAdapter(mediaController),
                notificationListener);

        notificationManager.setMediaSessionToken(mediaSession.getSessionToken());
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

        private final MediaControllerCompat controller;

        public DescriptionAdapter(MediaControllerCompat controller) {
            this.controller = controller;
        }

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            Log.d("TAG", "getCurrentContentTitle: " + controller.getMetadata().getDescription().getTitle());
            return controller.getMetadata().getDescription().getTitle();
        }

        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            return controller.getSessionActivity();
        }

        @Override
        public CharSequence getCurrentContentText(Player player) {
            Log.d("TAG", "getCurrentContentText: " + controller.getMetadata().getDescription().getSubtitle());
            return controller.getMetadata().getDescription().getSubtitle();
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            return controller.getMetadata().getDescription().getIconBitmap();
        }

    }

}