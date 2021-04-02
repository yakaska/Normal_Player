package ru.myitschool.normalplayer.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.slider.Slider;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.databinding.ActivityMainBinding;
import ru.myitschool.normalplayer.ui.fragment.MediaItemFragment;
import ru.myitschool.normalplayer.ui.model.NowPlayingMetadata;
import ru.myitschool.normalplayer.ui.viewmodel.MainActivityViewModel;
import ru.myitschool.normalplayer.ui.viewmodel.NowPlayingViewModel;
import ru.myitschool.normalplayer.utils.Event;
import ru.myitschool.normalplayer.utils.MediaIDUtil;
import ru.myitschool.normalplayer.utils.ProviderUtil;

import static ru.myitschool.normalplayer.common.playback.MusicService.SOURCE_PHONE;
import static ru.myitschool.normalplayer.common.playback.MusicService.SOURCE_VK;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainActivityViewModel mainActivityViewModel;

    private NowPlayingViewModel nowPlayingViewModel;

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        checkPermissions();

    }

    private void checkPermissions() {
        Dexter
                .withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        initUI();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).onSameThread().check();
    }

    private void initUI() {
        initMenu();

        binding.bottomSheetInclude.titlePeek.setSelected(true);

        BottomSheetBehavior<LinearLayout> sheetBehavior = BottomSheetBehavior.from(binding.bottomSheetInclude.bottomSheet);
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                View v = bottomSheet.findViewById(R.id.sheet_peek);
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        v.setVisibility(View.VISIBLE);
                        binding.bottomNavigation.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        v.setVisibility(View.GONE);
                        binding.bottomNavigation.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        
        mainActivityViewModel = new ViewModelProvider(this, ProviderUtil.provideMainActivityViewModel(this)).get(MainActivityViewModel.class);

        nowPlayingViewModel = new ViewModelProvider(this, ProviderUtil.provideNowPlayingViewModel(this)).get(NowPlayingViewModel.class);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mainActivityViewModel.getNavigateToFragment().observe(this, fragmentNavigationRequestEvent -> {
            MainActivityViewModel.FragmentNavigationRequest request = fragmentNavigationRequestEvent.getContentIfNotHandled();
            if (request != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, request.getFragment(), request.getTag());
                if (request.isBackStack()) {
                    transaction.addToBackStack(null);
                }
                transaction.commit();
            }
        });

        mainActivityViewModel.getNavigateToMediaItem().observe(MainActivity.this, new Observer<Event<String>>() {
            @Override
            public void onChanged(Event<String> event) {
                String content = event.getContentIfNotHandled();
                navigateToMediaItem(content);
            }
        });

        nowPlayingViewModel.getMediaMetadata().observe(this, new Observer<NowPlayingMetadata>() {
            @Override
            public void onChanged(NowPlayingMetadata nowPlayingMetadata) {
                updateUI(nowPlayingMetadata);
            }
        });

        nowPlayingViewModel.getPlayButtonRes().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                binding.bottomSheetInclude.playContent.setIconResource(integer);
                binding.bottomSheetInclude.playPeek.setIconResource(integer);
            }
        });

        nowPlayingViewModel.getMediaPosition().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                long time = aLong;
                binding.bottomSheetInclude.currentTimeContent.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, time));
                binding.bottomSheetInclude.seekbarContent.setValue((int) time);
                binding.bottomSheetInclude.progressBarPeek.setProgress((int) (time));
            }
        });

        nowPlayingViewModel.getShuffleButtonRes().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                binding.bottomSheetInclude.shuffleContent.setIconResource(integer);
            }
        });

        nowPlayingViewModel.getRepeatButtonRes().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                binding.bottomSheetInclude.repeatContent.setIconResource(integer);
            }
        });

        binding.bottomSheetInclude.playContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowPlayingViewModel.getMediaMetadata().getValue() != null) {
                    mainActivityViewModel.playMediaId(nowPlayingViewModel.getMediaMetadata().getValue().getMediaId());
                }
            }
        });

        binding.bottomSheetInclude.playPeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowPlayingViewModel.getMediaMetadata().getValue() != null) {
                    mainActivityViewModel.playMediaId(nowPlayingViewModel.getMediaMetadata().getValue().getMediaId());
                }
            }
        });

        binding.bottomSheetInclude.nextContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.skipToNext();

            }
        });

        binding.bottomSheetInclude.previousContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.skipToPrevious();
            }
        });

        binding.bottomSheetInclude.seekbarContent.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    nowPlayingViewModel.seekTo((long) value);
                }
            }
        });

        binding.bottomSheetInclude.collapseContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        binding.bottomSheetInclude.buttonExpandPeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        binding.bottomSheetInclude.shuffleContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.toggleShuffleMode();
            }
        });

        binding.bottomSheetInclude.repeatContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.toggleRepeatMode();
            }
        });

        binding.bottomSheetInclude.totalTimeContent.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, 0L));

        binding.bottomSheetInclude.currentTimeContent.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, 0L));

        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_music:
                        navigateToMediaItem(MediaIDUtil.MEDIA_ID_MUSICS_ALL);
                        break;
                    case R.id.nav_albums:
                        navigateToMediaItem(MediaIDUtil.MEDIA_ID_MUSICS_BY_ALBUM);
                        break;
                    case R.id.nav_artists:
                        navigateToMediaItem(MediaIDUtil.MEDIA_ID_MUSICS_BY_ARTIST);
                        break;
                    case R.id.nav_genres:
                        navigateToMediaItem(MediaIDUtil.MEDIA_ID_MUSICS_BY_GENRE);
                        break;
                }
                return true;
            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.nav_music);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(binding.drawer);
            }
        });
    }

    private void initMenu() {
        binding.drawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_phone:
                        setMediaSource(SOURCE_PHONE);
                        break;
                    case R.id.nav_vk:
                        setMediaSource(SOURCE_VK);
                        break;
                }
                return false;
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
            public boolean onQueryTextChange(String query) {
                mainActivityViewModel.search(query);
                return false;
            }
        });
    }

    private void setMediaSource(String source) {
        mainActivityViewModel.setMediaSource(source);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void updateUI(NowPlayingMetadata nowPlayingMetadata) {
        String id = nowPlayingMetadata.getMediaId();
        Log.d(TAG, "updateUI: ");
        if (nowPlayingMetadata.getAlbumArtUri() == null) {
            binding.bottomSheetInclude.albumArtContent.setImageResource(R.drawable.ic_default_art);
        } else {
            Picasso.get().load(nowPlayingMetadata.getAlbumArtUri()).placeholder(R.drawable.ic_default_art).into(binding.bottomSheetInclude.albumArtContent);
        }
        binding.bottomSheetInclude.titleContent.setText(nowPlayingMetadata.getTitle());
        binding.bottomSheetInclude.titlePeek.setText(String.format(getString(R.string.song_format), nowPlayingMetadata.getTitle(), nowPlayingMetadata.getSubtitle()));
        binding.bottomSheetInclude.subtitleContent.setText(nowPlayingMetadata.getSubtitle());
        binding.bottomSheetInclude.totalTimeContent.setText(nowPlayingMetadata.getDuration());
        binding.bottomSheetInclude.seekbarContent.setValue(0);
        binding.bottomSheetInclude.seekbarContent.setValueFrom(0);
        binding.bottomSheetInclude.seekbarContent.setValueTo((int) nowPlayingMetadata.getDurationMs() + 1000);
        binding.bottomSheetInclude.progressBarPeek.setMax((int) nowPlayingMetadata.getDurationMs());
    }

    private void navigateToMediaItem(String mediaId) {
        Fragment fragment = getBrowseFragment(mediaId);
        if (fragment == null) {
            fragment = MediaItemFragment.newInstance(mediaId);
        }
        mainActivityViewModel.showFragment(fragment, !isRootId(mediaId), mediaId);
    }
    
    private boolean isRootId(String mediaId) {
        return mediaId.equals(mainActivityViewModel.getRootMediaId().getValue());
    }

    private Fragment getBrowseFragment(String mediaId) {
        return getSupportFragmentManager().findFragmentByTag(mediaId);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}