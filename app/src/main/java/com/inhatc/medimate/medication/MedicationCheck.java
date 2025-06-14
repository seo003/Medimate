package com.inhatc.medimate.medication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.inhatc.medimate.R;
import com.inhatc.medimate.data.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MedicationCheck extends AppCompatActivity {

    private EditText mediDate, mediTime;
    private Button btnCheckMedication;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_check);

        dbHelper = new DBHelper(this);
        mediDate = findViewById(R.id.mediDate);
        mediTime = findViewById(R.id.mediTime);
        btnCheckMedication = findViewById(R.id.btnCheckMedication);

        // 현재 날짜 및 시간 설정
        setCurrentDateTime();

        // '복용확인' 버튼 클릭 리스너
        btnCheckMedication.setOnClickListener(v -> {
            recordMedicationLog();
        });

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

    // 복용 기록 저장
    private void recordMedicationLog() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        SharedPreferences prefs = getSharedPreferences("login", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            Log.e("MedicationCheck", "로그인된 사용자 ID를 찾을 수 없어 복용 기록을 저장할 수 없습니다.");
            Toast.makeText(this, "로그인 정보가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
        String takenDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date());
        String currentTimeForQuery = new SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(new Date());

        String query = "SELECT s.schedule_id, s.medication_id, s.dose_time FROM medication_schedule s " +
                "JOIN user_medication um ON s.medication_id = um.medication_id " +
                "WHERE s.user_id = ? " +
                "AND ? BETWEEN um.start_date AND um.end_date " +
                "AND abs(strftime('%s', s.dose_time) - strftime('%s', ?)) <= 3600";

        Cursor cursor = null;
        int savedCount = 0;
        try {
            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(currentUserId),
                    currentDate,
                    currentTimeForQuery
            });

            if (cursor != null && cursor.moveToFirst()) {
                Log.d("MedicationCheck", "쿼리 결과: " + cursor.getCount() + "개의 일치하는 스케줄을 찾았습니다.");
                do {
                    int scheduleId = cursor.getInt(cursor.getColumnIndexOrThrow("schedule_id"));
                    int medicationId = cursor.getInt(cursor.getColumnIndexOrThrow("medication_id"));
                    String scheduledTime = cursor.getString(cursor.getColumnIndexOrThrow("dose_time"));
                    String plannedDateTime = currentDate + " " + scheduledTime + ":00";

                    ContentValues logValues = new ContentValues();
                    logValues.put("medication_id", medicationId);
                    logValues.put("schedule_id", scheduleId);
                    logValues.put("planned_datetime", plannedDateTime);
                    logValues.put("taken_datetime", takenDateTime);
                    logValues.put("taken_flag", 1);

                    long logId = db.insert("medication_log", null, logValues);

                    if (logId != -1) {
                        savedCount++;
                        Log.d("MedicationCheck", "로그 저장 성공! (log_id=" + logId + ", 예정시간=" + plannedDateTime + ")");
                    }

                } while (cursor.moveToNext());

                Toast.makeText(this, savedCount + "건의 복용 기록이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                dbHelper.logAllTablesData("DB_DEBUG");

            } else {
                Log.w("MedicationCheck", "사용자(" + currentUserId + ")의 현재 시간 근처(+-1시간)에 맞는 스케줄 없음");
                Toast.makeText(this, "현재 시간에 맞는 복용 스케줄이 없습니다.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("MedicationCheck", "복용 기록 저장 중 오류 발생", e);
            Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}