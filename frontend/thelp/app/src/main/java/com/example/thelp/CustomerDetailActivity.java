package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.baidu.mapapi.map.MapView;
import com.bumptech.glide.Glide;
import com.example.data.Order;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.carbs.android.avatarimageview.library.AvatarImageView;

public class CustomerDetailActivity extends AppCompatActivity {
    private MapView mMapView = null;
    String picUrl = "https://overwatch.nosdn.127.net/2/heroes/Echo/hero-select-portrait.png";
    AvatarImageView aiv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);
        orderStatusList(Objects.requireNonNull(getIntent().getExtras()).getInt("ORDER_ID"));
        aiv = (AvatarImageView) this.findViewById(R.id.order_avatar_image);
        Glide
                .with(this)
                .load(picUrl)
                .centerCrop()
                .into(aiv);

        mMapView = (MapView) findViewById(R.id.bmapView);
    }

    private void orderStatusList(int orderId) {

        JsonObjectRequest req = RequestFactory.getOrderDetailRequest(
                orderId,
                CustomerDetailActivity.this.getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            Order order = Order.parseFromJSONResponse(response, orderId);
                            List<OrderStatusModel> arrayOfStatus = getDetailFromOrderDetail(order);
                            OrderStatusAdapter adapter = new OrderStatusAdapter(this, R.layout.item_order_state, arrayOfStatus);
                            ListView listView = findViewById(R.id.state_list);
                            listView.setAdapter(adapter);
                            getHandlerInfo(order);
                        } else {
                            String error = response.getString("error_msg");
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("CustomerDetail", "Fail " + error.getMessage())
        );
        if (req != null) {
            MySingleton.getInstance(this).addToRequestQueue(req);
        }
    }

    private void showHandlerInfo(String name, String score, String avatar) {
        TextView nameView = findViewById(R.id.order_name_tv);
        nameView.setText(name);
        TextView scoreView = findViewById(R.id.score_tv);
        scoreView.setText(score);
        Glide
                .with(this)
                .load(avatar)
                .centerCrop()
                .into(aiv);
    }

    private void getHandlerInfo(Order order) {
        if (order.employee == null) {
            showHandlerInfo(getResources().getString(R.string.order_active_text), "", picUrl);
            return;
        }
        JsonObjectRequest req = RequestFactory.getUserInfoRequest(
                order.employee_id,
                CustomerDetailActivity.this.getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            UserInfo userInfo = UserInfo.parseFromJSONResponse(response);
                            showHandlerInfo(userInfo.nickName, String.valueOf(userInfo.nickName), userInfo.avatar);
                        } else {
                            String error = response.getString("error_msg");
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("CustomerDetailUserInfo", "Fail " + error.getMessage())
        );
        if (req != null) {
            MySingleton.getInstance(this).addToRequestQueue(req);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    public static class OrderStatusAdapter extends ArrayAdapter<OrderStatusModel> {

        Context context;
        List<OrderStatusModel> order_status;
        private int rscId;

        public OrderStatusAdapter(Context context, int textViewResourceId, List<OrderStatusModel> order_status) {
            super(context, textViewResourceId, order_status);
            this.context = context;
            this.order_status = order_status;
            this.rscId = textViewResourceId;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            OrderStatusModel order_status_data = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(rscId, parent, false);
            ImageView iv_upper_line = view.findViewById(R.id.iv_upper_line);
            ImageView iv_lower_line = view.findViewById(R.id.iv_lower_line);
            final ImageView iv_circle = view.findViewById(R.id.iv_circle);
            TextView tv_status = view.findViewById(R.id.tv_status);
            TextView tv_orderstatus_time = view.findViewById(R.id.tv_orderstatus_time);
            LinearLayout ly_orderstatus_time = view.findViewById(R.id.ly_orderstatus_time);
            LinearLayout ly_status = view.findViewById(R.id.ly_status);

            // Populate the data into the template view using the data object

            tv_status.setText(order_status_data.getTv_status());
            tv_orderstatus_time.setText(order_status_data.getTv_orderstatus_time());

            if (position == 0) {
                iv_upper_line.setVisibility(View.INVISIBLE);
            } else if (position == order_status.size() - 1) {
                iv_lower_line.setVisibility(View.INVISIBLE);

            }
            if (order_status_data.isAchieved) {
                iv_circle.setBackgroundResource(R.drawable.ic_check_circle_black_24dp);
            } else {
                ly_orderstatus_time.setVisibility(View.GONE);
            }

            // Return the completed view to render on screen
            return view;
        }

    }

    class OrderStatusModel {
        private String tv_status;
        private String tv_orderstatus_time;
        private boolean isAchieved;

        public OrderStatusModel(String tv_status, String tv_orderstatus_time, boolean isAchieved) {
            this.tv_status = tv_status;
            this.tv_orderstatus_time = tv_orderstatus_time;
            this.isAchieved = isAchieved;
        }

        public String getTv_status() {
            return tv_status;
        }

        public String getTv_orderstatus_time() {
            return tv_orderstatus_time;
        }

        public ArrayList<OrderStatusModel> getStoreDetail() {
            ArrayList<OrderStatusModel> status = new ArrayList<OrderStatusModel>();
            status.add(new OrderStatusModel("Order Accepted", "05-30 8:30", true));
            status.add(new OrderStatusModel("On The Way", "05-30 9:00", false));
            status.add(new OrderStatusModel("Delivered", "05-30 9:30", false));
            return status;
        }
    }

    private List<OrderStatusModel> getDetailFromOrderDetail(Order order) {
        List<OrderStatusModel> status = new ArrayList<>();
        Resources res = CustomerDetailActivity.this.getResources();
        if (order.state.equals(res.getString(R.string.order_canceled))) {
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_canceled_text),
                    "", true)
            );
        } else {
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_active_text),
                    "", false));
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_accepted_text),
                    "", false));
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_finished_text),
                    "", false));
            if (order.state.equals(res.getString(R.string.order_active))) {
                status.get(0).tv_orderstatus_time = order.createTime;
                status.get(0).isAchieved = true;
            }
            if (order.state.equals(res.getString(R.string.order_accepted))) {
                status.get(1).tv_orderstatus_time = order.acceptTime;
                status.get(1).isAchieved = true;
            }
            if (order.state.equals(res.getString(R.string.order_finished))) {
                status.get(2).tv_orderstatus_time = order.finishTime;
                status.get(2).isAchieved = true;
            }
        }
        return status;
    }
}
