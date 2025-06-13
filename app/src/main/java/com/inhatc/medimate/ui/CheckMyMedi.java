package com.inhatc.medimate.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.medimate.R;
import com.inhatc.medimate.data.DBHelper;
import com.inhatc.medimate.medication.MedicationItem;

import java.util.List;

public class CheckMyMedi extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private DBHelper dbHelper;
    private List<MedicationItem> medicationItems;
    private static final String TAG = "CheckMyMedi";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_my_medi);

        // DBHelper 초기화
        dbHelper = new DBHelper(this);

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 로그인한 사용자 ID 가져오기 (로그인 시 SharedPreferences에 저장했다고 가정)
        SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
        int userId = pref.getInt("user_id", -1);

        Log.d(TAG, "현재 로그인된 사용자 ID: " + userId);

        if (userId != -1) {
            // DB에서 약물 목록 데이터 로드
            medicationItems = dbHelper.getMedicationListForUser(userId);

            // --- 2. DB에서 가져온 아이템 개수 확인 ---
            Log.d(TAG, "DB에서 가져온 약물 아이템 개수: " + medicationItems.size());

            // --- 3. 가져온 데이터 내용 직접 확인 (아이템이 있을 경우) ---
            if (!medicationItems.isEmpty()) {
                for (MedicationItem item : medicationItems) {
                    Log.d(TAG, "약 이름: " + item.getDrugName() + ", 스케줄: " + item.getSchedules());
                }
            } else {
                Log.w(TAG, "표시할 약물 데이터가 없습니다.");
            }
            // --- 2. DB에서 가져온 아이템 개수 확인 ---
            Log.d(TAG, "DB에서 가져온 약물 아이템 개수: " + medicationItems.size());

            // --- 3. 가져온 데이터 내용 직접 확인 (아이템이 있을 경우) ---
            if (!medicationItems.isEmpty()) {
                for (MedicationItem item : medicationItems) {
                    Log.d(TAG, "약 이름: " + item.getDrugName() + ", 스케줄: " + item.getSchedules());
                }
            } else {
                Log.w(TAG, "표시할 약물 데이터가 없습니다.");
            }


            // 어댑터 생성 및 RecyclerView에 연결
            adapter = new MedicationAdapter(medicationItems);
            recyclerView.setAdapter(adapter);
        } else {
            // 사용자 ID가 없는 경우 처리 (예: 로그인 화면으로 이동)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // DBHelper 닫기
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}