package ru.myitschool.normalplayer.api.vk.model.response;

import com.google.gson.annotations.SerializedName;

public class VkTokenResponse {

    @SerializedName("token")
    private String token;
    @SerializedName("user_agent")
    private String userAgent;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

}