package ru.myitschool.normalplayer.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import ru.myitschool.normalplayer.databinding.FragmentMediaitemBinding;
import ru.myitschool.normalplayer.ui.adapter.MediaItemAdapter;
import ru.myitschool.normalplayer.ui.model.MediaItemData;
import ru.myitschool.normalplayer.ui.viewmodel.MainActivityViewModel;
import ru.myitschool.normalplayer.ui.viewmodel.SongFragmentViewModel;
import ru.myitschool.normalplayer.utils.ProviderUtil;


public class MediaItemFragment extends Fragment implements MediaItemAdapter.OnItemClickListener {

    private static final String TAG = MediaItemFragment.class.getSimpleName();

    private static final String MEDIA_ID_ARG = "media_id_arg";

    private String mediaId;

    private MainActivityViewModel mainActivityViewModel;

    private SongFragmentViewModel songFragmentViewModel;

    private FragmentMediaitemBinding binding;

    private final MediaItemAdapter adapter = new MediaItemAdapter(this);

    public static MediaItemFragment newInstance(String mediaId) {
        Bundle args = new Bundle();
        args.putString(MEDIA_ID_ARG, mediaId);
        MediaItemFragment mediaItemFragment = new MediaItemFragment();
        mediaItemFragment.setArguments(args);
        return mediaItemFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMediaitemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mediaId = getArguments().getString(MEDIA_ID_ARG);
        if (mediaId == null) {
            return;
        }
        mainActivityViewModel = new ViewModelProvider(getActivity(), ProviderUtil.provideMainActivityViewModel(requireActivity())).get(MainActivityViewModel.class);
        songFragmentViewModel = new ViewModelProvider(this, ProviderUtil.provideSongFragmentViewModel(requireActivity(), mediaId)).get(SongFragmentViewModel.class);
        songFragmentViewModel.mediaItems.observe(getViewLifecycleOwner(), mediaItems -> {
            if (mediaItems != null && !mediaItems.isEmpty()) {
                binding.fragmentSongLoadingSpinner.setVisibility(View.GONE);
            } else {
                binding.fragmentSongLoadingSpinner.setVisibility(View.VISIBLE);
            }
            adapter.submitList(mediaItems);
        });
        binding.fragmentSongRecycler.setNestedScrollingEnabled(true);
        binding.fragmentSongRecycler.setItemAnimator(new DefaultItemAnimator());
        binding.fragmentSongRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fragmentSongRecycler.setAdapter(adapter);
    }

    @Override
    public void onItemClick(MediaItemData clickedItem) {
        mainActivityViewModel.mediaItemClicked(clickedItem);
    }

}