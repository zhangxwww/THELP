package com.example.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.thelp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Order> orderList;
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView orderImage;
        TextView orderTitle;
        TextView orderType;
        TextView orderDetail;
        MaterialButton viewButton;
        TextView orderTime;
        TextView orderPublish;

        public ViewHolder(View view) {
            super(view);
            orderImage = view.findViewById(R.id.order_image);
            orderTitle = view.findViewById(R.id.order_title);
            orderType = view.findViewById(R.id.order_type);
            orderDetail = view.findViewById(R.id.order_detail);
            orderTime = view.findViewById(R.id.order_time);
            orderPublish = view.findViewById(R.id.order_publish);
            viewButton = view.findViewById(R.id.view_button);
        }
    }

    public OrderAdapter(List<Order> orders) {
        orderList = orders;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_order, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderTitle.setText(order.getTitle());
        holder.orderType.setText(order.getType());
        holder.orderDetail.setText(order.getDetail());
        holder.orderPublish.setText(order.getEmployer());
        holder.orderTime.setText(order.getTime());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
