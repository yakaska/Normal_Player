package ru.myitschool.normalplayer.api.vk;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VkApi {
    @GET("/audio.get")
    Call<ArrayList<VkTrack>> getAllAudio(
            @Query("owner_id") int ownerId,
            @Query("album_id") int albumId,
            @Query("count") int count,
            @Query("offset") int offset,
            @Query("access_token") String accessToken,
            @Query("v") float v
    );



}
