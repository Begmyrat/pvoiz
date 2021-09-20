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

import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.databinding.ActivityLoginBinding;
import com.mobiloby.voiceofusers.databinding.ActivityRegisterBinding;
import com.mobiloby.voiceofusers.models.MobilobyResponse;
import com.mobiloby.voiceofusers.network.ApiClient;
import com.mobiloby.voiceofusers.network.RetroInterface;
import com.mobiloby.voiceofusers.viewModel.ActivityLoginViewModel;
import com.mobiloby.voiceofusers.viewModel.ActivityRegisterViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityRegister extends AppCompatActivity implements View.OnClickListener {

    ActivityRegisterBinding binding;
    ActivityRegisterViewModel viewModel;
    View view;
    ProgressDialog progressDialog;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        prepareMe();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
    }

    private void prepareMe() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        viewModel = new ViewModelProvider(this).get(ActivityRegisterViewModel.class);
        viewModel.init(getApplicationContext(), this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Your Voice");
        progressDialog.setMessage("İşleminiz gerçekleştiriliyor...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        binding.tSignIn.setOnClickListener(this);
        binding.tSignUp.setOnClickListener(this);

        observe();
    }

    private void observe() {
        viewModel.getIsRegisterError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    Toast.makeText(getApplicationContext(), "Bu kullanıcı adıyla başka bir kullanıcı mevcut. Lütfen başka kullanıcı adı seçiniz", Toast.LENGTH_SHORT).show();
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

        viewModel.getIsRegisterLoading().observe(this, new Observer<Boolean>() {
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
        
        viewModel.getIsRegisterValid().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    
                }
                else{
                    Toast.makeText(getApplicationContext(), "Kullanıcı adı ve şifre en az 5 karakterden oluşmalıdır.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getIsPasswordConfirmValid().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){

                }
                else{
                    Toast.makeText(getApplicationContext(), "Şifre ve şifre tekrarı farklı.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.t_signUp:
                viewModel.insertUser(binding.eUsername.getText().toString(), binding.ePassword.getText().toString(), binding.ePasswordConfirm.getText().toString());
                break;
            case R.id.t_signIn:
                startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                finish();
                break;
        }
    }
}