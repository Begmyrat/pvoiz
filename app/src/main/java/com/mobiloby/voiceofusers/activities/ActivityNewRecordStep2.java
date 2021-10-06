package com.mobiloby.voiceofusers.activities;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.desmond.squarecamera.CameraActivity;
import com.mobiloby.voiceofusers.BuildConfig;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.databinding.ActivityNewRecordStep2Binding;
import com.mobiloby.voiceofusers.helpers.AppConstants;
import com.mobiloby.voiceofusers.models.ServerResponse;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNewRecordStep2 extends AppCompatActivity implements View.OnClickListener{

    ActivityNewRecordStep2Binding binding;
    View view;
    String filename="", option="", userID="";
    int optionNumber=0;
    Bundle extras;
    float currentPosition=0, total=30;
    MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;
    Boolean isPlaying = false, isOptionsHidden = true, isDeleteOptionsHidden=true;
    Dialog builder;
    ProgressDialog progressDialog;
    SharedPreferences preferences;

    private static final int REQUEST_SELECT_PICTURE_FOR_FRAGMENT = 0x02;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";
    private static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GET_PHOTO = 2;
    private int requestMode = BuildConfig.VERSION_CODE;
    private UCropFragment fragment;
    Bitmap resimbit;
    private Uri resimuri;
    private static final String IMAGE_DIRECTORY = "/demonuts_upload_gallery";
    private static final int BUFFER_SIZE = 1024 * 10;
    String lat="", lon="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewRecordStep2Binding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        prepareMe();

    }

    private void prepareMe() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.textstroke));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        extras = getIntent().getExtras();
        filename = extras.getString("filename_record");
        lat = extras.getString("lat");
        lon = extras.getString("lon");
        mediaPlayer = new MediaPlayer();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userID = preferences.getString("voiz_user_id", "");

        binding.iPlay.setOnClickListener(this);
        binding.iNextIcon.setOnClickListener(this);
        binding.iTrashCircle.setOnClickListener(this);
        binding.iPublicIcon.setOnClickListener(this);
        binding.iFriendsIcon.setOnClickListener(this);
        binding.iPrivateIcon.setOnClickListener(this);
        binding.iCamIcon.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Filter");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);
    }

    private void playAudio(String url) {

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
            currentPosition = 0;
            total = mediaPlayer.getDuration()/1000;
            total+=2;
            binding.seekbar.setMax((int) total);
            if(total<10)
                binding.tCounterEnd.setText("00:0"+(int)total);
            else
                binding.tCounterEnd.setText("00:"+(int)total);
            run();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // below line is use to display a toast message.
        Toast.makeText(this, "Audio started playing..", Toast.LENGTH_SHORT).show();
    }

    public void run() {

        binding.iPlay.setImageResource(R.drawable.pause_bigger);

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                if(currentPosition<total){
                    currentPosition+=0.1;
                    binding.seekbar.setProgress((int) currentPosition);
                    if(currentPosition<10)
                        binding.tCounterStart.setText("00:0" + (int)currentPosition);
                    else
                        binding.tCounterStart.setText("00:" + (int)currentPosition);
                    handler.postDelayed(runnable, 140);
                }
                else{
                    binding.tCounterStart.setText("00:00");
                    binding.iPlay.setImageResource(R.drawable.play);
                    binding.seekbar.setProgress(0);
                    stopPlaying();
                }
            }
        };

        handler.postDelayed(runnable, 140);
    }

    private void stopPlaying() {
        if (mediaPlayer!=null) {
            currentPosition = total;
            // pausing the media player if media player
            // is playing we are calling below line to
            // stop our media player.
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;

            // below line is to display a message
            // when media player is paused.
            Toast.makeText(getApplicationContext(), "Audio has been paused", Toast.LENGTH_SHORT).show();
        } else {
            // this method is called when media
            // player is not playing.
            mediaPlayer = null;
            Toast.makeText(getApplicationContext(), "Audio has not played", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.i_play:
                isPlaying = !isPlaying;
                if(isPlaying){
                    binding.iPlay.setImageResource(R.drawable.pause_bigger);
                    playAudio(filename);
                    run();
                }
                else{
                    binding.iPlay.setImageResource(R.drawable.play);
                    stopPlaying();
                }
                break;
            case R.id.i_nextIcon:
                if(optionNumber>0){
                    clickUploadData();
                }
                else{
                    binding.iNextIcon.setImageResource(R.drawable.next_icon_yellow);
                    changeOptionsVisibleStatus();
                }
                break;
            case R.id.i_publicIcon:
                makeBold("Public");
                optionNumber = 1;
                changeOptionsVisibleStatus();
                break;
            case R.id.i_friendsIcon:
                makeBold("Only Friends");
                optionNumber = 2;
                changeOptionsVisibleStatus();
                break;
            case R.id.i_privateIcon:
                makeBold("Private");
                optionNumber = 3;
                changeOptionsVisibleStatus();
                break;
            case R.id.i_camIcon:
                clickCameraButton();
                break;
            case R.id.i_trashCircle:
                showPopUp();
                break;
        }
    }

    private void clickUploadData() {
        if(resimuri!=null){
            String pathRecord = filename;
            String pathImage = getFilePathFromURI(getApplicationContext(), resimuri, ".png");
            uploadMultiFile(pathRecord, pathImage);
        }
        else{
            String pathRecord = filename;
            uploadSingleFile(pathRecord);
        }
    }

    private void showPopUp() {
        builder = new Dialog(this, R.style.AlertDialogCustom);
        View view;
        view = LayoutInflater.from(this).inflate(R.layout.popup_verification, null);

        TextView t_title = view.findViewById(R.id.t_title);
        TextView t_description = view.findViewById(R.id.t_description);
        Button b_verify = view.findViewById(R.id.b_verify);
        Button b_cancel = view.findViewById(R.id.b_cancel);

        t_title.setText("Are you sure?");
        t_description.setText("Do you really want to delete these post? This process can not be undone.");

        b_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // will be deleted
                builder.dismiss();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        b_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });

        builder.setCancelable(true);
        builder.setContentView(view);
        builder.show();
    }

    public void changeOptionsVisibleStatus(){
        binding.consOptions.animate()
                .alpha(1f)
                .setDuration(1000);
    }

    public void initTexts(){
        Typeface face = ResourcesCompat.getFont(getApplicationContext(), R.font.quicksand_regular);

        binding.tPublic.setTypeface(face);
        binding.tFriends.setTypeface(face);
        binding.tPrivate.setTypeface(face);
    }

    public void makeBold(String option){
        initTexts();
        this.option = option;
        Typeface face = ResourcesCompat.getFont(getApplicationContext(), R.font.quicksand_bold);
        if(option.equals("Public"))
            binding.tPublic.setTypeface(face);
        else if(option.equals("Only Friends"))
            binding.tFriends.setTypeface(face);
        else
            binding.tPrivate.setTypeface(face);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, new String[]{CAMERA, CAMERA_SERVICE},
                AppConstants.CAMERA_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (requestCode == requestMode)
            {
                final Uri selectedUri = data.getData();
                if (selectedUri != null)
                {
                    startCrop(selectedUri);
                }
            }
            else if (requestCode == UCrop.REQUEST_CROP)
            {
                try {
                    handleCropResult(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (resultCode == UCrop.RESULT_ERROR)
        {
            handleCropError(data);
        }

        //RESİM çekilmiş ise resmi göster
        if (data != null && data.getData()!=null)
        {
            resimuri = data.getData();
            if ((resultCode == RESULT_OK) && (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_GET_PHOTO))
            {
                try
                {
                    if (Build.VERSION.SDK_INT >= 28)
                    {
                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), resimuri);
                        resimbit = ImageDecoder.decodeBitmap(source);
                    }
                    else
                    {
                        resimbit = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resimuri);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();

        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
//        options.setCircleDimmedLayer(true);
        options.withMaxResultSize(400,400);

        return uCrop.withOptions(options);
    }

    private UCrop basisConfig(@NonNull UCrop uCrop) {

        uCrop = uCrop.withAspectRatio(1, 1);
        uCrop = uCrop.withMaxResultSize(400,400);

        return uCrop;
    }


    private void handleCropResult(@NonNull Intent result) throws IOException {
        final Uri resultUri = UCrop.getOutput(result);
        resimuri = resultUri;
        if (resultUri != null) {
//            imageView.setImageURI(resultUri);

//            findViewById(R.id.c_done).setVisibility(View.VISIBLE);

            Glide
                    .with(this)
                    .load(new File(resultUri.getPath()))
                    .placeholder(R.drawable.ic_microphone)
                    .into(binding.iRecordPhotoTemp);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
//            Log.e(TAG, "handleCropError: ", cropError);
//            makeAlert.uyarıVer("Error!",  cropError.getMessage(), UserProfile.this, true);
        }
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME + Calendar.getInstance().getTimeInMillis();
        destinationFileName += ".jpeg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));

        uCrop = basisConfig(uCrop);
        uCrop = advancedConfig(uCrop);

        if (requestMode == REQUEST_SELECT_PICTURE_FOR_FRAGMENT) {
            setupFragment(uCrop);
        } else {
            uCrop.start(ActivityNewRecordStep2.this);
        }

    }

    public void setupFragment(UCrop uCrop) {
        fragment = uCrop.getFragment(uCrop.getIntent(this).getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment, UCropFragment.TAG)
                .commitAllowingStateLoss();
    }

    private boolean checkFileAccessPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestFileAccessPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        }
    }

    public void clickCameraButton() {

        boolean b = false;

        if(!checkCameraPermission()){
            b = true;
            requestCameraPermission();
        }
        if(!checkFileAccessPermission()) {
            b=true;
            requestFileAccessPermission();
        }
        if(b) return;

        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivityForResult(intent, requestMode);
    }

    public static String getFilePathFromURI(Context context, Uri contentUri, String type) {
        //copy file and send new file path

        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);

        Log.d("pathhh", wallpaperDirectory.getAbsolutePath());

        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        File copyFile = new File(wallpaperDirectory + File.separator + Calendar.getInstance()
                .getTimeInMillis()+type);
        // create folder if not exists

        copy(context, contentUri, copyFile);
        Log.d("vPath--->",copyFile.getAbsolutePath());
        Log.d("vPathContent--->",contentUri.getPath());

        return copyFile.getAbsolutePath();

    }

    public static void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copystream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int copystream(InputStream input, OutputStream output) throws Exception, IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                Log.e(e.getMessage(), String.valueOf(e));
            }
            try {
                in.close();
            } catch (IOException e) {
                Log.e(e.getMessage(), String.valueOf(e));
            }
        }
        return count;
    }

    private void uploadMultiFile(String pathRecord, String pathImage){

        progressDialog.show();

        String fileName = String.valueOf(Calendar.getInstance().getTimeInMillis());

        //Create a file object using file path
        File file1 = new File(pathRecord);
        File file2 = new File(pathImage);

        // Parsing any Media type file
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("*/*"), file1);
        RequestBody requestBody2 = RequestBody.create(MediaType.parse("*/*"), file2);

        MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("file1", file1.getName(), requestBody1);
        MultipartBody.Part fileToUpload2 = MultipartBody.Part.createFormData("file2", file2.getName(), requestBody2);
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), fileName);

        RetroInterface getResponse = ApiClient.getClient().create(RetroInterface.class);
        Call<ServerResponse> call = getResponse.uploadMulFile(fileToUpload1, fileToUpload2, userID, optionNumber, lat, lon);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call < ServerResponse > call, Response< ServerResponse > response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
                progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call < ServerResponse > call, Throwable t) {
                Toast.makeText(getApplicationContext(), "FAILED", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }

    private void uploadSingleFile(String pathRecord){

        progressDialog.show();

        String fileName = String.valueOf(Calendar.getInstance().getTimeInMillis());

        //Create a file object using file path
        File file1 = new File(pathRecord);

        // Parsing any Media type file
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("*/*"), file1);

        MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("file1", file1.getName(), requestBody1);

        RetroInterface getResponse = ApiClient.getClient().create(RetroInterface.class);
        Toast.makeText(getApplicationContext(), "filePath: " + pathRecord, Toast.LENGTH_SHORT).show();
        Call<ServerResponse> call = getResponse.uploadSingleFile(fileToUpload1, userID, optionNumber, lat, lon);

        call.enqueue(new Callback <ServerResponse> () {
            @Override
            public void onResponse(Call < ServerResponse > call, Response < ServerResponse > response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
                progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call < ServerResponse > call, Throwable t) {
                Toast.makeText(getApplicationContext(), "FAILED", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }
}