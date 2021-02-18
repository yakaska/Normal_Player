package ru.myitschool.normalplayer.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.utils.Utils;

public class MediaItemAdapter extends ListAdapter<MediaItemData, RecyclerView.ViewHolder> {

    private static final String TAG = MediaItemAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_LINE = 1;
    private static final int VIEW_TYPE_GRID = 2;

    private final OnItemClickListener itemClickListener;

    public MediaItemAdapter(OnItemClickListener itemClickListener) {
        super(MediaItemData.DIFF_CALLBACK);
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isBrowsable() ? VIEW_TYPE_GRID : VIEW_TYPE_LINE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LINE: {
                return new LineViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_line, parent, false), itemClickListener);
            }
            case VIEW_TYPE_GRID: {
                return new GridViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false), itemClickListener);
            }
            default: {
                Log.d(TAG, "onCreateViewHolder: ");
                return null;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        onBindViewHolder(holder, position, Collections.emptyList());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        MediaItemData item = getItem(position);
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_LINE: {
                LineViewHolder lineHolder = (LineViewHolder) viewHolder;
                boolean fullRefresh = payloads.isEmpty();
                if (!payloads.isEmpty()) {
                    for (int i = 0; i < payloads.size(); i++) {
                        if (payloads.get(i) instanceof Integer) {
                            lineHolder.stateIv.setImageResource(item.getPlaybackRes());
                            if (item.getPlaybackRes() == R.drawable.ic_pause_24 || item.getPlaybackRes() == R.drawable.ic_play_24) {
                                lineHolder.rootView.setBackgroundResource(R.color.colorBackgroundSelected);
                            } else {
                                lineHolder.rootView.setBackgroundResource(R.color.colorBackground);
                            }
                        } else {
                            fullRefresh = true;
                        }
                    }
                }

                if (fullRefresh) {
                    lineHolder.item = item;
                    lineHolder.titleTv.setText(item.getTitle());
                    lineHolder.artistTv.setText(item.getSubtitle());
                    if (item.isBrowsable()) {
                        lineHolder.durationTv.setVisibility(View.INVISIBLE);
                    } else {
                        lineHolder.durationTv.setText(Utils.convertMs(item.getDuration()));
                    }
                    if (item.getPlaybackRes() == R.drawable.ic_pause_24 || item.getPlaybackRes() == R.drawable.ic_play_24) {
                        lineHolder.rootView.setBackgroundResource(R.color.colorBackgroundSelected);
                    } else {
                        lineHolder.rootView.setBackgroundResource(R.color.colorBackground);
                    }
                    lineHolder.stateIv.setImageResource(item.getPlaybackRes());
                    Picasso.get().load(item.getAlbumArtUri()).placeholder(R.drawable.ic_default_art).into(lineHolder.artIv);
                }
                break;
            }

            case VIEW_TYPE_GRID: {
                GridViewHolder gridViewHolder = (GridViewHolder) viewHolder;
                gridViewHolder.item = item;
                gridViewHolder.titleTv.setText(item.getTitle());
                gridViewHolder.subtitleTv.setText(item.getSubtitle());
                Picasso.get().load(item.getAlbumArtUri()).placeholder(R.drawable.ic_default_art).into(gridViewHolder.artIv);
            }

        }
    }

    public interface OnItemClickListener {
        void onItemClick(MediaItemData clickedItem);
    }

    public static class LineViewHolder extends RecyclerView.ViewHolder {
        private MediaItemData item = null;
        private final ConstraintLayout rootView;
        private final ImageView artIv;
        private final ImageView stateIv;
        private final TextView titleTv;
        private final TextView artistTv;
        private final TextView durationTv;

        public LineViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            rootView = itemView.findViewById(R.id.item_line_root);
            artIv = itemView.findViewById(R.id.item_line_art);
            stateIv = itemView.findViewById(R.id.item_line_state);
            titleTv = itemView.findViewById(R.id.item_line_title);
            artistTv = itemView.findViewById(R.id.item_line_subtitle);
            durationTv = itemView.findViewById(R.id.item_line_duration);
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

    public static class GridViewHolder extends RecyclerView.ViewHolder {

        private MediaItemData item = null;
        private final MaterialCardView rootView;
        private final ImageView artIv;
        private final TextView titleTv;
        private final TextView subtitleTv;

        public GridViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            rootView = itemView.findViewById(R.id.item_grid_root);
            artIv = itemView.findViewById(R.id.item_grid_art);
            titleTv = itemView.findViewById(R.id.item_grid_title);
            subtitleTv = itemView.findViewById(R.id.item_grid_subtitle);
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
