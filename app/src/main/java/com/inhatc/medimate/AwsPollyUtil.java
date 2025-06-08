package com.inhatc.medimate;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class AwsPollyUtil {
    private static AmazonPollyPresigningClient pollyPresigningClient = null;
    private static MediaPlayer mediaPlayer = null;
    public static String[] loadKeys(Context context) {
        try {
            InputStream is = context.getAssets().open("api_key.env");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String accessKey = null;
            String secretKey = null;
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue; // 주석이나 빈 줄 무시
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if (key.equalsIgnoreCase("POLLY_ACCESS_KEY")) {
                        accessKey = value;
                    } else if (key.equalsIgnoreCase("POLLY_SECRET_KEY")) {
                        secretKey = value;
                    }
                }
            }

            reader.close();

            if (accessKey != null && secretKey != null) {
                return new String[]{accessKey, secretKey};
            } else {
                // 키를 찾지 못한 경우
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static void initPolly(Context context) {
        if (pollyPresigningClient != null) return;

        String[] keys = loadKeys(context);
        if (keys == null) {
            Log.e("AwsPollyUtil", "AWS 키를 불러오지 못했습니다.");
            return;
        }

        SimpleAWSCredentialsProvider credentialsProvider = new SimpleAWSCredentialsProvider(keys[0], keys[1]);
        pollyPresigningClient = new AmazonPollyPresigningClient(credentialsProvider);
        pollyPresigningClient.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
    }

//    AwsPollyUtil.speakText(this, "안녕하세요");
    public static void speakText(Context context, String text) {

        if (pollyPresigningClient == null) {
            initPolly(context);
            if (pollyPresigningClient == null) {
                Log.e("AwsPollyUtil", "Polly 클라이언트 초기화 실패");
                return;
            }
        }

        try {
            // 기존 MediaPlayer가 있으면 해제
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            SynthesizeSpeechPresignRequest synthReq = new SynthesizeSpeechPresignRequest()
                    .withText(text)
                    .withVoiceId("Seoyeon")
                    .withOutputFormat(OutputFormat.Mp3);

            URL presignedSynthesizeSpeechUrl = pollyPresigningClient.getPresignedSynthesizeSpeechUrl(synthReq);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
            mediaPlayer.prepare();
            mediaPlayer.start();

            // 재생 완료 후 해제
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.stop();
                mp.reset();
                mp.release();
                mediaPlayer = null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("AwsPollyUtil", "TTS 실패: " + e.getMessage());
        }
    }
}
