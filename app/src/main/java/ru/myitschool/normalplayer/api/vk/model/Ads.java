package ru.myitschool.normalplayer.api.vk.model;

import com.google.gson.annotations.SerializedName;

public class Ads {

    @SerializedName("account_age_type")
    private String accountAgeType;
    @SerializedName("content_id")
    private String contentId;
    @SerializedName("duration")
    private String duration;
    @SerializedName("puid1")
    private String puid1;
    @SerializedName("puid22")
    private String puid22;

    /**
     * No args constructor for use in serialization
     */
    public Ads() {
    }

    /**
     * @param duration
     * @param puid1
     * @param puid22
     * @param contentId
     * @param accountAgeType
     */
    public Ads(String accountAgeType, String contentId, String duration, String puid1, String puid22) {
        super();
        this.accountAgeType = accountAgeType;
        this.contentId = contentId;
        this.duration = duration;
        this.puid1 = puid1;
        this.puid22 = puid22;
    }

    public String getAccountAgeType() {
        return accountAgeType;
    }

    public void setAccountAgeType(String accountAgeType) {
        this.accountAgeType = accountAgeType;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPuid1() {
        return puid1;
    }

    public void setPuid1(String puid1) {
        this.puid1 = puid1;
    }

    public String getPuid22() {
        return puid22;
    }

    public void setPuid22(String puid22) {
        this.puid22 = puid22;
    }

}
