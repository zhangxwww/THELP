package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.data.ChatAdapter;
import com.example.data.Message;
import com.example.data.OrderAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private List<Message> messageList = new ArrayList<>();
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setupRecyclerView();
    }

    private void setupChatList() {
        String avatarUrl = "https://overwatch.nosdn.127.net/1/assets/img/pages/heroes/list/dva.png";
        for (int i = 0; i < 2; i++) {
            messageList.add(new Message(
                    avatarUrl, "马雨晴",
                    "这是马雨晴发出去的消息" + String.valueOf(i),
                    "2016年10月10日21:0" + String.valueOf(i),
                    true,
                    Message.TEXT,
                    Message.SEND));
        }
        for (int i = 0; i < 3; i++) {
            messageList.add(new Message(
                    avatarUrl, "张欣炜",
                    "这是张欣炜发出去的消息" + String.valueOf(i),
                    "2016年10月10日21:0" + String.valueOf(i),
                    true,
                    Message.TEXT,
                    Message.RECEIVE));
        }
        messageList.add(new Message(
                avatarUrl, "马雨晴",
                "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1793144512,3019469782&fm=26&gp=0.jpg",
                "2016年10月10日21:59",
                true,
                Message.IMAGE,
                Message.SEND));
        messageList.add(new Message(
                avatarUrl, "张欣炜",
                "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1793144512,3019469782&fm=26&gp=0.jpg",
                "2016年10月10日21:59",
                true,
                Message.IMAGE,
                Message.RECEIVE));
    }

    private void setupRecyclerView() {
        setupChatList();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ChatAdapter(messageList, this, R.layout.message_sent, R.layout.message_receive);
        recyclerView.setAdapter(adapter);
    }
}
