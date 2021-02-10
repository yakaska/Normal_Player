package ru.myitschool.normalplayer.playback;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;

import static ru.myitschool.normalplayer.playback.MusicService.MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS;

public class PersistentStorage {

    private static String PREFERENCES_NAME = "normal_player";
    private static String RECENT_SONG_MEDIA_ID_KEY = "recent_song_media_id";
    private static String RECENT_SONG_TITLE_KEY = "recent_song_title";
    private static String RECENT_SONG_SUBTITLE_KEY = "recent_song_subtitle";
    private static String RECENT_SONG_ICON_URI_KEY = "recent_song_icon_uri";
    private static String RECENT_SONG_POSITION_KEY = "recent_song_position";

    private static PersistentStorage instance;

    private SharedPreferences preferences;

    public static synchronized PersistentStorage getInstance(Context context) {
        if (instance == null) {
            instance = new PersistentStorage(context);
        }
        return instance;
    }

    private PersistentStorage(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void saveRecentSong(MediaDescriptionCompat description, long position) {
        preferences.edit()
                .putString(RECENT_SONG_MEDIA_ID_KEY, description.getMediaId())
                .putString(RECENT_SONG_TITLE_KEY, String.valueOf(description.getTitle()))
                .putString(RECENT_SONG_SUBTITLE_KEY, String.valueOf(description.getSubtitle()))
                .putString(RECENT_SONG_ICON_URI_KEY, String.valueOf(description.getIconUri()))
                .putLong(RECENT_SONG_POSITION_KEY, position)
                .apply();
    }

    public MediaItem loadRecentSong() {
        String mediaId = preferences.getString(RECENT_SONG_MEDIA_ID_KEY, null);
        if (mediaId == null) {
            return null;
        } else {
            long position = preferences.getLong(RECENT_SONG_POSITION_KEY, 0);
            Bundle extras = new Bundle();
            extras.putLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, position);
            return new MediaItem(new MediaDescriptionCompat.Builder()
                    .setMediaId(mediaId)
                    .setTitle(preferences.getString(RECENT_SONG_TITLE_KEY, ""))
                    .setSubtitle(preferences.getString(RECENT_SONG_SUBTITLE_KEY, ""))
                    .setIconUri(Uri.parse(preferences.getString(RECENT_SONG_ICON_URI_KEY, "")))
                    .setExtras(extras)
                    .build(), MediaItem.FLAG_PLAYABLE);
        }
    }

}
