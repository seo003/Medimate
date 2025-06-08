package com.inhatc.medimate.chatbot;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ChatApiService {

    private static final String BASE_URL = "https://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList";
    private static final String API_KEY = "";

    public static String getResponseByIntent(String message, ChatIntentClassifier.IntentType intent) {
        String drugName = ChatIntentClassifier.extractDrugNames(message).stream().findFirst().orElse(null);
        if (drugName == null) return "해당 약 정보를 찾을 수 없습니다.";

        switch (intent) {
            case DRUG_INFO:
                return getSingleTagValue(message, "efcyQesitm"); // 효능만 반환
            case SIDE_EFFECT:
                return getSingleTagValue(message, "seQesitm");
            case USAGE:
                return getSingleTagValue(message, "useMethodQesitm");
            case STORAGE:
                return getSingleTagValue(message, "depositMethodQesitm");
            case INTERACTION:
                return getSingleTagValue(message, "intrcQesitm");
            default:
                return "죄송해요. 질문을 잘 이해하지 못했어요.";
        }
    }

    public static String getSingleTagValue(String message, String tag) {
        String drugName = ChatIntentClassifier.extractDrugNames(message).stream().findFirst().orElse(null);
        if (drugName == null) return "해당 약 정보를 찾을 수 없습니다.";

        try {
            String urlStr = BASE_URL + "?serviceKey=" + API_KEY + "&itemName=" + URLEncoder.encode(drugName, "UTF-8") + "&type=xml";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream inputStream = conn.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() > 0) {
                Element item = (Element) items.item(0);
                String value = getTagValue(tag, item);
                return value.isEmpty() ? "해당 정보가 없습니다." : value;
            }
        } catch (Exception e) {
            Log.e("ChatApiService", "API 호출 실패", e);
        }

        return "해당 약 정보를 찾을 수 없습니다.";
    }

    public static String getFullDrugInfo(String drugName) {
        try {
            String urlStr = BASE_URL + "?serviceKey=" + API_KEY + "&itemName=" + URLEncoder.encode(drugName, "UTF-8") + "&type=xml";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream inputStream = conn.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() > 0) {
                Element item = (Element) items.item(0);

                StringBuilder sb = new StringBuilder();
                sb.append("📌 효능: ").append(getTagValue("efcyQesitm", item)).append("\n\n");
                sb.append("💊 복용법: ").append(getTagValue("useMethodQesitm", item)).append("\n\n");
                sb.append("⚠ 주의사항: ").append(getTagValue("atpnQesitm", item)).append("\n\n");
                sb.append("🚫 부작용: ").append(getTagValue("seQesitm", item)).append("\n\n");
                sb.append("🥗 병용주의: ").append(getTagValue("intrcQesitm", item)).append("\n\n");
                sb.append("📦 보관법: ").append(getTagValue("depositMethodQesitm", item));

                return sb.toString();
            }
        } catch (Exception e) {
            Log.e("ChatApiService", "API 전체 정보 조회 실패", e);
        }

        return "해당 약 정보를 찾을 수 없습니다.";
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0 && nodeList.item(0).getFirstChild() != null) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }
}
