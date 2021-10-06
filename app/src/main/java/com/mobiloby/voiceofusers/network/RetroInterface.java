package com.mobiloby.voiceofusers.network;

import com.mobiloby.voiceofusers.models.FriendObject;
import com.mobiloby.voiceofusers.models.FriendResponse;
import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.models.ServerResponse;
import com.mobiloby.voiceofusers.models.UserResponse;

import java.util.Map;
import java.util.Observable;

import io.reactivex.internal.operators.observable.ObservableAll;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;

public interface RetroInterface {

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
    @POST("upload_multi_files.php")
    Call <ServerResponse> uploadMulFile(
            @Part MultipartBody.Part file1,
            @Part MultipartBody.Part file2,
            @Part("user_id") String user_id,
            @Part("is_public") Integer is_public,
            @Part("item_lat") String item_lat,
            @Part("item_long") String item_long
            );


    @Multipart
    @POST("insert_user_with_photo.php")
    Call <ServerResponse> uploadUserWithPhoto(
            @Part MultipartBody.Part file1,
            @Part("user_phone") String user_phone,
            @Part("user_name") String user_name
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

    @FormUrlEncoded
    @POST("get_voices.php")
    Call<MobilobyResponse> getVoices2(@Field("user_id") String user_id, @Field("user_id_other") String user_id_other);

    @FormUrlEncoded
    @POST("insert_like.php")
    Call<MobilobyResponse> insertLike(@Field("user_id") Integer user_id, @Field("id") Integer id);

    @FormUrlEncoded
    @POST("delete_like.php")
    Call<MobilobyResponse> removeLike(@Field("user_id") Integer user_id, @Field("id") Integer id);

    @FormUrlEncoded
    @POST("insert_view.php")
    Call<MobilobyResponse> insertView(@Field("id") Integer id);

    @FormUrlEncoded
    @POST("insert_friend.php")
    Call<MobilobyResponse> insertFriend(@Field("user_id") Integer user_id, @Field("friend_id") Integer friend_id);

    @FormUrlEncoded
    @POST("update_friend_status.php")
    Call<MobilobyResponse> updateFriendStatus(@Field("user_id") Integer user_id, @Field("friend_id") Integer friend_id, @Field("status") Integer status);

    @FormUrlEncoded
    @POST("update_option.php")
    Call<MobilobyResponse> updateOption(@Field("id") Integer id, @Field("option") Integer option);

    @FormUrlEncoded
    @POST("get_voices_all.php")
    Call<MobilobyResponse> getVoicesAll(@Field("user_id") String user_id);

    @FormUrlEncoded
    @POST("get_voices_friends.php")
    Call<MobilobyResponse> getVoicesFriends(@Field("user_id") String user_id);

    @FormUrlEncoded
    @POST("get_user.php")
    Call<UserResponse> getUser(@Field("user_phone") String phone);

    @FormUrlEncoded
    @POST("get_friends.php")
    Call<FriendResponse> getFriends(@Field("user_id") String userID);

    @FormUrlEncoded
    @POST("get_requests.php")
    Call<FriendResponse> getRequests(@Field("user_id") String userID);

    @FormUrlEncoded
    @POST("insert_user.php")
    Call<ServerResponse> insertUser(@FieldMap() Map<String, String> param);

    @FormUrlEncoded
    @POST("send_otp.php")
    Call<ResponseBody> sendOTP(@Field("password") String password);

}
