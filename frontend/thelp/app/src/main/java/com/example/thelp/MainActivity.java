package com.example.thelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.data.Message;
import com.example.data.Order;
import com.example.data.OrderAdapter;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String defaultAvatar;
    private Drawer drawer;
    private MaterialSearchView searchView;
    private List<Order> orderList = new ArrayList<>();
    private EndlessOnScrollListener listener;
    private OrderAdapter adapter;
    private SearchCondition searchCondition = null;

    private static final int ADD_ACTIVITY_REQUEST = 233;
    private static final int CUSTOMER_DETAIL_REQUEST = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        defaultAvatar = "https://overwatch.nosdn.127.net/2/heroes/Echo/hero-select-portrait.png";
        setupDrawer("温斯顿", "17777777777", defaultAvatar);
        setupActionBar();
        setupSearchView();
        setupRecyclerView();
        setupAddActivityButton();
        setupRefreshLayout();
        new Thread(this::getUserInfo).start();
        new Thread(this::updateActivityList).start();
    }

    private void initOrderList() {
    }

    private void setupRecyclerView() {
        initOrderList();
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
                Log.d("Query Text Submit", "-----------");
                searchCondition.title = query;
                updateActivityList();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                Log.d("Query Text Change", "-----------");
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
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
                Log.d("Search View Closed", "-----------");
            }
        });
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
                        } else if (id == 3) {
                            intent = new Intent(MainActivity.this, HistoryActivity.class);
                        } else if (id == 4) {
                            intent = new Intent(MainActivity.this, MessageActivity.class);
                        }
                        if (intent != null) startActivity(intent);
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
            startActivityForResult(intent, ADD_ACTIVITY_REQUEST);
        });
    }

    private void setupRefreshLayout() {
        SwipeRefreshLayout layout = findViewById(R.id.layout_swipe_refresh);
        layout.setOnRefreshListener(() -> {
            layout.setRefreshing(true);
            updateActivityList();
            layout.setRefreshing(false);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ACTIVITY_REQUEST || requestCode == CUSTOMER_DETAIL_REQUEST) {
            if (resultCode == RESULT_OK) {
                updateActivityList();
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
            if (reward_inf >= 0) {
                map.put("order_reward_inf", String.valueOf(reward_inf));
            }
            if (reward_sup >= 0) {
                map.put("order_reward_sup", String.valueOf(reward_sup));
            }
            return map;
        }
    }
}
