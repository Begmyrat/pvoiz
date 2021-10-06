package com.mobiloby.voiceofusers.activities;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.desmond.squarecamera.CameraActivity;
import com.mobiloby.voiceofusers.BuildConfig;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.databinding.ActivityLoginAvatarBinding;
import com.mobiloby.voiceofusers.helpers.AppConstants;
import com.mobiloby.voiceofusers.models.ServerResponse;
import com.mobiloby.voiceofusers.models.VoiceObject;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;
import com.mobiloby.voiceofusers.viewModel.ActivityLoginViewModel;
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
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLoginAvatar extends AppCompatActivity implements View.OnClickListener{

    View view;
    ActivityLoginAvatarBinding binding;
    ActivityLoginViewModel viewModel;
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
    ProgressDialog progressDialog;
    String phone="", name="";
    RetroInterface getResponse;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginAvatarBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        viewModel = new ViewModelProvider(this).get(ActivityLoginViewModel.class);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        name = preferences.getString("voiz_username", "");
        phone = preferences.getString("voiz_phone", "");

        binding.tContinue.setOnClickListener(this);
        binding.tTryLater.setOnClickListener(this);
        binding.iCameraIconFill.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Filter");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);

        observe();
    }

    private void observe() {
        viewModel.getIsLoginLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getIsLoginError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(getApplicationContext(), "Error, try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getLoginResponse().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer==1){
                    Toast.makeText(getApplicationContext(), "success insert", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "error insert", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.t_continue:
//                viewModel.insertUser(viewModel.getPhoneNumber().getValue(), viewModel.getUsername().getValue(), "img_url");
                sendUserWithPhoto();
                break;
            case R.id.t_tryLater:
                insertUser();
                break;
            case R.id.i_cameraIconFill:
                clickCameraButton();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
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
                    .placeholder(R.drawable.voizlogo_record)
                    .circleCrop()
                    .into(binding.iAvatar);
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
            uCrop.start(ActivityLoginAvatar.this);
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
//
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

    private void uploadMultiFile(String pathImage){

        progressDialog.show();

        String fileName = String.valueOf(Calendar.getInstance().getTimeInMillis());

        //Create a file object using file path
        File file2 = new File(pathImage);

        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file2);

        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file1", file2.getName(), requestBody);

        RetroInterface getResponse = ApiClient.getClient().create(RetroInterface.class);
        Call<ServerResponse> call = getResponse.uploadUserWithPhoto(fileToUpload, phone, name.toLowerCase());

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call < ServerResponse > call, Response< ServerResponse > response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("voiz_user_id", serverResponse.getUser_id());
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
                Toast.makeText(getApplicationContext(), "FAILED : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }

    public void sendUserWithPhoto(){
        String pathImage = getFilePathFromURI(getApplicationContext(), resimuri, ".png");
        uploadMultiFile(pathImage);
    }

    public void insertUser() {

        getResponse = ApiClient.getClient().create(RetroInterface.class);

        Map<String, String> param = new HashMap();
        param.put("user_phone", phone);
        param.put("user_name", name.toLowerCase());
        param.put("user_image_url", "");

        Call<ServerResponse> call2 = getResponse.insertUser(param);

        call2.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if(response.isSuccessful()){
                    ServerResponse serverResponse = response.body();

                    if(serverResponse!=null){
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("voiz_user_id", serverResponse.getUser_id());
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Login error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Login error", Toast.LENGTH_SHORT).show();
            }

        });
    }

}