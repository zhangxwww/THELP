package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.data.Message;
import com.example.data.MessageAdapter;
import com.example.data.OrderAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private List<Message> messageList = new ArrayList<>();
    private RecyclerView recyclerView = null;
    private MessageAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setupToolbar();
        setupRecyclerView();
    }

    private void setupToolbar() {

        Button navBack = findViewById(R.id.nav_back_button);
        navBack.setOnClickListener(v -> backToMainActivity());
    }

    private void backToMainActivity() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setupRecyclerView() {
        getMsgList();
        recyclerView = findViewById(R.id.recycler_view_unread);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MessageAdapter(messageList, this);
        recyclerView.setAdapter(adapter);

//        listener = new MainActivity.EndLessOnScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int currentPage) {
//                getMoreData(currentPage);
//            }
//        };
//        recyclerView.addOnScrollListener(listener);
    }

    private void getMsgList(){
        for (int i = 0; i < 3; i++) {
            String picUrl = "https://overwatch.nosdn.127.net/2/heroes/Echo/hero-select-portrait.png";
            messageList.add(new Message(picUrl,"昵称"+String.valueOf(i), "订单奖励多少呀呀呀？", "00:00", false, Message.TEXT, Message.RECEIVE));
        }
    }
}
