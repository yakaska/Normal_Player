package ru.myitschool.normalplayer.api.vk.token;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.myitschool.normalplayer.api.vk.model.response.VkTokenResponse;

public interface VkTokenApi {

    @GET("token")
    Call<VkTokenResponse> getToken(
            @Query("username") String username,
            @Query("password") String password,
            @Query("grant_type") String grantType,
            @Query("client_id") String clientId,
            @Query("client_secret") String clientSecret,
            @Query("validate_token") String validateToken
    );

}
