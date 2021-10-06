package com.mobiloby.voiceofusers.activities;

import static android.Manifest.permission.RECORD_AUDIO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.databinding.ActivityNewRecordStep1Binding;
import com.mobiloby.voiceofusers.helpers.AppConstants;
import com.mobiloby.voiceofusers.viewModel.ActivityNewRecordStep1ViewModel;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class ActivityNewRecordStep1 extends AppCompatActivity implements View.OnClickListener{

    ActivityNewRecordStep1Binding binding;
    View view;
    private float x1,x2, y1,y2;
    static final int MIN_DISTANCE = 260;
    ActivityNewRecordStep1ViewModel viewModel;
    int height, width;
    Vibrator v;
    float currentProgress=0;
    Runnable runnable;
    Handler handler;
    Boolean isLocked = false, isRecording=false;
    MediaRecorder recorder;
    String fileName = "", lat="",lon="";
    Uri uri;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewRecordStep1Binding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        viewModel = new ViewModelProvider(this).get(ActivityNewRecordStep1ViewModel.class);
        viewModel.setIsLocked(false);
        viewModel.setIsDeleted(false);
        viewModel.setIsLogoClicked(false);

        prepareMe();

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        binding.circularProgressBar.setProgressMax(15);

        observe();

        runnable = new Runnable() {
            @Override
            public void run() {
                currentProgress+=0.01;
                binding.circularProgressBar.setProgress(currentProgress);
                if(currentProgress>=15){
                    binding.tContinue.setVisibility(View.VISIBLE);
                    binding.tTryAgain.setVisibility(View.VISIBLE);
                    currentProgress = 0;
                    handler.removeCallbacks(this);
                    binding.circularProgressBar.setProgress(0);
                }
                else{
                    handler.postDelayed(this,10);
                }
            }
        };

        handler = new Handler();


    }

    private void observe() {

        viewModel.getIsLocked().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){

                }
                else{

                }
            }
        });

        viewModel.getIsDeleted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){

                }
                else{

                }
            }
        });

        viewModel.getIsLogoClicked().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){

                }
                else{

                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        binding.circularProgressBar.setProgress(0);
        currentProgress = 0;
        binding.tListening.setText("Hold for recording");
        isRecording = false;
    }

    private void prepareMe() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        binding.tTryAgain.setOnClickListener(this);
        binding.tContinue.setOnClickListener(this);
        binding.iCloseIcon.setOnClickListener(this);

        extras = getIntent().getExtras();
        lat = extras.getString("lat");
        lon = extras.getString("lon");

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

            switch(event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                    x2 = event.getX();
                    y2 = event.getY();
                    float deltaX = x2 - x1;
                    if (x2>x1 && Math.abs(deltaX) > MIN_DISTANCE && y1>=height/2-120 && y1<=height/2+120 && y2>=height/2-120 && y2<=height/2+120 && x1>=width/2-120 && x1<=width/2+120)
                    {
                        binding.tListening.setText("Lock?");
                        binding.iUnlock.setImageResource(R.drawable.locked_record);
                    }
                    else if(x2<x1 && Math.abs(deltaX) > MIN_DISTANCE && y1>=height/2-120 && y1<=height/2+120 && y2>=height/2-120 && y2<=height/2+120 && x1>=width/2-120 && x1<=width/2+120){
                        binding.tListening.setText("Are you sure want to delete?");
                        binding.iTrashIcon.setImageResource(R.drawable.trash_side);
                    }
                    else
                    {
                        // consider as something else - a screen tap for example

                        if(!isLocked && currentProgress==0 && x1>=width/2-200 && x1<=width/2+200 && y1>=height/2-200 && y1<=height/2+200) {
//                            binding.circularProgressBar.setProgress(0);
//                            currentProgress = 0;
                            binding.iUnlock.setImageResource(R.drawable.unlock_icon);
                            binding.tListening.setText("Recording...");
                        }
                        binding.iTrashIcon.setImageResource(R.drawable.trash_icon);
                    }
                    break;

                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    if(currentProgress==0 && x1>=width/2-200 && x1<=width/2+200 && y1>=height/2-200 && y1<=height/2+200){
                        if(!checkAudiPermission()){
                            requestAudiPermission();
                            break;
                        }
                        Log.d("COORD" , "x1: " + x1 + " y1: " + y1 + " w: " + width/2 + " h: " + height/2 );
                        binding.tListening.setText("Recording...");
                        binding.tContinue.setVisibility(View.INVISIBLE);
                        binding.tTryAgain.setVisibility(View.INVISIBLE);
                        isRecording = true;
                        try {
                            startRecording();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        handler.postDelayed(runnable, 10);
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    y2 = event.getY();
                    float deltaX2 = x2 - x1;

                    if (!isLocked && x2>x1 && Math.abs(deltaX2) > MIN_DISTANCE && y1>=height/2-120 && y1<=height/2+120 && y2>=height/2-120 && y2<=height/2+120
                            && x1>=width/2-120 && x1<=width/2+120)
                    {
                        Toast.makeText(this, "Locked: " + deltaX2, Toast.LENGTH_SHORT).show ();
                        binding.tListening.setText("Locked");
                        isLocked = true;
                        binding.iUnlock.setImageResource(R.drawable.locked_record);
                        v.vibrate(80);
                        binding.tContinue.setVisibility(View.VISIBLE);
                        binding.tTryAgain.setVisibility(View.VISIBLE);
                    }
                    else if(x2<x1 && Math.abs(deltaX2) > MIN_DISTANCE && y1>=height/2-120 && y1<=height/2+120 && y2>=height/2-120 && y2<=height/2+120
                            && x1>=width/2-120 && x1<=width/2+120){
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show ();
                        binding.tListening.setText("Deleted :(");
                        isLocked = false;
                        binding.iTrashIcon.setImageResource(R.drawable.trash_side);
                        v.vibrate(80);
                        handler.removeCallbacks(runnable);
                        binding.circularProgressBar.setProgress(0);
                        currentProgress = 0;
                        stopRecording();
                        fileName = "";
                        uri = null;
                    }

                    else
                    {
                        // consider as something else - a screen tap for example
                        if(!isLocked && isRecording){
                            handler.removeCallbacks(runnable);
                            binding.circularProgressBar.setProgress(0);
                            currentProgress = 0;
                            stopRecording();
                            binding.tContinue.setVisibility(View.VISIBLE);
                            binding.tTryAgain.setVisibility(View.VISIBLE);
                            binding.tListening.setText("Hold for recording");
                        }
                    }
                    break;
            }

        return super.onTouchEvent(event);
    }

    private boolean checkAudiPermission() {
        return ContextCompat.checkSelfPermission(this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestAudiPermission(){
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO},
                AppConstants.RECORD_REQUEST);
    }

    private void startRecording() throws IOException {

        isRecording = true;

        fileName = getExternalCacheDir().getAbsolutePath() + "/" + Calendar.getInstance()
                .getTimeInMillis()+".mp3";

        uri = Uri.fromFile(new File(fileName));

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        recorder.prepare();

        recorder.start();
    }

    private void stopRecording() {
        isRecording = false;
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.t_continue:
                Toast.makeText(getApplicationContext(), "duration: " + binding.circularProgressBar.getProgress(), Toast.LENGTH_SHORT).show();
                binding.circularProgressBar.setProgress(0);
                currentProgress = 0;
                binding.tContinue.setVisibility(View.GONE);
                binding.tTryAgain.setVisibility(View.GONE);
                binding.iUnlock.setImageResource(R.drawable.unlock_icon);
                binding.iTrashIcon.setImageResource(R.drawable.trash_icon);
                binding.tListening.setText("Hold for recording");
                stopRecording();
                isRecording = false;
                handler.removeCallbacks(runnable);
                Intent intent = new Intent(getApplicationContext(), ActivityNewRecordStep2.class);
                intent.putExtra("filename_record", fileName);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                startActivity(intent);
                break;
            case R.id.i_closeIcon:
                finish();
                break;
            case R.id.t_tryAgain:
                fileName = "";
                isRecording = false;
                isLocked = false;
                currentProgress = 0;
                binding.circularProgressBar.setProgress(0);
                binding.tContinue.setVisibility(View.GONE);
                binding.tTryAgain.setVisibility(View.GONE);
                binding.iUnlock.setImageResource(R.drawable.unlock_icon);
                binding.iTrashIcon.setImageResource(R.drawable.trash_icon);
                binding.tListening.setText("Hold for recording");
                stopRecording();
                handler.removeCallbacks(runnable);
                break;
        }
    }
}