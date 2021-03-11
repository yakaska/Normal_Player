package ru.myitschool.normalplayer.ui.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.ui.model.MediaItemData;
import ru.myitschool.normalplayer.utils.PlayerUtil;

public class MediaItemAdapter extends ListAdapter<MediaItemData, MediaItemAdapter.LineViewHolder> {

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
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LineViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_line, parent, false), itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        onBindViewHolder(holder, position, Collections.emptyList());
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder lineViewHolder, int position, @NonNull List<Object> payloads) {
        MediaItemData item = getItem(position);
        boolean fullRefresh = payloads.isEmpty();
        if (!payloads.isEmpty()) {
            for (int i = 0; i < payloads.size(); i++) {
                if (payloads.get(i) instanceof Integer) {
                    if (item.getPlaybackRes() == R.drawable.ic_pause_24 || item.getPlaybackRes() == R.drawable.ic_play_24) {
                        lineViewHolder.rootView.setBackgroundResource(R.color.colorBackgroundSelected);
                    } else {
                        lineViewHolder.rootView.setBackgroundResource(R.color.colorBackground);
                    }
                } else {
                    fullRefresh = true;
                }
            }
        }

        if (fullRefresh) {
            lineViewHolder.item = item;
            lineViewHolder.titleTv.setText(item.getTitle());
            lineViewHolder.artistTv.setText(item.getSubtitle());
            if (item.isBrowsable()) {
                lineViewHolder.durationTv.setVisibility(View.INVISIBLE);
            } else {
                lineViewHolder.durationTv.setText(PlayerUtil.convertMs(item.getDuration()));
            }
            if (item.getPlaybackRes() == R.drawable.ic_pause_24 || item.getPlaybackRes() == R.drawable.ic_play_24) {
                lineViewHolder.rootView.setBackgroundResource(R.color.colorBackgroundSelected);
            } else {
                lineViewHolder.rootView.setBackgroundResource(R.color.colorBackground);
            }
            Picasso.get().load(item.getAlbumArtUri()).placeholder(R.drawable.ic_default_art).into(lineViewHolder.artIv);
            lineViewHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(lineViewHolder.rootView.getContext(), lineViewHolder.moreBtn);
                    popupMenu.inflate(R.menu.media_item);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.nav_goto_album:
                                    Toast.makeText(lineViewHolder.moreBtn.getContext(), "Go to album", Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.nav_goto_artist:
                                    Toast.makeText(lineViewHolder.moreBtn.getContext(), "Go to artist", Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.nav_share:
                                    Toast.makeText(lineViewHolder.moreBtn.getContext(), "Share", Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.nav_edit_tags:
                                    Toast.makeText(lineViewHolder.moreBtn.getContext(), "Edit tags", Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.nav_details:
                                    Toast.makeText(lineViewHolder.moreBtn.getContext(), "Details", Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.nav_delete:
                                    Toast.makeText(lineViewHolder.moreBtn.getContext(), "Delete", Toast.LENGTH_SHORT).show();
                                    return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MediaItemData clickedItem);
    }

    public static class LineViewHolder extends RecyclerView.ViewHolder {
        private MediaItemData item = null;
        private final ConstraintLayout rootView;
        private final ImageView artIv;
        private final TextView titleTv;
        private final TextView artistTv;
        private final TextView durationTv;
        private final ImageButton moreBtn;

        public LineViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            rootView = itemView.findViewById(R.id.item_line_root);
            artIv = itemView.findViewById(R.id.item_line_art);
            titleTv = itemView.findViewById(R.id.item_line_title);
            artistTv = itemView.findViewById(R.id.item_line_subtitle);
            durationTv = itemView.findViewById(R.id.item_line_duration);
            moreBtn = itemView.findViewById(R.id.item_line_more);
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
