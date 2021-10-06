package com.mobiloby.voiceofusers.fragments;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.activities.MainActivity;
import com.mobiloby.voiceofusers.adapters.MyRecordHistoryListAdapter;
import com.mobiloby.voiceofusers.databinding.FragmentProfileBinding;
import com.mobiloby.voiceofusers.databinding.FragmentProfileOtherBinding;
import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;
import com.mobiloby.voiceofusers.viewModel.FragmentProfileViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentProfileOther extends Fragment implements MyRecordHistoryListAdapter.ItemClickListener, View.OnClickListener{

    View view;
    MainActivity activity;
    FragmentProfileOtherBinding binding;
    FragmentProfileViewModel viewModel;
    SharedPreferences preferences;
    public String user_id="", userIDSELF="";
    List<VoiceObject> list;
    MyRecordHistoryListAdapter adapter;
    RetroInterface getResponse;
    Boolean isPlaying = false;
    int currentIndex = 0;
    public MediaPlayer mediaPlayer = new MediaPlayer();
    boolean isFriends = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileOtherBinding.inflate(inflater, container, false);
        view= binding.getRoot();
        activity = (MainActivity) getActivity();

        prepareMe();

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Toast.makeText(activity, "height: " + height, Toast.LENGTH_SHORT).show();
        System.out.println("height: " + height);

        if(height<2000){
            binding.cardview.getLayoutParams().height = height/2 - height/4;
        }

        setListeners();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                binding.iPlay.setImageResource(R.drawable.play);
                isPlaying = false;
            }
        });

        return view;
    }

    private void setListeners() {
        binding.iPlay.setOnClickListener(this);
        binding.iReply.setOnClickListener(this);
        binding.iLike.setOnClickListener(this);
        binding.iTrash.setOnClickListener(this);
        binding.tAdd.setOnClickListener(this);
        binding.tShowOnMaps.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        isPlaying = false;
        Toast.makeText(activity, "userID: " + user_id, Toast.LENGTH_SHORT).show();
        getDataFromApi(user_id, userIDSELF);

    }

    private void prepareMe() {
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        userIDSELF = preferences.getString("voiz_user_id","");
        list = new ArrayList<>();
        adapter = new MyRecordHistoryListAdapter(activity, list);
        adapter.setClickListenerSub(this);
        binding.recyclerviewHistory.setAdapter(adapter);
        binding.recyclerviewHistory.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        viewModel =  new ViewModelProvider(activity).get(FragmentProfileViewModel.class);
        isPlaying = false;
    }

    public void getDataFromApi(String userID, String userIDSelf) {

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.getVoices2(userID, userIDSelf);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful() && response.body()!=null){
                    list.clear();
                    list.addAll(Arrays.asList(response.body().getPro().clone()));
                    binding.tUsername.setText(""+response.body().getUsername());
                    adapter.notifyDataSetChanged();
                    adapter.update(list);

                    if(response.body().getIs_friend()==1){
                        binding.tAdd.setBackground(activity.getDrawable(R.drawable.add_button_background_stroke_gri));
                        binding.tAdd.setText(R.string.button_following);
                        isFriends = false;
                        binding.tAdd.setTextColor(activity.getResources().getColor(R.color.black));
                    }
                    else{
                        binding.tAdd.setBackground(activity.getDrawable(R.drawable.add_button_background_stroke));
                        binding.tAdd.setText(R.string.button_follow);
                        isFriends = false;
                        binding.tAdd.setTextColor(activity.getResources().getColor(R.color.white));
                    }

                    Glide
                            .with(activity)
                            .load("https://mobiloby.com/_pvoiz/upload/photo/" + response.body().getUser_image_url())
                            .placeholder(R.drawable.voizlogo_record)
                            .into(binding.iAvatar);

                    if(list.get(0).getIs_liked()==1){
                        binding.iLike.setImageResource(R.drawable.likeactive);
                    }
                    else{
                        binding.iLike.setImageResource(R.drawable.like);
                    }

                    if(list.size()>0){
                        adapter.notifyDataSetChanged();
                        adapter.update(list);
                        binding.tLikeNumber.setText(""+list.get(0).getItem_like());
                        binding.tViewNumber.setText(""+list.get(0).getItem_view());
                        binding.tTime.setText(list.get(0).getItem_date());

                        Glide
                                .with(activity)
                                .load("https://mobiloby.com/_pvoiz/upload/photo/" + list.get(0).getItem_image_url())
                                .placeholder(R.drawable.voizlogo_record)
                                .into(binding.iRectangleBox);

                    }
                }
                else{

                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {

            }

        });
    }

    public void insertView(Integer id) {

        list.get(currentIndex).setItem_view(list.get(currentIndex).getItem_view()+1);
        binding.tViewNumber.setText(""+list.get(currentIndex).getItem_view());

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.insertView(id);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful()){

                }
                else{

                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {

            }

        });
    }

    @Override
    public void onItemClick(View view, int position, List<VoiceObject> list) {

        if(currentIndex!=position){
            String address = getAddress(list.get(position).getItem_lat(), list.get(position).getItem_long());

            binding.tLocation.setText(address);
            binding.tLikeNumber.setText(""+list.get(position).getItem_like());
            binding.tViewNumber.setText("" + list.get(position).getItem_view());

            currentIndex = position;

            binding.tTime.setText(""+list.get(position).getItem_date());

            binding.iRectangleBox.setImageResource(R.drawable.voizlogo_record);

            Glide
                    .with(activity)
                    .load("https://mobiloby.com/_pvoiz/upload/photo/" + list.get(position).getItem_image_url())
                    .placeholder(R.drawable.voizlogo_record)
                    .into(binding.iRectangleBox);

            if(list.get(position).getIs_liked()==1){
                binding.iLike.setImageResource(R.drawable.likeactive);
            }
            else{
                binding.iLike.setImageResource(R.drawable.like);
            }

        }

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

    public void changePlayingStatus(){
        isPlaying =! isPlaying;

        if(isPlaying){
            insertView(list.get(currentIndex).getId());
            binding.iPlay.setImageResource(R.drawable.pause_bigger);
            playAudio(list.get(currentIndex).getItem_record_url());
        }
        else{
            binding.iPlay.setImageResource(R.drawable.play);
            stopPlaying();
        }
    }

    public void playAudio(String url) {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                binding.iPlay.setImageResource(R.drawable.play);
                isPlaying = false;
                Toast.makeText(activity, "bittii hou", Toast.LENGTH_SHORT).show();
            }
        });

        try {
            mediaPlayer.setDataSource("https://mobiloby.com/_pvoiz/upload/music/"+url);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlaying() {
        if (mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        } else {
            mediaPlayer = null;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.i_play:
                changePlayingStatus();
                break;
            case R.id.i_reply:
                Toast.makeText(activity, "This feature will be added as soon as possible", Toast.LENGTH_SHORT).show();
                break;
            case R.id.t_showOnMaps:
                activity.setCurrentItem(5, user_id);
                break;
            case R.id.i_add:
                if(!isFriends)
                    insertFriend(userIDSELF, user_id);
                break;
            case R.id.i_like:
                Toast.makeText(activity, "You liked this post", Toast.LENGTH_SHORT).show();
                if(list.get(currentIndex).getIs_liked()==0)
                    likePost(userIDSELF, list.get(currentIndex).getId());
                else
                    disLikePost(userIDSELF, list.get(currentIndex).getId());
                break;
        }

    }

    public void likePost(String userID, Integer recordID) {

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.insertLike(Integer.parseInt(userID), recordID);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful()){
                    if(response.body().getSuccess()==0){
                        Toast.makeText(activity, "Sorry, there was an error", Toast.LENGTH_SHORT).show();
                    }
                    else if(response.body().getSuccess() == 1){
                        Toast.makeText(activity, "Successfully liked", Toast.LENGTH_SHORT).show();
                        list.get(currentIndex).setItem_like(list.get(currentIndex).getItem_like()+1);
                        list.get(currentIndex).setIs_liked(1);
                        binding.tLikeNumber.setText(""+list.get(currentIndex).getItem_like());
                        binding.iLike.setImageResource(R.drawable.likeactive);
                    }
                    else{
                        Toast.makeText(activity, "You already liked it before", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(activity, "Sorry, there was an error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {
                Toast.makeText(activity, "Sorry, there was an error", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void disLikePost(String userID, Integer recordID) {

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.removeLike(Integer.parseInt(userID), recordID);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful()){
                    list.get(currentIndex).setIs_liked(0);
                    list.get(currentIndex).setItem_like(list.get(currentIndex).getItem_like()-1);
                    binding.iLike.setImageResource(R.drawable.like);
                    binding.tLikeNumber.setText(""+list.get(currentIndex).getItem_like());
                }
                else{

                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {

            }

        });
    }

    public void insertFriend(String userID, String userIDSELF) {

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.insertFriend(Integer.parseInt(userID), Integer.parseInt(userIDSELF));

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful()){
                    if(response.body().getSuccess()==0){
                        Toast.makeText(activity, "Sorry, there was an error0", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        binding.tAdd.setBackground(activity.getResources().getDrawable(R.drawable.add_button_background_stroke_gri));
                        binding.tAdd.setText(R.string.button_following);
                        binding.tAdd.setTextColor(activity.getResources().getColor(R.color.secondtextcolor));
                    }
                }
                else{
                    Toast.makeText(activity, "Sorry, there was an errorUnsuccess", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {
                Toast.makeText(activity, "Sorry, there was an errorFailure", Toast.LENGTH_SHORT).show();
            }

        });
    }

}