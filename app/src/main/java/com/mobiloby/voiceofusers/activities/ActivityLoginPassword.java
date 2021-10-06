package com.mobiloby.voiceofusers.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.databinding.ActivityLoginPasswordBinding;
import com.mobiloby.voiceofusers.databinding.ActivityLoginPhoneBinding;
import com.mobiloby.voiceofusers.viewModel.ActivityLoginViewModel;

public class ActivityLoginPassword extends AppCompatActivity implements View.OnClickListener{

    ActivityLoginPasswordBinding binding;
    ActivityLoginViewModel viewModel;
    View view;
    Bundle extras;
    boolean isOldUser = false;
    String otp="", phone="";
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPasswordBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);
        binding.tContinue.setOnClickListener(this);
        viewModel = new ViewModelProvider(this).get(ActivityLoginViewModel.class);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        extras = getIntent().getExtras();
        isOldUser = extras.getBoolean("voiz_isOldUser");
        otp = extras.getString("voiz_otp");

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        phone = preferences.getString("voiz_phone", "");

        Toast.makeText(getApplicationContext(), "otp: " + otp, Toast.LENGTH_SHORT).show();
        System.out.println("OTP: " + otp);
        Log.d("OTPMESSAGE:", otp);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.t_continue:
                checkVerificationCode();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    private void checkVerificationCode() {

        if(binding.ePassword.getText().toString().equals(otp)){
            if(isOldUser){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
            else{
                Intent intent = new Intent(getApplicationContext(), ActivityLoginName.class);
                intent.putExtra("voiz_phone", phone);
                startActivity(intent);
            }
        }
        else{
            Toast.makeText(getApplicationContext(), R.string.message_error_password, Toast.LENGTH_SHORT).show();
        }
    }
}