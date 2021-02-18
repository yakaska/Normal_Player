package ru.myitschool.normalplayer.utils;

import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import ru.myitschool.normalplayer.model.MusicProvider;

import static ru.myitschool.normalplayer.utils.MediaIDHelper.MEDIA_ID_MUSICS_ALL;
import static ru.myitschool.normalplayer.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM;
import static ru.myitschool.normalplayer.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST;
import static ru.myitschool.normalplayer.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;

public class QueueHelper {

    private static final String TAG = QueueHelper.class.getSimpleName();

    public static ArrayList<MediaMetadataCompat> getPlayingQueue(String mediaId, MusicProvider musicProvider) {

        // extract the browsing hierarchy from the media ID:
        String[] hierarchy = MediaIDHelper.getHierarchy(mediaId);

        Log.d(TAG, "getPlayingQueue: " + Arrays.toString(hierarchy));
        if (hierarchy.length != 2) {
            Log.e(TAG, "Could not build a playing queue for this mediaId: " + mediaId);
            return null;
        }

        String categoryType = hierarchy[0];
        String categoryValue = hierarchy[1];
        Log.d(TAG, "Creating playing queue for " + categoryType + "  " + categoryValue);

        Iterable<MediaMetadataCompat> tracks = null;
        // This sample only supports genre and by_search category types.
        if (categoryType.equals(MEDIA_ID_MUSICS_BY_GENRE)) {
            Log.d(TAG, "getPlayingQueue:  genre");
            tracks = musicProvider.getMusicsByGenre(categoryValue);
        } else if (categoryType.equals(MEDIA_ID_MUSICS_BY_ALBUM)) {
            Log.d(TAG, "getPlayingQueue: album");
            tracks = musicProvider.getMusicsByAlbum(categoryValue);
        } else if (categoryType.equals(MEDIA_ID_MUSICS_BY_ARTIST)) {
            Log.d(TAG, "getPlayingQueue: artist");
            tracks = musicProvider.getMusicsByArtist(categoryValue);
        } else if (categoryType.equals(MEDIA_ID_MUSICS_ALL)) {
            Log.d(TAG, "getPlayingQueue: " + categoryValue);
            tracks = musicProvider.getMusicsByGenre(categoryValue);
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

            // We create a hierarchy-aware mediaID, so we know what the queue is about by looking
            // at the QueueItem media IDs.
            String hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                    track.getDescription().getMediaId(), categories);
//
            MediaMetadataCompat trackCopy = new MediaMetadataCompat.Builder(track)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                    .build();
//
            // We don't expect queues to change after created, so we use the item index as the
            // queueId. Any other number unique in the queue would work.

            queue.add(trackCopy);
        }
        return queue;


    }
}
