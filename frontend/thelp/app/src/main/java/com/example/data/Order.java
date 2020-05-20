package com.example.data;

public class Order {
    private String title;
    private int orderId;
    private String type;
    private String detail;
    private String employee;
    private String time;
    private String avatar;

    public Order(String title, int orderId, String type, String detail, String employee, String time, String avatar) {
        this.title = title;
        this.orderId = orderId;
        this.type = type;
        this.detail = detail;
        this.employee = employee;
        this.time = time;
        this.avatar = avatar;
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

    public String getEmployee() {
        return employee;
    }

    public String getTime() {
        return time;
    }

    public String getAvatar() {
        return avatar;
    }
}
