package com.inhatc.medimate.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.inhatc.medimate.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            Intent intent;
            if (userId != -1) {
                // 자동 로그인 상태 → MainActivity로 이동
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // 로그인 필요 → SigninActivity로 이동
                intent = new Intent(SplashActivity.this, SigninActivity.class);
            }

            startActivity(intent);
            finish();
        }, 2000); // 2초 대기 후 실행
    }
}
