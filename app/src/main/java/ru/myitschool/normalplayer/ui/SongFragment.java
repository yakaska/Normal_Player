package ru.myitschool.normalplayer.ui;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import ru.myitschool.normalplayer.databinding.FragmentSongBinding;
import ru.myitschool.normalplayer.ui.viewmodel.MainActivityViewModel;
import ru.myitschool.normalplayer.ui.viewmodel.SongFragmentViewModel;
import ru.myitschool.normalplayer.utils.ProviderUtils;


public class SongFragment extends Fragment implements MediaItemAdapter.OnItemClickListener {

    private static String TAG = SongFragment.class.getSimpleName();

    private static String MEDIA_ID_ARG = "media_id_arg";

    private String mediaId;

    private MainActivityViewModel mainActivityViewModel;

    private SongFragmentViewModel songFragmentViewModel;

    private FragmentSongBinding binding;

    private MediaItemAdapter adapter = new MediaItemAdapter(this::onItemClick);

    public static SongFragment newInstance(String mediaId) {
        Bundle args = new Bundle();
        args.putString(MEDIA_ID_ARG, mediaId);
        SongFragment songFragment = new SongFragment();
        songFragment.setArguments(args);
        return songFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSongBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mediaId = getArguments().getString(MEDIA_ID_ARG);
        Log.d(TAG, "onActivityCreated: " + mediaId);
        if (mediaId == null) {
            return;
        }
        mainActivityViewModel = new ViewModelProvider(this, ProviderUtils.provideMainActivityViewModel(requireActivity())).get(MainActivityViewModel.class);
        songFragmentViewModel = new ViewModelProvider(this, ProviderUtils.provideSongFragmentViewModel(requireActivity(), mediaId)).get(SongFragmentViewModel.class);
        songFragmentViewModel.mediaItems.observe(getViewLifecycleOwner(), new Observer<List<MediaItem>>() {
            @Override
            public void onChanged(List<MediaItem> mediaItems) {
                if (mediaItems != null && !mediaItems.isEmpty()) {
                    binding.loadingSpinner.setVisibility(View.GONE);
                } else {
                    binding.loadingSpinner.setVisibility(View.VISIBLE);
                }
                adapter.submitList(mediaItems);
            }
        });
        binding.fragmentSongRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fragmentSongRecycler.setAdapter(adapter);
    }

    @Override
    public void onItemClick(MediaItem clickedItem) {
        Log.d(TAG, "onItemClick: " + clickedItem.getMediaId());
        mainActivityViewModel.mediaItemClicked(clickedItem);
    }

}