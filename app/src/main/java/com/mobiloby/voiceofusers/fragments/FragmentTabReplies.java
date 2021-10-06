package com.mobiloby.voiceofusers.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.MainActivity;
import com.mobiloby.voiceofusers.databinding.FragmentTabFriendsBinding;
import com.mobiloby.voiceofusers.databinding.FragmentTabRepliesBinding;

public class FragmentTabReplies extends Fragment {

    View view;
    MainActivity activity;
    FragmentTabRepliesBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTabRepliesBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        activity = (MainActivity) getActivity();

        return view;
    }
}