package ru.myitschool.normalplayer.api.vk.model;

import com.google.gson.annotations.SerializedName;

public class Album {

    @SerializedName("access_key")
    private String accessKey;
    @SerializedName("id")
    private Integer id;
    @SerializedName("owner_id")
    private Integer ownerId;
    @SerializedName("thumb")
    private Thumb thumb;
    @SerializedName("title")
    private String title;

    /**
     * No args constructor for use in serialization
     *
     */
    public Album() {
    }

    /**
     *
     * @param accessKey
     * @param thumb
     * @param id
     * @param ownerId
     * @param title
     */
    public Album(String accessKey, Integer id, Integer ownerId, Thumb thumb, String title) {
        super();
        this.accessKey = accessKey;
        this.id = id;
        this.ownerId = ownerId;
        this.thumb = thumb;
        this.title = title;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Thumb getThumb() {
        return thumb;
    }

    public void setThumb(Thumb thumb) {
        this.thumb = thumb;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
