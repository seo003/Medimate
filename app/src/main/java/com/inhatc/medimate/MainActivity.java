package com.inhatc.medimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.inhatc.medimate.util.DrugNameDictionary;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 약 이름 사전 초기화!!
        DrugNameDictionary.initialize(this);
        setContentView(R.layout.activity_main);

        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SharedPreferences 초기화 (로그아웃)
                SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear(); // 또는 editor.remove("user_id");
                editor.apply();

                // 로그인 화면으로 이동
                Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                startActivity(intent);
                finish(); // 현재 MainActivity 종료
            }
        });
    }
}
