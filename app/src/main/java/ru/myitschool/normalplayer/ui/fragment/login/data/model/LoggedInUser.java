package ru.myitschool.normalplayer.ui.fragment.login.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String token;
    private String displayName;

    public LoggedInUser(String token, String displayName) {
        this.token = token;
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
    }

    public String getDisplayName() {
        return displayName;
    }
}