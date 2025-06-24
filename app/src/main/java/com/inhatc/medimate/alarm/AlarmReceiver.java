package com.inhatc.medimate.alarm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.inhatc.medimate.ui.MediAlarmActivity;
import com.inhatc.medimate.util.AwsPollyUtil;

public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "알람을 수신했습니다!");

        // 1. TTS 실행
        AwsPollyUtil.speakText(context, "약 복용 시간입니다.");

        // 2. 알림 클릭 시 실행될 화면(Activity) 설정
        Intent notificationIntent = new Intent(context, MediAlarmActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 3. 푸시 알림 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationApp.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: 적절한 알림 아이콘으로 변경
                .setContentTitle("복약 시간입니다!")
                .setContentText("약을 복용하고 '복용확인' 버튼을 눌러주세요.")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 높은 우선순위
                .setContentIntent(pendingIntent) // 알림 클릭 시 이동
                .setAutoCancel(true); // 클릭하면 알림 자동 삭제

        // 4. 알림 표시
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 이 경우, 앱 시작 시 권한을 요청하는 로직이 반드시 필요합니다.
            Log.e("AlarmReceiver", "알림 권한이 없습니다.");
            return;
        }

        // 각 알림을 구분하기 위한 고유 ID
        int notificationId = intent.getIntExtra("schedule_id", (int) System.currentTimeMillis());
        notificationManager.notify(notificationId, builder.build());
    }
}
