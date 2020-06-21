package com.example.thelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.example.data.Order;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbs.android.avatarimageview.library.AvatarImageView;

public class HandlerDetailActivity extends AppCompatActivity {
    RelativeLayout bottomSheet;
    BottomSheetBehavior<RelativeLayout> behavior;
    String picUrl = "https://overwatch.nosdn.127.net/2/heroes/Echo/hero-select-portrait.png";
    AvatarImageView aiv;
    boolean isSharingLocation = false;
    private LocationClient locationClient = null;
    private int orderId;

    @BindView(R.id.order_title_tv)
    TextView orderTitleView;

    @BindView(R.id.order_location_tv)
    TextView orderLocationView;

    @BindView(R.id.order_time_tv)
    TextView orderTimeView;

    @BindView(R.id.order_reward_tv)
    TextView orderRewardView;

    @BindView(R.id.order_details_tv)
    TextView orderDetailView;

    @BindView(R.id.order_name_tv)
    TextView orderNameView;

    @BindView(R.id.order_ctime_tv)
    TextView orderCreateTimeView;

    @BindView(R.id.button_accept)
    Button acceptButton;

    @BindView(R.id.button_finish)
    Button finishButton;

    @BindView(R.id.button_share_location)
    Button shareLocationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler_detail);
        ButterKnife.bind(this);
        //底部抽屉栏展示地址
        bottomSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        aiv = (AvatarImageView) this.findViewById(R.id.order_avatar_image);
        setBottomSheet();
        Glide
                .with(this)
                .load(picUrl)
                .centerCrop()
                .into(aiv);

        orderId = Objects.requireNonNull(
                getIntent().getExtras()).getInt("ORDER_ID");
        new Thread(() ->
                getOrderInfo(orderId))
                .start();
        initLocationOption();
        bindButtonEvent(orderId);
    }

    private void showOrderInfo(Order order) {
        orderTitleView.post(() -> orderTitleView.setText(order.title));
        orderLocationView.post(() -> orderLocationView.setText(order.targetLocation));
        orderTimeView.post(() -> orderTimeView.setText(order.startTime));
        orderRewardView.post(() -> orderRewardView.setText(String.valueOf(order.reward)));
        orderDetailView.post(() -> orderDetailView.setText(order.detail));
        orderNameView.post(() -> orderNameView.setText(order.employer));
        orderCreateTimeView.post(() -> orderCreateTimeView.setText(order.createTime));
        int myUserId = ((myApplication) getApplicationContext()).getUserInfo().userId;
        if (order.employee_id == myUserId) {
            if (order.state.equals(getResources().getString(R.string.order_accepted))) {
                acceptButton.post(() -> {
                    acceptButton.setText(R.string.order_accepted_text);
                    acceptButton.post(() -> acceptButton.setVisibility(View.GONE));
                    finishButton.post(() -> finishButton.setVisibility(View.VISIBLE));
                    shareLocationButton.post(() -> shareLocationButton.setVisibility(View.VISIBLE));
                });
            } else if (order.state.equals(getResources().getString(R.string.order_finished))) {
                finishButton.post(() -> finishButton.setVisibility(View.GONE));
                shareLocationButton.post(() -> shareLocationButton.setVisibility(View.GONE));
            } else {
                // acceptButton.post(() -> acceptButton.setVisibility(View.VISIBLE));
            }
        } else {
            acceptButton.post(() -> acceptButton.setVisibility(View.VISIBLE));
        }

        ImageView orderImage = findViewById(R.id.order_image);
        ImageView orderTypeIcon = findViewById(R.id.order_type_icon);
        if (order.getType().equals(Order.types[0])) {
            orderImage.setImageResource(R.drawable.bicycle);
            orderTypeIcon.setBackgroundResource(R.drawable.ic_ordertype1);
        } else if (order.getType().equals(Order.types[1])) {
            orderImage.setImageResource(R.drawable.borrow);
            orderTypeIcon.setBackgroundResource(R.drawable.ic_ordertype2);
        } else if (order.getType().equals(Order.types[2])) {
            orderImage.setImageResource(R.drawable.find);
            orderTypeIcon.setBackgroundResource(R.drawable.ic_ordertype3);
        } else if (order.getType().equals(Order.types[3])) {
            orderImage.setImageResource(R.drawable.learn);
            orderTypeIcon.setBackgroundResource(R.drawable.ic_ordertype4);
        } else {
            orderTypeIcon.setBackgroundResource(R.drawable.ic_ordertype5);
        }
    }

    private void showCustomerInfo(UserInfo userInfo) {
        Glide
                .with(this)
                .load(userInfo.avatar)
                .centerCrop()
                .into(aiv);
    }

    private void getCustomerInfo(int customerId) {
        JsonObjectRequest req = RequestFactory.getUserInfoRequest(
                customerId,
                getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            UserInfo userInfo = UserInfo.parseFromJSONResponse(response);
                            showCustomerInfo(userInfo);
                            aiv.setOnClickListener(v -> {
                                Intent intent = new Intent(HandlerDetailActivity.this, PersonActivity.class);
                                intent.putExtra(UserInfo.USER_IDENTIFICATION, UserInfo.USER_OTHERS);
                                intent.putExtra(UserInfo.USER_INFO, userInfo);
                                startActivity(intent);
                            });
                        } else {
                            String error = response.getString("error_msg");
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("HandlerDetailUserInfo", "Fail " + error.getMessage())
        );
        if (req != null) {
            MySingleton.getInstance(this).addToRequestQueue(req);
        }
    }

    private void getOrderInfo(int orderId) {
        JsonObjectRequest req = RequestFactory.getOrderOperationRequest(
                orderId,
                Order.OperationType.DETAIL,
                getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            Order order = Order.parseFromJSONResponse(response, orderId);
                            showOrderInfo(order);
                            getCustomerInfo(order.employer_id);
                        } else {
                            String error = response.getString("error_msg");
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
    }

    private void bindButtonEvent(int orderId) {
        acceptButton.setOnClickListener(v -> {
            Order.OperationType type = Order.OperationType.ACCEPT;
            JsonObjectRequest req = RequestFactory.getOrderOperationRequest(
                    orderId, type, getResources().getString(R.string.url),
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                finishButton.setVisibility(View.VISIBLE);
                                shareLocationButton.setVisibility(View.VISIBLE);
                                acceptButton.setVisibility(View.GONE);
                                Snackbar.make(bottomSheet, "接单成功", Snackbar.LENGTH_SHORT).show();
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
        finishButton.setOnClickListener(v -> {
            Order.OperationType type = Order.OperationType.FINISH;
            JsonObjectRequest req = RequestFactory.getOrderOperationRequest(
                    orderId, type, getResources().getString(R.string.url),
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                finishButton.setVisibility(View.GONE);
                                shareLocationButton.setVisibility(View.GONE);
                                acceptButton.setVisibility(View.GONE);
                                Snackbar.make(bottomSheet, "成功完成", Snackbar.LENGTH_SHORT).show();
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
        shareLocationButton.setOnClickListener(v -> {
            // TODO
            if (isSharingLocation) {
                locationClient.stop();
                shareLocationButton.post(()->shareLocationButton.setText(R.string.share_location_button_text));
                isSharingLocation = false;
                JsonObjectRequest req = RequestFactory.shareHandlerLocationRequest(
                        orderId,
                        0,
                        0,
                        getResources().getString(R.string.url),
                        response -> {
                            try {
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    Log.d("ShareLocation","Success");
                                } else {
                                    String error = response.getString("error_msg");
                                    Log.d("ShareLocation Error", error);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.d("ShareLocation", "Fail " + error.getMessage())
                );
                if (req != null) {
                    MySingleton.getInstance(HandlerDetailActivity.this).addToRequestQueue(req);
                }
            } else {
                locationClient.start();
                shareLocationButton.post(()->shareLocationButton.setText(R.string.stop_share_location_button_text));
                isSharingLocation = true;

            }
        });
    }


    private void initLocationOption() {
        Log.e("initLocationOption:", "true");
        locationClient = new LocationClient(getApplicationContext());
        MyLocationListener myLocationListener = new MyLocationListener();
        locationClient.registerLocationListener(myLocationListener);

        LocationClientOption locationOption = new LocationClientOption();

        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationOption.setCoorType("bd0911");
        locationOption.setScanSpan(10000);


        locationOption.setIsNeedAddress(true);
        locationOption.setIsNeedLocationDescribe(true);
        locationOption.setIsNeedLocationDescribe(true);

        locationOption.setOpenGps(true);
        locationClient.setLocOption(locationOption);
    }

    /**
     * 实现定位回调
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {

            //获取纬度信息
            double latitude = location.getLatitude();
            Log.e("Location latitude:", String.valueOf(latitude));
            //获取经度信息
            double longitude = location.getLongitude();
            Log.e("Location longitude:", String.valueOf(longitude));
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            int errorCode = location.getLocType();
            String locationDescp = location.getLocationDescribe();
            Log.e("Location Descp:", locationDescp);

            JsonObjectRequest req = RequestFactory.shareHandlerLocationRequest(
                    orderId,
                    latitude,
                    longitude,
                    getResources().getString(R.string.url),
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                Log.d("ShareLocation","Success");
                            } else {
                                String error = response.getString("error_msg");
                                Log.d("ShareLocation Error", error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d("ShareLocation", "Fail " + error.getMessage())
            );
            if (req != null) {
                MySingleton.getInstance(HandlerDetailActivity.this).addToRequestQueue(req);
            }
        }
    }

    void setBottomSheet() {
        TextView detailsTv = findViewById(R.id.order_details_tv);
        ViewGroup.LayoutParams lp = detailsTv.getLayoutParams();
        detailsTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        int oldHeight = lp.height;
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
                String state = "null";
                switch (newState) {
                    case 1:
                        state = "STATE_DRAGGING";//过渡状态此时用户正在向上或者向下拖动bottom sheet
                        break;
                    case 2:
                        state = "STATE_SETTLING"; // 视图从脱离手指自由滑动到最终停下的这一小段时间
                        lp.height = oldHeight;
                        detailsTv.setLayoutParams(lp);
                        break;
                    case 3:
                        state = "STATE_EXPANDED"; //处于完全展开的状态
//                        Log.d("state", "expanded:" );
//                        TextView detailsTv = findViewById(R.id.order_details_tv);
//                        int oldH = detailsTv.getHeight();
//                        Log.d("oldHeight", "height:"+oldH );
//                        Log.d("getLineCount", "height:"+detailsTv.getLineCount() );
//                        Log.d("getLineHeight", "height:"+detailsTv.getLineHeight() );

                        lp.height = oldHeight + 300;
                        detailsTv.setLayoutParams(lp);
//                        int newH = detailsTv.getHeight();
//                        Log.d("newHeight", "height:"+newH );
                        break;
                    case 4:
                        state = "STATE_COLLAPSED"; //默认的折叠状态
                        lp.height = oldHeight;
                        detailsTv.setLayoutParams(lp);
                        break;
                    case 5:
                        state = "STATE_HIDDEN"; //下滑动完全隐藏 bottom sheet
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Log.d("BottomSheetDemo", "slideOffset:" + slideOffset);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isSharingLocation) {
            locationClient.stop();
        }
    }
}
