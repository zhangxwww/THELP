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
import com.example.data.Order;
import com.example.data.OrderAdapter;
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
    private EndLessOnScrollListener listener;
    private OrderAdapter adapter;
    private SearchCondition searchCondition = null;

    private static final int ADD_ACTIVITY_REQUEST = 233;

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
        getUserInfo();
        updateActivityList();

    }

    private void initOrderList() {
        for (int i = 0; i < 10; i++) {
            orderList.add(new Order("订单" + String.valueOf(i), i,
                    "类型" + String.valueOf(i % 4 + 1),
                    "订单详情" + String.valueOf(i + 1),
                    "发布者" + String.valueOf(i + 1),
                    "2020年7月" + String.valueOf(i) + "日",
                    "2020年8月" + String.valueOf(i) + "日",
                    defaultAvatar,
                    10,
                    "目的地" + String.valueOf(i)));
        }
    }

    private void setupRecyclerView() {
        initOrderList();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new OrderAdapter(orderList);
        recyclerView.setAdapter(adapter);
        listener = new EndLessOnScrollListener(linearLayoutManager) {
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
                        return false;
                    }
                })
                .withAccountHeader(headerResult)
                .build();
    }

    private void setupAddActivityButton() {
        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivityForResult(intent, ADD_ACTIVITY_REQUEST);
            }
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
        if (requestCode == ADD_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                updateActivityList();
            }
        }
    }

    private void getUserInfo() {
        JSONObject jsonObject = new JSONObject();

        String url = MainActivity.this.getString(R.string.url) + "/user/info";
        JsonObjectRequest infoRequest = RequestFactory.getRequest(
                Request.Method.POST,
                url,
                jsonObject,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            String name = response.getString("nickname");
                            String avatar = response.getString("avatar");
                            String phone = response.getString("phone");
                            setupDrawer(name, phone, avatar);
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
        MySingleton.getInstance(this).addToRequestQueue(infoRequest);
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
                                String startTime = o.getString("start_time");
                                String endTime = o.getString("end_time");
                                String avatar = o.getString("avatar");
                                avatar = defaultAvatarIfNull(avatar);
                                double reward = o.getDouble("reward");
                                String targetLocation = o.getString("target_location");
                                orderList.add(new Order(title, id, type, detail, employer,
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

    private abstract static class EndLessOnScrollListener extends RecyclerView.OnScrollListener {
        private LinearLayoutManager linearLayoutManager;
        private int currentPage = 1;
        private int previousTotal = 0;
        private boolean loading = true;

        EndLessOnScrollListener(LinearLayoutManager linearLayoutManager) {
            this.linearLayoutManager = linearLayoutManager;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = recyclerView.getChildCount();
            int totalItemCount = linearLayoutManager.getItemCount();
            int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem) {
                ++currentPage;
                onLoadMore(currentPage);
                loading = true;
            }
        }

        void reset() {
            currentPage = 1;
            previousTotal = 0;
            loading = true;
        }

        public abstract void onLoadMore(int currentPage);
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
