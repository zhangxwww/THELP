package com.example.thelp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.data.Order;
import com.example.data.OrderAdapter;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ORDER_STATE = "ORDER_STATE";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter adapter;
    private String defaultAvatar;
    private EndlessOnScrollListener listener;
    private Context ctx;

    public OrderListFragment(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment OrderListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderListFragment newInstance(String param1, Context ctx) {
        OrderListFragment fragment = new OrderListFragment(ctx);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        defaultAvatar = "https://overwatch.nosdn.127.net/2/heroes/Sigma/hero-select-portrait.png";
    }

    private void setupRecycler(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
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

    private void showOrderDetail(Order order) {
        UserInfo userInfo = ((myApplication) ctx.getApplicationContext()).getUserInfo();
        int id = userInfo.userId;
        Intent intent;
        if (id == order.getEmployerId()) {
            intent = new Intent(ctx, CustomerDetailActivity.class);
        } else {
            intent = new Intent(ctx, HandlerDetailActivity.class);
        }
        intent.putExtra("ORDER_ID", order.getOrderId());
        intent.putExtra(ORDER_STATE, Order.ORDER_ACCEPTED);
        startActivity(intent);
    }

    private void setupRefreshLayout(View view) {
        SwipeRefreshLayout layout = view.findViewById(R.id.layout_swipe_refresh);
        layout.setOnRefreshListener(() -> {
            layout.setRefreshing(true);
            updateActivityList();
            layout.setRefreshing(false);
        });
    }

    private void getMoreData(int page) {
        requestOrderInPage(page, false);
    }

    private void updateActivityList() {
        Log.d("UPDATE ACTIVITY LIST", "called");
        listener.reset();
        requestOrderInPage(1, true);
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

    private void requestOrderInPage(int page, boolean refresh) {
        Order.HistoryType historyType = (mParam1.equals("骑手"))
                ? Order.HistoryType.HANDLER
                : Order.HistoryType.CREATE;
        JsonObjectRequest req = RequestFactory.getOrderHistoryRequest(
                page, historyType, getResources().getString(R.string.url),
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
                            String error = response.getString("error_msg");
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("OrderListFragment", "Fail " + error.getMessage())
        );
        if (req != null) {
            MySingleton.getInstance(ctx).addToRequestQueue(req);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        setupRecycler(view);
        setupRefreshLayout(view);
        new Thread(this::updateActivityList).start();
        return view;
    }
}
