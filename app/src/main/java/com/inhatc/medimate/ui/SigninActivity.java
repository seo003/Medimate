package com.inhatc.medimate.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.inhatc.medimate.R;
import com.inhatc.medimate.data.DBHelper;

public class SigninActivity extends AppCompatActivity {

    private EditText editId, editPw;
    private DBHelper dbHelper;
    private Button btnSignin, btnSignup;  // btnSignup 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        editId = findViewById(R.id.editId);
        editPw = findViewById(R.id.editPw);
        btnSignin = findViewById(R.id.btnSignin);
        btnSignup = findViewById(R.id.btnSignup);  // 연결 추가

        dbHelper = new DBHelper(this);

        btnSignin.setOnClickListener(v -> {
            String loginId = editId.getText().toString();
            String password = editPw.getText().toString();

            int userId = dbHelper.checkUserCredentials(loginId, password);
            if (userId != -1) {
                SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
                prefs.edit().putInt("user_id", userId).apply();

                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show();
            }
        });

        // 👇 회원가입 버튼 클릭 시 SignupActivity로 이동
        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

}
