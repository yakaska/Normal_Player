package ru.myitschool.normalplayer.api.vk;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VkTrack {
    @SerializedName("title")
    private String title;

    @SerializedName("artist")
    private String artist;

    @SerializedName("url")
    private String url;

    @SerializedName("track_cover")
    private List<String> trackCover;

    @SerializedName("duration")
    private int duration;

    public VkTrack(String title, String artist, String url, List<String> trackCover, int duration) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.trackCover = trackCover;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getTrackCovers() {
        return trackCover;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "VkTrack{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", url='" + url + '\'' +
                ", trackCover=" + trackCover +
                ", duration=" + duration +
                '}';
    }
}
