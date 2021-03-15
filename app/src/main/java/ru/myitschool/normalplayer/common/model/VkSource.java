package ru.myitschool.normalplayer.common.model;

import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;

import com.CodeBoy.MediaFacer.mediaHolders.audioContent;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.myitschool.normalplayer.api.vk.VkApi;
import ru.myitschool.normalplayer.api.vk.VkTrack;

public class VkSource implements MusicProviderSource {

    public VkSource() {
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        ArrayList<VkTrack> vkTracks = fetchVkTracks();
        ArrayList<MediaMetadataCompat> metadata = new ArrayList<>();
        for (VkTrack vkTrack:vkTracks) {
            try {
                metadata.add(buildFromVkTrack(vkTrack));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return metadata.iterator();
    }

    private MediaMetadataCompat buildFromVkTrack(VkTrack vkTrack) throws IOException {
        String id = String.valueOf(vkTrack.getUrl().hashCode());
        Bitmap icon = Picasso.get().load(vkTrack.getTrackCovers().get(0)).get();
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, vkTrack.getUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, vkTrack.getTrackCovers().get(0))
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, vkTrack.getTrackCovers().get(1))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, vkTrack.getTrackCovers().get(0))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, vkTrack.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, vkTrack.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, vkTrack.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, vkTrack.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Not_implemented_yet")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, icon)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, vkTrack.getDuration())
                .build();
    }

    private ArrayList<VkTrack> fetchVkTracks() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(VkApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        VkApi vkApi = retrofit.create(VkApi.class);
        try {
            return (ArrayList<VkTrack>) vkApi.getAllAudio("+79035710726", "79d3bb9dbdebf8bd163ccf53eca75c10891e79400d433b33812fd8e1bffc60075b810af7b319284551a9e").execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
