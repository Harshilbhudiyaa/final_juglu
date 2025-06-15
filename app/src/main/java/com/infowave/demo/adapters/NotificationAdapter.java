package com.infowave.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context context;
    private List<Notification> notifications;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.userName.setText(notification.getUser());
        holder.action.setText(notification.getAction());
        holder.timestamp.setText(notification.getTimestamp());

        // Set appropriate icon based on notification type
        switch (notification.getType()) {
            case Notification.TYPE_LIKE:
                holder.icon.setImageResource(R.drawable.ic_heart_filled);
                break;
            case Notification.TYPE_COMMENT:
                holder.icon.setImageResource(R.drawable.ic_comment);
                break;
            case Notification.TYPE_FOLLOW:
                holder.icon.setImageResource(R.drawable.ic_person_add);
                break;
            case Notification.TYPE_MENTION:
                holder.icon.setImageResource(R.drawable.ic_mention);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView userName, action, timestamp;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.notification_icon);
            userName = itemView.findViewById(R.id.user_name);
            action = itemView.findViewById(R.id.action);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
} 