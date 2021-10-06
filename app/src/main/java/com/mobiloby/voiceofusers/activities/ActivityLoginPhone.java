package com.mobiloby.voiceofusers.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.mobiloby.voiceofusers.databinding.ActivityLoginPhoneBinding;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.models.UserResponse;
import com.mobiloby.voiceofusers.viewModel.ActivityLoginViewModel;

import java.util.Random;

public class ActivityLoginPhone extends AppCompatActivity implements View.OnClickListener{

    ActivityLoginPhoneBinding binding;
    ActivityLoginViewModel viewModel;
    View view;
    ProgressDialog progressDialog;
    SharedPreferences preferences;
    Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        prepareMe();
        random = new Random();
    }

    private void prepareMe() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        viewModel = new ViewModelProvider(this).get(ActivityLoginViewModel.class);
        viewModel.init(getApplicationContext(), this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Your Voice");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        binding.tContinue.setOnClickListener(this);

        observe();
    }

    private void observe() {
        viewModel.getIsLoginError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(getApplicationContext(), R.string.message_try_again, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getIsLoginLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
//                    progressDialog.show();
                }
                else{
//                    progressDialog.dismiss();
                }
            }
        });

        viewModel.getUserResponse().observe(this, new Observer<UserResponse>() {
            @Override
            public void onChanged(UserResponse userResponse) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("voiz_phone", binding.ePhone.getText().toString());
                if(userResponse.getSuccess() == 1){
                    editor.putString("voiz_user_id", viewModel.getUserResponse().getValue().getPro()[0].getUser_id());
                }
                editor.commit();

                String otp = String.format("%04d", random.nextInt(10000));
                Intent intent = new Intent(getApplicationContext(), ActivityLoginPassword.class);
                intent.putExtra("voiz_isOldUser", userResponse.getSuccess()==1);
                intent.putExtra("voiz_otp",otp);
                startActivity(intent);
                finish();
            }
        });

//        viewModel.getUserResponse().observe(this, new Observer<Integer>() {
//            @Override
//            public void onChanged(Integer success) {
//                viewModel.setIsOldUser(success==1);
//
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putString("voiz_phone", binding.ePhone.getText().toString());
//                if(success == 1){
//                    editor.putString("voiz_user_id", viewModel.getUserResponse().getValue().getPro()[0].getUser_id());
//                }
//                editor.commit();
//
//                String otp = String.format("%04d", random.nextInt(10000));
//                Intent intent = new Intent(getApplicationContext(), ActivityLoginPassword.class);
//                intent.putExtra("voiz_isOldUser", success==1);
//                intent.putExtra("voiz_phone", viewModel.getPhoneNumber().getValue());
//                intent.putExtra("voiz_otp",otp);
//                startActivity(intent);
//                finish();
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.t_continue:
                viewModel.getUser(binding.ePhone.getText().toString());
                break;
        }
    }
}