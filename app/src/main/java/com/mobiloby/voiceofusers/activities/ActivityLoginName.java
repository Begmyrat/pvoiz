package com.mobiloby.voiceofusers.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;

import com.mobiloby.voiceofusers.R;
import com.mobiloby.voiceofusers.databinding.ActivityLoginNameBinding;
import com.mobiloby.voiceofusers.viewModel.ActivityLoginViewModel;

public class ActivityLoginName extends AppCompatActivity implements View.OnClickListener{

    View view;
    ActivityLoginNameBinding binding;
    ActivityLoginViewModel viewModel;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginNameBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        binding.tContinue.setOnClickListener(this);
        viewModel = new ViewModelProvider(this).get(ActivityLoginViewModel.class);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.t_continue:
                viewModel.setUsername(binding.eUsername.getText().toString());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("voiz_username", binding.eUsername.getText().toString());
                editor.commit();
                startActivity(new Intent(getApplicationContext(), ActivityLoginAvatar.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
    }
}