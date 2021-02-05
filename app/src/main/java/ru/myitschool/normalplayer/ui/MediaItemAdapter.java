package ru.myitschool.normalplayer.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.utils.Utils;

public class MediaItemAdapter extends ListAdapter<MediaItemData, MediaItemAdapter.ItemViewHolder> {

    private static final String TAG = MediaItemAdapter.class.getSimpleName();

    private final OnItemClickListener itemClickListener;

    public MediaItemAdapter(OnItemClickListener itemClickListener) {
        super(MediaItemData.DIFF_CALLBACK);
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
        MediaItemData item = getItem(position);
        boolean fullRefresh = payloads.isEmpty();

        if (!payloads.isEmpty()) {
            for (int i = 0; i<payloads.size(); i++) {
                if (payloads.get(i) instanceof Integer) {
                    Log.d(TAG, "onBindViewHolder: payload");
                    holder.stateIv.setImageResource(item.getPlaybackRes());
                } else {
                    Log.d(TAG, "onBindViewHolder: payload not understood");
                    fullRefresh = true;
                }
            }
        }

        if (fullRefresh) {
            holder.item = item;
            holder.titleTv.setText(item.getTitle());
            holder.artistTv.setText(item.getSubtitle());
            holder.durationTv.setText(Utils.convertMs(item.getDuration()));
            holder.stateIv.setImageResource(item.getPlaybackRes());
            Log.d(TAG, "onBindViewHolder: " + item.getPlaybackRes());
            Picasso.get().load(item.getAlbumArtUri()).placeholder(R.drawable.ic_default_art).into(holder.artIv);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MediaItemData clickedItem);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private MediaItemData item = null;
        private ImageView artIv;
        private ImageView stateIv;
        private TextView titleTv;
        private TextView artistTv;
        private TextView durationTv;

        public ItemViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            artIv = itemView.findViewById(R.id.item_song_art);
            stateIv = itemView.findViewById(R.id.item_song_state);
            titleTv = itemView.findViewById(R.id.item_song_title);
            artistTv = itemView.findViewById(R.id.item_song_artist);
            durationTv = itemView.findViewById(R.id.item_song_duration);
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
