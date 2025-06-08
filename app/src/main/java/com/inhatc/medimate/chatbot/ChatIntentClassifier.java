package com.inhatc.medimate.chatbot;

import android.util.Log;

import com.inhatc.medimate.util.DrugNameCorrector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatIntentClassifier {

    private static final String TAG = "Chatbot";

    public enum IntentType {
        DRUG_INFO,
        SIDE_EFFECT,
        USAGE,
        STORAGE,
        INTERACTION,
        UNKNOWN
    }

    /** 메인 Intent 분류 */
    public static IntentType classifyIntent(String message) {
        message = message.trim();

        if (Pattern.compile("효능|효과|뭐에 좋아|무슨 약").matcher(message).find()) {
            Log.d(TAG, "[Classify] 효능/기본 정보");
            return IntentType.DRUG_INFO;
        } else if (Pattern.compile("부작용|안 좋은 점|위험|조심").matcher(message).find()) {
            Log.d(TAG, "[Classify] 부작용");
            return IntentType.SIDE_EFFECT;
        } else if (Pattern.compile("복용법|어떻게 먹|용량|몇 번").matcher(message).find()) {
            Log.d(TAG, "[Classify] 복용법");
            return IntentType.USAGE;
        } else if (Pattern.compile("보관|어디에 둬|보관 방법").matcher(message).find()) {
            Log.d(TAG, "[Classify] 보관");
            return IntentType.STORAGE;
        } else if (Pattern.compile("같이 먹으면|병용|함께 복용").matcher(message).find()) {
            Log.d(TAG, "[Classify] 병용");
            return IntentType.INTERACTION;
        }

        Log.d(TAG, "[Classify] 알 수 없음");
        return IntentType.UNKNOWN;
    }

    /** 약 이름 추출 및 보정 */
    public static List<String> extractDrugNames(String sentence) {
        List<String> result = new ArrayList<>();

        // 전처리: 특수문자 제거 및 소문자화
        sentence = sentence.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ").toLowerCase();
        sentence = sentence.replaceAll("\\s+", " ");

        // 조사 제거 및 보정
        String[] tokens = sentence.split(" ");
        for (String token : tokens) {
            token = token.replaceAll("(은|는|이|가|을|를|에|의|으로|로|과|와|도|만|보다|처럼|께서|한테|에서|까지|부터|든지|라도|마저|조차|이나|나|야|이나마|이든지)$", "");

            String corrected = DrugNameCorrector.correct(token);
            Log.d(TAG, "[extractDrugNames] 원본: " + token + " → 보정: " + corrected);

            if (corrected != null && !result.contains(corrected)) {
                result.add(corrected);
                Log.d(TAG, "[extractDrugNames] ✅ 정확히 일치: " + corrected);
            }
        }

        Log.d(TAG, "[extractDrugNames] 최종 결과 리스트: " + result);
        return result;
    }

    /** 단일 태그 추출 (특정 XML 태그 요청 시 사용) */
    public static String extractTagFromMessage(String message) {
        message = message.toLowerCase();

        if (message.contains("효능") || message.contains("효과") || message.contains("좋아")) return "efcyQesitm";
        if (message.contains("복용법") || message.contains("용량") || message.contains("어떻게")) return "useMethodQesitm";
        if (message.contains("부작용") || message.contains("이상반응")) return "seQesitm";
        if (message.contains("주의") || message.contains("조심")) return "atpnQesitm";
        if (message.contains("음식") || message.contains("같이") || message.contains("병용") || message.contains("함께")) return "intrcQesitm";
        if (message.contains("보관")) return "depositMethodQesitm";

        return null;
    }
}
