package ru.myitschool.normalplayer.common.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.MediaMetadataCompat;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.api.vk.VkService;
import ru.myitschool.normalplayer.api.vk.VkTrack;

public class VkSource implements MusicProviderSource {

    private final Context context;

    private final Bitmap defaultArt;

    public VkSource(Context context) {
        this.context = context;
        this.defaultArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification);
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        ArrayList<VkTrack> vkTracks = fetchVkTracks();
        ArrayList<MediaMetadataCompat> metadata = new ArrayList<>();
        for (VkTrack vkTrack : vkTracks) {
            metadata.add(buildFromVkTrack(vkTrack));
        }
        return metadata.iterator();
    }

    private MediaMetadataCompat buildFromVkTrack(VkTrack vkTrack) {
        String id = String.valueOf(vkTrack.getUrl().hashCode());
        Bitmap icon = defaultArt;
        String highRes = "android.resource://ru.myitschool.normalplayer/" + R.drawable.ic_default_art;
        String lowRes = "android.resource://ru.myitschool.normalplayer/" + R.drawable.ic_default_art;
        if (!vkTrack.getTrackCovers().isEmpty()) {
            try {
                icon = Picasso.get().load(vkTrack.getTrackCovers().get(1)).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            highRes = vkTrack.getTrackCovers().get(1);
            lowRes = vkTrack.getTrackCovers().get(0);
        }
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, vkTrack.getUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, highRes)
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, highRes)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, highRes)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, vkTrack.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, vkTrack.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, vkTrack.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, vkTrack.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, vkTrack.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Not_implemented_yet")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, icon)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, vkTrack.getDuration())
                .build();
    }

    private ArrayList<VkTrack> fetchVkTracks() {
        try {
            ArrayList<VkTrack> tracks = VkService.getInstance(context).getVkApi().getAllAudio("79035710726", "c3dc14510b5894de040bcdc001d4232f89d20bd67c0861a4890cf6814d21a4781dc75595f8290855f1011").execute().body();
            return tracks == null ? new ArrayList<>() : tracks;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

}
