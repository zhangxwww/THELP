package com.example.data;

public class Message {

    private String avatar;
    private String nickname;
    private String content;
    private String time;
    private boolean isRead;

    public Message(String avatar, String nickname, String content, String time, boolean isRead){
        this.avatar = avatar;
        this.nickname = nickname;
        this.content = content;
        this.time = time;
        this.isRead = isRead;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getContent() {
        return content;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTime() {
        return time;
    }
}
