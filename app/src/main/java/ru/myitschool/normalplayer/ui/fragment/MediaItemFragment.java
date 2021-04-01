package ru.myitschool.normalplayer.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.databinding.FragmentMediaitemBinding;
import ru.myitschool.normalplayer.ui.activity.MainActivity;
import ru.myitschool.normalplayer.ui.adapter.MediaItemAdapter;
import ru.myitschool.normalplayer.ui.model.MediaItemData;
import ru.myitschool.normalplayer.ui.viewmodel.MainActivityViewModel;
import ru.myitschool.normalplayer.ui.viewmodel.MediaItemFragmentViewModel;
import ru.myitschool.normalplayer.utils.ProviderUtil;


public class MediaItemFragment extends Fragment implements MediaItemAdapter.OnItemClickListener {

    private static final String TAG = MediaItemFragment.class.getSimpleName();

    private static final String MEDIA_ID_ARG = "media_id_arg";

    private String mediaId;

    private MainActivityViewModel mainActivityViewModel;

    private MediaItemFragmentViewModel mediaItemFragmentViewModel;

    private FragmentMediaitemBinding binding;

    private final MediaItemAdapter adapter = new MediaItemAdapter(this);

    public static MediaItemFragment newInstance(String mediaId) {
        Log.d(TAG, "newInstance: ");
        Bundle args = new Bundle();
        args.putString(MEDIA_ID_ARG, mediaId);
        MediaItemFragment mediaItemFragment = new MediaItemFragment();
        mediaItemFragment.setArguments(args);
        return mediaItemFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        Log.d(TAG, "onActivityCreated: ");
        if (mediaId == null) {
            return;
        }
        mainActivityViewModel = new ViewModelProvider(getActivity(), ProviderUtil.provideMainActivityViewModel(requireActivity())).get(MainActivityViewModel.class);
        mediaItemFragmentViewModel = new ViewModelProvider(this, ProviderUtil.provideSongFragmentViewModel(requireActivity(), mediaId)).get(MediaItemFragmentViewModel.class);
        mediaItemFragmentViewModel.mediaItems.observe(getViewLifecycleOwner(), mediaItems -> {
            if (mediaItems != null && !mediaItems.isEmpty()) {
                binding.spinner.setVisibility(View.GONE);
            } else {
                binding.spinner.setVisibility(View.VISIBLE);
            }
            adapter.modifyList(mediaItems);
        });
        binding.recycler.setNestedScrollingEnabled(true);
        binding.recycler.setItemAnimator(new DefaultItemAnimator());
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);
        initMenu();
    }

    private void initMenu() {
        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_vk:
                        Intent intent = new Intent(getContext().getApplicationContext(), MainActivity.class);
                        mediaItemFragmentViewModel.send();
                        startActivity(intent);
                        break;
                    case R.id.menu_settings:
                        break;
                    case R.id.menu_sort:
                        break;
                }
                return true;
            }
        });
        MenuItem searchMenuItem = binding.toolbar.getMenu().findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    @Override
    public void onItemClick(MediaItemData clickedItem) {
        mainActivityViewModel.mediaItemClicked(clickedItem);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_vk:
                Log.d(TAG, "onOptionsItemSelected: " + "VK");
                break;
            case R.id.menu_settings:
                Log.d(TAG, "onOptionsItemSelected: " + "SETTINGS");
                break;
            case R.id.menu_sort:
                Log.d(TAG, "onOptionsItemSelected: " + "SORT");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}