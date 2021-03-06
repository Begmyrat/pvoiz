package com.mobiloby.voiceofusers.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.MainActivity;
import com.mobiloby.voiceofusers.databinding.FragmentLikesBinding;

public class FragmentLikes extends Fragment {

    View view;
    MainActivity activity;
    FragmentLikesBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLikesBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        return view;
    }
}