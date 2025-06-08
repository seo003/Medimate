package com.inhatc.medimate.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DurApiConstants {
    public static final String BASE_URL = "https://apis.data.go.kr/1471000/DURPrdlstInfoService03";
    public static final String INTERACTION_ENDPOINT = "/getUsjntTabooInfoList03";
    public static final String ITEM_INFO_ENDPOINT = "/getDurPrdlstInfoList03";
    public static final String SERVICE_KEY = " ";

    public static String getIngredientCode(String drugName) {
        try {
            String encodedName = URLEncoder.encode(drugName, "UTF-8");
            String urlStr = BASE_URL + ITEM_INFO_ENDPOINT +
                    "?serviceKey=" + SERVICE_KEY +
                    "&type=xml" +
                    "&itemName=" + encodedName;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            InputStream inputStream = conn.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() > 0) {
                Element item = (Element) items.item(0);
                String ingrCode = getTagValue("INGR_CODE", item);
                return ingrCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0 && nodeList.item(0).getFirstChild() != null) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }
}
