package ru.myitschool.normalplayer.api.vk;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VkApi {

    String BASE_URL = "http://192.168.1.82:5000/";

    @GET("/audio")
    Call<List<VkTrack>> getAllAudio(@Query("login") String login, @Query("token") String token);

    @GET("/token")
    Call<VkToken> getToken(@Query("login") String login, @Query("password") String password);

}
