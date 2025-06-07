package com.inhatc.medimate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class AddMediActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button btnSelectImage, btnAnalyze;
    private ImageView imagePreview;
    private TextView txtResult;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medi);

        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        imagePreview = findViewById(R.id.imagePreview);
        txtResult = findViewById(R.id.txtResult);

        // 사진 선택
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
            }
        });

        // 약 추가하기 (모의 분석 결과 출력)
        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImageUri == null) {
                    txtResult.setText("이미지를 먼저 선택하세요.");
                    return;
                }

                // 샘플 JSON 결과 (실제 서버 응답이라고 가정)
                String json = "{\n" +
                        "  \"조제일자\": \"2025-06-05\",\n" +
                        "  \"약품목록\": [\n" +
                        "    {\"약 이름\": \"로프민캡슐\", \"투약량(1회)\": \"2.00정\", \"횟수\": \"3회\", \"기간\": \"4일분\"},\n" +
                        "    {\"약 이름\": \"메디솔론정\", \"투약량(1회)\": \"0.50정\", \"횟수\": \"3회\", \"기간\": \"4일분\"},\n" +
                        "    {\"약 이름\": \"포타겔현틱액\", \"투약량(1회)\": \"1.00정\", \"횟수\": \"3회\", \"기간\": \"4일분\"},\n" +
                        "    {\"약 이름\": \"싸이프로신정25\", \"투약량(1회)\": \"2.00정\", \"횟수\": \"2회\", \"기간\": \"4일분\"},\n" +
                        "    {\"약 이름\": \"라푸원정\", \"투약량(1회)\": \"1.00정\", \"횟수\": \"2회\", \"기간\": \"4일분\"}\n" +
                        "  ]\n" +
                        "}";

                try {
                    JSONObject jsonObject = new JSONObject(json);
                    StringBuilder resultText = new StringBuilder();

                    resultText.append("📅 조제일자: ")
                            .append(jsonObject.getString("조제일자"))
                            .append("\n\n💊 약품목록:\n");

                    JSONArray meds = jsonObject.getJSONArray("약품목록");
                    for (int i = 0; i < meds.length(); i++) {
                        JSONObject item = meds.getJSONObject(i);
                        resultText.append("🔹 ")
                                .append(item.getString("약 이름"))
                                .append(" - ")
                                .append(item.getString("투약량(1회)")).append(", ")
                                .append(item.getString("횟수")).append(", ")
                                .append(item.getString("기간"))
                                .append("\n");
                    }

                    txtResult.setText(resultText.toString());

                } catch (Exception e) {
                    txtResult.setText("JSON 파싱 오류: " + e.getMessage());
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri); // 화면에 이미지 표시
        }
    }
}
