package com.example.data;

import android.media.MediaExtractor;

import com.example.thelp.R;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {
    public final static int TEXT = 0;
    public final static int IMAGE = 1;
    public final static int SEND = 2;
    public final static int RECEIVE = 3;
    private String avatar;
    private String nickname;
    private String content;
    private String time;
    private int type;
    private int position;
    private boolean isRead;

    private int id;

    public Message(String avatar, String nickname, String content, String time, boolean isRead, int messageType, int position) {
        this.avatar = avatar;
        this.nickname = nickname;
        this.content = content;
        this.time = time;
        this.isRead = isRead;
        this.type = messageType;
        this.position = position;
    }

    public Message(int id, String avatar, String nickname, String content, String time, boolean isRead, int messageType) {
        this.id = id;
        this.avatar = avatar;
        this.nickname = nickname;
        this.content = content;
        this.time = time;
        this.isRead = isRead;
        this.type = messageType;
    }


    public int getId() {
        return id;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getContent() {
        if (type == TEXT) {
            if (content.length() < 20) {
                return content;
            } else {
                return content.substring(0, 20) + "...";
            }
        } else if (type == IMAGE) {
            return "[图片]";
        } else {
            // TODO: raise error
            return "";
        }
    }

    public String getFullContent() {
        return content;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public static Message parseFromWebSocketRsponse(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);
        int fromId = jsonObject.getInt("from_id");
        String content = jsonObject.getString("content");
        String name = jsonObject.getString("name");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String contentType = jsonObject.getString("content_type");
        int type = Message.TEXT;
        if (contentType.equals("IMAGE")) {
            type = Message.IMAGE;
        }
        Message msg = new Message(fromId, null, name, content, simpleDateFormat.format(date), false, type);
        return msg;

    }
    public static List<Message> listParseFromJSONResponse(JSONObject response, int selfId, String ip) throws JSONException {
        JSONArray list = response.getJSONArray("msg_list");
        int len = list.length();
        List<Message> messageList = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            JSONObject msg = (JSONObject) list.get(i);
            JSONObject from = msg.getJSONObject("from");
            String avatar = from.getString("avatar");
            if (!avatar.startsWith("http")) {
                avatar = ip + avatar;
            }
            String name = from.getString("name");
            JSONObject to = msg.getJSONObject("to");
            int toId = to.getInt("id");

            int position = SEND;

            if (selfId == toId) {
                position = RECEIVE;
            }

            String content = msg.getString("content");
            String contentType = msg.getString("content_type");
            int type = TEXT;
            if (contentType.equals("IMAGE")) {
                type = IMAGE;
                content = ip + content;
            }
            String time = msg.getString("time");
            boolean has_read = msg.getBoolean("has_read");
            messageList.add(new Message(avatar, name, content, time, has_read, type, position));
        }
        return messageList;
    }
}
