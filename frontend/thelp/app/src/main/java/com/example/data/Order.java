package com.example.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Order {
    public int orderId;

    public int employer_id;
    public int employee_id;

    public String avatar;

    public String employer;
    public String employee;

    public String title;
    public String detail;

    public String type;
    public String state;

    public String startTime;
    public String endTime;
    public String createTime;
    public String acceptTime;
    public String finishTime;

    public double reward;
    public double assessment;

    public String targetLocation;
    public String handlerLocation;

    public Order() {

    }

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

    public static Order parseFromJSONResponse(JSONObject response, int orderId) throws JSONException {
        Order order = new Order();
        order.orderId = orderId;
        order.employer_id = response.getInt("customer_id");
        if (response.has("handler_id")) {
            order.employee_id = response.getInt("handler_id");
        } else {
            order.employee_id = -1;
        }
        order.employer = response.getString("customer");
        if (response.has("handler")) {
            order.employee = response.getString("handler");
        } else {
            order.employee = null;
        }
        order.title = response.getString("title");
        order.detail = response.getString("description");
        order.type = response.getString("genre");
        order.state = response.getString("state");
        order.startTime = response.getString("start_time");
        order.endTime = response.getString("end_time");
        order.createTime = response.getString("create_time");
        order.acceptTime = response.getString("accept_time");
        order.finishTime = response.getString("finish_time");
        order.reward = response.getDouble("reward");
        order.assessment = response.getDouble("assessment");
        order.targetLocation = response.getString("target_location");
        order.handlerLocation = response.getString("handler_location");

        return order;
    }

    public enum OperationType {
        ACCEPT,
        CANCEL,
        ABORT,
        FINISH,
        DETAIL
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

    public String getStartTime() {
        return startTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getAcceptTime() {
        return acceptTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getAvatar() {
        return avatar;
    }
}
