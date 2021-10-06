package com.mobiloby.voiceofusers.viewModel;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.mobiloby.voiceofusers.helpers.AppConstants;
import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.models.UserResponse;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentProfileViewModel extends ViewModel {

    MutableLiveData<List<VoiceObject>> data = new MutableLiveData<>();
    MutableLiveData<Boolean> dataError = new MutableLiveData<>();
    MutableLiveData<Boolean> optionError = new MutableLiveData<>();
    MutableLiveData<Boolean> dataLoading = new MutableLiveData<>();
    MutableLiveData<String> dataInfo = new MutableLiveData<>();
    MutableLiveData<String> userID = new MutableLiveData<>();
    MutableLiveData<VoiceObject> currentData = new MutableLiveData<>();
    MutableLiveData<Boolean> isOnlyFriends = new MutableLiveData<>();
    MutableLiveData<Boolean> isLocked = new MutableLiveData<>();
    MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
    MutableLiveData<Integer> playerIndex = new MutableLiveData<>();
    RetroInterface getResponse;
    public MediaPlayer mediaPlayer = new MediaPlayer();

    Context context;
    Activity activity;

    public MutableLiveData<List<VoiceObject>> getDataObject(){
        return data;
    }
    public MutableLiveData<Boolean> getDataErrorObject(){
        return dataError;
    }
    public MutableLiveData<Boolean> getDataLoadingObject(){
        return dataLoading;
    }
    public MutableLiveData<String> getDataInfo(){ return dataInfo; }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public MutableLiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void changePlayingStatus(){
        isPlaying.postValue(!getIsPlaying().getValue());
        if(playerIndex!=null && playerIndex.getValue()!=null && playerIndex.getValue()>=0){
            if(!getIsPlaying().getValue()){
                Toast.makeText(context, "bashladi", Toast.LENGTH_SHORT).show();
                playAudio(data.getValue().get(playerIndex.getValue()).getItem_record_url());
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isPlaying.postValue(false);
                    }
                });
                
            }
            else{
                stopPlaying();
            }
        }
    }

    public MutableLiveData<Boolean> getOptionError() {
        return optionError;
    }

    public void setOptionError(MutableLiveData<Boolean> optionError) {
        this.optionError = optionError;
    }

    public void setIsPlaying(Boolean isPlaying) {
        this.isPlaying.postValue(isPlaying);
    }

    public MutableLiveData<Integer> getPlayerIndex() {
        return playerIndex;
    }

    public void setPlayerIndex(Integer playerIndex) {
        this.playerIndex.postValue(playerIndex);
    }

    public MutableLiveData<Boolean> getIsLocked() {
        return isLocked;
    }

    public void changeLock(){
        isLocked.postValue(!isLocked.getValue());

        if(isLocked.getValue()){
            updateOption(3);
        }
        else{
            if(isOnlyFriends.getValue()){
                updateOption(2);
            }
            else{
                updateOption(1);
            }
        }
    }

    public void changeOption(){
        if(!isLocked.getValue()){
            isOnlyFriends.postValue(!isOnlyFriends.getValue());

            if(isOnlyFriends.getValue()){
                updateOption(2);
            }
            else{
                updateOption(1);
            }
        }
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked.postValue(isLocked);
    }

    public MutableLiveData<Boolean> getIsOnlyFriends() {
        return isOnlyFriends;
    }

    public void setIsOnlyFriends(Boolean isOnlyFriends) {
        this.isOnlyFriends.postValue(isOnlyFriends);
    }

    public MutableLiveData<VoiceObject> getCurrentData() {
        return currentData;
    }

    public void setCurrentData(MutableLiveData<VoiceObject> currentData) {
        this.currentData = currentData;
    }

    public MutableLiveData<String> getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID.postValue(userID);
    }

    public void refreshData(String userID){
        getDataFromApi(userID);
    }

    public void setDataInfo(String info){
        dataInfo.setValue(info);
    }

    public void init(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }
    
    public void getDataFromApi(String userID) {
        dataLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.getVoices2(userID, userID);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful()){
                    data.postValue(Arrays.asList(response.body().getPro().clone()));
                    dataError.postValue(false);
                    dataLoading.postValue(false);
                }
                else{
                    dataError.postValue(true);
                    dataLoading.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {
                dataError.postValue(true);
                dataLoading.postValue(false);
            }

        });
    }

    public void updateOption(Integer option) {
        dataLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.updateOption(data.getValue().get(getPlayerIndex().getValue()).getId(), option);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful()){
                    optionError.postValue(false);
                    dataLoading.postValue(false);
                }
                else{
                    optionError.postValue(true);
                    dataLoading.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {
                optionError.postValue(true);
                dataLoading.postValue(false);
            }

        });
    }

    public void playAudio(String url) {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource("https://mobiloby.com/_pvoiz/upload/music/"+url);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlaying() {
        if (mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        } else {
            mediaPlayer = null;
        }
    }

    public void pausePlaying() {
        if (mediaPlayer!=null) {
            mediaPlayer.pause();
        } else {
            mediaPlayer = null;
        }
    }
}
