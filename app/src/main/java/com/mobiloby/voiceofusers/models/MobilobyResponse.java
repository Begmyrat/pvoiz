package com.mobiloby.voiceofusers.models;

import com.google.gson.annotations.SerializedName;
import com.mobiloby.voiceofusers.models.VoiceObject;

public class MobilobyResponse {
    @SerializedName("success")
    Integer success;
    @SerializedName("pro")
    VoiceObject[] pro;

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
