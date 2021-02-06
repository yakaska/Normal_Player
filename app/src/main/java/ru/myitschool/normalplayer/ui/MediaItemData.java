package ru.myitschool.normalplayer.ui;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

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

    private String mediaId;
    private String title;
    private String subtitle;
    private Uri albumArtUri;
    private long duration;
    private boolean browsable;
    private int playbackRes;

    public MediaItemData(String mediaId, String title, String subtitle, Uri albumArtUri, long duration, boolean browsable, int playbackRes) {
        this.mediaId = mediaId;
        this.title = title;
        this.subtitle = subtitle;
        this.albumArtUri = albumArtUri;
        this.duration = duration;
        this.browsable = browsable;
        this.playbackRes = playbackRes;
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
}