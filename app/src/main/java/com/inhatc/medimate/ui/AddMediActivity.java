package com.inhatc.medimate.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inhatc.medimate.R;
import com.inhatc.medimate.data.DBHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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

    private static final int PICK_IMAGE_REQUEST = 1;

    private String CLOVA_URL;
    private String CLOVA_KEY;
    private String GPT_KEY;

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

        loadApiKeys();

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

    private void loadApiKeys() {
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("api_key.env");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("CLOVA_URL=")) {
                    CLOVA_URL = line.substring("CLOVA_URL=".length()).trim();
                } else if (line.startsWith("CLOVA_KEY=")) {
                    CLOVA_KEY = line.substring("CLOVA_KEY=".length()).trim();
                } else if (line.startsWith("GPT_KEY=")) {
                    GPT_KEY = line.substring("GPT_KEY=".length()).trim();
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                            "    { \"약 이름\": \"로프민캡슐\", \"투약량(1회)\": \"2.0정\", \"횟수\": \"3회\", \"기간\": \"4일분\" }\n" +
                            "  ]\n" +
                            "}\n" +
                            "- **투약량(1회)**는 복약안내에서 뽑고, JSON 키는 정확히 유지하세요."
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
                        sb.append("📅 조제일자: ").append(resultJson.getString("조제일자")).append("\n\n");
                        sb.append("💊 약품목록:\n");
                        JSONArray meds = resultJson.getJSONArray("약품목록");

                        SQLiteDatabase db = new DBHelper(AddMediActivity.this).getWritableDatabase();

                        SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
                        int userId = prefs.getInt("user_id", -1);
                        if (userId == -1) {
                            runOnUiThread(() -> txtResult.setText("로그인 정보 없음"));
                            return;
                        }

                        String dispenseDate = String.valueOf(resultJson.get("조제일자"));
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                        Date startDate = sdf.parse(dispenseDate);

                        for (int i = 0; i < meds.length(); i++) {
                            JSONObject item = meds.getJSONObject(i);
                            String drugName = String.valueOf(item.get("약 이름"));
                            String dose = String.valueOf(item.get("투약량(1회)"));
                            int timesPerDay = Integer.parseInt(String.valueOf(item.get("횟수")).replaceAll("[^0-9]", ""));
                            int durationDays = Integer.parseInt(String.valueOf(item.get("기간")).replaceAll("[^0-9]", ""));

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(startDate);
                            cal.add(Calendar.DATE, durationDays - 1);
                            String endDate = sdf.format(cal.getTime());

                            long drugId;
                            Cursor cursor = db.rawQuery("SELECT drug_id FROM drug WHERE item_name = ?", new String[]{drugName});
                            if (cursor.moveToFirst()) {
                                drugId = cursor.getLong(0);
                            } else {
                                ContentValues drugValues = new ContentValues();
                                drugValues.put("item_name", drugName);
                                drugValues.put("entp_name", "");
                                drugValues.put("main_ingredient", "");
                                drugValues.put("efficacy", "");
                                drugValues.put("warning", "");
                                drugValues.put("image_url", "");
                                drugValues.put("item_seq", drugName + "_seq");
                                drugId = db.insert("drug", null, drugValues);
                            }
                            cursor.close();

                            ContentValues medValues = new ContentValues();
                            medValues.put("user_id", userId);
                            medValues.put("drug_id", drugId);
                            medValues.put("daily_frequency", timesPerDay);
                            medValues.put("start_date", dispenseDate);
                            medValues.put("end_date", endDate);
                            medValues.put("ocr_raw_text", item.toString());
                            long medicationId = db.insert("user_medication", null, medValues);

                            String[] baseTimes = {"08:00", "13:00", "18:00", "22:00"};
                            for (int j = 0; j < timesPerDay && j < baseTimes.length; j++) {
                                ContentValues schedValues = new ContentValues();
                                schedValues.put("medication_id", medicationId);
                                schedValues.put("user_id", userId);
                                schedValues.put("repeat_days", "daily");
                                schedValues.put("dose_time", baseTimes[j]);
                                schedValues.put("memo", dose);
                                db.insert("medication_schedule", null, schedValues);
                            }

                            sb.append("🔹 ").append(drugName).append(" – ").append(dose)
                                    .append(", ").append(timesPerDay).append("회, ").append(durationDays).append("일분\n");
                        }

                        db.close();
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
