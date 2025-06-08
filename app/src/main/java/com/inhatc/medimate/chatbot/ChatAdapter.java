package com.inhatc.medimate.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.medimate.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        if (msg.isUser()) {
            holder.userText.setVisibility(View.VISIBLE);
            holder.botText.setVisibility(View.GONE);
            holder.userText.setText(msg.getMessage());
        } else {
            holder.botText.setVisibility(View.VISIBLE);
            holder.userText.setVisibility(View.GONE);
            holder.botText.setText(msg.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView userText;
        TextView botText;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            userText = itemView.findViewById(R.id.text_user);
            botText = itemView.findViewById(R.id.text_bot);
        }
    }
}
