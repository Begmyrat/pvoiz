package com.mobiloby.voiceofusers.viewModel;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLoginViewModel extends ViewModel {

    Context context;
    Activity activity;
    RetroInterface getResponse;
    MutableLiveData<Boolean> isLoginLoading = new MutableLiveData<>();
    MutableLiveData<Integer> loginResponse = new MutableLiveData<>();
    MutableLiveData<Boolean> isLoginError = new MutableLiveData<>();

    public void init(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    public void loginUser(String username, String password) {

        isLoginLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.insertUser(username, password);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful()){
                    loginResponse.postValue(response.body().getSuccess());
                    isLoginError.postValue(false);
                    isLoginLoading.postValue(false);
                }
                else{
                    isLoginError.postValue(true);
                    isLoginLoading.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {
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
}
