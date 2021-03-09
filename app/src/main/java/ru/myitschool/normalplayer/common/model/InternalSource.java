package ru.myitschool.normalplayer.common.model;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.CodeBoy.MediaFacer.AudioGet;
import com.CodeBoy.MediaFacer.MediaFacer;
import com.CodeBoy.MediaFacer.mediaHolders.audioContent;

import java.util.ArrayList;
import java.util.Iterator;

public class InternalSource implements MusicProviderSource {

    private final Context context;

    public InternalSource(Context context) {
        this.context = context;
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        ArrayList<audioContent> audioContents = fetchAudioContent();
        ArrayList<MediaMetadataCompat> metadata = new ArrayList<>();
        for (audioContent a : audioContents) {
            if (a.getFilePath().endsWith(".midi") || a.getFilePath().endsWith(".mid")) continue;
            metadata.add(buildFromAudioContent(a));
        }
        return metadata.iterator();
    }

    private MediaMetadataCompat buildFromAudioContent(audioContent audioContent) {
        String id = String.valueOf(audioContent.getFilePath().hashCode());
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, audioContent.getAssetFileStringUri())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audioContent.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audioContent.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audioContent.getDuration())
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Not_implemented_yet")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, String.valueOf(audioContent.getArt_uri()))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audioContent.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, audioContent.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, String.valueOf(audioContent.getArt_uri()))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, String.valueOf(audioContent.getArt_uri()))
                .build();
    }

    private ArrayList<audioContent> fetchAudioContent() {
        return MediaFacer.withAudioContex(context).getAllAudioContent(AudioGet.externalContentUri);
    }
}
