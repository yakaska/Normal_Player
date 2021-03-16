package ru.myitschool.normalplayer.ui.model;

import android.content.Context;
import android.net.Uri;

import ru.myitschool.normalplayer.R;

public class NowPlayingMetadata {

    private String mediaId;

    private Uri albumArtUri;

    private String title;

    private String subtitle;

    private String duration;
    private long durationMs;

    public NowPlayingMetadata(String mediaId, Uri albumArtUri, String title, String subtitle, String duration, long durationMs) {
        this.mediaId = mediaId;
        this.albumArtUri = albumArtUri;
        this.title = title;
        this.subtitle = subtitle;
        this.duration = duration;
        this.durationMs = durationMs;
    }


    public static String timestampToMSS(Context context, long position) {
        int totalSeconds = (int) Math.floor(position / 1000);
        int minutes = totalSeconds / 60;
        int remainingSeconds = totalSeconds - (minutes * 60);
        if (position < 0) return context.getString(R.string.duration_unknown);
        else
            return String.format(context.getString(R.string.duration_format), minutes, remainingSeconds);
    }

    public String getMediaId() {
        return mediaId;
    }

    public Uri getAlbumArtUri() {
        return albumArtUri;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDuration() {
        return duration;
    }

    public long getDurationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        return "NowPlayingMetadata{" +
                "mediaId='" + mediaId + '\'' +
                ", albumArtUri=" + albumArtUri +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", duration='" + duration + '\'' +
                ", durationMs=" + durationMs +
                '}';
    }
}
