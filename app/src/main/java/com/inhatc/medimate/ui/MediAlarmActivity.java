package com.inhatc.medimate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.inhatc.medimate.R;
import com.inhatc.medimate.medication.MedicationCheck;

public class MediAlarmActivity extends AppCompatActivity {

    private Button btnMediAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medi_alarm);

        btnMediAlarm = findViewById(R.id.btnMediAlarm);

        btnMediAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(MediAlarmActivity.this, MedicationCheck.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}