package ru.myitschool.normalplayer.utils;

import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;

import ru.myitschool.normalplayer.common.model.MusicProvider;

import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_ALL;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_BY_ALBUM;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_BY_ARTIST;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_BY_GENRE;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_BY_VK;

public class QueueUtil {

    private static final String TAG = QueueUtil.class.getSimpleName();

    public static ArrayList<MediaMetadataCompat> getPlayingQueue(String mediaId, MusicProvider musicProvider) {
        String[] hierarchy = MediaIDUtil.getHierarchy(mediaId);
        if (hierarchy.length != 2) {
            return null;
        }
        String categoryType = hierarchy[0];
        String categoryValue = hierarchy[1];
        Iterable<MediaMetadataCompat> tracks = null;
        switch (categoryType) {
            case MEDIA_ID_MUSICS_BY_VK:
                tracks = musicProvider.getMusicsByVk();
                break;
            case MEDIA_ID_MUSICS_BY_GENRE:
                tracks = musicProvider.getMusicsByGenre(categoryValue);
                break;
            case MEDIA_ID_MUSICS_BY_ALBUM:
                tracks = musicProvider.getMusicsByAlbum(categoryValue);
                break;
            case MEDIA_ID_MUSICS_BY_ARTIST:
                tracks = musicProvider.getMusicsByArtist(categoryValue);
                break;
            case MEDIA_ID_MUSICS_ALL:
                tracks = musicProvider.getMusicsByGenre(categoryValue);
                break;
        }
        if (tracks == null) {
            Log.e(TAG, "Unrecognized category type: " + categoryType + " for media " + mediaId);
            return null;
        }
        return convertToQueue(tracks, hierarchy[0], hierarchy[1]);
    }

    private static ArrayList<MediaMetadataCompat> convertToQueue(Iterable<MediaMetadataCompat> tracks, String... categories) {
        ArrayList<MediaMetadataCompat> queue = new ArrayList<>();
        for (MediaMetadataCompat track : tracks) {
            String hierarchyAwareMediaID = MediaIDUtil.createMediaID(
                    track.getDescription().getMediaId(), categories);
            MediaMetadataCompat trackCopy = new MediaMetadataCompat.Builder(track)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                    .build();
            queue.add(trackCopy);
        }
        return queue;
    }
}
