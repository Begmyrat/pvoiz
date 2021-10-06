package com.mobiloby.voiceofusers.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.MainActivity;
import com.mobiloby.voiceofusers.adapters.MyRecordListAdapter;
import com.mobiloby.voiceofusers.databinding.FragmentHomeOtherBinding;
import com.mobiloby.voiceofusers.helpers.GpsUtils;
import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHomeOther extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraIdleListener, MyRecordListAdapter.ItemClickListener {

    View view;
    MainActivity activity;
    FragmentHomeOtherBinding binding;
    public String userID = "";
    RetroInterface getResponse;
    ProgressDialog progressDialog;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    boolean isMapReady = false, isMarkerClicked=false;
    ArrayList<VoiceObject> voiceObjects, voiceObjectsCopy;
    BottomSheetDialog bd;
    Map<String, Integer> markers = new HashMap<>();
    ArrayList<Marker> markerList;
    SharedPreferences preferences;
    MyRecordListAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeOtherBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        activity = (MainActivity) getActivity();

        prepareMe();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getDataFromApiAll(userID);
    }

    private void prepareMe() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Your Voice");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);

        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
//        userID = preferences.getString("voiz_user_id", "");
        voiceObjects = new ArrayList<>();
        voiceObjectsCopy = new ArrayList<>();
        markerList = new ArrayList<>();
        markers = new HashMap<>();

        adapter = new MyRecordListAdapter(activity, voiceObjectsCopy);
        adapter.setClickListenerSub(this);
        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    public void getDataFromApiAll(String userID) {

        Toast.makeText(activity, "other user id: " + userID, Toast.LENGTH_SHORT).show();

        progressDialog.show();

        voiceObjects.clear();

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.getVoices2(userID, userID);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {

                progressDialog.dismiss();

                if(response.isSuccessful()){
                    voiceObjects.addAll(Arrays.asList(response.body().getPro().clone()));
                    insertPins();
                }
                else{
                    Toast.makeText(activity, "Data alinamadi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {
                progressDialog.dismiss();
            }

        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
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

        for(int i=0;i<voiceObjects.size();i++){

            Double dlat = Double.parseDouble(voiceObjects.get(i).getItem_lat());
            Double dlong = Double.parseDouble(voiceObjects.get(i).getItem_long());
            LatLng latLng = new LatLng(dlat, dlong);

            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(voiceObjects.get(i).getItem_date())
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
        }

        progressDialog.dismiss();
    }

    @Override
    public void onCameraIdle() {

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

    public String getAddress(String lat, String lng) {

        try {
            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 1);
            if (addresses != null && addresses.size() > 0) {

                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();

                return ""+state+", "+country.toUpperCase();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
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
}