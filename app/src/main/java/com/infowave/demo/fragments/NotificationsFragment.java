package com.infowave.demo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.infowave.demo.R;
import com.infowave.demo.adapters.NotificationAdapter;
import com.infowave.demo.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadNotifications();
    }

    private void initializeViews(View view) {
        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
    }

    private void setupRecyclerView() {
        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(requireContext(), notifications);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationsRecyclerView.setAdapter(notificationAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshNotifications);
    }

    private void loadNotifications() {
        // TODO: Load actual notifications from your data source
        notifications.clear();
        notifications.add(new Notification("John Doe", "liked your post", "2m ago", Notification.TYPE_LIKE));
        notifications.add(new Notification("Jane Smith", "commented on your post", "15m ago", Notification.TYPE_COMMENT));
        notifications.add(new Notification("Mike Johnson", "started following you", "1h ago", Notification.TYPE_FOLLOW));
        notifications.add(new Notification("Sarah Wilson", "mentioned you in a comment", "2h ago", Notification.TYPE_MENTION));
        notificationAdapter.notifyDataSetChanged();
    }

    private void refreshNotifications() {
        // Simulate network delay
        swipeRefreshLayout.postDelayed(() -> {
            loadNotifications();
            swipeRefreshLayout.setRefreshing(false);
        }, 1500);
    }
} 