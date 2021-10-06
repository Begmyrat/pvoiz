package com.mobiloby.voiceofusers.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.MainActivity;
import com.mobiloby.voiceofusers.adapters.ViewPagerFragmentAdapter;
import com.mobiloby.voiceofusers.databinding.FragmentSocialBinding;

import java.util.ArrayList;

public class FragmentSocial extends Fragment implements TabLayout.OnTabSelectedListener{

    View view;
    MainActivity activity;
    FragmentSocialBinding binding;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    ArrayList<Fragment> fragments;
    FragmentManager fm;
    ViewPagerFragmentAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSocialBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        activity = (MainActivity) getActivity();

        prepareMe();

        tabLayout.setOnTabSelectedListener(this);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });



        return view;
    }

    private void prepareMe() {
        tabLayout = view.findViewById(R.id.tablayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addTab(tabLayout.newTab().setText("Friends"));
        tabLayout.addTab(tabLayout.newTab().setText("Replies"));
        viewPager = view.findViewById(R.id.viewPager);
        fragments = new ArrayList<>();
        fragments.add(new FragmentTabFriends());
        fragments.add(new FragmentTabReplies());
        fm = getChildFragmentManager();
        pagerAdapter = new ViewPagerFragmentAdapter(fm, getLifecycle(), fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setSaveEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}