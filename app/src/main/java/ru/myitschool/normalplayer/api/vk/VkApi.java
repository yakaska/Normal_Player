package ru.myitschool.normalplayer.api.vk;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import ru.myitschool.normalplayer.api.vk.model.response.VkMusicResponse;

public interface VkApi {

    @GET("audio.get")
    Call<VkMusicResponse> getAllAudio(
            @Query("count") Integer count,
            @Query("access_token") String accessToken,
            @Query("v") String version
    );

}
