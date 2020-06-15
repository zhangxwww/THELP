package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.data.ChatAdapter;
import com.example.data.FullImageInfo;
import com.example.data.GlideEngine;
import com.example.data.Message;
import com.example.data.OrderAdapter;
import com.google.android.material.button.MaterialButton;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;

// TODO: 修改这个方法从websocket获取数据
public class ChatActivity extends AppCompatActivity {
    private String avatarUrl = "https://overwatch.nosdn.127.net/1/assets/img/pages/heroes/list/dva.png";
    private String avatarUrl2 = "https://overwatch.nosdn.127.net/2/heroes/Sigma/hero-select-portrait.png";
    private List<Message> messageList = new ArrayList<>();
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setupRecyclerView();
        setupClickListeners();
    }

    private void setupChatList() {
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

    private void onClickSelectImage() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // TODO: 发送图片消息至WebSocket
                        for (LocalMedia localMedia: result) {
                            String photoPath = localMedia.getPath();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            messageList.add(new Message(
                                    avatarUrl, "马雨晴",
                                    photoPath,
                                    simpleDateFormat.format(date),
                                    true,
                                    Message.IMAGE,
                                    Message.SEND));
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancel() {
                        // onCancel Callback
                    }
                });
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
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                messageList.add(new Message(
                        avatarUrl, "马雨晴",
                        chatEdit.getText().toString(),
                        simpleDateFormat.format(date),
                        true,
                        Message.TEXT,
                        Message.SEND));
                adapter.notifyDataSetChanged();
                chatEdit.clearFocus();
                chatEdit.setText("");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(ChatActivity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(chatEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                // TODO: 发送到websocket
                // TODO: 自动下拉视角到最底端？
            }
        });
    }
    // TODO: 发送消息、图片到websocket
}
