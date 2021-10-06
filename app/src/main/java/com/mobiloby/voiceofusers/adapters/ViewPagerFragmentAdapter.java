package com.mobiloby.voiceofusers.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> arrayList = new ArrayList<>();


    public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, ArrayList<Fragment> list) {
        super(fragmentManager, lifecycle);
        arrayList = list;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        return arrayList.get(position);

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}