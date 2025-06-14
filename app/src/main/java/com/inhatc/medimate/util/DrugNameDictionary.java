package com.inhatc.medimate.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class DrugNameDictionary {

    private static final Set<String> drugNames = new HashSet<>();
    private static final String TAG = "DrugNameDictionary";

    public static void initialize(Context context) {
        if (!drugNames.isEmpty()) return;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("clean_item_names.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    drugNames.add(line);
                    // ✅ 약어 보정도 자동으로 추가
                    String shortName = normalizeShortName(line);
                    if (shortName != null && !shortName.equals(line)) {
                        DrugNameCorrector.register(shortName, line);
//                        Log.d(TAG, "약어 보정 자동 등록: " + shortName + " → " + line);
                    }
                }
            }
            reader.close();
            Log.d(TAG, "총 약물 개수: " + drugNames.size());
        } catch (Exception e) {
            Log.e(TAG, "약물 사전 초기화 실패", e);
        }
    }

    public static boolean isKnownDrug(String name) {
        return drugNames.contains(name);
    }

    // 포함된 이름으로 대체 검색
    public static String findClosestMatch(String name) {
        for (String known : drugNames) {
            if (known.contains(name)) {
                Log.d(TAG, "🔎 포함 매칭 성공: " + name + " → " + known);
                return known;
            }
        }
        return null;
    }

    private static String normalizeShortName(String fullName) {
        if (fullName.contains("(")) {
            return fullName.substring(0, fullName.indexOf("(")).replaceAll("[\\s\\d]+", "").trim();
        }
        return fullName
                .replaceAll("정|캡슐|연질캡슐|정제|액|마이크로그램|밀리그람|오랄액|퍼|스프레이|이알서방정|이알서방캡슐|\\d+|mg|-", "")
                .trim();
    }
}