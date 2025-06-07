package com.inhatc.medimate;

import android.content.Context;

import org.json.JSONObject;

import java.io.InputStream;

public class AwsKeyLoader {
    public static String[] loadKeys(Context context) {
        try {
            InputStream is = context.getAssets().open("aws_polly_key.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonStr = new String(buffer, "UTF-8");
            JSONObject json = new JSONObject(jsonStr);

            String accessKey = json.getString("accessKey");
            String secretKey = json.getString("secretKey");

            return new String[]{accessKey, secretKey};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
