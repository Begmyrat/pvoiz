package com.mobiloby.voiceofusers.network;

import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.models.ServerResponse;

import java.util.Map;
import java.util.Observable;

import io.reactivex.internal.operators.observable.ObservableAll;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;

public interface RetroInterface {

    String VIDEOURL = "https://mobiloby.com/_filter/";

    @Multipart
    @POST("upload_single_file.php")
    Call<String> uploadImage(
            @Part MultipartBody.Part file,
            @Part("filename") RequestBody name,
            @Part("user_id") String user_id,
            @Part("is_public") Integer is_public,
            @Part("item_lat") String item_lat,
            @Part("item_long") String item_long
    );

    @Multipart
    @POST("upload_multi_file.php")
    Call <ServerResponse> uploadMulFile(
            @Part MultipartBody.Part file1,
            @Part MultipartBody.Part file2,
            @Part("user_id") String user_id,
            @Part("is_public") Integer is_public,
            @Part("item_lat") String item_lat,
            @Part("item_long") String item_long
            );

    @Multipart
    @POST("upload_single_file.php")
    Call <ServerResponse> uploadSingleFile(
            @Part MultipartBody.Part file1,
            @Part("user_id") String user_id,
            @Part("is_public") Integer is_public,
            @Part("item_lat") String item_lat,
            @Part("item_long") String item_long
    );

    @GET("get_voice.php")
    Call<MobilobyResponse> getVoices(@QueryMap Map<String, String> params);

    @FormUrlEncoded
    @POST("get_voice.php")
    Call<MobilobyResponse> getVoices2(@Field("user_id") String user_id);

    @FormUrlEncoded
    @POST("get_user.php")
    Call<MobilobyResponse> getUser(@Field("user_id") String user_name);

    @FormUrlEncoded
    @POST("insert_user.php")
    Call<MobilobyResponse> insertUser(@Field("insert_id") String user_name, String password);

}
