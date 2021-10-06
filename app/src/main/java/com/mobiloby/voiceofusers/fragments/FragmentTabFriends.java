package com.mobiloby.voiceofusers.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.MainActivity;
import com.mobiloby.voiceofusers.adapters.MyFriendListAdapter;
import com.mobiloby.voiceofusers.databinding.FragmentTabFriendsBinding;
import com.mobiloby.voiceofusers.models.FriendObject;
import com.mobiloby.voiceofusers.models.FriendResponse;
import com.mobiloby.voiceofusers.viewModel.FragmentTabFriendsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentTabFriends extends Fragment implements MyFriendListAdapter.ItemClickListener{

    View view;
    FragmentTabFriendsBinding binding;
    MainActivity activity;
    MyFriendListAdapter adapter;
    FragmentTabFriendsViewModel viewModel;
    String userID="";
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    List<FriendObject> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTabFriendsBinding.inflate(inflater, container, false);
        view = binding.getRoot();
        activity = (MainActivity) getActivity();

        viewModel = new ViewModelProvider(activity).get(FragmentTabFriendsViewModel.class);

        prepareMe();

        observe();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getData();
    }

    public void getData(){
        viewModel.getRequests(userID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    private void observe() {
        viewModel.getData().observe(activity, new Observer<FriendResponse>() {
            @Override
            public void onChanged(FriendResponse friendResponse) {
                if(friendResponse.getPro()!=null){
                    list.clear();
                    list.addAll(Arrays.asList(friendResponse.getPro().clone()));
                    adapter.notifyDataSetChanged();
                }
            }
        });

        viewModel.getIsError().observe(activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(activity, "Error while getting friends and requests", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getIsLoading().observe(activity, new Observer<Boolean>() {
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


    }

    private void prepareMe() {

        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        userID = preferences.getString("voiz_user_id","");
        list = new ArrayList<>();

        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Your Voice");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);

        adapter = new MyFriendListAdapter(this, activity, list);
        adapter.setClickListenerSub(this);
        binding.recyclerview.setAdapter(adapter);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

    }

    @Override
    public void onItemClick(View view, int position, List<FriendObject> list) {

        activity.setCurrentItem(5, list.get(position).getUser_id());

    }
}