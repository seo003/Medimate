package com.inhatc.medimate;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "medimate.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 회원 테이블 생성
        db.execSQL("CREATE TABLE users (" +
                "user_id TEXT NOT NULL PRIMARY KEY, " +
                "user_pw TEXT NOT NULL, " +
                "user_email TEXT NOT NULL, " +
                "user_ph TEXT NOT NULL)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 테이블 변경 시 처리
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // 아이디 중복 체크
    public boolean isUserIdExists(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM users WHERE user_id = ?", new String[]{userId});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // 로그인 처리
    public boolean checkUserCredentials(String userId, String userPw) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE user_id = ? AND user_pw = ?", new String[]{userId, userPw});
        boolean valid = cursor.moveToFirst();
        cursor.close();
        db.close();
        return valid;
    }

}


