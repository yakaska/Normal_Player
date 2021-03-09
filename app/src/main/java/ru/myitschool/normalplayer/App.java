package ru.myitschool.normalplayer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String NOW_PLAYING_CHANNEL_ID = "ru.myitschool.normalplayer.NOW_PLAYING";

    @Override
    public void onCreate() {
        super.onCreate();
        
        createNotificationChannels();
        
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nowPlayingChannel = new NotificationChannel(
                    NOW_PLAYING_CHANNEL_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nowPlayingChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nowPlayingChannel.setDescription("This is Channel 1");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(nowPlayingChannel);
        }
    }
}
