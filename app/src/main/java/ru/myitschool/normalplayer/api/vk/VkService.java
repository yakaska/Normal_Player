package ru.myitschool.normalplayer.api.vk;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VkService {

    private static final String BASE_URL = "http://192.168.1.82:5000/";

    private static VkService instance;

    private final Retrofit retrofit;

    private VkService(Context context) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static VkService getInstance(Context context) {
        if (instance == null) {
            instance = new VkService(context);
        }
        return instance;
    }

    public VkApi getVkApi() {
        return retrofit.create(VkApi.class);
    }
}
