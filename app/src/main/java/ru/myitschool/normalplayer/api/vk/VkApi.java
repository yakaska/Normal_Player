package ru.myitschool.normalplayer.api.vk;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.myitschool.normalplayer.api.vk.model.response.VkMusicResponse;
import ru.myitschool.normalplayer.api.vk.model.response.VkTokenResponse;

public interface VkApi {
    @GET("/audio.get")
    Call<VkMusicResponse> getAllAudio(
            @Query("count") Integer count,
            @Query("access_token") String accessToken
    );

    @GET("/token.get")
    Call<VkTokenResponse> getToken(
            @Query(value = "login", encoded = true) String login,
            @Query(value = "password", encoded = true) String password
    );

}
