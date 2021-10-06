package com.mobiloby.voiceofusers.viewModel;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.models.UserResponse;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLoginViewModel extends ViewModel {

    Context context;
    Activity activity;
    RetroInterface getResponse;
    MutableLiveData<Boolean> isLoginLoading = new MutableLiveData<>();
    MutableLiveData<Integer> loginResponse = new MutableLiveData<>();
    MutableLiveData<UserResponse> userResponse = new MutableLiveData<>();
    MutableLiveData<Boolean> isLoginError = new MutableLiveData<>();
    MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    MutableLiveData<String> username = new MutableLiveData<>();
    MutableLiveData<String> otpPassword = new MutableLiveData<>();
    MutableLiveData<Boolean> isOldUser = new MutableLiveData<>();
    MutableLiveData<String> oldUserId = new MutableLiveData<>();

    public void init(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    public void getUser(String phoneNumber) {

        isLoginLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<UserResponse> call = getResponse.getUser(phoneNumber);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.isSuccessful()){
//                    loginResponse.postValue(response.body().getSuccess());
//                    oldUserId.postValue(response.body().getPro()[0].getUser_id());
                    userResponse.postValue(response.body());
                    isLoginError.postValue(false);
                    isLoginLoading.postValue(false);
                    isOldUser.postValue(true);
                }
                else{
                    isLoginError.postValue(true);
                    isLoginLoading.postValue(false);
                    isOldUser.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                isLoginError.postValue(true);
                isLoginLoading.postValue(false);
                isOldUser.postValue(false);
            }

        });
    }

//    public void insertUser(String phone, String username, String imgUrl) {
//
//        getResponse = ApiClient.getClient().create(RetroInterface.class);
//
//        Map<String, String> param = new HashMap();
//        param.put("user_phone", "+905453754372");
//        param.put("user_name", "bigimi");
//        param.put("user_image_url", "url");
//
//        Call<ResponseBody> call2 = getResponse.insertUser(param);
//
//        call2.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if(response.isSuccessful()){
////                    loginResponse.postValue(response.body().getSuccess());
//                    isLoginError.postValue(false);
//                    isLoginLoading.postValue(false);
//                }
//                else{
//                    isLoginError.postValue(true);
//                    isLoginLoading.postValue(false);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                isLoginError.postValue(true);
//                isLoginLoading.postValue(false);
//            }
//
//        });
//    }

    public void sendOTP(String id) {

        isLoginLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);
        Random random = new Random();
        otpPassword.postValue(id);

        Call<ResponseBody> call = getResponse.sendOTP(id);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
//                    loginResponse.postValue(response.body().getSuccess());
                    isLoginError.postValue(false);
                    isLoginLoading.postValue(false);
                }
                else{
                    isLoginError.postValue(true);
                    isLoginLoading.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoginError.postValue(true);
                isLoginLoading.postValue(false);
            }

        });
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public RetroInterface getGetResponse() {
        return getResponse;
    }

    public void setGetResponse(RetroInterface getResponse) {
        this.getResponse = getResponse;
    }

    public MutableLiveData<Boolean> getIsLoginLoading() {
        return isLoginLoading;
    }

    public void setIsLoginLoading(MutableLiveData<Boolean> isLoginLoading) {
        this.isLoginLoading = isLoginLoading;
    }

    public MutableLiveData<Integer> getLoginResponse() {
        return loginResponse;
    }

    public void setLoginResponse(MutableLiveData<Integer> loginResponse) {
        this.loginResponse = loginResponse;
    }

    public MutableLiveData<Boolean> getIsLoginError() {
        return isLoginError;
    }

    public void setIsLoginError(MutableLiveData<Boolean> isLoginError) {
        this.isLoginError = isLoginError;
    }

    public MutableLiveData<String> getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(MutableLiveData<String> phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public MutableLiveData<String> getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username.postValue(username);
    }

    public MutableLiveData<String> getOtpPassword() {
        return otpPassword;
    }

    public void setOtpPassword(String otpPassword) {
        this.otpPassword.postValue(otpPassword);
    }

    public MutableLiveData<Boolean> getIsOldUser() {
        return isOldUser;
    }

    public void setIsOldUser(Boolean isOldUser) {
        this.isOldUser.postValue(isOldUser);
    }

    public MutableLiveData<UserResponse> getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(MutableLiveData<UserResponse> userResponse) {
        this.userResponse = userResponse;
    }
}
