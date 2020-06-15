package com.example.data;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thelp.MessageActivity;
import com.example.thelp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import cn.carbs.android.avatarimageview.library.AvatarImageView;
import cn.carbs.android.avatarimageview.library.SquareAvatarImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messageList;
    private Activity curActivity;
    static class ViewHolder extends RecyclerView.ViewHolder {
        SquareAvatarImageView avatar;
        TextView name;
        TextView time;
        TextView content;
        ImageView isRead;
        LinearLayout separator;

        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.avatar);
            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.time);
            content = view.findViewById(R.id.content);
            isRead = view.findViewById(R.id.is_read);
            separator = view.findViewById(R.id.separator);
        }
    }

    public MessageAdapter(List<Message> messages, Activity activity) {
        messageList = messages;
        curActivity = activity;

    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Message message = messageList.get(position);

        Glide
                .with(curActivity)
                .load(message.getAvatar())
                .centerCrop()
                .into(holder.avatar);
        holder.name.setText(message.getNickname());
        holder.time.setText(message.getTime());
        holder.content.setText(message.getContent());
        if (message.isRead()){
            holder.isRead.setVisibility(View.INVISIBLE);
        }else{
            holder.isRead.setVisibility(View.VISIBLE);
        }

        if (position == messageList.size() - 1){
            holder.separator.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
