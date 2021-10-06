package com.mobiloby.voiceofusers.viewModel;

import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mobiloby.voiceofusers.models.FriendObject;
import com.mobiloby.voiceofusers.models.FriendResponse;
import com.mobiloby.voiceofusers.models.UserObject;
import com.mobiloby.voiceofusers.models.UserResponse;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentTabFriendsViewModel extends ViewModel {

    MutableLiveData<FriendResponse> data = new MutableLiveData<>();
    MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    MutableLiveData<Boolean> isError = new MutableLiveData<>();
    RetroInterface getResponse;

    public MutableLiveData<FriendResponse> getData() {
        return data;
    }

    public void setData(MutableLiveData<FriendResponse> data) {
        this.data = data;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setIsLoading(MutableLiveData<Boolean> isLoading) {
        this.isLoading = isLoading;
    }

    public MutableLiveData<Boolean> getIsError() {
        return isError;
    }

    public void setIsError(MutableLiveData<Boolean> isError) {
        this.isError = isError;
    }

    public void getFriends(String userID) {

        isLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<FriendResponse> call = getResponse.getFriends(userID);

        call.enqueue(new Callback<FriendResponse>() {
            @Override
            public void onResponse(Call<FriendResponse> call, Response<FriendResponse> response) {
                if(response.isSuccessful()){
                    data.postValue(response.body());
                    isLoading.postValue(false);
                    isError.postValue(false);

                }
                else{
                    isLoading.postValue(false);
                    isError.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<FriendResponse> call, Throwable t) {
                isLoading.postValue(false);
                isError.postValue(true);
            }

        });
    }

    public void getRequests(String userID) {

        isLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<FriendResponse> call = getResponse.getRequests(userID);

        call.enqueue(new Callback<FriendResponse>() {
            @Override
            public void onResponse(Call<FriendResponse> call, Response<FriendResponse> response) {
                if(response.isSuccessful()){
                    data.postValue(response.body());
                    isLoading.postValue(false);
                    isError.postValue(false);
                }
                else{
                    isLoading.postValue(false);
                    isError.postValue(true);
                }
                getFriends(userID);
            }

            @Override
            public void onFailure(Call<FriendResponse> call, Throwable t) {
                isLoading.postValue(false);
                isError.postValue(true);
                getFriends(userID);
            }

        });
    }
}
