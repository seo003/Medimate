package com.inhatc.medimate.api;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DurApiService {

    private static final String BASE_URL = "https://apis.data.go.kr/1471000/DURPrdlstInfoService04/getUsjntTabooInfoList";
    private static final String API_KEY = " ";

    public static boolean checkInteraction(String code1, String code2) {
        try {
            String urlStr = BASE_URL + "?serviceKey=" + API_KEY +
                    "&ingrCode1=" + code1 + "&ingrCode2=" + code2 + "&type=xml";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream stream = conn.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(stream);

            NodeList nodes = doc.getElementsByTagName("item");
            return nodes.getLength() > 0; // 항목 존재 시 병용금기 있음
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
