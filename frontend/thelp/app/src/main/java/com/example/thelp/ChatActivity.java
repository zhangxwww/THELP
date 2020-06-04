package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.data.ChatAdapter;
import com.example.data.FullImageInfo;
import com.example.data.Message;
import com.example.data.OrderAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

// TODO: 修改这个方法从websocket获取数据
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
        String avatarUrl2 = "https://overwatch.nosdn.127.net/2/heroes/Sigma/hero-select-portrait.png";
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
                    avatarUrl2, "张欣炜",
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
                avatarUrl2, "张欣炜",
                "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1793144512,3019469782&fm=26&gp=0.jpg",
                "2016年10月10日21:59",
                true,
                Message.IMAGE,
                Message.RECEIVE));
    }

    // TODO: 滑动刷新
    private void setupRecyclerView() {
        setupChatList();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ChatAdapter(messageList, this, R.layout.message_sent, R.layout.message_receive);
        recyclerView.setAdapter(adapter);
        setupAdapterOnClickListener();
    }

    private void setupAdapterOnClickListener() {
        this.adapter.setClickListener(new ChatAdapter.OnItemClickedListener() {
            @Override
            public void onAvatarClicked(int position) {
                // TODO: 跳转到对应个人信息页面
            }

            @Override
            public void onImageClicked(View view, int position) {
                int location[] = new int[2];
                view.getLocationOnScreen(location);
                FullImageInfo fullImageInfo = new FullImageInfo();
                fullImageInfo.setLocationX(location[0]);
                fullImageInfo.setLocationY(location[1]);
                fullImageInfo.setWidth(view.getWidth());
                fullImageInfo.setHeight(view.getHeight());
                fullImageInfo.setImageUrl(messageList.get(position).getFullContent());
                EventBus.getDefault().postSticky(fullImageInfo);
                startActivity(new Intent(ChatActivity.this, FullImageActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }
    // TODO: 发送消息、图片到websocket

}
