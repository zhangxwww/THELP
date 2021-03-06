package com.example.thelp;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.data.Order;
import com.example.data.OrderAdapter;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.example.websocket.ChatMessageReceiver;
import com.example.websocket.JWebSocketClientService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnDateSetListener {
    private Context mContext;
    private String defaultAvatar;
    private Drawer drawer;
    private MaterialSearchView searchView;
    private TimePickerDialog startTimePickerDialog;
    private TimePickerDialog endTimePickerDialog;
    private TextInputLayout startTime;
    private TextInputLayout endTime;
    private TextInputLayout type;
    private TextInputLayout reward;
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private AutoCompleteTextView orderTypeDropDownMenu;

    private List<Order> orderList = new ArrayList<>();
    private EndlessOnScrollListener listener;
    private OrderAdapter adapter;
    private SearchCondition searchCondition = null;
    private LinearLayout searchConditionLayout = null;

    private static final int ADD_ACTIVITY_REQUEST = 233;
    private static final int CUSTOMER_DETAIL_REQUEST = 123;
    private static final int PERSON_REQUEST = 110;
    private static final int LOG_ACT_REQUEST = 151;

    private ChatMessageReceiver chatMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpLoginAct();
    }

    private void setUpLoginAct() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(intent,LOG_ACT_REQUEST);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new OrderAdapter(orderList);
        adapter.setOnDetailClickListener(this::showOrderDetail);
        recyclerView.setAdapter(adapter);
        listener = new EndlessOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                getMoreData(currentPage);
            }
        };
        recyclerView.addOnScrollListener(listener);
    }

    private void setupSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchConditionLayout.setVisibility(View.GONE);
                if (!query.toLowerCase().equals("all")) {
                    searchCondition.title = query;
                }

                String order_type = Objects.requireNonNull(type.getEditText()).getEditableText().toString();
                String order_reward = Objects.requireNonNull(reward.getEditText()).getEditableText().toString();
                String order_start = Objects.requireNonNull(startTime.getEditText()).getEditableText().toString();
                String order_end = Objects.requireNonNull(endTime.getEditText()).getEditableText().toString();

                if (!order_type.equals(getString(R.string.default_no_limit_type))){
                    searchCondition.type = order_type;
                }
                if (!order_reward.equals(String.valueOf(0))){
                    searchCondition.reward_inf = Integer.parseInt(order_reward);
                    Log.e("reward.inf = ", order_reward);
                }
                if (!order_start.equals(getString(R.string.default_no_limit_time))){
                    searchCondition.start_time = order_start;
                }
                if (!order_end.equals(getString(R.string.default_no_limit_time))){
                    searchCondition.end_time = order_end;
                }
                searchView.closeSearch();
                updateActivityList();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                Log.d("Query Text Change", "-----------");
                searchConditionLayout.setVisibility(View.VISIBLE);
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
                Log.d("Search View Shown", "-----------");
                if (searchCondition == null) {
                    searchCondition = new SearchCondition();
                } else {
                    searchView.setQuery(searchCondition.title, false);
                }
                searchConditionLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
                Log.d("Search View Closed", "-----------");
                searchConditionLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDateSet(TimePickerDialog dialog, long count) {
        String text = getDateToString(count);
        if (dialog == startTimePickerDialog) {
            Objects.requireNonNull(startTime.getEditText()).setText(text);
        } else if (dialog == endTimePickerDialog) {
            Objects.requireNonNull(endTime.getEditText()).setText(text);
        }
    }

    public String getDateToString(long time) {
        Date d = new Date(time);
        return sf.format(d);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        searchView = findViewById(R.id.search_view);
        searchView.setMenuItem(item);

        return true;
    }

    private void setupActionBar() {
        Toolbar myToolbar = findViewById(R.id.app_bar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawer.isDrawerOpen()) {
                    drawer.openDrawer();
                }
            }
        });
    }

    private void setupDrawer(String name, String email, String avatar) {
        avatar = defaultAvatarIfNull(avatar);
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }
        });
        new DrawerBuilder().withActivity(this).build();
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.home).withIcon(GoogleMaterial.Icon.gmd_home);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.center).withIcon(GoogleMaterial.Icon.gmd_person);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.order).withIcon(GoogleMaterial.Icon.gmd_history);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.pm).withIcon(GoogleMaterial.Icon.gmd_message);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withOnlyMainProfileImageVisible(true)
                .withHeaderBackground(R.color.colorBackground)
                .addProfiles(
                        new ProfileDrawerItem().withName(name).withEmail(email).withIcon(avatar)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return true;
                    }
                })
                .build();
        drawer = new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        item4
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        int id = (int) drawerItem.getIdentifier();
                        Intent intent = null;
                        if (id == 2) {
                            intent = new Intent(MainActivity.this, PersonActivity.class);
                            intent.putExtra(UserInfo.USER_IDENTIFICATION, UserInfo.USER_SELF);
                            startActivityForResult(intent, PERSON_REQUEST);
                        } else if (id == 3) {
                            intent = new Intent(MainActivity.this, HistoryActivity.class);
                            startActivity(intent);
                        } else if (id == 4) {
                            intent = new Intent(MainActivity.this, MessageActivity.class);
                            startActivity(intent);
                        }
                        return false;
                    }
                })
                .withAccountHeader(headerResult)
                .build();
    }

    private void setupAddActivityButton() {
        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            intent.putExtra(AddActivity.STATE, AddActivity.ORDER_CREATE);
            startActivityForResult(intent, ADD_ACTIVITY_REQUEST);
        });
    }

    private void setupRefreshLayout() {
        SwipeRefreshLayout layout = findViewById(R.id.layout_swipe_refresh);
        layout.setOnRefreshListener(() -> {
            layout.setRefreshing(true);
            updateActivityListWithNoSearchCondition();
            layout.setRefreshing(false);
        });
    }

    private void setupSearchConditionButtons() {
        startTime = findViewById(R.id.order_start_time);
        endTime = findViewById(R.id.order_end_time);
        long tenYears = 10L * 365 * 1000 * 60 * 60 * 24L;

        startTimePickerDialog = new TimePickerDialog.Builder()
                .setCallBack(this)
                .setCancelStringId("取消")
                .setSureStringId("确认")
                .setTitleStringId("开始时间")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + tenYears)
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(getResources().getColor(R.color.colorAccent))
                .setType(Type.ALL)
                .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(16)
                .build();

        endTimePickerDialog = new TimePickerDialog.Builder()
                .setCallBack(this)
                .setCancelStringId("取消")
                .setSureStringId("确认")
                .setTitleStringId("结束时间")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + tenYears)
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(getResources().getColor(R.color.colorAccent))
                .setType(Type.ALL)
                .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(16)
                .build();

        Button reviseStartTimeButton = findViewById(R.id.button_revise_start_time);
        Button reviseEndTimeButton = findViewById(R.id.button_revise_end_time);

        reviseStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimePickerDialog.show(getSupportFragmentManager(), "month_day_hour_minute");
            }
        });
        reviseEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTimePickerDialog.show(getSupportFragmentManager(), "month_day_hour_minute");
            }
        });

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(MainActivity.this,
                        R.layout.item_dropdownmenu,
                        Order.types);

        orderTypeDropDownMenu = findViewById(R.id.type_dropdownmenu);
        orderTypeDropDownMenu.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOG_ACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                // TODO
                setContentView(R.layout.activity_main);
                mContext = MainActivity.this;
                defaultAvatar = "https://overwatch.nosdn.127.net/2/heroes/Echo/hero-select-portrait.png";
                searchConditionLayout = findViewById(R.id.search_condition);
                type = findViewById(R.id.order_type);
                reward = findViewById(R.id.order_reward);
                setupActionBar();
                setupSearchView();
                setupRecyclerView();
                setupAddActivityButton();
                setupRefreshLayout();
                setupSearchConditionButtons();
                new Thread(this::getUserInfo).start();
                new Thread(this::updateActivityList).start();

                startJWebSClientService();
                doRegisterReceiver();
                //检测通知是否开启
