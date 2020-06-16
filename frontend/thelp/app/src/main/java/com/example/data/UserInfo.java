package com.example.data;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {

    public int userId;
    public String phone;
    public String nickName;
    public String avatar;
    public String signature;
    public double score;

    private static String url;

    public UserInfo(int id, String phone, String nickName,
                    String avatar, String signature, double score) {
        this.userId = id;
        this.phone = phone;
        this.nickName = nickName;
        this.avatar = avatar;
        this.signature = signature;
        this.score = score;
    }

    public static void setUrl(String u) {
        url = u;
    }

    public static UserInfo parseFromJSONResponse(JSONObject response) throws JSONException {
        String name = response.getString("nickname");
        String avatar = response.getString("avatar");
        if (!avatar.startsWith("http")) {
            avatar = url + avatar;
        }
        String phone = response.getString("phone");
        int userId = response.getInt("user_id");
        String signature = response.getString("signature");
        double score = response.getDouble("score");
        return new UserInfo(userId, phone, name, avatar, signature, score);
    }
}
