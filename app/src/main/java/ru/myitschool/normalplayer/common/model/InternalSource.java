package ru.myitschool.normalplayer.common.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import com.CodeBoy.MediaFacer.AudioGet;
import com.CodeBoy.MediaFacer.MediaFacer;
import com.CodeBoy.MediaFacer.mediaHolders.audioContent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ru.myitschool.normalplayer.R;

public class InternalSource extends MusicProviderSource {

    public InternalSource(Context context) {
        super(context, MusicProviderSource.SourceType.INTERNAL);
        super.defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification);
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
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), audioContent.getArt_uri());
        } catch (IOException e) {
            e.printStackTrace();
            bitmap = super.defaultBitmap;
        }

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, audioContent.getAssetFileStringUri())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, String.valueOf(audioContent.getArt_uri()))
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, String.valueOf(audioContent.getArt_uri()))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, String.valueOf(audioContent.getArt_uri()))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audioContent.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, audioContent.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, audioContent.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audioContent.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audioContent.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Not_implemented_yet")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audioContent.getDuration())
                .putLong(MusicProviderSource.SOURCE_TYPE_KEY, super.sourceType.getValue())
                .build();
    }

    private ArrayList<audioContent> fetchAudioContent() {
        return MediaFacer.withAudioContex(context).getAllAudioContent(AudioGet.externalContentUri);
    }
}
