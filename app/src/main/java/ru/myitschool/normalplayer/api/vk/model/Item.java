package ru.myitschool.normalplayer.api.vk.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Item {

    @SerializedName("access_key")
    private String accessKey;
    @SerializedName("ads")
    private Ads ads;
    @SerializedName("album")
    private Album album;
    @SerializedName("artist")
    private String artist;
    @SerializedName("date")
    private Integer date;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("id")
    private Integer id;
    @SerializedName("is_explicit")
    private Boolean isExplicit;
    @SerializedName("is_hq")
    private Boolean isHq;
    @SerializedName("is_licensed")
    private Boolean isLicensed;
    @SerializedName("main_artists")
    private List<MainArtist> mainArtists = new ArrayList<MainArtist>();
    @SerializedName("owner_id")
    private Integer ownerId;
    @SerializedName("short_videos_allowed")
    private Boolean shortVideosAllowed;
    @SerializedName("stories_allowed")
    private Boolean storiesAllowed;
    @SerializedName("stories_cover_allowed")
    private Boolean storiesCoverAllowed;
    @SerializedName("title")
    private String title;
    @SerializedName("track_code")
    private String trackCode;
    @SerializedName("url")
    private String url;
    @SerializedName("subtitle")
    private String subtitle;
    @SerializedName("genre_id")
    private Integer genreId;

    /**
     * No args constructor for use in serialization
     *
     */
    public Item() {
    }

    /**
     *
     * @param date
     * @param trackCode
     * @param genreId
     * @param isExplicit
     * @param isHq
     * @param artist
     * @param album
     * @param mainArtists
     * @param ownerId
     * @param title
     * @param storiesCoverAllowed
     * @param url
     * @param duration
     * @param ads
     * @param accessKey
     * @param subtitle
     * @param isLicensed
     * @param id
     * @param shortVideosAllowed
     * @param storiesAllowed
     */
    public Item(String accessKey, Ads ads, Album album, String artist, Integer date, Integer duration, Integer id, Boolean isExplicit, Boolean isHq, Boolean isLicensed, List<MainArtist> mainArtists, Integer ownerId, Boolean shortVideosAllowed, Boolean storiesAllowed, Boolean storiesCoverAllowed, String title, String trackCode, String url, String subtitle, Integer genreId) {
        super();
        this.accessKey = accessKey;
        this.ads = ads;
        this.album = album;
        this.artist = artist;
        this.date = date;
        this.duration = duration;
        this.id = id;
        this.isExplicit = isExplicit;
        this.isHq = isHq;
        this.isLicensed = isLicensed;
        this.mainArtists = mainArtists;
        this.ownerId = ownerId;
        this.shortVideosAllowed = shortVideosAllowed;
        this.storiesAllowed = storiesAllowed;
        this.storiesCoverAllowed = storiesCoverAllowed;
        this.title = title;
        this.trackCode = trackCode;
        this.url = url;
        this.subtitle = subtitle;
        this.genreId = genreId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Ads getAds() {
        return ads;
    }

    public void setAds(Ads ads) {
        this.ads = ads;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsExplicit() {
        return isExplicit;
    }

    public void setIsExplicit(Boolean isExplicit) {
        this.isExplicit = isExplicit;
    }

    public Boolean getIsHq() {
        return isHq;
    }

    public void setIsHq(Boolean isHq) {
        this.isHq = isHq;
    }

    public Boolean getIsLicensed() {
        return isLicensed;
    }

    public void setIsLicensed(Boolean isLicensed) {
        this.isLicensed = isLicensed;
    }

    public List<MainArtist> getMainArtists() {
        return mainArtists;
    }

    public void setMainArtists(List<MainArtist> mainArtists) {
        this.mainArtists = mainArtists;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean getShortVideosAllowed() {
        return shortVideosAllowed;
    }

    public void setShortVideosAllowed(Boolean shortVideosAllowed) {
        this.shortVideosAllowed = shortVideosAllowed;
    }

    public Boolean getStoriesAllowed() {
        return storiesAllowed;
    }

    public void setStoriesAllowed(Boolean storiesAllowed) {
        this.storiesAllowed = storiesAllowed;
    }

    public Boolean getStoriesCoverAllowed() {
        return storiesCoverAllowed;
    }

    public void setStoriesCoverAllowed(Boolean storiesCoverAllowed) {
        this.storiesCoverAllowed = storiesCoverAllowed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrackCode() {
        return trackCode;
    }

    public void setTrackCode(String trackCode) {
        this.trackCode = trackCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Integer getGenreId() {
        return genreId;
    }

    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }

    @Override
    public String toString() {
        return "Item{" +
                "accessKey='" + accessKey + '\'' +
                ", ads=" + ads +
                ", album=" + album +
                ", artist='" + artist + '\'' +
                ", date=" + date +
                ", duration=" + duration +
                ", id=" + id +
                ", isExplicit=" + isExplicit +
                ", isHq=" + isHq +
                ", isLicensed=" + isLicensed +
                ", mainArtists=" + mainArtists +
                ", ownerId=" + ownerId +
                ", shortVideosAllowed=" + shortVideosAllowed +
                ", storiesAllowed=" + storiesAllowed +
                ", storiesCoverAllowed=" + storiesCoverAllowed +
                ", title='" + title + '\'' +
                ", trackCode='" + trackCode + '\'' +
                ", url='" + url + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", genreId=" + genreId +
                '}';
    }
}