//        checkNotification(mContext);
            }
        }
        if (requestCode == ADD_ACTIVITY_REQUEST || requestCode == CUSTOMER_DETAIL_REQUEST) {
            if (resultCode == RESULT_OK) {
                updateActivityList();
            }
        }
        if (requestCode == PERSON_REQUEST) {
            if (resultCode == RESULT_OK) {
                UserInfo userInfo = ((myApplication) getApplicationContext()).getUserInfo();
                setupDrawer(userInfo.nickName, userInfo.phone, userInfo.avatar);
            }
        }
    }

    private void getUserInfo() {
        if (!getUserInfoFromLocal()) {
            UserInfo.setUrl(getResources().getString(R.string.url));
            getUserInfoFromRemote();
        }
    }

    private boolean getUserInfoFromLocal() {
        UserInfo userInfo = ((myApplication) getApplicationContext()).getUserInfo();
        if (userInfo == null) {
            return false;
        }
        setupDrawer(userInfo.nickName, userInfo.phone, userInfo.avatar);
        return true;
    }

    private void getUserInfoFromRemote() {

        JsonObjectRequest infoRequest = RequestFactory.getUserInfoRequest(
                null,
                MainActivity.this.getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            myApplication myApp = (myApplication) getApplicationContext();
                            UserInfo userInfo = UserInfo.parseFromJSONResponse(response);
                            myApp.setUserInfo(userInfo);
                            setupDrawer(userInfo.nickName, userInfo.phone, userInfo.avatar);
                        } else {
                            CoordinatorLayout cl = findViewById(R.id.main_background);
                            String error = response.getString("error_msg");
                            Snackbar.make(cl, error, Snackbar.LENGTH_SHORT).show();
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("INFO", "Fail " + error.getMessage())
        );

        if (infoRequest != null) {
            MySingleton.getInstance(this).addToRequestQueue(infoRequest);
        }
    }



    private void updateActivityListWithNoSearchCondition() {
        Log.d("UPDATE ACTIVITY LIST", "called");
        listener.reset();
        requestOrderInPage(1, null, true);
    }

    private void updateActivityList() {
        Log.d("UPDATE ACTIVITY LIST", "called");
        listener.reset();
        requestOrderInPage(1, searchCondition, true);
    }

    private void getMoreData(int page) {
        Log.d("GET MORE DATA", "" + page);
        requestOrderInPage(page, searchCondition, false);
    }

    private void refreshList(List<Order> orderList) {
        this.orderList.clear();
        this.orderList.addAll(orderList);
        adapter.notifyDataSetChanged();
    }

    private void showMoreOrder(List<Order> orderList) {
        int oldSize = this.orderList.size();
        int count = orderList.size();
        this.orderList.addAll(orderList);
        adapter.notifyItemRangeChanged(oldSize, count);
    }

    private void showOrderDetail(Order order) {
        UserInfo userInfo = ((myApplication) getApplicationContext()).getUserInfo();
        int id = userInfo.userId;
        Intent intent;
        if (id == order.getEmployerId()) {
            intent = new Intent(MainActivity.this, CustomerDetailActivity.class);
        } else {
            intent = new Intent(MainActivity.this, HandlerDetailActivity.class);
        }
        intent.putExtra("ORDER_ID", order.getOrderId());
        intent.putExtra(Order.ORDER_STATE, Order.ORDER_NOT_ACCEPTED);
        if (id == order.employer_id) {
            startActivityForResult(intent, CUSTOMER_DETAIL_REQUEST);
        } else {
            startActivity(intent);
        }
    }

    private void requestOrderInPage(int page, SearchCondition sc, boolean refresh) {
        // TODO send request
        Map<String, String> map = new HashMap<>();
        map.put("page", String.valueOf(page));
        int num_each_page = MainActivity.this.getResources().getInteger(R.integer.num_each_page);
        map.put("num_each_page", String.valueOf(num_each_page));
        if (sc != null) {
            map.putAll(sc.getCondition());
        }
        String json = new Gson().toJson(map);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        String url = MainActivity.this.getString(R.string.url) + "/order/homepage";
        JsonObjectRequest request = RequestFactory.getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray list = response.getJSONArray("order_list");
                            int len = list.length();
                            List<Order> orderList = new ArrayList<>();
                            for (int i = 0; i < len; ++i) {
                                JSONObject o = (JSONObject) list.get(i);
                                int id = o.getInt("order_id");
                                String title = o.getString("title");
                                String detail = o.getString("description");
                                String type = o.getString("genre");
                                String employer = o.getString("customer_name");
                                int employer_id = o.getInt("customer_id");
                                String startTime = o.getString("start_time");
                                String endTime = o.getString("end_time");
                                String avatar = o.getString("avatar");
                                avatar = defaultAvatarIfNull(avatar);
                                double reward = o.getDouble("reward");
                                String targetLocation = o.getString("target_location");
                                orderList.add(new Order(title, id, type, detail, employer, employer_id,
                                        startTime, endTime, avatar, reward, targetLocation));
                            }

                            if (refresh) {
                                refreshList(orderList);
                            } else {
                                showMoreOrder(orderList);
                            }
                            if (MainActivity.this.orderList.size() == 0) {
                                CoordinatorLayout cl = findViewById(R.id.main_background);
                                Snackbar.make(cl, R.string.no_search_order, Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            CoordinatorLayout cl = findViewById(R.id.main_background);
                            String error = response.getString("error_msg");
                            Snackbar.make(cl, error, Snackbar.LENGTH_LONG).show();
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("Homepage", "Fail " + error.getMessage())
        );
        MySingleton.getInstance(this).addToRequestQueue(request);
    }

    private String defaultAvatarIfNull(String avatar) {
        if (avatar == null || avatar.equals("null")) {
            return defaultAvatar;
        }
        return avatar;
    }

    private class SearchCondition {
        public String start_time = null, end_time = null;
        public String title = null;
        public String location = null;
        public String type = null;
        public int reward_inf = -1, reward_sup = -1;

        Map<String, String> getCondition() {
            Map<String, String> map = new HashMap<>();
            if (start_time != null) {
                map.put("order_start_time", start_time);
            }
            if (end_time != null) {
                map.put("order_end_time", end_time);
            }
            if (title != null) {
                map.put("order_title", title);
            }
            if (location != null) {
                map.put("order_location", location);
            }
            if (type != null) {
                map.put("order_type", type);
            }
            if (reward_inf >= 0) {
                map.put("order_reward_inf", String.valueOf(reward_inf));
            }
            if (reward_sup >= 0) {
                map.put("order_reward_sup", String.valueOf(reward_sup));
            }
            return map;
        }
    }

    private void startJWebSClientService() {
        Log.e("MainActivity", "Start WebSocketClientService.");
        Intent intent = new Intent(mContext, JWebSocketClientService.class);
        startService(intent);
    }

    private void doRegisterReceiver() {
        if (chatMessageReceiver == null) {
            CoordinatorLayout cl = findViewById(R.id.main_background);
            chatMessageReceiver = new ChatMessageReceiver(cl);
            IntentFilter filter = new IntentFilter("com.example.websocket.receive");
            registerReceiver(chatMessageReceiver, filter);
        }
    }



    /**
     * 检测是否开启通知
     *
     * @param context
     */
    private void checkNotification(final Context context) {
        if (!isNotificationEnabled(context)) {
            new AlertDialog.Builder(context).setTitle("温馨提示")
                    .setMessage("你还未开启系统通知，将影响消息的接收，要去开启吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setNotification(context);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
        }
    }

    /**
     * 如果没有开启通知，跳转至设置界面
     *
     * @param context
     */
    private void setNotification(Context context) {
        Intent localIntent = new Intent();
        //直接跳转到应用通知设置的代码：
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            localIntent.putExtra("app_package", context.getPackageName());
            localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.addCategory(Intent.CATEGORY_DEFAULT);
            localIntent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            }
        }
        context.startActivity(localIntent);
    }

    /**
     * 获取通知权限,监测是否开启了系统通知
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isNotificationEnabled(Context context) {

        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null;
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        doRegisterReceiver();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        unregisterReceiver(chatMessageReceiver);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatMessageReceiver != null) unregisterReceiver(chatMessageReceiver);
        Intent intent = new Intent(mContext, JWebSocketClientService.class);
        stopService(intent);
        Log.e("MAIN ACT","on destroy()");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            Log.e("MAIN ACT","on onKeyDown()");
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
