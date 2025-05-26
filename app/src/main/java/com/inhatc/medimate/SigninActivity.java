package com.inhatc.medimate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SigninActivity extends AppCompatActivity {

    private EditText editId, editPw;
    private Button btnSignin, btnSignup;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        editId = findViewById(R.id.editId);
        editPw = findViewById(R.id.editPw);
        btnSignin = findViewById(R.id.btnSignin);
        btnSignup = findViewById(R.id.btnSignup);

        dbHelper = new DBHelper(this);

        // 회원가입 화면으로 이동
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 처리
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = editId.getText().toString().trim();
                String userPw = editPw.getText().toString().trim();

                if (userId.isEmpty() || userPw.isEmpty()) {
                    Toast.makeText(SigninActivity.this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dbHelper.checkUserCredentials(userId, userPw)) {
                    Toast.makeText(SigninActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                    // 자동 로그인 정보 저장
                    getSharedPreferences("login", MODE_PRIVATE)
                            .edit()
                            .putString("user_id", userId)
                            .apply();

                    // 다음 화면으로 이동
                    Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(SigninActivity.this, "아이디 또는 비밀번호가 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
