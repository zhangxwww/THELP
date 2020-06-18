package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.data.ChatAdapter;
import com.example.data.FullImageInfo;
import com.example.data.GlideEngine;
import com.example.data.Message;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.example.websocket.JWebSocketClient;
import com.example.websocket.JWebSocketClientService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

// TODO: 修改这个方法从websocket获取数据
public class ChatActivity extends AppCompatActivity {
    private List<Message> messageList = new ArrayList<>();
    RecyclerView recyclerView = null;
    private int otherId;
    private int selfId;
    private String selfName = null;
    private String selfAvatar = null;
    private String otherName = null;
    private String otherAvatar = null;
    private ChatAdapter adapter;
    private Context context;

    private JWebSocketClient client;
    private JWebSocketClientService.JWebSocketClientBinder binder;
    private JWebSocketClientService jWebSClientService;
    private ChatActivity.ChatMessageReceiver chatMessageReceiver;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("ChatActivity", "服务与活动成功绑定");
            binder = (JWebSocketClientService.JWebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
            client = jWebSClientService.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("ChatActivity", "服务与活动成功断开");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        UserInfo otherUserInfo = (UserInfo) Objects.requireNonNull(
                getIntent().getSerializableExtra(UserInfo.USER_INFO));
        otherId = otherUserInfo.userId;
        UserInfo selfUserInfo = ((myApplication) getApplicationContext()).getUserInfo();
        selfId = selfUserInfo.userId;
        selfAvatar = selfUserInfo.avatar;
        selfName = selfUserInfo.nickName;
        otherAvatar = otherUserInfo.avatar;
        otherName = otherUserInfo.nickName;
        ((TextView) findViewById(R.id.other_name)).setText(otherUserInfo.nickName);
        recyclerView = findViewById(R.id.recycler_view);
        context = ChatActivity.this;

        bindService();

        doRegisterReceiver();

        setupRecyclerView();
        setupClickListeners();
        Toolbar myToolbar = findViewById(R.id.app_bar);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                int position = Objects.requireNonNull(getIntent().getExtras().getInt(MessageActivity.CHAT_POSITION));
                intent.putExtra(MessageActivity.CHAT_POSITION, position);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(chatMessageReceiver);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private void requestMessageInPage(int page, int otherId, int selfId, boolean refresh) {
        JsonObjectRequest req = RequestFactory.getMessageHistorySingleRequest(
                otherId, page,
                getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            List<Message> messagesRaw = Message.listParseFromJSONResponse(response, selfId, getResources().getString(R.string.url));
                            messageList.addAll(messagesRaw);
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
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

    // TODO: 滑动刷新
    private void setupRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ChatAdapter(messageList, this, R.layout.message_sent, R.layout.message_receive);
        recyclerView.setAdapter(adapter);
        setupAdapterOnClickListener();
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        requestMessageInPage(1, otherId, selfId, false);
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

    private void onClickSelectImage() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        RelativeLayout cl = findViewById(R.id.chat_activity_bg);
                        for (LocalMedia localMedia : result) {
                            String photoPath = localMedia.getPath();
                            // 先上传图片
                            new Thread(() -> {
                                File file = new File(photoPath);
                                RequestFactory.uploadFile(
                                        file,
                                        getResources().getString(R.string.url),
                                        "/msg",
                                        new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                e.printStackTrace();
                                                Snackbar.make(cl, getResources().getString(R.string.send_image_failed), Snackbar.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                ((ChatActivity) context).runOnUiThread(() -> {
                                                    //此时已在主线程中，可以更新UI了
                                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                                                    Date date = new Date(System.currentTimeMillis());
                                                    messageList.add(new Message(
                                                            selfAvatar, selfName,
                                                            photoPath,
                                                            simpleDateFormat.format(date),
                                                            true,
                                                            Message.IMAGE,
                                                            Message.SEND));
                                                    adapter.notifyDataSetChanged();
                                                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                                                    });

                                                // 发送图片到webSocket
                                                Map<String, String> msg_info = new HashMap<>();
                                                msg_info.put("to_id", String.valueOf(otherId));
                                                msg_info.put("content_type", "IMAGE");
                                                msg_info.put("content", file.getName());

                                                String json = new Gson().toJson(msg_info);
                                                JSONObject jsonObject;
                                                try {
                                                    jsonObject = new JSONObject(json);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    return;
                                                }
                                                jWebSClientService.sendMsg(jsonObject.toString());
                                                Snackbar.make(cl, getResources().getString(R.string.send_image_succeed), Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                            }).start();

                        }

                    }

                    @Override
                    public void onCancel() {
                        // onCancel Callback
                    }
                });
    }

    private void addMessage(Message msg) {
        messageList.add(msg);
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

    }
    private void setupClickListeners() {
        ImageButton imageSelect = findViewById(R.id.image_select);
        imageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSelectImage();
            }
        });

        EditText chatEdit = findViewById(R.id.edit_text);
        MaterialButton confirmButton = findViewById(R.id.confirm_button);

        chatEdit.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    confirmButton.setVisibility(View.VISIBLE);
                } else {
                    confirmButton.setVisibility(View.GONE);
                }
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client != null && client.isOpen()) {
                    // 发送文字到webSocket
                    Map<String, String> msg_info = new HashMap<>();
                    msg_info.put("to_id", String.valueOf(otherId));
                    msg_info.put("content_type", "TEXT");
                    msg_info.put("content", Objects.requireNonNull(chatEdit.getText().toString()));

                    String json = new Gson().toJson(msg_info);
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    jWebSClientService.sendMsg(jsonObject.toString());
                } else {
                    //TODO
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                messageList.add(new Message(
                        selfAvatar, selfName,
                        chatEdit.getText().toString(),
                        simpleDateFormat.format(date),
                        true,
                        Message.TEXT,
                        Message.SEND));
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                chatEdit.clearFocus();
                chatEdit.setText("");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(ChatActivity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(chatEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }
    // TODO: 发送消息、图片到websocket

    /**
     * 绑定服务
     */
    private void bindService() {
        Intent bindIntent = new Intent(ChatActivity.this, JWebSocketClientService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private class ChatMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            //TODO
            try {
                Message msg = Message.parseFromWebSocketRsponse(message);
                if (msg.getId() == otherId) {
                    requestMessageInPage(1, otherId, selfId, false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 动态注册广播
     */
    private void doRegisterReceiver() {
        chatMessageReceiver = new ChatActivity.ChatMessageReceiver();
        IntentFilter filter = new IntentFilter("com.xch.servicecallback.content");
        registerReceiver(chatMessageReceiver, filter);
    }


}
