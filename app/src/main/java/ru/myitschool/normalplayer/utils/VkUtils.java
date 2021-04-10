package ru.myitschool.normalplayer.utils;

import android.content.Context;
import android.util.Log;

public class VkUtils {

    private static final String VK_PREFS = "vk_prefs";

    private static final String KEY_TOKEN = "key_token";

    public static String getToken(Context context) {
        return context.getSharedPreferences(VK_PREFS, Context.MODE_PRIVATE).getString(KEY_TOKEN, null);
    }

    public static void saveToken(Context context, String token) {
        Log.d("TOKEN", "saveToken: " + token);
        context.getSharedPreferences(VK_PREFS, Context.MODE_PRIVATE).edit().putString(KEY_TOKEN, token).commit();
    }

}
