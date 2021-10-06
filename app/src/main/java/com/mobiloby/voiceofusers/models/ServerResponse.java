package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;
import com.mobiloby.voiceofusers.models.VoiceObject;

public class ServerResponse {
    // variable name should be same as in the json response from php
    @SerializedName("success")
    boolean success;
    @SerializedName("message")
    String message;
    @SerializedName("user_id")
    String user_id;

    VoiceObject[] pro;

    public VoiceObject[] getPro() {
        return pro;
    }

    public void setPro(VoiceObject[] pro) {
        this.pro = pro;
    }

    public String getMessage() {
        return message;
    }
    public boolean getSuccess() {
        return success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
