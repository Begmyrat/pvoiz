package com.mobiloby.voiceofusers.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.Observable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mobiloby.voiceofusers.databinding.ActivityMainBinding;
import com.mobiloby.voiceofusers.databinding.ActivityRegisterBinding;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.network.RetroInterface;
import com.mobiloby.voiceofusers.adapters.MyRecordListAdapter;
import com.mobiloby.voiceofusers.helpers.AppConstants;
import com.mobiloby.voiceofusers.helpers.GpsUtils;
import com.mobiloby.voiceofusers.helpers.JSONParser;
import com.mobiloby.voiceofusers.helpers.SnapHelperOneByOne;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.viewModel.MainViewModel;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, MyRecordListAdapter.ItemClickListener, GoogleMap.OnCameraIdleListener, View.OnClickListener {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private MainViewModel viewModel;
    private boolean isGPS = false;
    LatLng latLng;
    boolean isMapReady = false, isMarkerClicked=false;
    ArrayList<VoiceObject> voiceObjects, voiceObjectsCopy;
    BottomSheetDialog bd;
    Map<String, Integer> markers = new HashMap<>();
    ArrayList<Marker> markerList;
    ProgressDialog progressDialog;
    String state="", country="", belediye="";
    LocationManager manager;
    MyRecordListAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    ActivityMainBinding binding;
    View view;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        prepareMe();
        progressDialog.show();

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.init(getApplicationContext(), this);
        viewModel.refreshData("1");

        observe();
    }

    private void prepareMe() {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        voiceObjects = new ArrayList<>();
        voiceObjectsCopy = new ArrayList<>();
        markerList = new ArrayList<>();
        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        isGPS = manager.isProviderEnabled( LocationManager.GPS_PROVIDER );

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Your Voice");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);

        adapter = new MyRecordListAdapter(this, voiceObjectsCopy);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);


    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.init(getApplicationContext(), this);
    }

    private void observe() {
        viewModel.getDataObject().observe(this, new Observer<List<VoiceObject>>() {
            @Override
            public void onChanged(List<VoiceObject> objects) {
                adapter.update(objects);
                voiceObjects.clear();
                voiceObjectsCopy.clear();

                voiceObjectsCopy.addAll(viewModel.getDataObject().getValue());
                voiceObjects.addAll(viewModel.getDataObject().getValue());
                insertPins();
            }
        });

        viewModel.getDataErrorObject().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(getApplicationContext(), "Error while getting data bro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getDataLoadingObject().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    progressDialog.show();
                }
                else{
                    progressDialog.dismiss();
                }
            }
        });

        viewModel.getDataInfo().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {

            }
        });

        viewModel.getLatLng().observe(this, new Observer<LatLng>() {
            @Override
            public void onChanged(LatLng latLng) {
                // Showing the current location in Google Map
                CameraPosition camPos = new CameraPosition.Builder()
                        .target(new LatLng(viewModel.getLatLng().getValue().latitude, viewModel.getLatLng().getValue().longitude))
                        .zoom(50)
                        .build();
                CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
                mMap.animateCamera(camUpd3);

                for(int i=0;i<markerList.size();i++){
                    if(markers.get(markerList.get(i).getId())==-1){
                        markerList.get(i).remove();
                    }
                }

                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Marker in Current")
                        // below line is use to add custom marker on our map.
                        .icon(BitmapFromVector(getApplicationContext(), R.drawable.pin_self));

                Marker marker = mMap.addMarker(markerOptions);
                markers.put(marker.getId(), -1);
                markerList.add(marker);
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
                        @Override
                        public void gpsStatus(boolean isGPSEnable) {
                            // turn on GPS
                            isGPS = isGPSEnable;
                        }
                    });

                    viewModel.getLocationData();

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 200:{

                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }

                }

                break;
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        progressDialog.dismiss();

        try {

            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_maps));

            if(!isSuccess){
                Toast.makeText(getApplicationContext(), "Maps style loads failed", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mMap = googleMap;
        isMapReady = true;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraIdleListener(this);
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        viewModel.setLatLng(latLng);
    }

    public void clickLocationButton(View view) {
        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {
                    // turn on GPS
                    isGPS = isGPSEnable;
                }
            });
            return;
        }
        viewModel.getLocationData();
    }

    private void insertPins() {

        markerList.clear();
        if(isMapReady)
            mMap.clear();

        progressDialog.show();

        for(int i=0;i<markerList.size();i++){
            if(markers.get(markerList.get(i).getId())!=-1){
                markerList.get(i).remove();
            }
        }

        for(int i=0;i<viewModel.getDataObject().getValue().size();i++){

            Double dlat = Double.parseDouble(viewModel.getDataObject().getValue().get(i).getItem_lat());
            Double dlong = Double.parseDouble(viewModel.getDataObject().getValue().get(i).getItem_long());
            LatLng latLng = new LatLng(dlat, dlong);

            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(viewModel.getDataObject().getValue().get(i).getItem_date())
                    // below line is use to add custom marker on our map.
                    .icon(BitmapFromVector(getApplicationContext(), R.drawable.pin_others));


            if(isMapReady){
                Marker marker = mMap.addMarker(markerOptions);
                markers.put(marker.getId(), i);
                markerList.add(marker);
            }
        }

        if(isMapReady){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker markerItem : markerList) {
                builder.include(markerItem.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 70; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            if(isMapReady)
                mMap.animateCamera(cu);
        }

        progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

    public void popupPlay(VoiceObject voiceObject){
        isMarkerClicked = true;
        bd = new BottomSheetDialog(this);
        bd.setCancelable(true);
        View view;
        view = LayoutInflater.from(this).inflate(R.layout.bottom_layout_mediaplayer, null);

        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(layoutManager);
        LinearSnapHelper linearSnapHelper = new SnapHelperOneByOne();
        linearSnapHelper.attachToRecyclerView(recyclerView);

        adapter = new MyRecordListAdapter(getApplicationContext(), voiceObjectsCopy);
        recyclerView.setAdapter(adapter);

        TextView t_address = view.findViewById(R.id.t_address);
        TextView t_date = view.findViewById(R.id.t_date);

        bd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                adapter.stopPlaying();
                adapter.selectedIndex = -1;
                isMarkerClicked = false;
                bd.dismiss();
            }
        });

        t_address.setText(belediye + ", " + state + ", " + country + ".");
        t_date.setText(voiceObject.getItem_date());

        bd.setContentView(view);
        bd.show();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        
        if(isMapReady && marker!=null && markers.get(marker.getId())!=null && !isMarkerClicked && markers.get(marker.getId())==-1){
            Toast.makeText(getApplicationContext(), "This is your location bruh!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(isMapReady && marker!=null && markers.get(marker.getId())!=null && !isMarkerClicked){
            int pos = markers.get(marker.getId());
            VoiceObject m = voiceObjects.get(pos);
            voiceObjectsCopy.clear();
            for(int i=0;i<voiceObjects.size();i++){
                if(voiceObjects.get(i).getItem_long().equals(m.getItem_long()) && voiceObjects.get(i).getItem_lat().equals(m.getItem_lat())){
                    voiceObjectsCopy.add(voiceObjects.get(i));
                }
            }
            popupPlay(m);
        }

        return false;
    }

    private boolean isFileLessThan2MB(File file) {
        int maxFileSize = 2 * 1024 * 1024;
        Long l = file.length();
        String fileSize = l.toString();
        int finalFileSize = Integer.parseInt(fileSize);
        return finalFileSize <= maxFileSize;
    }

    @Override
    public void onItemClick(View view, int position, List<VoiceObject> list) {

    }


    public void clickRecordButton(View view) {
        // go to the record page to get a new record from user
        if(viewModel.getLatLng().getValue()!=null){
            Intent intent = new Intent(getApplicationContext(), ActivityNewRecord.class);
            intent.putExtra("lat", ""+viewModel.getLatLng().getValue().latitude);
            intent.putExtra("lon", ""+viewModel.getLatLng().getValue().longitude);
            startActivity(intent);
        }
        else{
            if (!isGPS) {
                Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
                new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
                    @Override
                    public void gpsStatus(boolean isGPSEnable) {
                        // turn on GPS
                        isGPS = isGPSEnable;
                    }
                });
                return;
            }
            viewModel.getLocationData();
        }
    }

    @Override
    public void onCameraIdle() {
//        Toast.makeText(getApplicationContext(), "latLong: " + mMap.getCameraPosition().target.latitude + " " + mMap.getCameraPosition().target.longitude, Toast.LENGTH_SHORT).show();
//         gets the bound of the map's camera (entire screen)
//        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//         check the latlng is inside of the latLngBounds
//        latLngBounds.contains(latLng);
    }

    public void checkLanguage(){
        String language = Locale.getDefault().getLanguage();
        Toast.makeText(getApplicationContext(), "language: " + language, Toast.LENGTH_SHORT).show();

        if(!language.equals("tr")){
            language = "en";
        }

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getApplicationContext().getResources().updateConfiguration(configuration, getApplicationContext().getResources().getDisplayMetrics());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.i_profile:
                startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                break;
        }
    }
}