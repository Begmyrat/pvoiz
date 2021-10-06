package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;
import com.mobiloby.voiceofusers.models.VoiceObject;

public class MobilobyResponse {
    @SerializedName("success")
    Integer success;
    @SerializedName("pro")
    VoiceObject[] pro;
    @SerializedName("is_friend")
    Integer is_friend;
    @SerializedName("user_name")
    String username;
    @SerializedName("user_image_url")
    String user_image_url;

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getIs_friend() {
        return is_friend;
    }

    public void setIs_friend(Integer is_friend) {
        this.is_friend = is_friend;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_image_url() {
        return user_image_url;
    }

    public void setUser_image_url(String user_image_url) {
        this.user_image_url = user_image_url;
    }

    public MobilobyResponse(){}

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public VoiceObject[] getPro() {
        return pro;
    }

    public void setPro(VoiceObject[] pro) {
        this.pro = pro;
    }
}
