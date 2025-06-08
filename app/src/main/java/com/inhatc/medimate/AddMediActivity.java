package com.inhatc.medimate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddMediActivity extends AppCompatActivity {

    // api key, url 파일 따로 만들어 백업
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String CLOVA_URL = " ";
    private static final String CLOVA_KEY = " ";
    private static final String GPT_KEY = " ";

    private ImageView imagePreview;
    private TextView txtResult;
    private Button btnAnalyze;

    private Uri selectedImageUri = null;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medi);

        imagePreview = findViewById(R.id.imagePreview);
        txtResult = findViewById(R.id.txtResult);
        btnAnalyze = findViewById(R.id.btnAnalyze);

        imagePreview.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
        });

        btnAnalyze.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                txtResult.setText("이미지를 먼저 선택하세요.");
                return;
            }
            txtResult.setText("분석 중...");
            executor.execute(this::processImage);
        });
    }

    private void processImage() {
        try {
            byte[] imageData = readBytesFromUri(selectedImageUri);

            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("message", "{\"images\":[{\"format\":\"jpg\",\"name\":\"image\"}],\"requestId\":\"1234\",\"version\":\"V2\",\"timestamp\":1234567890}")
                    .addFormDataPart("file", "image.jpg", RequestBody.create(imageData, MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url(CLOVA_URL)
                    .addHeader("X-OCR-SECRET", CLOVA_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> txtResult.setText("OCR 실패: " + e.getMessage()));
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> txtResult.setText("OCR 응답 오류"));
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray fields = json.getJSONArray("images").getJSONObject(0).getJSONArray("fields");
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < fields.length(); i++) {
                            sb.append(fields.getJSONObject(i).getString("inferText")).append(" ");
                        }
                        callGpt(sb.toString());
                    } catch (Exception e) {
                        runOnUiThread(() -> txtResult.setText("JSON 파싱 오류: " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            runOnUiThread(() -> txtResult.setText("오류: " + e.getMessage()));
        }
    }

    private void callGpt(String ocrText) {
        try {
            JSONObject json = new JSONObject();
            json.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content",
                    "당신은 한국어 처방전(약제비 계산서/영수증)에서 복약 정보를 정확히 추출하는 전문가입니다.\n" +
                            "주어진 텍스트에서 다음 JSON 구조로 반환하세요:\n" +
                            "{\n" +
                            "  \"조제일자\": \"YYYY-MM-DD\",\n" +
                            "  \"약품목록\": [\n" +
                            "    { \"약 이름\": \"로프민캡슐\", \"투약량(1회)\": \"2.0정\", \"횟수\": \"3회\", \"기간\": \"4일분\" },\n" +
                            "    { \"약 이름\": \"메디솔론정\", \"투약량(1회)\": \"0.5정\", \"횟수\": \"3회\", \"기간\": \"5일분\" }\n" +
                            "  ]\n" +
                            "}\n\n" +
                            "- **투약량(1회)** 필드는 “1정씩”, “0.5정씩” 등 처방전의 **복약안내**(용법) 부분에서 뽑아야 합니다.\n" +
                            "- ( ) 안에 붙은 약제의 표준 강도(예: 10mg, 0.25mg)는 무시하세요.\n" +
                            "- JSON 키는 **조제일자**, **약품목록**, 그 안의 **약 이름**, **투약량(1회)**, **횟수**, **기간** 입니다."
            );

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", ocrText);

            messages.put(system);
            messages.put(user);
            json.put("messages", messages);

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + GPT_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> txtResult.setText("GPT 실패: " + e.getMessage()));
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject fullJson = new JSONObject(response.body().string());
                        String reply = fullJson.getJSONArray("choices").getJSONObject(0)
                                .getJSONObject("message").getString("content");

                        JSONObject resultJson = new JSONObject(reply);
                        StringBuilder sb = new StringBuilder();
                        sb.append("\uD83D\uDCC5 조제일자: ").append(resultJson.getString("조제일자")).append("\n\n");
                        sb.append("\uD83D\uDC8A 약품목록:\n");
                        JSONArray meds = resultJson.getJSONArray("약품목록");
                        for (int i = 0; i < meds.length(); i++) {
                            JSONObject item = meds.getJSONObject(i);
                            sb.append("\uD83D\uDD39 ").append(item.getString("약 이름"))
                                    .append(" – ").append(item.getString("투약량(1회)"))
                                    .append(", ").append(item.getString("횟수"))
                                    .append(", ").append(item.getString("기간"))
                                    .append("\n");
                        }
                        runOnUiThread(() -> txtResult.setText(sb.toString()));

                    } catch (Exception e) {
                        runOnUiThread(() -> txtResult.setText("GPT 파싱 오류: " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            runOnUiThread(() -> txtResult.setText("GPT 요청 오류: " + e.getMessage()));
        }
    }

    private byte[] readBytesFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);
        }
    }
}