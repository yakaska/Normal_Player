package ru.myitschool.normalplayer.api.vk.token;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VkTokenService {

    private static VkTokenService instance = null;
    public static final String BASE_URL = "https://mp3-api.herokuapp.com/";

    private final VkTokenApi vkTokenApi;

    public static VkTokenService getInstance() {
        if (instance == null) {
            instance = new VkTokenService();
        }
        return instance;
    }

    private VkTokenService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.vkTokenApi = retrofit.create(VkTokenApi.class);
    }

    public VkTokenApi getVkTokenApi() {
        return this.vkTokenApi;
    }

}
