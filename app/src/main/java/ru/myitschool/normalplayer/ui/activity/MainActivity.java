package ru.myitschool.normalplayer.ui.activity;

import android.Manifest;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainActivityViewModel mainActivityViewModel;

    private NowPlayingViewModel nowPlayingViewModel;

    private ActivityMainBinding activityMainBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(activityMainBinding.getRoot());

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
        activityMainBinding.bottomSheetInclude.titlePeek.setSelected(true);

        BottomSheetBehavior<LinearLayout> sheetBehavior = BottomSheetBehavior.from(activityMainBinding.bottomSheetInclude.bottomSheet);
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                View v = bottomSheet.findViewById(R.id.sheet_peek);
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        v.setVisibility(View.VISIBLE);
                        activityMainBinding.bottomNavigation.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        v.setVisibility(View.GONE);
                        activityMainBinding.bottomNavigation.setVisibility(View.GONE);
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
                activityMainBinding.bottomSheetInclude.playContent.setIconResource(integer);
                activityMainBinding.bottomSheetInclude.playPeek.setIconResource(integer);
            }
        });

        nowPlayingViewModel.getMediaPosition().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                long time = aLong;
                activityMainBinding.bottomSheetInclude.currentTimeContent.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, time));
                activityMainBinding.bottomSheetInclude.seekbarContent.setValue((int) time);
                activityMainBinding.bottomSheetInclude.progressBarPeek.setProgress((int) (time));
            }
        });

        nowPlayingViewModel.getShuffleButtonRes().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                activityMainBinding.bottomSheetInclude.shuffleContent.setIconResource(integer);
            }
        });

        nowPlayingViewModel.getRepeatButtonRes().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                activityMainBinding.bottomSheetInclude.repeatContent.setIconResource(integer);
            }
        });

        activityMainBinding.bottomSheetInclude.playContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowPlayingViewModel.getMediaMetadata().getValue() != null) {
                    mainActivityViewModel.playMediaId(nowPlayingViewModel.getMediaMetadata().getValue().getMediaId());
                }
            }
        });

        activityMainBinding.bottomSheetInclude.playPeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowPlayingViewModel.getMediaMetadata().getValue() != null) {
                    mainActivityViewModel.playMediaId(nowPlayingViewModel.getMediaMetadata().getValue().getMediaId());
                }
            }
        });

        activityMainBinding.bottomSheetInclude.nextContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.skipToNext();

            }
        });

        activityMainBinding.bottomSheetInclude.previousContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.skipToPrevious();
            }
        });

        activityMainBinding.bottomSheetInclude.seekbarContent.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    nowPlayingViewModel.seekTo((long) value);
                }
            }
        });

        activityMainBinding.bottomSheetInclude.collapseContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        activityMainBinding.bottomSheetInclude.buttonExpandPeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        activityMainBinding.bottomSheetInclude.shuffleContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.toggleShuffleMode();
            }
        });

        activityMainBinding.bottomSheetInclude.repeatContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.toggleRepeatMode();
            }
        });

        activityMainBinding.bottomSheetInclude.totalTimeContent.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, 0L));

        activityMainBinding.bottomSheetInclude.currentTimeContent.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, 0L));

        activityMainBinding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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

        activityMainBinding.bottomNavigation.setSelectedItemId(R.id.nav_music);
    }

    private void updateUI(NowPlayingMetadata nowPlayingMetadata) {
        String id = nowPlayingMetadata.getMediaId();
        Log.d(TAG, "updateUI: ");
        if (nowPlayingMetadata.getAlbumArtUri() == null) {
            activityMainBinding.bottomSheetInclude.albumArtContent.setImageResource(R.drawable.ic_default_art);
        } else {
            Picasso.get().load(nowPlayingMetadata.getAlbumArtUri()).placeholder(R.drawable.ic_default_art).into(activityMainBinding.bottomSheetInclude.albumArtContent);
        }
        activityMainBinding.bottomSheetInclude.titleContent.setText(nowPlayingMetadata.getTitle());
        activityMainBinding.bottomSheetInclude.titlePeek.setText(String.format(getString(R.string.song_format), nowPlayingMetadata.getTitle(), nowPlayingMetadata.getSubtitle()));
        activityMainBinding.bottomSheetInclude.subtitleContent.setText(nowPlayingMetadata.getSubtitle());
        activityMainBinding.bottomSheetInclude.totalTimeContent.setText(nowPlayingMetadata.getDuration());
        activityMainBinding.bottomSheetInclude.seekbarContent.setValue(0);
        activityMainBinding.bottomSheetInclude.seekbarContent.setValueFrom(0);
        activityMainBinding.bottomSheetInclude.seekbarContent.setValueTo((int) nowPlayingMetadata.getDurationMs() + 1000);
        activityMainBinding.bottomSheetInclude.progressBarPeek.setMax((int) nowPlayingMetadata.getDurationMs());
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

}