package ru.myitschool.normalplayer.common.model;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.utils.MediaIDUtil;

import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_ALL;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_BY_ALBUM;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_BY_ARTIST;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_BY_GENRE;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_MUSICS_BY_SEARCH;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.MEDIA_ID_ROOT;
import static ru.myitschool.normalplayer.utils.MediaIDUtil.createMediaID;

public class MusicProvider {

    private static final String TAG = MusicProvider.class.getSimpleName();
    public static final String EXTRA_DURATION = "extra_duration";

    private MusicProviderSource source;

    // Categorized caches for music track data:
    private ConcurrentMap<String, List<MediaMetadataCompat>> musicListByArtist;
    private ConcurrentMap<String, List<MediaMetadataCompat>> musicListByAlbum;
    private ConcurrentMap<String, List<MediaMetadataCompat>> musicListByGenre;
    private final ConcurrentMap<String, MutableMediaMetadata> musicListById;

    private final Set<String> favoriteTracks;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State currentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider(Context context) {
        this(new InternalSource(context));
    }

    public MusicProvider(MusicProviderSource source) {
        this.source = source;
        musicListByArtist = new ConcurrentHashMap<>();
        musicListByAlbum = new ConcurrentHashMap<>();
        musicListByGenre = new ConcurrentHashMap<>();
        musicListById = new ConcurrentHashMap<>();
        favoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

    /**
     * Get an iterator over the list of all music
     *
     * @return music
     */
    public Iterable<String> getAllMusic() {
        if (currentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return musicListById.keySet();
    }

    /**
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getGenres() {
        if (currentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return musicListByGenre.keySet();
    }

    /**
     * Get an iterator over the list of albums
     *
     * @return albums
     */
    public Iterable<String> getAlbums() {
        if (currentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return musicListByAlbum.keySet();
    }

    /**
     * Get an iterator over the list of artists
     *
     * @return artists
     */
    public Iterable<String> getArtists() {
        if (currentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return musicListByArtist.keySet();
    }

    /**
     * Get an iterator over a shuffled collection of all songs
     */
    public Iterable<MediaMetadataCompat> getShuffledMusic() {
        if (currentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        List<MediaMetadataCompat> shuffled = new ArrayList<>(musicListById.size());
        for (MutableMediaMetadata mutableMetadata : musicListById.values()) {
            shuffled.add(mutableMetadata.metadata);
        }
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * Get music tracks of the given genre
     */
    public Iterable<MediaMetadataCompat> getMusicsByArtist(String artist) {
        if (currentState != State.INITIALIZED || !musicListByArtist.containsKey(artist)) {
            return Collections.emptyList();
        }
        return musicListByArtist.get(artist);
    }

    /**
     * Get music tracks of the given genre
     */
    public Iterable<MediaMetadataCompat> getMusicsByGenre(String genre) {
        if (currentState != State.INITIALIZED || !musicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        return musicListByGenre.get(genre);
    }

    /**
     * Get music tracks of the given album
     */
    public Iterable<MediaMetadataCompat> getMusicsByAlbum(String album) {
        if (currentState != State.INITIALIZED || !musicListByAlbum.containsKey(album)) {
            return Collections.emptyList();
        }
        return musicListByAlbum.get(album);
    }


    /**
     * Very basic implementation of a search that filter music tracks with title containing
     * the given query.
     */

    public Iterable<MediaMetadataCompat> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_TITLE, query);
    }


    /**
     * Very basic implementation of a search that filter music tracks with album containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ALBUM, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicByArtist(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ARTIST, query);
    }

    Iterable<MediaMetadataCompat> searchMusic(String metadataField, String query) {
        if (currentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        query = query.toLowerCase(Locale.US);
        for (MutableMediaMetadata track : musicListById.values()) {
            if (track.metadata.getString(metadataField).toLowerCase(Locale.US)
                    .contains(query)) {
                result.add(track.metadata);
            }
        }
        return result;
    }


    /**
     * Return the MediaMetadataCompat for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public MediaMetadataCompat getMusic(String musicId) {
        return musicListById.containsKey(musicId) ? musicListById.get(musicId).metadata : null;
    }

    public void setFavorite(String musicId, boolean favorite) {
        if (favorite) {
            favoriteTracks.add(musicId);
        } else {
            favoriteTracks.remove(musicId);
        }
    }

    public boolean isInitialized() {
        return currentState == State.INITIALIZED;
    }

    public boolean isFavorite(String musicId) {
        return favoriteTracks.contains(musicId);
    }

    public void retrieveMediaAsync(final Callback callback) {
        Log.d(TAG, "retrieveMediaAsync called");
        if (currentState == State.INITIALIZED) {
            if (callback != null) {
                // Nothing to do, execute callback immediately
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return currentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : musicListById.values()) {
            String genre = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            List<MediaMetadataCompat> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metadata);
        }
        musicListByGenre = newMusicListByGenre;
    }

    //buildListsByAlbum
    private synchronized void buildListsByAlbum() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByAlbum = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : musicListById.values()) {
            String album = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
            List<MediaMetadataCompat> list = newMusicListByAlbum.get(album);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByAlbum.put(album, list);
            }
            list.add(m.metadata);
        }
        musicListByAlbum = newMusicListByAlbum;
    }

    private synchronized void buildListsByArtist() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByArtist = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : musicListById.values()) {
            String artist = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            List<MediaMetadataCompat> list = newMusicListByArtist.get(artist);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByArtist.put(artist, list);
            }
            list.add(m.metadata);
        }
        musicListByArtist = newMusicListByArtist;
    }

    public synchronized void retrieveMedia() {
        try {
            if (currentState == State.NON_INITIALIZED) {
                currentState = State.INITIALIZING;

                Iterator<MediaMetadataCompat> tracks = source.iterator();
                while (tracks.hasNext()) {
                    MediaMetadataCompat item = tracks.next();
                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    musicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                }
                buildListsByGenre();
                buildListsByAlbum();
                buildListsByArtist();
                currentState = State.INITIALIZED;
            }
        } finally {
            if (currentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                currentState = State.NON_INITIALIZED;
            }
        }
    }

    public List<MediaBrowserCompat.MediaItem> getChildren(String mediaId, Resources resources) {
        Log.d(TAG, "getChildren: ");
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (!MediaIDUtil.isBrowseable(mediaId)) {
            return mediaItems;
        }

        if (MEDIA_ID_ROOT.equals(mediaId)) {
            mediaItems.add(createBrowsableMediaItemForRoot(resources));


        } else if (MEDIA_ID_MUSICS_ALL.equals(mediaId)) {

            for (String id : getAllMusic()) {
                mediaItems.add(createMediaItem(getMusic(id), MEDIA_ID_MUSICS_ALL));
            }

        } else if (MEDIA_ID_MUSICS_BY_GENRE.equals(mediaId)) {
            for (String genre : getGenres()) {
                mediaItems.add(createBrowsableMediaItemForGenre(genre, resources));
            }

        } else if (mediaId.startsWith(MEDIA_ID_MUSICS_BY_GENRE)) {
            String genre = MediaIDUtil.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicsByGenre(genre)) {
                mediaItems.add(createMediaItem(metadata, MEDIA_ID_MUSICS_BY_GENRE));
            }

        } else if (MEDIA_ID_MUSICS_BY_ALBUM.equals(mediaId)) {
            for (String album : getAlbums()) {
                mediaItems.add(createBrowsableMediaItemForAlbum(album, resources));
            }

        } else if (mediaId.startsWith(MEDIA_ID_MUSICS_BY_ALBUM)) {
            String album = MediaIDUtil.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicsByAlbum(album)) {
                mediaItems.add(createMediaItem(metadata, MEDIA_ID_MUSICS_BY_ALBUM));
            }

        } else if(MEDIA_ID_MUSICS_BY_ARTIST.equals(mediaId)) {
            for (String artist : getArtists()) {
                mediaItems.add(createBrowsableMediaItemForArtist(artist, resources));
            }

        } else if (mediaId.startsWith(MEDIA_ID_MUSICS_BY_ARTIST)){
            String artist = MediaIDUtil.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicsByArtist(artist)) {
                mediaItems.add(createMediaItem(metadata, MEDIA_ID_MUSICS_BY_ARTIST));
            }
        } else {
            Log.w(TAG, "Skipping unmatched mediaId: " + mediaId);

        }
        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_MUSICS_BY_ALBUM)
                .setTitle(resources.getString(R.string.browse_genres))
                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForGenre(String genre, Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(createMediaID(null, MEDIA_ID_MUSICS_BY_GENRE, genre))
                .setTitle(genre)
                .setSubtitle(resources.getString(
                        R.string.browse_musics_by_genre_subtitle, genre))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    //createBrowsableMediaItemForAlbum
    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForAlbum(String album, Resources resources) {
        MediaMetadataCompat metadata = searchMusicByAlbum(album).iterator().next();
        String iconUri = "";
        if (metadata != null) {
            iconUri = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        }
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(createMediaID(null, MEDIA_ID_MUSICS_BY_ALBUM, album))
                .setTitle(album)
                .setSubtitle(resources.getString(
                        R.string.browse_musics_by_album_subtitle, album))
                .setIconUri(Uri.parse(iconUri))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForArtist(String artist, Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(createMediaID(null, MEDIA_ID_MUSICS_BY_ARTIST, artist))
                .setTitle(artist)
                .setSubtitle(resources.getString(R.string.browse_musics_by_artist_subtitle, artist))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    public MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String key) {
        String uniq;

        switch (key) {
            case MEDIA_ID_MUSICS_BY_GENRE:
                uniq = metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
                break;
            case MEDIA_ID_MUSICS_BY_ALBUM:
                uniq = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
                break;
            case MEDIA_ID_MUSICS_BY_ARTIST:
                uniq = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                break;
            case MEDIA_ID_MUSICS_BY_SEARCH:
                uniq = MEDIA_ID_MUSICS_BY_SEARCH;
                break;
            default:
                uniq = metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
                break;
        }

        String hierarchyAwareMediaID = createMediaID(metadata.getDescription().getMediaId(), key, uniq);
        Bundle extras = new Bundle();
        extras.putLong(EXTRA_DURATION, metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setSubtitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .setIconUri(Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)))
                .setMediaId(hierarchyAwareMediaID)
                .setIconBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
                .setExtras(extras)
                .build();

        Log.d(TAG, "createMediaItem: extra " + extras.getLong(EXTRA_DURATION));
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    }

}
