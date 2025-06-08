package com.inhatc.medimate.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DrugLookupUtil {

    private static final String BASE_URL = "https://apis.data.go.kr/1471000/DrugInfoService/getDrugInfoList";
    private static final String API_KEY = "";

    public static String getIngredientCodeByName(String drugName) {
        try {
            String encoded = URLEncoder.encode(drugName, "UTF-8");
            String urlStr = BASE_URL + "?serviceKey=" + API_KEY + "&itemName=" + encoded + "&type=xml";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream stream = conn.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(stream);

            NodeList items = doc.getElementsByTagName("item");
            if (items.getLength() > 0) {
                Element item = (Element) items.item(0);
                return getTagValue("INGR_CODE", item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag);
        if (nodes.getLength() > 0 && nodes.item(0).getFirstChild() != null) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }
}
