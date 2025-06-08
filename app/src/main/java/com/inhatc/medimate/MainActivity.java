package com.inhatc.medimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.inhatc.medimate.util.DrugNameDictionary;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnAddMedi, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 약 이름 사전 초기화!!
        DrugNameDictionary.initialize(this);
        setContentView(R.layout.activity_main);

        btnAddMedi = findViewById(R.id.btnAddMedi);
        btnLogout = findViewById(R.id.btnLogout);

        btnAddMedi.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMediActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
            pref.edit().clear().apply();

            Intent intent = new Intent(MainActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
