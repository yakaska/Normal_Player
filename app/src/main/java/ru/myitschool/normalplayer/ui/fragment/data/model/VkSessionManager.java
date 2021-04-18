package ru.myitschool.normalplayer.ui.fragment.data.model;

import android.content.Context;
import android.content.SharedPreferences;

public class VkSessionManager {

    private static final String VK_PREFS = "vk_prefs";

    private static final String KEY_TOKEN = "key_token";

    private final SharedPreferences preferences;

    public VkSessionManager(Context context) {
        this.preferences = context.getSharedPreferences(VK_PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token){
        preferences.edit().putString(KEY_TOKEN, token).commit();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

}
