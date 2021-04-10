package ru.myitschool.normalplayer.api.vk.token;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.myitschool.normalplayer.api.vk.model.response.VkTokenResponse;

public interface VkTokenApi {

    @GET("token.get")
    Call<VkTokenResponse> getToken(
            @Query(value = "login", encoded = true) String login,
            @Query(value = "password", encoded = true) String password
    );

}
