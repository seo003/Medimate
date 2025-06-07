package com.inhatc.medimate;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MedicationCheck extends AppCompatActivity {

    private EditText mediDate;
    private EditText mediTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_check);

        // EditText 참조
        mediDate = findViewById(R.id.mediDate);
        mediTime = findViewById(R.id.mediTime);

        // 현재 날짜 및 시간 설정
        setCurrentDateTime();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setCurrentDateTime() {
        // 한국 시간대 설정
        TimeZone seoulTimeZone = TimeZone.getTimeZone("Asia/Seoul");

        // 현재 시간 객체
        Date now = new Date();

        // 날짜 포맷
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        dateFormat.setTimeZone(seoulTimeZone);
        String currentDate = dateFormat.format(now);

        // 시간 포맷
        SimpleDateFormat timeFormat = new SimpleDateFormat("a h시 mm분", Locale.KOREA);
        timeFormat.setTimeZone(seoulTimeZone);
        String currentTime = timeFormat.format(now);

        mediDate.setText(currentDate);
        mediTime.setText(currentTime);

        Log.d("MedicationCheck", "현재시간: " + currentDate + " " + currentTime);
    }
}