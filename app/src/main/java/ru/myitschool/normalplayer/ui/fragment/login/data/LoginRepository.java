package ru.myitschool.normalplayer.ui.fragment.login.data;

import android.content.Context;

import ru.myitschool.normalplayer.ui.fragment.login.data.model.VkSessionManager;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    private VkSessionManager vkSessionManager;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private String token = null;

    // private constructor : singleton access
    private LoginRepository(Context context, LoginDataSource dataSource) {
        this.dataSource = dataSource;
        this.vkSessionManager = new VkSessionManager(context);
        this.token = this.vkSessionManager.getToken();
    }

    public static LoginRepository getInstance(Context context, LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(context, dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public void logout() {
        token = null;
        dataSource.logout();
    }

    private void setLoggedInUser(String token) {
        this.token = token;
        vkSessionManager.saveToken(token);
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result<String> login(String username, String password) {
        // handle login
        Result<String> result = dataSource.login(username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<String>) result).getData());
        }
        return result;
    }
}