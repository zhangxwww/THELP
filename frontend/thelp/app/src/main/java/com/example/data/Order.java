package com.example.data;

public class Order {
    private String title;
    private int orderId;
    private String type;
    private String detail;
    private String employer;
    private int employer_id;
    private String startTime;
    private String endTime;
    private double reward;
    private String avatar;
    private String targetLocation;

    public Order(String title, int orderId, String type,
                 String detail, String employer, int employer_id,
                 String start_time, String end_time,
                 String avatar, double reward, String target_location) {
        this.title = title;
        this.orderId = orderId;
        this.type = type;
        this.detail = detail;
        this.employer = employer;
        this.employer_id = employer_id;
        this.startTime = start_time;
        this.endTime = end_time;
        this.avatar = avatar;
        this.reward = reward;
        this.targetLocation = target_location;
    }

    public String getTitle() {
        return title;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getType() {
        return type;
    }

    public String getDetail() {
        return detail;
    }

    public String getEmployer() {
        return employer;
    }

    public int getEmployerId() {
        return employer_id;
    }

    public String getTime() {
        return startTime;
    }

    public String getAvatar() {
        return avatar;
    }
}
