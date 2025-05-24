package com.inhatc.medimate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 2초 후 SigninActivity로 전환
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();
        }, 2000); // 2000ms = 2초
    }
}
