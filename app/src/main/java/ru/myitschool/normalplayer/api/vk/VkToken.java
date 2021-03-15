package ru.myitschool.normalplayer.api.vk;

import com.google.gson.annotations.SerializedName;

public class VkToken {
    @SerializedName("access_token")
    private String token;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("expires_in")
    private int expiresIn;

    public VkToken(String token, String userId, int expiresIn) {
        this.token = token;
        this.userId = userId;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    @Override
    public String toString() {
        return "VkToken{" +
                "token='" + token + '\'' +
                ", userId='" + userId + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
