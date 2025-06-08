package com.inhatc.medimate.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DrugInteractionChecker {

    /**
     * 두 DUR 성분코드(INGR_CODE)를 기반으로 병용금기 여부를 확인한다.
     * @param ingrCodeA 성분 A 코드
     * @param ingrCodeB 성분 B 코드
     * @return 금기일 경우 true, 문제 없으면 false
     */
    public static boolean check(String ingrCodeA, String ingrCodeB) {
        try {
            String serviceKey = " "; // 인코딩된키
            String urlStr = "https://apis.data.go.kr/1471000/DURPrdlstInfoService03/getDurInteractionInfoList03";

            urlStr += "?serviceKey=" + serviceKey;
            urlStr += "&type=json";
            urlStr += "&itemName1=" + URLEncoder.encode(ingrCodeA, "UTF-8");
            urlStr += "&itemName2=" + URLEncoder.encode(ingrCodeB, "UTF-8");

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            String result = responseBuilder.toString();
            return result.contains("금기") || result.contains("병용주의") || result.contains("병용금기");

        } catch (Exception e) {
            e.printStackTrace();
            return false; // 실패 시 기본 false
        }
    }
}
