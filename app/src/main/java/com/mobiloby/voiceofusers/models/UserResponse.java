package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("success")
    Integer success;
    @SerializedName("pro")
    UserObject[] pro;

    public UserResponse(){}

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public UserObject[] getPro() {
        return pro;
    }

    public void setPro(UserObject[] pro) {
        this.pro = pro;
    }
}
