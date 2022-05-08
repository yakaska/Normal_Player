package ru.myitschool.normalplayer.api.vk.model.response;

import com.google.gson.annotations.SerializedName;

public class VkTokenResponse{

	@SerializedName("access_token")
	private String accessToken;

	@SerializedName("user_id")
	private int userId;

	@SerializedName("expires_in")
	private int expiresIn;

	public String getAccessToken(){
		return accessToken;
	}

	public int getUserId(){
		return userId;
	}

	public int getExpiresIn(){
		return expiresIn;
	}
}