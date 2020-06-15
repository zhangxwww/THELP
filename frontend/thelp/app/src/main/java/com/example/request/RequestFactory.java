package com.example.request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.example.thelp.CustomerDetailActivity;
import com.example.thelp.MainActivity;
import com.example.thelp.R;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestFactory {
    private static String sessionid = null;

    public static JsonObjectRequest getRequest(
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers;
                if (sessionid != null) {
                    headers = new HashMap<>();
                    headers.put("Cookie", sessionid);
                } else {
                    headers = super.getHeaders();
                }
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(
                    NetworkResponse response) {
                Response<JSONObject> superResponse = super
                        .parseNetworkResponse(response);
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                if (rawCookies != null) {
                    sessionid = rawCookies.substring(0, rawCookies.indexOf(";"));
                    Log.d("sessionid", "sessionid----------------" + sessionid);
                }
                return superResponse;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, 1.0f));
        return request;
    }

    public static JsonObjectRequest getUserInfoRequest(
            Integer userId, String ip,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        Map<String, String> map = new HashMap<>();
        if (userId != null) {
            map.put("user_id", String.valueOf(userId));
        }
        String json = new Gson().toJson(map);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        String url = ip + "/user/info";
        return getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                listener,
                errorListener
        );
    }

    public static JsonObjectRequest getUserEditRequest(
            HashMap<String, String> map, String ip,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        String json = new Gson().toJson(map);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        String url = ip + "/user/edit";
        return getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                listener,
                errorListener
        );
    }

    public static JsonObjectRequest getOrderDetailRequest(
            int orderId, String ip,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        Map<String, String> map = new HashMap<>();
        map.put("order_id", String.valueOf(orderId));
        String json = new Gson().toJson(map);

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        String url = ip + "/order/detail";
        return getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                listener,
                errorListener
        );
    }
}
