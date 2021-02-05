package ru.myitschool.normalplayer.ui;

import android.Manifest;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import ru.myitschool.normalplayer.R;
import ru.myitschool.normalplayer.databinding.ActivityMainBinding;
import ru.myitschool.normalplayer.ui.viewmodel.MainActivityViewModel;
import ru.myitschool.normalplayer.utils.Event;
import ru.myitschool.normalplayer.utils.MediaIDHelper;
import ru.myitschool.normalplayer.utils.ProviderUtils;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainActivityViewModel viewModel;

    private ActivityMainBinding binding;

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, ProviderUtils.provideMainActivityViewModel(this)).get(MainActivityViewModel.class);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        viewModel.getNavigateToFragment().observe(this, fragmentNavigationRequestEvent -> {
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

        viewModel.getRootMediaId().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String mediaId) {
                Log.d(TAG, "onChanged: observing root id");
                if (mediaId != null) {
                    navigateToMediaItem(mediaId);
                }
            }
        });

        viewModel.getNavigateToMediaItem().observe(MainActivity.this, new Observer<Event<String>>() {
            @Override
            public void onChanged(Event<String> event) {
                Log.d(TAG, "onChanged: navigate");
                String content = event.getContentIfNotHandled();
                Log.d(TAG, "onChanged: navigate: " + content);
                navigateToMediaItem(content);
            }
        });
        
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_music:
                        navigateToMediaItem(MediaIDHelper.MEDIA_ID_MUSICS_ALL);
                        break;
                    case R.id.nav_albums:
                        navigateToMediaItem(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM);
                        break;
                }
                return true;
            }
        });
        binding.bottomNavigation.setSelectedItemId(R.id.nav_music);
    }

    private void navigateToMediaItem(String mediaId) {
        Fragment fragment = getBrowseFragment(mediaId);
        if (fragment == null) {
            fragment = SongFragment.newInstance(mediaId);
        }
        viewModel.showFragment(fragment, !isRootId(mediaId), mediaId);
    }

    private boolean isRootId(String mediaId) {
        return mediaId.equals(viewModel.getRootMediaId().getValue());
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