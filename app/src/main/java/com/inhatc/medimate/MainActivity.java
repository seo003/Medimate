package com.inhatc.medimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnAddMedi, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
