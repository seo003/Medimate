package com.inhatc.medimate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    private EditText etLoginId, etPassword, etName, etPhone, etGuardianPhone;
    private TextView tvBirthDate;
    private Spinner spGender;
    private Button btnSignUp;

    private DBHelper dbHelper;
    private String selectedBirthDate = "";
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DBHelper(this);

        etLoginId = findViewById(R.id.etLoginId);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etGuardianPhone = findViewById(R.id.etGuardianPhone);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        spGender = findViewById(R.id.spGender);
        btnSignUp = findViewById(R.id.btnSignUp);

        // 성별 스피너 초기화
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"남자", "여자"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        // 생년월일 선택
        tvBirthDate.setOnClickListener(v -> {
            com.inhatc.medimate.DatePickerFragment fragment = new com.inhatc.medimate.DatePickerFragment(date -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                selectedBirthDate = sdf.format(date);
                tvBirthDate.setText(selectedBirthDate);
            });
            fragment.show(getSupportFragmentManager(), "datePicker");
        });

        // 회원가입 버튼 클릭
        btnSignUp.setOnClickListener(v -> {
            String loginId = etLoginId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String guardianPhone = etGuardianPhone.getText().toString().trim();
            selectedGender = spGender.getSelectedItem().toString();

            if (TextUtils.isEmpty(loginId) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)
                    || TextUtils.isEmpty(selectedBirthDate) || TextUtils.isEmpty(selectedGender) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.isUserIdExists(loginId)) {
                Toast.makeText(this, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean result = dbHelper.registerFullUser(
                    loginId, password, name, selectedBirthDate, selectedGender, phone, guardianPhone
            );

            if (result) {
                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignupActivity.this, SigninActivity.class));
                finish();
            } else {
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
