package com.inhatc.medimate.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.inhatc.medimate.R;
import com.inhatc.medimate.alarm.AlarmScheduler;
import com.inhatc.medimate.chatbot.ChatbotActivity;
import com.inhatc.medimate.util.DrugNameDictionary;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button btnAddMedi, btnLogout, btnCheckMyMedi, btnChatbot;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 사용자에게 왜 권한이 필요한지 설명하는 것이 좋습니다.
                    Toast.makeText(this, "알림 권한이 거부되어 복약 알림을 받을 수 없습니다.", Toast.LENGTH_LONG).show();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 약 이름 사전 초기화!!
        DrugNameDictionary.initialize(this);
        setContentView(R.layout.activity_main);

        btnAddMedi = findViewById(R.id.btnAddMedi);
        btnCheckMyMedi = findViewById(R.id.btnCheckMyMedi);
        btnLogout = findViewById(R.id.btnLogout);
        btnChatbot = findViewById(R.id.btnChatbot);

        btnAddMedi.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMediActivity.class);
            startActivity(intent);
        });

        btnCheckMyMedi.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CheckMyMedi.class);
            startActivity(intent);
        });

        btnChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
            pref.edit().clear().apply();

            Intent intent = new Intent(MainActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();
        });

        askNotificationPermission();

        SharedPreferences prefs = getSharedPreferences("login", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId != -1) {
            Log.d("MainActivity", "저장된 사용자 ID(" + currentUserId + ")의 알람을 설정합니다.");
            AlarmScheduler.rescheduleAllAlarms(this, currentUserId);

        } else {
            Log.e("MainActivity", "오류: 로그인 ID를 찾을 수 없습니다.");
        }
    }

    // 알람 권한 확인
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}
