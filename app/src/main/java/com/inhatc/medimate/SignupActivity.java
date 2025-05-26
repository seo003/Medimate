package com.inhatc.medimate;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText editId, editPw, editEmail, editPhone;
    private Button btnRegister;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editId = findViewById(R.id.editId);
        editPw = findViewById(R.id.editPw);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        btnRegister = findViewById(R.id.btnRegister);

        dbHelper = new DBHelper(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = editId.getText().toString().trim();
                String userPw = editPw.getText().toString().trim();
                String userEmail = editEmail.getText().toString().trim();
                String userPhone = editPhone.getText().toString().trim();

                if (userId.isEmpty() || userPw.isEmpty() || userEmail.isEmpty() || userPhone.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 중복 아이디 체크
                if (dbHelper.isUserIdExists(userId)) {
                    Toast.makeText(SignupActivity.this, "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                // DB에 사용자 추가
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("user_pw", userPw);
                values.put("user_email", userEmail);
                values.put("user_ph", userPhone);

                long result = db.insert("users", null, values);
                db.close();

                if (result == -1) {
                    Toast.makeText(SignupActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignupActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, SigninActivity.class));
                    finish();
                }
            }
        });
    }
}
