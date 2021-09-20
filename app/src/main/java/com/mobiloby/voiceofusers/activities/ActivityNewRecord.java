package com.mobiloby.voiceofusers.activities;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.BuildConfig;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.models.ServerResponse;
import com.mobiloby.voiceofusers.network.RetroInterface;
import com.mobiloby.voiceofusers.helpers.AppConstants;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;

import org.json.JSONException;
import org.json.JSONObject;

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

public class ActivityNewRecord extends AppCompatActivity {

    boolean isStarted = false, isPlaying=false, isRecording=false;
    AppCompatButton b_start;
    AppCompatButton b_play;
    MediaPlayer mediaPlayer = new MediaPlayer();
    MediaRecorder recorder;
    String fileName = "", lat="", lon="";
    Uri uri;
    SeekBar seekBar;
    TextView t_counterStart, t_counterEnd;
    int currentPosition = 0, total=30;
    Handler handler;
    Runnable runnable;
    private static final int REQUEST_SELECT_PICTURE_FOR_FRAGMENT = 0x02;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";
    private static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GET_PHOTO = 2;
    private int requestMode = BuildConfig.VERSION_CODE;
    private UCropFragment fragment;
    Bitmap resimbit;
    private Uri resimuri;
    ImageView imageView;
    private static final String IMAGE_DIRECTORY = "/demonuts_upload_gallery";
    private static final int BUFFER_SIZE = 1024 * 10;
    ProgressDialog progressDialog;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);

        prepareMe();

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }

    @SuppressLint("WrongViewCast")
    private void prepareMe() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        b_start = findViewById(R.id.b_startRecord);
