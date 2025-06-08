package com.inhatc.medimate.chatbot;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.medimate.R;
import com.inhatc.medimate.util.DrugNameDictionary;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private static final String TAG = "Chatbot";

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // ✅ 약 이름 사전 초기화 (1회)
        DrugNameDictionary.initialize(this);
        Log.d(TAG, "약 이름 사전 초기화 완료");

        // 🔌 UI 바인딩
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        // 🪄 RecyclerView 설정
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // ▶️ 전송 버튼 클릭 시 처리
        sendButton.setOnClickListener(v -> {
            String userInput = inputMessage.getText().toString().trim();
            if (!userInput.isEmpty()) {
                Log.d(TAG, "사용자 입력: " + userInput);

                // 🗣️ 사용자 입력 표시
                messageList.add(new ChatMessage(userInput, true));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
                inputMessage.setText("");

                // 📌 Intent, 태그 분류
                ChatIntentClassifier.IntentType intent = ChatIntentClassifier.classifyIntent(userInput);
                String tag = ChatIntentClassifier.extractTagFromMessage(userInput);
                Log.d(TAG, "분류된 태그: " + tag);
                Log.d(TAG, "분류된 Intent: " + intent);

                // 🕐 응답 대기 표시
                messageList.add(new ChatMessage("잠시만 기다려주세요...", false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);

                // ⚙️ API 호출 (백그라운드)
                new Thread(() -> {
                    String response = (tag != null)
                            ? ChatApiService.getSingleTagValue(userInput, tag)
                            : ChatApiService.getResponseByIntent(userInput, intent);

                    Log.d(TAG, "API 응답: " + response);

                    runOnUiThread(() -> {
                        // ⏹️ 대기 메시지 제거
                        messageList.remove(messageList.size() - 1);
                        chatAdapter.notifyItemRemoved(messageList.size());

                        // 💬 응답 메시지 표시
                        messageList.add(new ChatMessage(response, false));
                        chatAdapter.notifyItemInserted(messageList.size() - 1);
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }).start();
            }
        });
    }
}
