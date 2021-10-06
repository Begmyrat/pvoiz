package com.mobiloby.voiceofusers.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.fragments.FragmentTabFriends;
import com.mobiloby.voiceofusers.models.FriendObject;
import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.models.UserObject;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFriendListAdapter extends RecyclerView.Adapter<MyFriendListAdapter.ViewHolder>{

    private List<FriendObject> list;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    RetroInterface getResponse;
    String userId="";
    SharedPreferences preferences;
    FragmentTabFriends fragment;

    // data is passed into the constructor
    public MyFriendListAdapter(FragmentTabFriends fragment, Context context, List<FriendObject> list) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        this.fragment = fragment;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        userId = preferences.getString("voiz_user_id", "");
    }

    public void update(List<FriendObject> list){
        this.list = list;
        notifyDataSetChanged();
    }

    // inflates the row layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_friends, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(holder!=null && holder.i_verify!=null){
            FriendObject m = list.get(position);

            holder.t_username.setText(""+m.getFriend_name());

            Glide
                    .with(context)
                    .load("https://mobiloby.com/_pvoiz/upload/photo/"+m.getUser_image_url())
                    .placeholder(R.drawable.voizlogo_record)
                    .circleCrop()
                    .into(holder.i_avatar);

            if(m.getStatus()==1){
                holder.i_verify.setVisibility(View.GONE);
                holder.i_block.setVisibility(View.GONE);
                holder.i_cancel.setVisibility(View.GONE);
                holder.t_username.setTextColor(context.getResources().getColor(R.color.black));
            }
            else{
                holder.i_verify.setVisibility(View.VISIBLE);
                holder.i_block.setVisibility(View.VISIBLE);
                holder.i_cancel.setVisibility(View.VISIBLE);
                holder.t_username.setTextColor(context.getResources().getColor(R.color.thirdtext));
            }
        }

    }

    public void updateData(ArrayList<FriendObject> list){
        this.list = list;
        notifyDataSetChanged();
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return list.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView t_username;
        ImageView i_verify, i_cancel, i_block, i_avatar;

        ViewHolder(View itemView) {
            super(itemView);

            t_username = itemView.findViewById(R.id.t_username);
            i_verify = itemView.findViewById(R.id.i_verify);
            i_cancel = itemView.findViewById(R.id.i_cancel);
            i_block = itemView.findViewById(R.id.i_block);
            i_avatar = itemView.findViewById(R.id.i_avatar);

            if(i_verify!=null){
                i_verify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "myID: " + userId + " friendID: " + list.get(getAdapterPosition()).getUser_id(), Toast.LENGTH_SHORT).show();
                        updateRequest(userId, list.get(getAdapterPosition()).getUser_id(), 1);
                    }
                });

                i_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                i_block.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), list);
        }
    }

    // convenience method for getting data at click position
    public FriendObject getItem(int id) {
        return list.get(id);
    }

    // allows clicks events to be caught
    public void setClickListenerSub(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, List<FriendObject> list);
    }

    public void updateRequest(String userID, String userIDSELF, Integer status) {

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Call<MobilobyResponse> call = getResponse.updateFriendStatus(Integer.parseInt(userID), Integer.parseInt(userIDSELF), status);

        call.enqueue(new Callback<MobilobyResponse>() {
            @Override
            public void onResponse(Call<MobilobyResponse> call, Response<MobilobyResponse> response) {
                if(response.isSuccessful()){
                    if(response.body().getSuccess()==0){
                        Toast.makeText(context, "Sorry, there was an error0", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show();
                        fragment.getData();
                    }
                }
                else{
                    Toast.makeText(context, "Sorry, there was an errorUnsuccess", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MobilobyResponse> call, Throwable t) {
                Toast.makeText(context, "Sorry, there was an errorFailure", Toast.LENGTH_SHORT).show();
            }

        });
    }

}

