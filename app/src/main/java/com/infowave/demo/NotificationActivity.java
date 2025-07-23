package com.infowave.demo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.adapters.NotificationAdapter2;
import com.infowave.demo.models.NotificationEvent;
import com.infowave.demo.models.NotificationItem;
import com.infowave.demo.models.NotificationListItem;
import com.infowave.demo.models.SectionHeader;
import com.infowave.demo.supabase.FollowRepository;
import com.infowave.demo.supabase.NotificationRepository;
import com.infowave.demo.adapters.NotificationGrouper;
import com.infowave.demo.adapters.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter2 adapter;
    private final List<NotificationListItem> uiList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifications = findViewById(R.id.recyclerViewNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter2(this, uiList, new NotificationAdapter2.OnActionClickListener() {
            @Override
            public void onAcceptClicked(NotificationItem item, int position) {
                if (item.getFriendshipId() == null) return;
                FollowRepository.acceptRequest(NotificationActivity.this, item.getFriendshipId(),
                        new FollowRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(NotificationActivity.this, "Accepted", Toast.LENGTH_SHORT).show();
                                fetchData();
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(NotificationActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onDeclineClicked(NotificationItem item, int position) {
                if (item.getFriendshipId() == null) return;
                FollowRepository.deleteFriendship(NotificationActivity.this, item.getFriendshipId(),
                        new FollowRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(NotificationActivity.this, "Declined", Toast.LENGTH_SHORT).show();
                                fetchData();
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(NotificationActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        rvNotifications.setAdapter(adapter);

        fetchData();
    }

    private void fetchData() {
        NotificationRepository.fetchNotifications(this, new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(List<NotificationEvent> events) {
                buildUiList(events);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(NotificationActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                Log.e("NOTIF_ACTIVITY", error);
            }
        });
    }

    private void buildUiList(List<NotificationEvent> events) {
        List<NotificationEvent> grouped = NotificationGrouper.groupLikes(events);
        Collections.sort(grouped, (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        uiList.clear();

        String lastHeader = null;
        for (NotificationEvent ev : grouped) {
            long millis = TimeUtils.parseIsoToMillis(ev.getCreatedAt());
            String section = TimeUtils.getSectionTitle(millis);
            if (!section.equals(lastHeader)) {
                uiList.add(new SectionHeader(section));
                lastHeader = section;
            }

            NotificationItem.Action act;
            switch (ev.getType()) {
                case FOLLOW_REQUEST:  act = NotificationItem.Action.FOLLOW_REQUEST; break;
                case FOLLOW_ACCEPTED: act = NotificationItem.Action.ACCEPTED; break;
                case LIKE_POST:       act = NotificationItem.Action.LIKE_POST; break;
                case COMMENT_POST:    act = NotificationItem.Action.COMMENT_POST; break;
                case LIKE_STORY:      act = NotificationItem.Action.LIKE_STORY; break;
                case COMMENT_STORY:   act = NotificationItem.Action.COMMENT_STORY; break;
                default:              act = NotificationItem.Action.LIKE;
            }

            String timeAgo = TimeUtils.getTimeAgo(ev.getCreatedAt());

            NotificationItem item = new NotificationItem(
                    ev.getFromUserName(),
                    ev.getMessage(),
                    timeAgo,
                    0,
                    act
            ).setAvatarUrl(ev.getFromUserAvatarUrl())
                    .setThumbUrl(ev.getThumbnailUrl())
                    .setFriendshipId(ev.getFriendshipId())
                    .setRawId(ev.getId())
                    .setIsoCreatedAt(ev.getCreatedAt());

            uiList.add(item);
        }

        adapter.notifyDataSetChanged();
    }
}
