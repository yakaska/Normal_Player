package ru.myitschool.normalplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "main_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelMain = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Channel Main",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelMain.setDescription("This is Channel Main");
            channelMain.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channelMain);
        }
    }

}
