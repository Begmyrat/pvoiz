package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;

public class FriendResponse {
    @SerializedName("success")
    Integer success;
    @SerializedName("pro")
    FriendObject[] pro;

    public FriendResponse(){}

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public FriendObject[] getPro() {
        return pro;
    }

    public void setPro(FriendObject[] pro) {
        this.pro = pro;
    }
}
