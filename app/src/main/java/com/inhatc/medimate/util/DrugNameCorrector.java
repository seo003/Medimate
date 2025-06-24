package com.inhatc.medimate.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DrugNameCorrector {

    private static final String TAG = "DrugNameCorrector";
    private static final Map<String, String> corrections = new HashMap<>();
    private static final Set<String> knownDrugNames = new HashSet<>();

    // 수동 보정값 (optional)
    static {
        corrections.put("타이레놀", "타이레놀정500밀리그람");
        corrections.put("게보린", "게보린정");
        corrections.put("아스피린", "경동아스피린장용정");
        corrections.put("펜잘", "펜잘큐정");
        corrections.put("판콜", "판콜아이시럽");
        corrections.put("비타민씨", "유한비타민씨디정");
        corrections.put("써스펜", "써스펜나이트시럽");
    }

    public static void register(String shortName, String fullName) {
        corrections.put(shortName, fullName);
//        Log.d(TAG, "[register] 수동 등록: " + shortName + " → " + fullName);
    }
    public static void initialize(Context context) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("clean_item_names.txt"))
            );

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    knownDrugNames.add(trimmed);
                }
            }

            Log.d(TAG, "✅ 총 " + knownDrugNames.size() + "개의 약품명 로딩 완료");
        } catch (Exception e) {
            Log.e(TAG, "❌ 약품명 로딩 실패", e);
        }
    }

    public static String correct(String input) {
        String normalized = normalizeName(input);

        // 1. 수동 보정 우선
        if (corrections.containsKey(normalized)) {
            String corrected = corrections.get(normalized);
            Log.d(TAG, "[correct] 수동 보정: " + input + " → " + corrected);
            return corrected;
        }

        // 2. 정규 등록 이름 그대로 존재
        if (knownDrugNames.contains(normalized)) {
            Log.d(TAG, "[correct] 정규 등록 이름 사용: " + normalized);
            return normalized;
        }

        // 3. clean_item_names에서 유사한 항목 찾기
        String matched = findMatchingKnownDrug(normalized);
        if (matched != null) {
            Log.d(TAG, "[correct] 유사한 이름 매칭: " + input + " → " + matched);
            return matched;
        }

        Log.d(TAG, "[correct] ❌ 보정 실패 또는 미등록: " + input);
        return null;
    }

    private static String normalizeName(String name) {
        return name
                .replaceAll("(은|는|이|가|을|를|에|의|으로|로|랑|과|와|도|만)$", "")
                .toLowerCase()
                .trim();
    }

    public static String findMatchingKnownDrug(String normalized) {
        String bestMatch = null;
        for (String candidate : knownDrugNames) {
            if (candidate.contains(normalized)) {
                if (bestMatch == null || candidate.length() < bestMatch.length()) {
                    bestMatch = candidate;
                }
            }
        }
        Log.d(TAG, "[findMatchingKnownDrug] " + (bestMatch != null ? "✅ 매칭: " + bestMatch : "❌ 일치 없음: " + normalized));
        return bestMatch;
    }

    public static boolean isValid(String name) {
        return knownDrugNames.contains(name);
    }
}
