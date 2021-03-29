package ru.myitschool.normalplayer.api.vk.model;

import com.google.gson.annotations.SerializedName;

public class Thumb {

    @SerializedName("height")
    private Integer height;
    @SerializedName("photo_1200")
    private String photo1200;
    @SerializedName("photo_135")
    private String photo135;
    @SerializedName("photo_270")
    private String photo270;
    @SerializedName("photo_300")
    private String photo300;
    @SerializedName("photo_34")
    private String photo34;
    @SerializedName("photo_600")
    private String photo600;
    @SerializedName("photo_68")
    private String photo68;
    @SerializedName("width")
    private Integer width;

    /**
     * No args constructor for use in serialization
     *
     */
    public Thumb() {
    }

    /**
     *
     * @param photo1200
     * @param photo68
     * @param photo270
     * @param photo34
     * @param width
     * @param photo135
     * @param photo300
     * @param height
     * @param photo600
     */
    public Thumb(Integer height, String photo1200, String photo135, String photo270, String photo300, String photo34, String photo600, String photo68, Integer width) {
        super();
        this.height = height;
        this.photo1200 = photo1200;
        this.photo135 = photo135;
        this.photo270 = photo270;
        this.photo300 = photo300;
        this.photo34 = photo34;
        this.photo600 = photo600;
        this.photo68 = photo68;
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getPhoto1200() {
        return photo1200;
    }

    public void setPhoto1200(String photo1200) {
        this.photo1200 = photo1200;
    }

    public String getPhoto135() {
        return photo135;
    }

    public void setPhoto135(String photo135) {
        this.photo135 = photo135;
    }

    public String getPhoto270() {
        return photo270;
    }

    public void setPhoto270(String photo270) {
        this.photo270 = photo270;
    }

    public String getPhoto300() {
        return photo300;
    }

    public void setPhoto300(String photo300) {
        this.photo300 = photo300;
    }

    public String getPhoto34() {
        return photo34;
    }

    public void setPhoto34(String photo34) {
        this.photo34 = photo34;
    }

    public String getPhoto600() {
        return photo600;
    }

    public void setPhoto600(String photo600) {
        this.photo600 = photo600;
    }

    public String getPhoto68() {
        return photo68;
    }

    public void setPhoto68(String photo68) {
        this.photo68 = photo68;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

}
