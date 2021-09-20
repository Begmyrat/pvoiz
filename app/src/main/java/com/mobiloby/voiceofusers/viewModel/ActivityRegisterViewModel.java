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

public class ActivityRegisterViewModel extends ViewModel {

    Context context;
    Activity activity;
    RetroInterface getResponse;
    MutableLiveData<Boolean> isRegisterLoading = new MutableLiveData<>();
    MutableLiveData<Integer> registerResponse = new MutableLiveData<>();
    MutableLiveData<Boolean> isRegisterError = new MutableLiveData<>();
    MutableLiveData<Boolean> isPasswordConfirmValid = new MutableLiveData<>();
    MutableLiveData<Boolean> isRegisterValid = new MutableLiveData<>();

    public void init(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    public void insertUser(String username, String password, String passwordConfirm) {

        if(username.length()<5){
            isRegisterValid.postValue(false);
        }
        else if(!password.equals(passwordConfirm)){
            isPasswordConfirmValid.postValue(false);
            isRegisterValid.postValue(true);
        }
        else{
            isRegisterLoading.postValue(true);
            isPasswordConfirmValid.postValue(true);
            isRegisterValid.postValue(true);

            getResponse = ApiClient.getClient().create(RetroInterface.class);

            Call<MobilobyResponse> call = getResponse.insertUser(username, password);

            call.enqueue(new Callback<MobilobyResponse>() {
                @Override
                public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                    if(response.isSuccessful()){
                        registerResponse.postValue(response.body().getSuccess());
                        isRegisterError.postValue(false);
                        isRegisterLoading.postValue(false);
                    }
                    else{
                        isRegisterError.postValue(true);
                        isRegisterLoading.postValue(false);
                    }
                }

                @Override
                public void onFailure(Call<MobilobyResponse> call, Throwable t) {
                    isRegisterError.postValue(true);
                    isRegisterLoading.postValue(false);
                }

            });
        }

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

    public MutableLiveData<Boolean> getIsRegisterLoading() {
        return isRegisterLoading;
    }

    public void setIsRegisterLoading(MutableLiveData<Boolean> isRegisterLoading) {
        this.isRegisterLoading = isRegisterLoading;
    }

    public MutableLiveData<Integer> getRegisterResponse() {
        return registerResponse;
    }

    public void setRegisterResponse(MutableLiveData<Integer> registerResponse) {
        this.registerResponse = registerResponse;
    }

    public MutableLiveData<Boolean> getIsRegisterError() {
        return isRegisterError;
    }

    public void setIsRegisterError(MutableLiveData<Boolean> isRegisterError) {
        this.isRegisterError = isRegisterError;
    }

    public MutableLiveData<Boolean> getIsPasswordConfirmValid() {
        return isPasswordConfirmValid;
    }

    public void setIsPasswordConfirmValid(MutableLiveData<Boolean> isPasswordConfirmValid) {
        this.isPasswordConfirmValid = isPasswordConfirmValid;
    }

    public MutableLiveData<Boolean> getIsRegisterValid() {
        return isRegisterValid;
    }

    public void setIsRegisterValid(MutableLiveData<Boolean> isRegisterValid) {
        this.isRegisterValid = isRegisterValid;
    }
}
