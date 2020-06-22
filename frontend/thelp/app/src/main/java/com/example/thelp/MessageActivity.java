package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.data.Message;
import com.example.data.MessageAdapter;
import com.example.data.Order;
import com.example.data.OrderAdapter;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private List<Message> messageList = new ArrayList<>();
    private RecyclerView recyclerView = null;
    private MessageAdapter adapter = null;
    public static final int READ_SUCCESS_CODE = 0;
    public static final String CHAT_POSITION = "CHAT_POSITION";


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
//        getMsgList();
        recyclerView = findViewById(R.id.recycler_view_unread);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MessageAdapter(messageList, this);
        adapter.setOnClickListener(this::toChatRoom);
        recyclerView.setAdapter(adapter);
        requestLatestMessageInPage(1,false);

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

    private void requestLatestMessageInPage(int page, boolean refresh) {
        JsonObjectRequest req = RequestFactory.getMessageHistoryRequest(
                page, getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray list = response.getJSONArray("user_msg_list");
                            int len = list.length();
                            if (refresh) {
                                messageList.clear();
                            }
                            for (int i = len - 1; i >= 0; i--) {
                                JSONObject msg = (JSONObject) list.get(i);
                                String avatar = msg.getString("other_avatar");
                                String content = msg.getString("content");
                                String time = msg.getString("time");
                                boolean hasRead = msg.getBoolean("has_read");
                                if (!avatar.startsWith("http")) {
                                    avatar = getResources().getString(R.string.url) + avatar;
                                }
                                String name = msg.getString("other_name");
                                int id = msg.getInt("other_id");
                                String contentType = msg.getString("content_type");
                                int type = Message.TEXT;
                                if (contentType.equals("IMAGE")) {
                                    type = Message.IMAGE;
                                }
                                messageList.add(new Message(id, avatar, name, content, time, hasRead, type));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            String error = response.getString("error_msg");
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("MessageHistorySingle", "Fail " + error.getMessage())
        );
        if (req != null) {
            MySingleton.getInstance(this).addToRequestQueue(req);
        }
    }

    private void toChatRoom(int position) {
        Message message = messageList.get(position);
        Intent intent;
        intent = new Intent(MessageActivity.this, ChatActivity.class);
        UserInfo userInfo = new UserInfo(message.getId(),null,message.getNickname(),message.getAvatar(),null,-1);
        intent.putExtra(UserInfo.USER_INFO, userInfo);
        intent.putExtra(CHAT_POSITION, position);
        startActivityForResult(intent,READ_SUCCESS_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_SUCCESS_CODE) {
            if (resultCode == RESULT_OK) {
                int position = data.getExtras().getInt(CHAT_POSITION);
                messageList.get(position).setRead(true);
                adapter.notifyDataSetChanged();
            }
            requestLatestMessageInPage(1,true);
        }
    }
}
