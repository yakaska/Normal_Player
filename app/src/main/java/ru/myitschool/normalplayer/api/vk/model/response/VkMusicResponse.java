package ru.myitschool.normalplayer.api.vk.model.response;

import com.google.gson.annotations.SerializedName;

import ru.myitschool.normalplayer.api.vk.model.Response;

public class VkMusicResponse {

    @SerializedName("response")
    private Response response;

    /**
     * No args constructor for use in serialization
     *
     */
    public VkMusicResponse() {
    }

    /**
     *
     * @param response
     */
    public VkMusicResponse(Response response) {
        super();
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

}