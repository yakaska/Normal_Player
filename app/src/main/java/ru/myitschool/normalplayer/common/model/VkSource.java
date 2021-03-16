package ru.myitschool.normalplayer.common.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.api.vk.VkApi;
import ru.myitschool.normalplayer.api.vk.VkTrack;

public class VkSource implements MusicProviderSource {

    private final Bitmap defaultArt;

    public VkSource(Context context) {
        defaultArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification);
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
        Log.d("VK", "buildFromVkTrack: " + vkTrack.getDuration());
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
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(VkApi.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        VkApi vkApi = retrofit.create(VkApi.class);
        try {
            return new ArrayList<>(vkApi.getAllAudio("+79035710726", "79d3bb9dbdebf8bd163ccf53eca75c10891e79400d433b33812fd8e1bffc60075b810af7b319284551a9e").execute().body());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

}
