package ru.myitschool.normalplayer.ui;

import android.Manifest;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.databinding.ActivityMainBinding;
import ru.myitschool.normalplayer.ui.viewmodel.MainActivityViewModel;
import ru.myitschool.normalplayer.ui.viewmodel.NowPlayingViewModel;
import ru.myitschool.normalplayer.utils.Event;
import ru.myitschool.normalplayer.utils.MediaIDHelper;
import ru.myitschool.normalplayer.utils.ProviderUtils;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainActivityViewModel mainActivityViewModel;

    private NowPlayingViewModel nowPlayingViewModel;

    private ActivityMainBinding activityMainBinding;

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(activityMainBinding.getRoot());

        BottomSheetBehavior sheetBehavior = BottomSheetBehavior.from(activityMainBinding.bottomSheetInclude.bottomSheet);
        sheetBehavior.setHideable(false);
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


        mainActivityViewModel = new ViewModelProvider(this, ProviderUtils.provideMainActivityViewModel(this)).get(MainActivityViewModel.class);

        nowPlayingViewModel = new ViewModelProvider(this, ProviderUtils.provideNowPlayingViewModel(this)).get(NowPlayingViewModel.class);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mainActivityViewModel.getNavigateToFragment().observe(this, fragmentNavigationRequestEvent -> {
            MainActivityViewModel.FragmentNavigationRequest request = fragmentNavigationRequestEvent.getContentIfNotHandled();
            Log.d(TAG, "onCreate: " + request.getFragment().getClass().getName());
            if (request != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, request.getFragment(), request.getTag());
                if (request.isBackStack()) {
                    transaction.addToBackStack(null);
                }
                transaction.commit();
            }
        });

        mainActivityViewModel.getRootMediaId().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String mediaId) {
                Log.d(TAG, "onChanged: observing root id");
                if (mediaId != null) {
                    navigateToMediaItem(mediaId);
                }
            }
        });

        mainActivityViewModel.getNavigateToMediaItem().observe(MainActivity.this, new Observer<Event<String>>() {
            @Override
            public void onChanged(Event<String> event) {
                Log.d(TAG, "onChanged: navigate");
                String content = event.getContentIfNotHandled();
                Log.d(TAG, "onChanged: navigate: " + content);
                navigateToMediaItem(content);
            }
        });

        nowPlayingViewModel.getMediaMetadata().observe(this, new Observer<NowPlayingMetadata>() {
            @Override
            public void onChanged(NowPlayingMetadata nowPlayingMetadata) {
                updateUI(nowPlayingMetadata);
            }
        });

        nowPlayingViewModel.getMediaButtonRes().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                activityMainBinding.bottomSheetInclude.buttonPlay.setImageResource(integer);
                activityMainBinding.bottomSheetInclude.buttonPlayPeek.setImageResource(integer);
            }
        });

        nowPlayingViewModel.getMediaPosition().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                Log.d(TAG, "onChanged: time" + aLong);
                long time = aLong;
                activityMainBinding.bottomSheetInclude.textCurrentTime.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, time));
                activityMainBinding.bottomSheetInclude.playerSeekBar.setProgress((int) time);
                activityMainBinding.bottomSheetInclude.progressBarPeek.setProgress((int) time);
            }
        });

        activityMainBinding.bottomSheetInclude.buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowPlayingViewModel.getMediaMetadata().getValue() != null) {
                    mainActivityViewModel.playMediaId(nowPlayingViewModel.getMediaMetadata().getValue().getMediaId());
                }
            }
        });

        activityMainBinding.bottomSheetInclude.buttonPlayPeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowPlayingViewModel.getMediaMetadata().getValue() != null) {
                    mainActivityViewModel.playMediaId(nowPlayingViewModel.getMediaMetadata().getValue().getMediaId());
                }
            }
        });

        activityMainBinding.bottomSheetInclude.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.skipToNext();
            }
        });

        activityMainBinding.bottomSheetInclude.buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nowPlayingViewModel.skipToPrevious();
            }
        });

        activityMainBinding.bottomSheetInclude.playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    nowPlayingViewModel.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        activityMainBinding.bottomSheetInclude.buttonCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        activityMainBinding.bottomSheetInclude.textTotalTime.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, 0L));

        activityMainBinding.bottomSheetInclude.textCurrentTime.setText(NowPlayingMetadata.timestampToMSS(MainActivity.this, 0L));

        activityMainBinding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_music:
                        navigateToMediaItem(MediaIDHelper.MEDIA_ID_MUSICS_ALL);
                        break;
                    case R.id.nav_albums:
                        navigateToMediaItem(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM);
                        break;
                    case R.id.nav_artists:
                        navigateToMediaItem(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST);
                        break;
                }
                return true;
            }
        });

        activityMainBinding.bottomNavigation.setSelectedItemId(R.id.nav_music);
    }

    private void updateUI(NowPlayingMetadata nowPlayingMetadata) {
        if (nowPlayingMetadata.getAlbumArtUri() == null) {
            activityMainBinding.bottomSheetInclude.imageAlbumArt.setImageResource(R.drawable.ic_default_art);
        } else {
            Picasso.get().load(nowPlayingMetadata.getAlbumArtUri()).placeholder(R.drawable.ic_default_art).into(activityMainBinding.bottomSheetInclude.imageAlbumArt);
            Picasso.get().load(nowPlayingMetadata.getAlbumArtUri()).placeholder(R.drawable.ic_default_art).into(activityMainBinding.bottomSheetInclude.imageAlbumArtPeek);
        }
        activityMainBinding.bottomSheetInclude.textTitle.setText(nowPlayingMetadata.getTitle());
        activityMainBinding.bottomSheetInclude.textTitlePeek.setText(nowPlayingMetadata.getTitle());
        activityMainBinding.bottomSheetInclude.textSubtitle.setText(nowPlayingMetadata.getSubtitle());
        activityMainBinding.bottomSheetInclude.textSubtitlePeek.setText(nowPlayingMetadata.getSubtitle());
        activityMainBinding.bottomSheetInclude.textTotalTime.setText(nowPlayingMetadata.getDuration());
        activityMainBinding.bottomSheetInclude.playerSeekBar.setMax((int) nowPlayingMetadata.getDurationMs());
        activityMainBinding.bottomSheetInclude.playerSeekBar.setProgress(0);
        activityMainBinding.bottomSheetInclude.progressBarPeek.setMax((int) nowPlayingMetadata.getDurationMs());
        activityMainBinding.bottomSheetInclude.progressBarPeek.setProgress(0);
    }

    private void navigateToMediaItem(String mediaId) {
        Fragment fragment = getBrowseFragment(mediaId);
        if (fragment == null) {
            fragment = SongFragment.newInstance(mediaId);
        }
        mainActivityViewModel.showFragment(fragment, !isRootId(mediaId), mediaId);
    }

    private boolean isRootId(String mediaId) {
        return mediaId.equals(mainActivityViewModel.getRootMediaId().getValue());
    }

    private Fragment getBrowseFragment(String mediaId) {
        return getSupportFragmentManager().findFragmentByTag(mediaId);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void onDenied() {
        Toast.makeText(this, "Needs external storage", Toast.LENGTH_SHORT).show();
        finish();
    }
}