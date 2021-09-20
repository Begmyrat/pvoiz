package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;

public class VoiceObject {
    @SerializedName("id")
    String id;
    @SerializedName("item_lat")
    String item_lat;
    @SerializedName("item_long")
    String item_long;
    @SerializedName("user_id")
    String user_id;
    @SerializedName("item_date")
    String item_date;
    @SerializedName("is_public")
    String is_public;
    @SerializedName("item_record_url")
    String item_record_url;
    @SerializedName("item_image_url")
    String item_image_url;

    String full_address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public VoiceObject(String user_id, String is_public, String item_record_url, String item_image_url, String item_lat, String item_long, String item_date) {
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

    public String getIs_public() {
        return is_public;
    }

    public void setIs_public(String is_public) {
        this.is_public = is_public;
    }

    public String getItem_record_url() {
        return item_record_url;
    }

    public void setItem_record_url(String item_record_url) {
        this.item_record_url = item_record_url;
    }
}
