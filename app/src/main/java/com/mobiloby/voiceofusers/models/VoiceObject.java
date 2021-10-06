package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;

public class VoiceObject {
    @SerializedName("id")
    Integer id;
    @SerializedName("item_lat")
    String item_lat;
    @SerializedName("item_long")
    String item_long;
    @SerializedName("user_id")
    String user_id;
    @SerializedName("item_date")
    String item_date;
    @SerializedName("is_public")
    Integer is_public;
    @SerializedName("item_record_url")
    String item_record_url;
    @SerializedName("item_image_url")
    String item_image_url;
    @SerializedName("view_number")
    Integer item_view;
    @SerializedName("like_number")
    Integer item_like;
    @SerializedName("user_name")
    String user_name;
    @SerializedName("user_image_url")
    String user_image_url;
    @SerializedName("is_liked")
    Integer is_liked;
    @SerializedName("is_friend")
    Integer is_friend;

    public Integer getIs_friend() {
        return is_friend;
    }

    public void setIs_friend(Integer is_friend) {
        this.is_friend = is_friend;
    }

    public Integer getIs_liked() {
        return is_liked;
    }

    public void setIs_liked(Integer is_liked) {
        this.is_liked = is_liked;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_image_url() {
        return user_image_url;
    }

    public void setUser_image_url(String user_image_url) {
        this.user_image_url = user_image_url;
    }

    String full_address;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public VoiceObject() {}

    public String getFull_address() {
        return full_address;
    }

    public void setFull_address(String full_address) {
        this.full_address = full_address;
    }

    public String getItem_image_url() {
        return item_image_url;
    }

    public void setItem_image_url(String item_image_url) {
        this.item_image_url = item_image_url;
    }

    public VoiceObject(String user_id, Integer is_public, String item_record_url, String item_image_url, String item_lat, String item_long, String item_date) {
        this.item_lat = item_lat;
        this.item_long = item_long;
        this.user_id = user_id;
        this.item_date = item_date;
        this.is_public = is_public;
        this.item_record_url = item_record_url;
        this.item_image_url = item_image_url;
    }

    public VoiceObject(String item_lat, String item_long) {
        this.item_lat = item_lat;
        this.item_long = item_long;
    }

    public String getItem_lat() {
        return item_lat;
    }

    public void setItem_lat(String item_lat) {
        this.item_lat = item_lat;
    }

    public String getItem_long() {
        return item_long;
    }

    public void setItem_long(String item_long) {
        this.item_long = item_long;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getItem_date() {
        return item_date;
    }

    public void setItem_date(String item_date) {
        this.item_date = item_date;
    }

    public Integer getIs_public() {
        return is_public;
    }

    public void setIs_public(Integer is_public) {
        this.is_public = is_public;
    }

    public String getItem_record_url() {
        return item_record_url;
    }

    public void setItem_record_url(String item_record_url) {
        this.item_record_url = item_record_url;
    }

    public Integer getItem_view() {
        return item_view;
    }

    public void setItem_view(Integer item_view) {
        this.item_view = item_view;
    }

    public Integer getItem_like() {
        return item_like;
    }

    public void setItem_like(Integer item_like) {
        this.item_like = item_like;
    }
}
