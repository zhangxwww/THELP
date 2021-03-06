package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.example.data.Order;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbs.android.avatarimageview.library.AvatarImageView;

import static android.view.View.GONE;

public class CustomerDetailActivity extends AppCompatActivity {
    private MapView mMapView = null;
    private LinearLayout handlerLayout = null;
    private RelativeLayout bottomSheet = null;
    private List<OrderStatusModel> arrayOfStatus = new ArrayList<>();

    BaiduMap mBaiduMap = null;

    private int orderId;
    String picUrl = "https://overwatch.nosdn.127.net/2/heroes/Echo/hero-select-portrait.png";
    AvatarImageView aiv;

    @BindView(R.id.button_cancel)
    Button cancelButton;

    @BindView(R.id.button_abort)
    Button abortButton;

    @BindView(R.id.button_assess)
    Button assessButton;

    @BindView(R.id.button_edit)
    Button editButton;

    @BindView(R.id.order_location_tv)
    TextView locationState;

    private static final int ASSESS_CODE = 0;
    private static final int EDIT_CODE = 1;


    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);
        ButterKnife.bind(this);
        handlerLayout = this.findViewById(R.id.order_handler_layout);
        bottomSheet = findViewById(R.id.bottom_sheet);

        orderId = Objects.requireNonNull(getIntent().getExtras()).getInt("ORDER_ID");
        orderStatusList(orderId);

        aiv = (AvatarImageView) this.findViewById(R.id.order_avatar_image);
        Glide
                .with(this)
                .load(picUrl)
                .centerCrop()
                .into(aiv);

        mMapView = (MapView) findViewById(R.id.bmapView);
        bindButtonEvent(orderId);

        // 开启地图控件的定位功能
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

    }



    //    -------------------------------------获取handler位置------------------------------------------------
    private static final long GET_LOCATION_RATE = 10 * 1000;
    private Handler mHandler = new Handler();
    private Runnable getLocationRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("CustomerDetail", "获取handler位置");
            JsonObjectRequest req = RequestFactory.getHandlerLocationRequest(
                    orderId,
                    getResources().getString(R.string.url),
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                double latitude = Double.parseDouble(response.getString("latitude"));
                                double longitude = Double.parseDouble(response.getString("longitude"));
                                if (latitude == 0 && longitude == 0) {
                                    locationState.post(() -> locationState.setText(getString(R.string.not_share_location_text)));
                                } else {
                                    locationState.post(() -> locationState.setText(getString(R.string.share_location_text)));
                                    MyLocationData locData = new MyLocationData.Builder()
                                            .latitude(latitude)
                                            .longitude(longitude).build();
                                    LatLng cenpt =  new LatLng(latitude,longitude);
                                    MapStatus mMapStatus = new MapStatus.Builder()
                                            .target(cenpt)
                                            .zoom(19)
                                            .build();

                                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                                    mBaiduMap.setMapStatus(mMapStatusUpdate);
                                    // 在地图上显示定位图标
                                    mBaiduMap.setMyLocationData(locData);
                                }
                            } else {
                                String error = response.getString("error_msg");
                                Log.d("Error Msg", error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d("getLocation", "Fail " + error.getMessage())
            );
            if (req != null) {
                MySingleton.getInstance(CustomerDetailActivity.this).addToRequestQueue(req);
            }
            //每隔10s,获取一次
            mHandler.postDelayed(this, GET_LOCATION_RATE);
        }
    };



    private void orderStatusList(int orderId) {

        JsonObjectRequest req = RequestFactory.getOrderOperationRequest(
                orderId,
                Order.OperationType.DETAIL,
                CustomerDetailActivity.this.getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            order = Order.parseFromJSONResponse(response, orderId);
                            arrayOfStatus = getDetailFromOrderDetail(order);
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
                            aiv.setOnClickListener(v -> {
                                Intent intent = new Intent(CustomerDetailActivity.this, PersonActivity.class);
                                intent.putExtra(UserInfo.USER_IDENTIFICATION, UserInfo.USER_OTHERS);
                                intent.putExtra(UserInfo.USER_INFO, userInfo);
                                startActivity(intent);
                            });
                            showHandlerInfo(userInfo.nickName, String.valueOf(userInfo.score), userInfo.avatar);
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
            tv_status.setText(order_status_data.getState());
            tv_orderstatus_time.setText(order_status_data.getOrderStateTime());

            if (position == 0) {
                iv_upper_line.setVisibility(View.INVISIBLE);
            }
            if (position == order_status.size() - 1) {
                iv_lower_line.setVisibility(View.INVISIBLE);
            }
            if (order_status_data.isAchieved) {
                iv_circle.setBackgroundResource(R.drawable.ic_check_circle_black_24dp);
            } else {
                tv_orderstatus_time.setVisibility(GONE);
            }

            if (order_status_data.orderStateTime == null || order_status_data.orderStateTime.length() == 0) {
                tv_orderstatus_time.setVisibility(GONE);
            }

            return view;
        }

    }

    class OrderStatusModel {
        private String state;
        private String orderStateTime;
        private boolean isAchieved;

        public OrderStatusModel(String mState, String mOrderStateTime, boolean isAchieved) {
            this.state = mState;
            this.orderStateTime = mOrderStateTime;
            this.isAchieved = isAchieved;
        }

        public String getState() {
            return state;
        }

        public String getOrderStateTime() {
            return orderStateTime;
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
        String stat = order.state;
        if (stat.equals(res.getString(R.string.order_canceled))) {
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_canceled_text),
                    "", true)
            );
            handlerLayout.setVisibility(GONE);
        } else {
            int stateCode = 0;
            if (stat.equals(res.getString(R.string.order_active))) {
                stateCode = 1;
                handlerLayout.setVisibility(GONE);
                cancelButton.post(() -> cancelButton.setVisibility(View.VISIBLE));
                editButton.post(() -> editButton.setVisibility(View.VISIBLE));
            } else if (stat.equals(res.getString(R.string.order_accepted))) {
                stateCode = 2;
                abortButton.post(() -> abortButton.setVisibility(View.VISIBLE));
                mHandler.post(getLocationRunnable);
            } else if (stat.equals(res.getString(R.string.order_finished))) {
                stateCode = 3;
                locationState.post(() -> locationState.setVisibility(View.GONE));
                assessButton.post(() -> assessButton.setVisibility(View.VISIBLE));
            } else if (stat.equals(res.getString(R.string.order_assessed))) {
                stateCode = 4;
                locationState.post(() -> locationState.setVisibility(View.GONE));
            }
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_active_text),
                    order.getCreateTime(), stateCode >= 1));
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_accepted_text),
                    order.getAcceptTime(), stateCode >= 2 ));
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_finished_text),
                    order.getFinishTime(), stateCode >= 3));
            status.add(new OrderStatusModel(
                    res.getString(R.string.order_assessed_text),
                    "", stateCode == 4));
        }
        return status;
    }

    private void bindButtonEvent(int orderId) {
        Resources res = CustomerDetailActivity.this.getResources();

        assessButton.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDetailActivity.this, AssessActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            intent.putExtra("HANDLER_ID", order.employee_id);
            startActivityForResult(intent, ASSESS_CODE);
        });

        cancelButton.setOnClickListener(v -> {
            JsonObjectRequest req = RequestFactory.getOrderOperationRequest(
                    orderId, Order.OperationType.CANCEL, res.getString(R.string.url),
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                cancelButton.setVisibility(GONE);
                                editButton.setVisibility(GONE);
                                arrayOfStatus.clear();
                                arrayOfStatus.add(new OrderStatusModel(
                                        res.getString(R.string.order_canceled_text),
                                        "", true)
                                );
                                OrderStatusAdapter adapter = new OrderStatusAdapter(this, R.layout.item_order_state, arrayOfStatus);
                                ListView listView = findViewById(R.id.state_list);
                                listView.setAdapter(adapter);
                            } else {
                                String error = response.getString("error_msg");
                                Snackbar.make(bottomSheet, error, Snackbar.LENGTH_SHORT).show();
                                Log.d("Error Msg", error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d("HandlerDetail", "Fail " + error.getMessage())
            );
            if (req != null) {
                MySingleton.getInstance(this).addToRequestQueue(req);
            }
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDetailActivity.this, AddActivity.class);
            intent.putExtra(AddActivity.STATE, AddActivity.ORDER_EDIT);
            intent.putExtra("ORDER_ID", orderId);
            startActivityForResult(intent, EDIT_CODE);
        });

        abortButton.setOnClickListener(v -> {
            JsonObjectRequest req = RequestFactory.getOrderOperationRequest(
                    orderId, Order.OperationType.ABORT, res.getString(R.string.url),
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                abortButton.setVisibility(GONE);
                                cancelButton.setVisibility(View.VISIBLE);
                                editButton.setVisibility(View.VISIBLE);
                                handlerLayout.setVisibility(View.GONE);
                                arrayOfStatus.get(1).isAchieved = false;
                                OrderStatusAdapter adapter = new OrderStatusAdapter(this, R.layout.item_order_state, arrayOfStatus);
                                ListView listView = findViewById(R.id.state_list);
                                listView.setAdapter(adapter);
                            } else {
                                String error = response.getString("error_msg");
                                Snackbar.make(bottomSheet, error, Snackbar.LENGTH_SHORT).show();
                                Log.d("Error Msg", error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d("HandlerDetail", "Fail " + error.getMessage())
            );
            if (req != null) {
                MySingleton.getInstance(this).addToRequestQueue(req);
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ASSESS_CODE) {
            if (resultCode == RESULT_OK) {
                assessButton.setVisibility(GONE);
                orderStatusList(orderId);
                Snackbar.make(bottomSheet, "评分成功", Snackbar.LENGTH_SHORT).show();
            }
        }
        if (requestCode == EDIT_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(bottomSheet, "修改成功", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
