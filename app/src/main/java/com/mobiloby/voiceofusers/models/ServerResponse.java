package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;
import com.mobiloby.voiceofusers.models.VoiceObject;

public class ServerResponse {
    // variable name should be same as in the json response from php
    @SerializedName("success")
    boolean success;
    @SerializedName("message")
    String message;

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
}
