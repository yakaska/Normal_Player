package ru.myitschool.normalplayer.api.vk;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VkService {

    private static VkService instance = null;
    public static final String BASE_URL = "http://192.168.1.82:5000/";

    private final VkApi vkApi;

    public static VkService getInstance() {
        if (instance == null) {
            instance = new VkService();
        }
        return instance;
    }

    private VkService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.vkApi = retrofit.create(VkApi.class);
    }

    public VkApi getVkApi() {
        return this.vkApi;
    }
}
