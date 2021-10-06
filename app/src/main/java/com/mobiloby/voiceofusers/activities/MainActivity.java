package com.mobiloby.voiceofusers.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.adapters.ViewPagerFragmentAdapter;
import com.mobiloby.voiceofusers.databinding.ActivityMainBinding;
import com.mobiloby.voiceofusers.fragments.FragmentHome;
import com.mobiloby.voiceofusers.fragments.FragmentHomeOther;
import com.mobiloby.voiceofusers.fragments.FragmentLikes;
import com.mobiloby.voiceofusers.fragments.FragmentProfile;
import com.mobiloby.voiceofusers.fragments.FragmentProfileOther;
import com.mobiloby.voiceofusers.fragments.FragmentSocial;
import com.mobiloby.voiceofusers.helpers.AppConstants;
import com.mobiloby.voiceofusers.helpers.GpsUtils;
import com.mobiloby.voiceofusers.viewModel.FragmentHomeViewModel;
import com.mobiloby.voiceofusers.viewModel.FragmentProfileViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    ActivityMainBinding binding;
    View view;
    ConstraintLayout constraintLayout;
    ConstraintSet constraintSet;
    ArrayList<Fragment> fragmentList;
    ViewPagerFragmentAdapter pagerAdapter;
    LatLng latLng;
    FragmentProfileViewModel viewModelProfile;
    private boolean isContinue = false;
    FragmentHome fragmentHome;
    FragmentSocial fragmentSocial;
    FragmentLikes fragmentLikes;
    FragmentProfile fragmentProfile;
    FragmentHomeViewModel viewModelHome;
    FragmentProfileOther fragmentProfileOther;
    FragmentHomeOther fragmentHomeOther;
    public Boolean isNewRecordButtonClicked = false;
    SharedPreferences preferences;
    String userID="";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        viewModelProfile = new ViewModelProvider(this).get(FragmentProfileViewModel.class);
        viewModelHome = new ViewModelProvider(this).get(FragmentHomeViewModel.class);

        fragmentHome = new FragmentHome();
        fragmentSocial = new FragmentSocial();
        fragmentLikes = new FragmentLikes();
        fragmentProfile = new FragmentProfile();
        fragmentProfileOther = new FragmentProfileOther();
        fragmentHomeOther = new FragmentHomeOther();

        fragmentList = new ArrayList<>();
        fragmentList.add(fragmentHome);
        fragmentList.add(fragmentSocial);
        fragmentList.add(fragmentLikes);
        fragmentList.add(fragmentProfile);
        fragmentList.add(fragmentProfileOther);
        fragmentList.add(fragmentHomeOther);

        pagerAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setCurrentItem(0, false);
        binding.viewPager.setUserInputEnabled(false);
        binding.viewPager.setOffscreenPageLimit(4);

        binding.iHome.setOnClickListener(this);
        binding.iSocial.setOnClickListener(this);
        binding.iLikes.setOnClickListener(this);
        binding.iProfile.setOnClickListener(this);
        binding.iAdd.setOnClickListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userID = preferences.getString("voiz_user_id", "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

//        isNewRecordButtonClicked = false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.i_home) {
            binding.viewPager.setCurrentItem(0,false);
            constraintLayout = findViewById(R.id.cons_main);
            constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.viewPager, ConstraintSet.BOTTOM, R.id.i_bottom, ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(constraintLayout);
        }
        else{
            constraintLayout = findViewById(R.id.cons_main);
            constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.viewPager, ConstraintSet.BOTTOM, R.id.i_bottom, ConstraintSet.TOP, 0);
            constraintSet.applyTo(constraintLayout);

            if(v.getId() == R.id.i_social)
                binding.viewPager.setCurrentItem(1,false);
            else if(v.getId() == R.id.i_likes)
                binding.viewPager.setCurrentItem(2,false);
            else if(v.getId() == R.id.i_profile) {
                viewModelProfile.setUserID(userID);
                binding.viewPager.setCurrentItem(3, false);
            }
            else if(v.getId() == R.id.i_add){
                fragmentHome.getViewModel().setIsNewRecordButtonClicked(true);
            }
        }
    }

    public void setCurrentItem(int index, String userID){
        if(index==4)
            fragmentProfileOther.user_id = userID;
        else if(index==5)
            fragmentHomeOther.userID = userID;
        constraintLayout = findViewById(R.id.cons_main);
        constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.viewPager, ConstraintSet.BOTTOM, R.id.i_bottom, ConstraintSet.TOP, 0);
        constraintSet.applyTo(constraintLayout);
        binding.viewPager.setCurrentItem(index, false);
    }
}