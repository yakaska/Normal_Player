package ru.myitschool.normalplayer.ui;

import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import ru.myitschool.normalplayer.R;

public class MediaItemAdapter extends ListAdapter<MediaItem, MediaItemAdapter.ItemViewHolder> {

    private final OnItemClickListener itemClickListener;

    private static final DiffUtil.ItemCallback<MediaItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<MediaItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull MediaItem oldItem, @NonNull MediaItem newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaItem oldItem, @NonNull MediaItem newItem) {
            return oldItem.getMediaId().equals(newItem.getMediaId());
        }
    };

    public MediaItemAdapter(OnItemClickListener itemClickListener) {
        super(DIFF_CALLBACK);
        this.itemClickListener = itemClickListener;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false), itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        onBindViewHolder(holder, position, Collections.emptyList());
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        MediaItem item = getItem(position);
        boolean fullRefresh = payloads.isEmpty();
        if (fullRefresh) {
            holder.item = item;
            holder.titleTv.setText(item.getDescription().getTitle());
            holder.artistTv.setText(item.getDescription().getSubtitle());
            Picasso.get().load(item.getDescription().getIconUri()).into(holder.artIv);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MediaItem clickedItem);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        MediaItem item = null;

        ImageView artIv;
        TextView titleTv;
        TextView artistTv;
        TextView durationTv;

        public ItemViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            artIv = itemView.findViewById(R.id.item_song_art);
            titleTv = itemView.findViewById(R.id.item_song_title);
            artistTv = itemView.findViewById(R.id.item_song_artist);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item != null) {
                        itemClickListener.onItemClick(item);
                    }
                }
            });
        }

    }
}
