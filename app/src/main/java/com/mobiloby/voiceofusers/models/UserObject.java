package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;

public class UserObject {
    @SerializedName("user_id")
    String user_id;
    @SerializedName("user_phone")
    String user_phone;
    @SerializedName("user_name")
    String user_name;
    @SerializedName("user_image_url")
    String user_image_url;
    @SerializedName("user_register_date")
    String user_register_date;

    public UserObject() {}

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
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

    public String getUser_register_date() {
        return user_register_date;
    }

    public void setUser_register_date(String user_register_date) {
        this.user_register_date = user_register_date;
    }
}
