package com.inhatc.medimate.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.inhatc.medimate.data.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AlarmScheduler {

    public static void rescheduleAllAlarms(Context context, int userId) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
            String currentTime = new SimpleDateFormat("HH:mm", Locale.KOREA).format(new Date());

            String query = "SELECT s.schedule_id, s.dose_time FROM medication_schedule s " +
                    "JOIN user_medication um ON s.medication_id = um.medication_id " +
                    "WHERE s.user_id = ? " +
                    "AND ? BETWEEN um.start_date AND um.end_date " +
                    "AND s.dose_time > ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId), today, currentTime});

            if (cursor != null && cursor.moveToFirst()) {
                Log.d("AlarmScheduler", "총 " + cursor.getCount() + "개의 유효한 스케줄에 대해 알람을 설정합니다.");
                do {
                    int scheduleId = cursor.getInt(cursor.getColumnIndexOrThrow("schedule_id"));
                    String doseTime = cursor.getString(cursor.getColumnIndexOrThrow("dose_time"));
                    setAlarmForSchedule(context, scheduleId, doseTime);
                    count++;
                } while (cursor.moveToNext());
            } else {
                Log.d("AlarmScheduler", "알람을 설정할 (남아있는) 스케줄이 없습니다.");
            }
        } catch (Exception e) {
            Log.e("AlarmScheduler", "전체 알람 설정 중 오류 발생", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d("AlarmScheduler", "총 " + count + "개의 알람 설정 완료.");

    }


    public static void setAlarmForSchedule(Context context, int scheduleId, String doseTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(context, "정확한 알람 설정을 위해 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(intent);
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("schedule_id", scheduleId);

        intent.setAction("com.inhatc.medimate.ALARM_FOR_SCHEDULE_" + scheduleId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, scheduleId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String[] timeParts = doseTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        Log.d("AlarmScheduler", "알람 설정 완료: schedule_id=" + scheduleId + ", 시간=" + doseTime);
    }
}