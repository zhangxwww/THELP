package com.example.data;

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

    public Message(String avatar, String nickname, String content, String time, boolean isRead, int position){
        this.avatar = avatar;
        this.nickname = nickname;
        this.content = content;
        this.time = time;
        this.type = TEXT;
        this.isRead = isRead;
        this.position = position;
    }

    public Message(String avatar, String nickname, String content, String time, boolean isRead, int messageType, int position){
        this.avatar = avatar;
        this.nickname = nickname;
        this.content = content;
        this.time = time;
        this.isRead = isRead;
        this.type = messageType;
        this.position = position;
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

    public int getPosition() { return position;}
}
