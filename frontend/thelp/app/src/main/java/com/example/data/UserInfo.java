package com.example.data;

public class UserInfo {

    public int userId;
    public String phone;
    public String nickName;
    public String avatar;
    public String signature;
    public double score;

    public UserInfo(int id, String phone, String nickName,
                    String avatar, String signature, double score) {
        this.userId = id;
        this.phone = phone;
        this.nickName = nickName;
        this.avatar = avatar;
        this.signature = signature;
        this.score = score;
    }

}
