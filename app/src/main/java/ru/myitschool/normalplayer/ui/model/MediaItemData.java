package ru.myitschool.normalplayer.ui.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Comparator;

public class MediaItemData {

    public static final int PLAYBACK_RES_CHANGED = 1;

    public static final DiffUtil.ItemCallback<MediaItemData> DIFF_CALLBACK = new DiffUtil.ItemCallback<MediaItemData>() {
        @Override
        public boolean areItemsTheSame(@NonNull MediaItemData oldItem, @NonNull MediaItemData newItem) {
            return oldItem.mediaId.equals(newItem.mediaId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaItemData oldItem, @NonNull MediaItemData newItem) {
            return oldItem.mediaId.equals(newItem.mediaId) && oldItem.playbackRes == newItem.playbackRes;
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull MediaItemData oldItem, @NonNull MediaItemData newItem) {
            if (oldItem.playbackRes != newItem.playbackRes) {
                return PLAYBACK_RES_CHANGED;
            } else {
                return null;
            }
        }
    };

    //A-Z ... А-Я
    public static Comparator<MediaItemData> byTitleAsc = new Comparator<MediaItemData>() {
        @Override
        public int compare(MediaItemData o1, MediaItemData o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    };

    //Z-A ... Я-А
    public static Comparator<MediaItemData> byTitleDesc = new Comparator<MediaItemData>() {
        @Override
        public int compare(MediaItemData o1, MediaItemData o2) {
            return -(o1.getTitle().compareTo(o2.getTitle()));
        }
    };

    private String mediaId;
    private String title;
    private String subtitle;
    private Uri mediaUri;
    private Uri albumArtUri;
    private long duration;
    private boolean browsable;
    private int playbackRes;
    private long sourceType;

    public MediaItemData(String mediaId, String title, String subtitle, Uri mediaUri, Uri albumArtUri, long duration, boolean browsable, int playbackRes, long sourceType) {
        this.mediaId = mediaId;
        this.title = title;
        this.subtitle = subtitle;
        this.mediaUri = mediaUri;
        this.albumArtUri = albumArtUri;
        this.duration = duration;
        this.browsable = browsable;
        this.playbackRes = playbackRes;
        this.sourceType = sourceType;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Uri getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(Uri mediaUri) {
        this.mediaUri = mediaUri;
    }

    public Uri getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(Uri albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isBrowsable() {
        return browsable;
    }

    public void setBrowsable(boolean browsable) {
        this.browsable = browsable;
    }

    public int getPlaybackRes() {
        return playbackRes;
    }

    public void setPlaybackRes(int playbackRes) {
        this.playbackRes = playbackRes;
    }

    public long getSourceType() {
        return sourceType;
    }
}