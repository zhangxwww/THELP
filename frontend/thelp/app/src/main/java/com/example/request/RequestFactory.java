package com.example.request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.data.Order;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class RequestFactory {
    private static String sessionid = null;

    public static String getSessionid() {
        return sessionid;
    }

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

    public static JsonObjectRequest getOrderOperationRequest(
            int orderId, Order.OperationType type, String ip,
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
        String url;
        switch (type) {
            case DETAIL:
                url = ip + "/order/detail";
                break;
            case ACCEPT:
                url = ip + "/order/accept";
                break;
            case CANCEL:
                url = ip + "/order/cancel";
                break;
            case ABORT:
                url = ip + "/order/abort";
                break;
            case FINISH:
                url = ip + "/order/finish";
                break;
            default:
                return null;
        }
        return getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                listener,
                errorListener
        );
    }

    public static JsonObjectRequest getOrderHistoryRequest(
            int page, Order.HistoryType type, String ip,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        Map<String, String> map = new HashMap<>();
        map.put("page", String.valueOf(page));
        map.put("num_each_page", "10");
        String json = new Gson().toJson(map);

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        String url;
        switch (type) {
            case CREATE:
                url = ip + "/order/history/create";
                break;
            case HANDLER:
                url = ip + "/order/history/handler";
                break;
            default:
                return null;
        }
        return getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                listener,
                errorListener
        );
    }

    public static JsonObjectRequest getOrderAssessRequest(
            int orderId, int assess, String ip,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        Map<String, String> map = new HashMap<>();
        map.put("order_id", String.valueOf(orderId));
        map.put("assess", String.valueOf(assess));
        String json = new Gson().toJson(map);

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        String url = ip + "/order/assess";
        return getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                listener,
                errorListener
        );
    }

    public static void uploadFile(File file, String ip, String area, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        MediaType contentType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(contentType, file))
                .build();
        // RequestBody body = RequestBody.create(contentType, file);
        String url = ip + area + "/upload";
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .addHeader("cookie", sessionid)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static JsonObjectRequest getMessageHistorySingleRequest(
            int otherId, int page, String ip,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        Map<String, String> map = new HashMap<>();
        map.put("page", String.valueOf(page));
        map.put("num_each_page", "10");
        map.put("other_id", String.valueOf(otherId));
        String json = new Gson().toJson(map);

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        String url = ip + "/msg/history/single";
        return getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                listener,
                errorListener
        );
    }

    public static JsonObjectRequest getMessageHistoryRequest(
            int page, String ip,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener) {

        Map<String, String> map = new HashMap<>();
        map.put("page", String.valueOf(page));
        String json = new Gson().toJson(map);

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        String url = ip + "/msg/history";
        return getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                listener,
                errorListener
        );
    }

}
