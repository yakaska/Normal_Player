package ru.myitschool.normalplayer.api.vk.token;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.myitschool.normalplayer.api.vk.VkService;
import ru.myitschool.normalplayer.utils.UserAgentInterceptor;

public class VkTokenService {

    public static final String BASE_URL = "http://oauth.vk.com/";
    private static VkTokenService instance = null;
    private final VkTokenApi vkTokenApi;

    private VkTokenService() {
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(VkService.USER_AGENT);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(userAgentInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.vkTokenApi = retrofit.create(VkTokenApi.class);
    }

    public static VkTokenService getInstance() {
        if (instance == null) {
            instance = new VkTokenService();
        }
        return instance;
    }

    public VkTokenApi getVkTokenApi() {
        return this.vkTokenApi;
    }

}
