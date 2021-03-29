package ru.myitschool.normalplayer.api.vk.model;

import com.google.gson.annotations.SerializedName;

public class MainArtist {

    @SerializedName("domain")
    private String domain;
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;

    /**
     * No args constructor for use in serialization
     *
     */
    public MainArtist() {
    }

    /**
     *
     * @param domain
     * @param name
     * @param id
     */
    public MainArtist(String domain, String id, String name) {
        super();
        this.domain = domain;
        this.id = id;
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}