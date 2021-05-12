package ru.myitschool.normalplayer.api.vk;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.myitschool.normalplayer.utils.UserAgentInterceptor;

public class VkService {

    private static VkService instance = null;
    public static final String BASE_URL = "https://api.vk.com/method/";
    public static final String USER_AGENT = "VKAndroidApp/5.52-4543 (Android 5.1.1; SDK 22; x86_64; unknown Android SDK built for x86_64; en; 320x240)";

    private final VkApi vkApi;

    public static VkService getInstance() {
        if (instance == null) {
            instance = new VkService();
        }
        return instance;
    }

    private VkService() {
        UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor(USER_AGENT);
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(userAgentInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.vkApi = retrofit.create(VkApi.class);
    }

    public VkApi getVkApi() {
        return this.vkApi;
    }
}
