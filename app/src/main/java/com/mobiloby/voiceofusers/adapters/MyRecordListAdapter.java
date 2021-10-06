package com.mobiloby.voiceofusers.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import com.mobiloby.voiceofusers.models.VoiceObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyRecordListAdapter extends RecyclerView.Adapter<MyRecordListAdapter.ViewHolder>{

    private List<VoiceObject> list;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    MediaPlayer mediaPlayer = new MediaPlayer();
    public int selectedIndex=-1;

    // data is passed into the constructor
    public MyRecordListAdapter(Context context, List<VoiceObject> list) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        
    }

    public void update(List<VoiceObject> list){
        this.list = list;
        notifyDataSetChanged();
    }

    // inflates the row layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.record_list_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        VoiceObject m = list.get(position);

        holder.t_username.setText(m.getUser_name());
        holder.t_time.setText(m.getItem_date());

        if(selectedIndex == position){
            holder.i_play.setImageResource(R.drawable.pause);
        }
        else{
            holder.i_play.setImageResource(R.drawable.play_bottom);
        }

        Glide
                .with(context)
                .load("https://mobiloby.com/_pvoiz/upload/photo/"+m.getUser_image_url())
                .placeholder(R.drawable.voizlogo_record)
                .circleCrop()
                .into(holder.i_avatar);
    }

    public void updateData(ArrayList<VoiceObject> list){
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

        TextView t_username, t_time;
        ImageView i_play, i_reply, i_avatar;

        ViewHolder(View itemView) {
            super(itemView);

            t_username = itemView.findViewById(R.id.t_username);
            t_time = itemView.findViewById(R.id.t_time);
            i_play = itemView.findViewById(R.id.i_playBottom);
            i_reply = itemView.findViewById(R.id.i_replyBottom);
            i_avatar = itemView.findViewById(R.id.i_avatar);

            i_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    playAudio(list.get(getAdapterPosition()).getItem_url());
                    if(selectedIndex == getAdapterPosition()){
                        selectedIndex = -1;
                        notifyDataSetChanged();
                        stopPlaying();
                    }
                    else{
                        selectedIndex = getAdapterPosition();
                        notifyDataSetChanged();
                        stopPlaying();
                        playAudio(list.get(selectedIndex).getItem_record_url(), i_play);
                    }
                }
            });

            i_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "reply pos: " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), list);
        }
    }

    // convenience method for getting data at click position
    public VoiceObject getItem(int id) {
        return list.get(id);
    }

    // allows clicks events to be caught
    public void setClickListenerSub(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, List<VoiceObject> list);
    }

    private void playAudio(String url, ImageView i_play) {

        url = "https://mobiloby.com/_pvoiz/upload/music/"+url;

        // initializing media player
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                i_play.setImageResource(R.drawable.play);
                selectedIndex = -1;
                Toast.makeText(context, "bitti", Toast.LENGTH_SHORT).show();
            }
        });

        // below line is use to set the audio
        // stream type for our media player.
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // below line is use to set our
        // url to our media player.

        try {
            mediaPlayer.setDataSource(url);
            // below line is use to prepare
            // and start our media player.
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // below line is use to display a toast message.
//        Toast.makeText(context, "Audio started playing..", Toast.LENGTH_SHORT).show();
    }

    public void stopPlaying() {
        if (mediaPlayer!=null) {
            // pausing the media player if media player
            // is playing we are calling below line to
            // stop our media player.
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;

            // below line is to display a message
            // when media player is paused.
//            Toast.makeText(context, "Audio has been paused", Toast.LENGTH_SHORT).show();
        } else {
            // this method is called when media
            // player is not playing.
            mediaPlayer = null;
//            Toast.makeText(context, "Audio has not played", Toast.LENGTH_SHORT).show();
        }
    }

}

