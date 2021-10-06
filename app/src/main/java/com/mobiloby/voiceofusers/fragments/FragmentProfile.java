package com.mobiloby.voiceofusers.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.MainActivity;
import com.mobiloby.voiceofusers.adapters.MyRecordHistoryListAdapter;
import com.mobiloby.voiceofusers.databinding.FragmentProfileBinding;
import com.mobiloby.voiceofusers.helpers.SnapHelperOneByOne;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.viewModel.FragmentProfileViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FragmentProfile extends Fragment implements MyRecordHistoryListAdapter.ItemClickListener, View.OnClickListener{


    View view;
    MainActivity activity;
    FragmentProfileBinding binding;
    MyRecordHistoryListAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    FragmentProfileViewModel viewModel;
    String userID="";
    List<VoiceObject> voiceObjectList;
    ProgressDialog progressDialog;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        prepareMe();
        Toast.makeText(activity, "id: " + userID, Toast.LENGTH_SHORT).show();

        setListeners();

        observe();

        return view;
    }



    @Override
    public void onResume() {
        super.onResume();

        viewModel.setUserID(userID);
    }

    private void setListeners() {
        binding.iLock.setOnClickListener(this);
        binding.iOption.setOnClickListener(this);
        binding.iTrash.setOnClickListener(this);
        binding.iReply.setOnClickListener(this);
        binding.iPlay.setOnClickListener(this);
        binding.iLike.setOnClickListener(this);
        binding.tShowOnMaps.setOnClickListener(this);
    }

    private void observe() {
        viewModel.getUserID().observe(activity, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                userID = s;
                viewModel.refreshData(userID);
            }
        });

        viewModel.getDataObject().observe(activity, new Observer<List<VoiceObject>>() {
            @Override
            public void onChanged(List<VoiceObject> list) {
                voiceObjectList.clear();
                voiceObjectList.addAll(list);
                adapter.update(list);
                adapter.notifyDataSetChanged();
                Toast.makeText(activity, "size: " + voiceObjectList.size(), Toast.LENGTH_SHORT).show();
                if(voiceObjectList.size()>0){

                    binding.tUsername.setText(voiceObjectList.get(0).getUser_name());

                    Glide
                            .with(activity)
                            .load("https://mobiloby.com/_pvoiz/upload/photo/" + voiceObjectList.get(0).getUser_image_url())
                            .placeholder(R.drawable.ic_microphone)
                            .into(binding.iAvatar);

                    if(voiceObjectList.get(0).getIs_public() == 1){
                        viewModel.setIsOnlyFriends(false);
                        viewModel.setIsLocked(false);
                    }
                    else if(voiceObjectList.get(0).getIs_public() == 2){
                        viewModel.setIsOnlyFriends(true);
                        viewModel.setIsLocked(false);
                    }
                    else{
                        viewModel.setIsLocked(true);
                    }
                    viewModel.setIsPlaying(false);
                    viewModel.setPlayerIndex(0);
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

        viewModel.getDataErrorObject().observe(activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(activity, "data alinamadi, tekrar dene", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getIsLocked().observe(activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    binding.iLock.setImageResource(R.drawable.lock);
                    binding.iOption.setImageResource(R.drawable.private_option);
                    binding.tOption.setText("Private");
                    try {
                        viewModel.getDataObject().getValue().get(viewModel.getPlayerIndex().getValue()).setIs_public(3);
                    }catch (Exception e){}
                }
                else {
                    binding.iLock.setImageResource(R.drawable.lock_passive);
                    if(viewModel.getIsOnlyFriends().getValue()) {
                        binding.tOption.setText("Only Friends");
                        binding.iOption.setImageResource(R.drawable.friends_option);
                        try {
                            viewModel.getDataObject().getValue().get(viewModel.getPlayerIndex().getValue()).setIs_public(2);
                        }catch (Exception e){}
                    }
                    else {
                        binding.tOption.setText("All");
                        binding.iOption.setImageResource(R.drawable.all_option);
                        try {
                            viewModel.getDataObject().getValue().get(viewModel.getPlayerIndex().getValue()).setIs_public(1);
                        }catch (Exception e){}
                    }
                }
            }
        });

        viewModel.getIsOnlyFriends().observe(activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    binding.iOption.setImageResource(R.drawable.friends_option);
                    binding.tOption.setText("Only Friends");
                }
                else{
                    binding.iOption.setImageResource(R.drawable.all_option);
                    binding.tOption.setText("All");
                }
            }
        });

        viewModel.getPlayerIndex().observe(activity, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if( voiceObjectList!=null && integer<voiceObjectList.size()){

                    viewModel.setIsPlaying(false);

                    binding.tLocation.setText(""+voiceObjectList.get(integer).getFull_address() + " Ankara, TURKEY");
                    binding.tLikeNumber.setText(""+voiceObjectList.get(integer).getItem_like());
                    binding.tViewNumber.setText("view " + voiceObjectList.get(integer).getItem_view());

                    if(viewModel.getDataObject().getValue().get(integer).getIs_public()==1){
                        viewModel.setIsLocked(false);
                        viewModel.setIsOnlyFriends(false);
                    }
                    else if(viewModel.getDataObject().getValue().get(integer).getIs_public()==2){
                        viewModel.setIsLocked(false);
                        viewModel.setIsOnlyFriends(true);
                    }
                    else{
                        viewModel.setIsLocked(true);
                        viewModel.setIsOnlyFriends(false);
                    }

                    Glide
                            .with(activity)
                            .load(voiceObjectList.get(integer).getItem_record_url())
                            .placeholder(R.drawable.photo_temp)
                            .into(binding.iRectangleBox);

                    if(voiceObjectList.get(integer).getIs_public() == 1){
                        binding.iOption.setImageResource(R.drawable.all_option);
                        binding.iLock.setImageResource(R.drawable.lock_passive);
                    }
                    else if(voiceObjectList.get(integer).getIs_public() == 2){
                        binding.iOption.setImageResource(R.drawable.friends_option);
                        binding.iLock.setImageResource(R.drawable.lock_passive);
                    }
                    else{
                        binding.iOption.setImageResource(R.drawable.passive_option);
                        binding.iLock.setImageResource(R.drawable.lock);
                    }

                }
            }
        });

        viewModel.getIsPlaying().observe(activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    binding.iPlay.setImageResource(R.drawable.pause_bigger);
                }
                else{
                    binding.iPlay.setImageResource(R.drawable.play);
                    viewModel.stopPlaying();
                }
            }
        });
        
        viewModel.getOptionError().observe(activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(activity, "option Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void prepareMe() {
        voiceObjectList = new ArrayList<>();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Filter");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);
        viewModel = new ViewModelProvider(activity).get(FragmentProfileViewModel.class);
        viewModel.init(activity.getApplicationContext(), activity);
        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerviewHistory.setLayoutManager(layoutManager);
        adapter = new MyRecordHistoryListAdapter(activity, voiceObjectList);
        binding.recyclerviewHistory.setAdapter(adapter);
        adapter.setClickListenerSub(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        userID = preferences.getString("voiz_user_id","");
    }

    @Override
    public void onItemClick(View view, int position, List<VoiceObject> list) {
        Toast.makeText(activity, "isPublic: " + list.get(position).getIs_public(), Toast.LENGTH_SHORT).show();
        viewModel.setPlayerIndex(position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.i_option:
                viewModel.changeOption();
                break;
            case R.id.i_lock:
                viewModel.changeLock();
                break;
            case R.id.i_play:
                viewModel.changePlayingStatus();
                break;
            case R.id.i_trash:
                break;
            case R.id.i_reply:
                break;
            case R.id.i_like:
                break;
            case R.id.t_showOnMaps:
                activity.setCurrentItem(5, userID);
                break;
            case R.id.cardview_avatar:
                break;
        }
    }
}