package com.example.websocket;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.example.data.Message;
import com.example.thelp.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

public class ChatMessageReceiver extends BroadcastReceiver {
    private View view;

    public ChatMessageReceiver(View v) {
        view = v;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        try {
            Message msg = Message.parseFromWebSocketRsponse(message);
            String info;
            if (msg.getType() == Message.TEXT) {
                info = msg.getNickname() + " 发来信息: " + msg.getContent();
            } else {
                info = msg.getNickname() + " 发来[图片]";
            }
            Snackbar.make(view, info, Snackbar.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}