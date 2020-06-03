package com.example.thelp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.data.Order;
import com.example.data.OrderAdapter;

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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter adapter;
    private String defaultAvatar;
    private EndlessOnScrollListener listener;

    public OrderListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment OrderListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderListFragment newInstance(String param1) {
        OrderListFragment fragment = new OrderListFragment();
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

    // TODO: acquire order list here
    // param suggests whether it's customer or rider order
    private void setupOrderList(String param) {
        for (int i = 0; i < 10; i++) {
            orderList.add(new Order(param + "订单" + String.valueOf(i), i,
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

    private void setupRecycler(View view) {
        setupOrderList(this.mParam1);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new OrderAdapter(orderList);
        recyclerView.setAdapter(adapter);
        listener = new EndlessOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                // getMoreData(currentPage);
                // TODO: setup the scroller
            }
        };
        recyclerView.addOnScrollListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        setupRecycler(view);
        return view;
    }
}
