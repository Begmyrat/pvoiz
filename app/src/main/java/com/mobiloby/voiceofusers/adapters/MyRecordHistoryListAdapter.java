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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.models.VoiceObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyRecordHistoryListAdapter extends RecyclerView.Adapter<MyRecordHistoryListAdapter.ViewHolder> implements MediaPlayer.OnCompletionListener {

    private List<VoiceObject> list;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    MediaPlayer mediaPlayer = new MediaPlayer();
    public int selectedIndex=-1;

    // data is passed into the constructor
    public MyRecordHistoryListAdapter(Context context, List<VoiceObject> list) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        getAddress(context);
        mediaPlayer.setOnCompletionListener(this);
        
    }

    public void update(List<VoiceObject> list){
        this.list = list;
        getAddress(context);
        notifyDataSetChanged();
    }

    // inflates the row layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_record_history, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        VoiceObject m = list.get(position);

        Glide
                .with(context)
                .load("https://mobiloby.com/_pvoiz/upload/photo/" + m.getUser_image_url())
                .placeholder(R.drawable.history_record_temp_photo)
                .into(holder.i_image);

        holder.t_address.setText(""+m.getFull_address());

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

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopPlaying();
        selectedIndex = -1;
        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView t_address;
        ImageView i_image;

        ViewHolder(View itemView) {
            super(itemView);

            i_image = itemView.findViewById(R.id.i_gray);
            t_address = itemView.findViewById(R.id.t_address);

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

    private void playAudio(String url) {

        String audioUrl = url;

        // initializing media player
        mediaPlayer = new MediaPlayer();

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

    public void getAddress(Context context) {

        for(int i=0;i<list.size();i++){
            VoiceObject voiceObject = list.get(i);

            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(voiceObject.getItem_lat()), Double.parseDouble(voiceObject.getItem_long()), 1);
                if (addresses != null && addresses.size() > 0) {

                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();

                    list.get(i).setFull_address(""+state + ", " + country.toUpperCase());

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    }



}

