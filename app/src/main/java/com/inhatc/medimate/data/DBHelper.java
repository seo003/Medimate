package com.inhatc.medimate.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.inhatc.medimate.medication.MedicationItem;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "medimate.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 사용자 테이블
        db.execSQL("CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "login_id TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "birth_date TEXT NOT NULL, " +
                "gender TEXT, " +
                "phone TEXT UNIQUE, " +
                "guardian_phone TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

        // 약물 정보 테이블
        db.execSQL("CREATE TABLE drug (" +
                "drug_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_name TEXT, " +
                "entp_name TEXT, " +
                "main_ingredient TEXT, " +
                "efficacy TEXT, " +
                "warning TEXT, " +
                "image_url TEXT, " +
                "item_seq TEXT UNIQUE)");

        // 사용자 약 복용 정보
        db.execSQL("CREATE TABLE user_medication (" +
                "medication_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "drug_id INTEGER NOT NULL, " +
                "daily_frequency INTEGER, " +
                "start_date TEXT NOT NULL, " +
                "end_date TEXT NOT NULL, " +
                "ocr_raw_text TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                "FOREIGN KEY(drug_id) REFERENCES drug(drug_id))");

        // 복약 스케줄
        db.execSQL("CREATE TABLE medication_schedule (" +
                "schedule_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medication_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "repeat_days TEXT DEFAULT 'daily', " +
                "dose_time TEXT NOT NULL, " +
                "memo TEXT, " +
                "FOREIGN KEY(medication_id) REFERENCES user_medication(medication_id), " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id))");

        // 복약 로그
        db.execSQL("CREATE TABLE medication_log (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medication_id INTEGER NOT NULL, " +
                "schedule_id INTEGER NOT NULL, " +
                "planned_datetime TEXT NOT NULL, " +
                "taken_datetime TEXT, " +
                "taken_flag INTEGER DEFAULT 0, " +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(medication_id) REFERENCES user_medication(medication_id), " +
                "FOREIGN KEY(schedule_id) REFERENCES medication_schedule(schedule_id))");

        // 성분 정보
        db.execSQL("CREATE TABLE ingredient (" +
                "ingredient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "ingredient_code TEXT UNIQUE)");

        // 약-성분 다대다
        db.execSQL("CREATE TABLE drug_ingredient (" +
                "drug_id INTEGER, " +
                "ingredient_id INTEGER, " +
                "PRIMARY KEY(drug_id, ingredient_id), " +
                "FOREIGN KEY(drug_id) REFERENCES drug(drug_id), " +
                "FOREIGN KEY(ingredient_id) REFERENCES ingredient(ingredient_id))");

        // 병용 금기 성분
        db.execSQL("CREATE TABLE drug_interaction (" +
                "interaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ingredient_1_id INTEGER, " +
                "ingredient_2_id INTEGER, " +
                "risk_level TEXT, " +
                "reason TEXT, " +
                "FOREIGN KEY(ingredient_1_id) REFERENCES ingredient(ingredient_id), " +
                "FOREIGN KEY(ingredient_2_id) REFERENCES ingredient(ingredient_id))");

        // 병원 방문 정보
        db.execSQL("CREATE TABLE hospital_visit (" +
                "visit_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "hospital_name TEXT, " +
                "department TEXT, " +
                "addr TEXT, " +
                "appointment_date TEXT, " +
                "visit_type TEXT, " +
                "diagnosis TEXT, " +
                "prescription TEXT, " +
                "memo TEXT, " +
                "tel TEXT, " +
                "x_pos TEXT, " +
                "y_pos TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 테이블 삭제 후 재생성 (테스트 용도)
        db.execSQL("DROP TABLE IF EXISTS hospital_visit");
        db.execSQL("DROP TABLE IF EXISTS drug_interaction");
        db.execSQL("DROP TABLE IF EXISTS drug_ingredient");
        db.execSQL("DROP TABLE IF EXISTS ingredient");
        db.execSQL("DROP TABLE IF EXISTS medication_log");
        db.execSQL("DROP TABLE IF EXISTS medication_schedule");
        db.execSQL("DROP TABLE IF EXISTS user_medication");
        db.execSQL("DROP TABLE IF EXISTS drug");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // 로그인 확인
    public int checkUserCredentials(String loginId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM users WHERE login_id = ? AND password = ?", new String[]{loginId, password});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return userId; // 로그인 성공 시 user_id, 실패 시 -1 반환
    }


    // 로그인 ID로 user_id 얻기
    public int getUserIdByLoginId(String loginId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM users WHERE login_id = ?", new String[]{loginId});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    // 아이디 중복 확인
    public boolean isUserIdExists(String loginId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT login_id FROM users WHERE login_id = ?", new String[]{loginId});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // 회원가입
    public boolean registerFullUser(String loginId, String password, String name,
                                    String birthDate, String gender, String phone, String guardianPhone) {
        if (isUserIdExists(loginId)) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("login_id", loginId);
        values.put("password", password);
        values.put("name", name);
        values.put("birth_date", birthDate);
        values.put("gender", gender);
        values.put("phone", phone);
        values.put("guardian_phone", guardianPhone);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    // 사용자의 약물 목록과 스케줄 가져오기
    public List<MedicationItem> getMedicationListForUser(int userId) {
        List<MedicationItem> medicationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 1. 사용자가 복용하는 약물 정보와 약의 이름을 JOIN으로 가져온다.
        String query = "SELECT um.medication_id, d.item_name, um.start_date, um.end_date " +
                "FROM user_medication um " +
                "JOIN drug d ON um.drug_id = d.drug_id " +
                "WHERE um.user_id = ? " +
                "ORDER BY um.start_date DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                // 2. 각 약물 정보(medication_id)에 해당하는 스케줄을 조회한다.
                int medicationId = cursor.getInt(0);
                String drugName = cursor.getString(1);
                String period = cursor.getString(2) + " ~ " + cursor.getString(3);

                Cursor scheduleCursor = db.rawQuery("SELECT dose_time, memo FROM medication_schedule WHERE medication_id = ?",
                        new String[]{String.valueOf(medicationId)});

                StringBuilder schedulesBuilder = new StringBuilder();
                if (scheduleCursor.moveToFirst()) {
                    do {
                        String time = scheduleCursor.getString(0);
                        String memo = scheduleCursor.getString(1);
                        schedulesBuilder.append(time).append(" - ").append(memo).append("\n");
                    } while (scheduleCursor.moveToNext());
                }
                scheduleCursor.close();

                // 마지막 줄바꿈 문자 제거
                if (schedulesBuilder.length() > 0) {
                    schedulesBuilder.setLength(schedulesBuilder.length() - 1);
                }

                // 3. 최종적으로 MedicationItem 객체를 만들어 리스트에 추가한다.
                medicationList.add(new MedicationItem(drugName, period, schedulesBuilder.toString()));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return medicationList;
    }
}
