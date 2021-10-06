package com.mobiloby.voiceofusers.viewModel;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.MainActivity;
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

public class FragmentHomeViewModel extends ViewModel {

    MutableLiveData<List<VoiceObject>> data = new MutableLiveData<>();
    MutableLiveData<Boolean> dataError = new MutableLiveData<>();
    MutableLiveData<Boolean> dataLoading = new MutableLiveData<>();
    MutableLiveData<String> dataInfo = new MutableLiveData<>();
    MutableLiveData<String> privateInfo = new MutableLiveData<>();
    RetroInterface getResponse;
    MutableLiveData<LatLng> mapLatLng = new MutableLiveData<>();
    MutableLiveData<Boolean> isNewRecordButtonClicked = new MutableLiveData<>();

    private FusedLocationProviderClient mFusedLocationClient;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    Context context;
    Activity activity;
    MutableLiveData<LatLng> latLng = new MutableLiveData<>();
    final int KONUM_IZNI_REQUEST = 1000;
    private boolean isContinue = false;
    MutableLiveData<UserResponse> userResponse = new MutableLiveData<>();

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
    public MutableLiveData<LatLng> getLatLng(){ return latLng; }

    public MutableLiveData<LatLng> getMapLatLng() {
        return mapLatLng;
    }

    public void setMapLatLng(MutableLiveData<LatLng> mapLatLng) {
        this.mapLatLng = mapLatLng;
    }

    public MutableLiveData<String> getPrivateInfo() {
        return privateInfo;
    }

    public void setPrivateInfo(String privateInfo) {
        this.privateInfo.postValue(privateInfo);
    }

    public void refreshData(String userID, boolean isOnlyFriends){
        if(isOnlyFriends)
            getDataFromApiFriends(userID);
        else
            getDataFromApiAll(userID);
    }

    public void setDataInfo(String info){
        dataInfo.setValue(info);
    }

    public void setLatLng(LatLng latLng){
        this.latLng.postValue(latLng);
    }

    public void init(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        checkLocationPermissions();
        getLocation();
        getLocationData();
    }

    public void getDataFromApiAll(String userID) {
        dataLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.getVoicesAll(userID);

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

    public void getDataFromApiFriends(String userID) {
        dataLoading.postValue(true);

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.getVoicesFriends(userID);

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

    public void getLocation() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10sn
        locationRequest.setFastestInterval(5 * 1000); // 5sn

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        // Creating a LatLng object for the current location
                        latLng.postValue(new LatLng(wayLatitude, wayLongitude));

                    } else {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                }
            }
        };
    }

    public void getLocationData() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();

                        latLng.postValue(new LatLng(wayLatitude, wayLongitude));

                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                });
            }
        }
    }

    public void checkLocationPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            READ_EXTERNAL_STORAGE,
                            WRITE_EXTERNAL_STORAGE
                    },
                    KONUM_IZNI_REQUEST);
        }
    }

    public void changeCamera(String address){
        mapLatLng.postValue(getLocationFromAddress(address));
    }

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(activity);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    public MutableLiveData<Boolean> getIsNewRecordButtonClicked() {
        return isNewRecordButtonClicked;
    }

    public void setIsNewRecordButtonClicked(Boolean isNewRecordButtonClicked) {
        this.isNewRecordButtonClicked.postValue(isNewRecordButtonClicked);
    }
}