//        b_play = findViewById(R.id.b_playRecord);
        b_play = findViewById(R.id.b_playRecord);
        seekBar = findViewById(R.id.seekbar);
        t_counterStart = findViewById(R.id.t_counterStart);
        t_counterEnd = findViewById(R.id.t_counterEnd);
        imageView = findViewById(R.id.imageview);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Filter");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);

        extras = getIntent().getExtras();
        if(extras!=null){
            lat = extras.getString("lat");
            lon = extras.getString("lon");
        }
    }

    private boolean checkAudiPermission() {
        return ContextCompat.checkSelfPermission(this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestAudiPermission(){
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO},
                AppConstants.RECORD_REQUEST);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, new String[]{CAMERA, CAMERA_SERVICE},
                AppConstants.CAMERA_REQUEST);
    }

    public void clickStartButton(View view) throws IOException {

        if(!checkAudiPermission()){
            requestAudiPermission();
            return;
        }

        isStarted = !isStarted;

        if(isStarted){
            stopPlaying();
            b_play.setText("Play");
            t_counterEnd.setText("00:30");
            fileName = "";
            isRecording = true;
            if(handler!=null)
                handler.removeCallbacks(runnable);
            total = 30;
            currentPosition = 0;
            seekBar.setMax(total);
            b_start.setText("Stop");
            seekBar.setProgress(0);
            run();
            startRecording();
        }
        else{
            isRecording = false;
            currentPosition = total;
            b_play.setAlpha(1);
            stopRecording();
            b_start.setText("Yeniden");
            t_counterStart.setText("00:00");
        }
    }

    public void run() {

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                if(currentPosition<total){
                    currentPosition++;
                    seekBar.setProgress(currentPosition);
                    if(currentPosition<10)
                        t_counterStart.setText("00:0" + currentPosition);
                    else
                        t_counterStart.setText("00:" + currentPosition);
                    handler.postDelayed(runnable, 1000);
                }
                else{
                    t_counterStart.setText("00:00");
                    b_start.setText("Yeniden");
                    b_play.setText("Play");
                    isPlaying = false;
                    seekBar.setProgress(0);
                }
            }
        };

        handler.postDelayed(runnable, 1000);
    }



    private void startRecording() throws IOException {
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
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    public void clickPlay(View view) {
        if(fileName!="" && !isRecording){
            isPlaying = !isPlaying;

            if(isPlaying) {
                playAudio(fileName);
                b_play.setText("Stop");
            }
            else{
                stopPlaying();
                b_play.setText("Play");
            }
        }
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
            seekBar.setMax(total);
            if(total<10)
                t_counterEnd.setText("00:0"+total);
            else
                t_counterEnd.setText("00:"+total);
            run();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // below line is use to display a toast message.
        Toast.makeText(this, "Audio started playing..", Toast.LENGTH_SHORT).show();
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

    public void clickNext(View view) {
        if(isStarted){
            Toast.makeText(getApplicationContext(), "Ses kaydını durdurmanız gerekiyor.", Toast.LENGTH_SHORT).show();
        }
        else if(uri!=null && resimuri!=null){
            String pathRecord = getFilePathFromURI(getApplicationContext(), uri, ".mp3");
            String pathImage = getFilePathFromURI(getApplicationContext(), resimuri, ".png");
            uploadMultiFile(pathRecord, pathImage);
        }
        else if(uri!=null){
            String pathRecord = getFilePathFromURI(getApplicationContext(), uri, ".mp3");
//            uploadRecord(pathRecord);
            uploadSingleFile(pathRecord);
        }
        else{
            Toast.makeText(getApplicationContext(), "Ses kaydı yapılmadı. Lütfen ses kaydı ekleyiniz", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBack(View view) {
        finish();
    }

    // camera ucrop


    @RequiresApi(api = Build.VERSION_CODES.P)
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void handleCropResult(@NonNull Intent result) throws IOException {
        final Uri resultUri = UCrop.getOutput(result);
        resimuri = resultUri;
        if (resultUri != null) {
//            imageView.setImageURI(resultUri);

            findViewById(R.id.c_done).setVisibility(View.VISIBLE);

//            Glide
//                    .with(this)
//                    .load(new File(resultUri.getPath()))
//                    .placeholder(R.drawable.ic_microphone)
//                    .into(imageView);
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
            uCrop.start(ActivityNewRecord.this);
        }

    }

    public void setupFragment(UCrop uCrop) {
        fragment = uCrop.getFragment(uCrop.getIntent(this).getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment, UCropFragment.TAG)
                .commitAllowingStateLoss();
    }

    public void clickCameraButton(View view) {

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

    private void uploadRecord(String path){

        progressDialog.show();

        String pdfname = String.valueOf(Calendar.getInstance().getTimeInMillis());

//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(RetroInterface.VIDEOURL)
//                .addConverterFactory(ScalarsConverterFactory.create())
//                .build();

        //Create a file object using file path
        File file = new File(path);

//        if(!isFileLessThan2MB(file)) {
//            Toast.makeText(getApplicationContext(), "Dosya en fazla 2MB olmasıgerekiyor.", Toast.LENGTH_SHORT).show();
//            progressDialog.dismiss();
//            return;
//        }

        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("filename", file.getName(), requestBody);
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), pdfname);

        RetroInterface getResponse = ApiClient.getClient().create(RetroInterface.class);
        Call<String> call = getResponse.uploadImage(fileToUpload, filename, "1", 1, lat, lon);
        Log.d("assss","asss");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("mullllll", response.body().toString());
                try {
                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    jsonObject.toString().replace("\\\\","");

                    if (jsonObject.getString("status").equals("true")) {
                        Toast.makeText(getApplicationContext(), "SUCCCESSSSS", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "catchhhhh: " + e.getMessage() + " path: " + path, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("gttt", call.toString());
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "FAILURE: " + t.getMessage() + " path: " + path, Toast.LENGTH_SHORT).show();
                System.out.println("RETROFAIL: " + t.getMessage() + " ");
            }
        });

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

        VoiceObject v = new VoiceObject();
        v.setUser_id("1");
        v.setIs_public("1");
        v.setItem_lat("lat");
        v.setItem_long("long");
        v.setItem_record_url("item url");

        RetroInterface getResponse = ApiClient.getClient().create(RetroInterface.class);
        Call<ServerResponse> call = getResponse.uploadMulFile(fileToUpload1, fileToUpload2, "1", 1, lat, lon);

        call.enqueue(new Callback <ServerResponse> () {
            @Override
            public void onResponse(Call < ServerResponse > call, Response < ServerResponse > response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
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
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), fileName);

        VoiceObject v = new VoiceObject();
        v.setUser_id("1");
        v.setIs_public("1");
        v.setItem_lat("lat");
        v.setItem_long("long");
        v.setItem_record_url("item url");

        RetroInterface getResponse = ApiClient.getClient().create(RetroInterface.class);
        Call<ServerResponse> call = getResponse.uploadSingleFile(fileToUpload1, "1", 1, lat, lon);

        call.enqueue(new Callback <ServerResponse> () {
            @Override
            public void onResponse(Call < ServerResponse > call, Response < ServerResponse > response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void clickRemoveImage(View view) {
        findViewById(R.id.c_done).setVisibility(View.GONE);
        resimuri = null;
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
}