package ru.myitschool.normalplayer.common.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.api.vk.VkService;
import ru.myitschool.normalplayer.api.vk.model.Item;
import ru.myitschool.normalplayer.api.vk.model.Response;
import ru.myitschool.normalplayer.api.vk.model.response.VkMusicResponse;

public class VkSource implements MusicProviderSource {
    private final Context context;

    private final Bitmap defaultArt;

    public VkSource(Context context) {
        this.context = context;
        defaultArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification);
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        VkMusicResponse response = fetchVkTracks();
        List<Item> itemList = response.getResponse().getItems();
        ArrayList<MediaMetadataCompat> metadata = new ArrayList<>();
        for (Item track : itemList) {
            metadata.add(buildFromVkTrack(track));
        }
        return metadata.iterator();
    }

    private MediaMetadataCompat buildFromVkTrack(Item item) {
        String id = item.getUrl();
        String url = toMp3(item.getUrl());
        Log.d("URL", url);
        String highRes = "android.resource://ru.myitschool.normalplayer/" + R.drawable.ic_default_art;
        String lowRes = "android.resource://ru.myitschool.normalplayer/" + R.drawable.ic_default_art;
        String title = item.getTitle();
        String artist = item.getArtist();
        Integer duration = item.getDuration() * 1000;
        Bitmap icon = defaultArt;
        if (item.getAlbum() != null) {
            if (item.getAlbum().getThumb() != null) {
                highRes = item.getAlbum().getThumb().getPhoto600();
                lowRes = item.getAlbum().getThumb().getPhoto300();
                try {
                    icon = Picasso.get().load(highRes).placeholder(R.drawable.ic_default_art).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, url)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, highRes)
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, highRes)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, lowRes)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Not_implemented_yet")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, icon)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build();
    }

    private VkMusicResponse fetchVkTracks() {
        try {
            return VkService.getInstance().getVkApi().getAllAudio(500, "5e71d7130d3098608375813d40c468eee7a17eb95d2a55f85caff75d638906b3a43f9ec2439ab928326e5").execute().body();
        } catch (IOException e) {
            Log.e("VK", "fetchVkTracks: " + e.getMessage());
            return new VkMusicResponse(new Response());
        }
    }

    private String toMp3(String url) {
        url = url.replaceAll("/[0-9a-z]+(/audios)?/([0-9a-f]+)/index.m3u8", "$1/$2.mp3");
        return url;
    }

}
