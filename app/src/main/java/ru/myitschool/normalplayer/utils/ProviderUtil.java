package ru.myitschool.normalplayer.utils;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;

import ru.myitschool.normalplayer.common.MusicServiceConnection;
import ru.myitschool.normalplayer.common.playback.MusicService;
import ru.myitschool.normalplayer.ui.fragment.login.data.LoginDataSource;
import ru.myitschool.normalplayer.ui.fragment.login.data.LoginRepository;
import ru.myitschool.normalplayer.ui.fragment.login.LoginViewModel;
import ru.myitschool.normalplayer.ui.viewmodel.MainActivityViewModel;
import ru.myitschool.normalplayer.ui.viewmodel.MediaItemFragmentViewModel;
import ru.myitschool.normalplayer.ui.viewmodel.NowPlayingViewModel;

public class ProviderUtil {

    public static MusicServiceConnection provideMusicServiceConnection(Context context) {
        return MusicServiceConnection.getInstance(context, new ComponentName(context, MusicService.class));
    }

    public static MainActivityViewModel.Factory provideMainActivityViewModel(Context context) {
        Context appContext = context.getApplicationContext();
        MusicServiceConnection connection = provideMusicServiceConnection(appContext);
        return new MainActivityViewModel.Factory(connection);
    }

    public static MediaItemFragmentViewModel.Factory provideSongFragmentViewModel(Context context, String mediaId) {
        Context appContext = context.getApplicationContext();
        MusicServiceConnection connection = provideMusicServiceConnection(appContext);
        return new MediaItemFragmentViewModel.Factory(mediaId, connection);
    }

    public static NowPlayingViewModel.Factory provideNowPlayingViewModel(Context context) {
        Context appContext = context.getApplicationContext();
        MusicServiceConnection connection = provideMusicServiceConnection(appContext);
        return new NowPlayingViewModel.Factory((Application) appContext, connection);
    }

    public static LoginViewModel.Factory provideLoginViewModel(Context context) {
        Context appContext = context.getApplicationContext();
        LoginRepository loginRepository = LoginRepository.getInstance(appContext, new LoginDataSource());
        return new LoginViewModel.Factory(loginRepository);
    }

}
