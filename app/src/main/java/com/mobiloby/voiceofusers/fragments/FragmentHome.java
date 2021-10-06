package com.mobiloby.voiceofusers.fragments;

import static android.Manifest.permission.RECORD_AUDIO;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.ActivityNewRecord;
import com.mobiloby.voiceofusers.activities.ActivityNewRecordStep1;
import com.mobiloby.voiceofusers.activities.MainActivity;
import com.mobiloby.voiceofusers.adapters.MyRecordListAdapter;
import com.mobiloby.voiceofusers.databinding.FragmentHomeBinding;
import com.mobiloby.voiceofusers.helpers.AppConstants;
import com.mobiloby.voiceofusers.helpers.GpsUtils;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.viewModel.FragmentHomeViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FragmentHome extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, MyRecordListAdapter.ItemClickListener, GoogleMap.OnCameraIdleListener, View.OnClickListener {

    View view;
    MainActivity activity;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private FragmentHomeViewModel viewModel;
    private boolean isGPS = false;
    LatLng latLng;
    boolean isMapReady = false, isMarkerClicked=false, isOnlyFriends=false;
    ArrayList<VoiceObject> voiceObjects, voiceObjectsCopy;
    BottomSheetDialog bd;
    Map<String, Integer> markers = new HashMap<>();
    ArrayList<Marker> markerList;
    ProgressDialog progressDialog;
    LocationManager manager;
    MyRecordListAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FragmentHomeBinding binding;
    String userID;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        prepareMe();

        observe();

        binding.eSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    performSearch(binding.eSearch.getText().toString());
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    public FragmentHomeViewModel getViewModel(){
        return viewModel;
    }

    private void performSearch(String address) {
        viewModel.changeCamera(address);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    private void prepareMe() {

        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        userID = preferences.getString("voiz_user_id", "");

        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity,R.color.white));// set status background white
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        binding.iLocation.setOnClickListener(this);

        voiceObjects = new ArrayList<>();
        voiceObjectsCopy = new ArrayList<>();
        markerList = new ArrayList<>();
        manager = (LocationManager) activity.getSystemService( Context.LOCATION_SERVICE );
        isGPS = manager.isProviderEnabled( LocationManager.GPS_PROVIDER );

        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Your Voice");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);

        adapter = new MyRecordListAdapter(activity, voiceObjectsCopy);
        adapter.setClickListenerSub(this);
        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);

        viewModel = new ViewModelProvider(activity).get(FragmentHomeViewModel.class);
        viewModel.init(activity, activity);

        binding.iAllOrPrivate.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.init(activity, activity);
        viewModel.refreshData(userID, false);
    }

    private void observe() {
        viewModel.getDataObject().observe(activity, new Observer<List<VoiceObject>>() {
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

        viewModel.getDataErrorObject().observe(activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(activity, "Error while getting data bro", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getDataLoadingObject().observe(activity, new Observer<Boolean>() {
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

        viewModel.getDataInfo().observe(activity, new Observer<String>() {
            @Override
            public void onChanged(String s) {

            }
        });

        viewModel.getLatLng().observe(activity, new Observer<LatLng>() {
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
                        .icon(BitmapFromVector(activity, R.drawable.pin_self));

                Marker marker = mMap.addMarker(markerOptions);
                markers.put(marker.getId(), -1);
                markerList.add(marker);
//                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                if(activity.isNewRecordButtonClicked){
                    activity.isNewRecordButtonClicked = false;
                    Intent intent = new Intent(activity, ActivityNewRecordStep1.class);
//                    Intent intent = new Intent(activity, ActivityNewRecord.class);
                    intent.putExtra("lat", ""+viewModel.getLatLng().getValue().latitude);
                    intent.putExtra("lon", ""+viewModel.getLatLng().getValue().longitude);
                    startActivity(intent);
                }
            }
        });

        viewModel.getMapLatLng().observe(activity, new Observer<LatLng>() {
            @Override
            public void onChanged(LatLng latLng) {
                if(latLng!=null){
                    Toast.makeText(activity, "observed", Toast.LENGTH_SHORT).show();
                    CameraPosition camPos = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(10)
                            .build();
                    CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
                    mMap.animateCamera(camUpd3);
                }
            }
        });
        
        viewModel.getIsNewRecordButtonClicked().observe(activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Toast.makeText(activity, "new record clicked", Toast.LENGTH_SHORT).show();
                activity.isNewRecordButtonClicked = true;
                clickLocationButton();
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
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    new GpsUtils(activity).turnGPSOn(new GpsUtils.onGpsListener() {
                        @Override
                        public void gpsStatus(boolean isGPSEnable) {
                            // turn on GPS
                            isGPS = isGPSEnable;
                        }
                    });

                    viewModel.getLocationData();

                } else {
                    Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 200:{

                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.checkSelfPermission(activity, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
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

            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.style_maps));

            if(!isSuccess){
                Toast.makeText(activity, "Maps style loads failed", Toast.LENGTH_SHORT).show();
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

    public LatLng getLatLng(){
        return latLng;
    }

    public void clickLocationButton() {

        isGPS = manager.isProviderEnabled( LocationManager.GPS_PROVIDER );

        if (!isGPS) {
            Toast.makeText(activity, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            new GpsUtils(activity).turnGPSOn(new GpsUtils.onGpsListener() {
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
                    .icon(BitmapFromVector(activity, R.drawable.pin_others));


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
            if(markerList.size()>0){
                LatLngBounds bounds = builder.build();
                int padding = 70; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                if(isMapReady)
                    mMap.animateCamera(cu);
            }
            else{
                viewModel.changeCamera("Istanbul, Turkey");
            }
        }

        progressDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

    public void popupPlay(VoiceObject voiceObject){
        isMarkerClicked = true;
        bd = new BottomSheetDialog(activity, R.style.AppBottomSheetDialogTheme);
        bd.setCancelable(true);

        View view = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_player, null);
        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerView = view.findViewById(R.id.recyclerview_records);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyRecordListAdapter(activity, voiceObjectsCopy);
        recyclerView.setAdapter(adapter);
        adapter.setClickListenerSub(this);

        String address = getAddress(voiceObject.getItem_lat(), voiceObject.getItem_long());

        TextView t_address = view.findViewById(R.id.t_location);
        ImageView i_close = view.findViewById(R.id.i_cross);

        i_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.stopPlaying();
                adapter.selectedIndex = -1;
                isMarkerClicked = false;
                bd.dismiss();
            }
        });

        bd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                adapter.stopPlaying();
                adapter.selectedIndex = -1;
                adapter.notifyDataSetChanged();
                isMarkerClicked = false;
                bd.dismiss();
            }
        });

        t_address.setText(address);

        bd.setContentView(view);
        bd.show();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        if(isMapReady && marker!=null && markers.get(marker.getId())!=null && !isMarkerClicked && markers.get(marker.getId())==-1){
            Toast.makeText(activity, "This is your location bruh!", Toast.LENGTH_SHORT).show();
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

        if((""+list.get(position).getUser_id()).equals(userID)){

        }
        else{
            activity.setCurrentItem(4, list.get(position).getUser_id());
            Toast.makeText(activity, "clicked", Toast.LENGTH_SHORT).show();
            isMarkerClicked = false;
            bd.dismiss();
        }
    }

    @Override
    public void onCameraIdle() {
//        Toast.makeText(activity, "latLong: " + mMap.getCameraPosition().target.latitude + " " + mMap.getCameraPosition().target.longitude, Toast.LENGTH_SHORT).show();
//         gets the bound of the map's camera (entire screen)
//        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//         check the latlng is inside of the latLngBounds
//        latLngBounds.contains(latLng);
    }

    public void checkLanguage(){
        String language = Locale.getDefault().getLanguage();
        Toast.makeText(activity, "language: " + language, Toast.LENGTH_SHORT).show();

        if(!language.equals("tr")){
            language = "en";
        }

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        activity.getResources().updateConfiguration(configuration, activity.getResources().getDisplayMetrics());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.i_location:
                clickLocationButton();
                break;
            case R.id.i_allOrPrivate:
                isOnlyFriends =! isOnlyFriends;
                if(isOnlyFriends) {
                    viewModel.refreshData(userID, true);
                    binding.iAllOrPrivate.setImageResource(R.drawable.friends_option);
                }
                else {
                    viewModel.refreshData(userID, false);
                    binding.iAllOrPrivate.setImageResource(R.drawable.all_option);
                }
                break;
        }
    }

    public String getAddress(String lat, String lng) {

            try {
                Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 1);
                if (addresses != null && addresses.size() > 0) {



                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String belediye = addresses.get(0).getSubAdminArea();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                    return ""+state+", "+country.toUpperCase();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "";
    }
}