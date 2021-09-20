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
import android.widget.EditText;
import android.widget.Toast;

import com.mobiloby.voiceofusers.databinding.ActivityLoginBinding;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.network.RetroInterface;
import com.mobiloby.voiceofusers.viewModel.ActivityLoginViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener{

    ActivityLoginBinding binding;
    ActivityLoginViewModel viewModel;
    View view;
    ProgressDialog progressDialog;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        prepareMe();
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

        binding.tSignIn.setOnClickListener(this);
        binding.tSignUp.setOnClickListener(this);
        binding.tForgotPassword.setOnClickListener(this);

        observe();
    }

    private void observe() {
        viewModel.getIsLoginError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(getApplicationContext(), "Kullanıcı adı veya şifre hatalı. Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
                }
                else{
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username", binding.eUsername.getText().toString());
                    editor.commit();

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });

        viewModel.getIsLoginLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    progressDialog.show();
                }
                else{
                    progressDialog.dismiss();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.t_signIn:
//                viewModel.loginUser(binding.eUsername.getText().toString(), binding.ePassword.getText().toString());
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                break;
            case R.id.t_signUp:
                startActivity(new Intent(getApplicationContext(), ActivityRegister.class));
                finish();
                break;
            case R.id.t_forgotPassword:
                break;
        }
    }
}