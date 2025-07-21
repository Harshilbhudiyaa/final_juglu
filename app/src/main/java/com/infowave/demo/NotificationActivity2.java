package com.infowave.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.adapters.NotificationAdapter;
import com.infowave.demo.adapters.NotificationAdapter2;
import com.infowave.demo.models.NotificationItem;
import com.infowave.demo.models.NotificationListItem;
import com.infowave.demo.models.SectionHeader;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity2 extends AppCompatActivity {

    private RecyclerView rvNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification2);

        rvNotifications = findViewById(R.id.recyclerViewNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        List<NotificationListItem> data = buildDummyData();
        NotificationAdapter2 adapter = new NotificationAdapter2(this, data);
        rvNotifications.setAdapter(adapter);
    }

    private List<NotificationListItem> buildDummyData() {
        List<NotificationListItem> list = new ArrayList<>();

        list.add(new SectionHeader("Today"));
        list.add(new NotificationItem("hetal.savaliya.5055",
                "started following you", "3h", R.drawable.image1,
                NotificationItem.Action.FOLLOW_REQUEST));
        list.add(new NotificationItem("ii._.sahil..radadiya_..ii",
                "started following you", "3h", R.drawable.image1,
                NotificationItem.Action.FOLLOW_REQUEST));
        list.add(new NotificationItem("mr_patoliya_007",
                "started following you", "3h", R.drawable.image1,
                NotificationItem.Action.FOLLOW_REQUEST));

        list.add(new SectionHeader("Yesterday"));
        list.add(new NotificationItem("het_vaja_25",
                "posted a thread you might like", "14h", R.drawable.image1,
                NotificationItem.Action.STORY));
        list.add(new NotificationItem("m.i.l.u.__25",
                "liked your post", "20h", R.drawable.image1,
                NotificationItem.Action.LIKE));

        list.add(new SectionHeader("Last 7 days"));
        list.add(new NotificationItem("hetal.kumbhani.73",
                "started following you", "1d", R.drawable.image1,
                NotificationItem.Action.FOLLOWING));
        list.add(new NotificationItem("kartavya__5157",
                "started following you", "3d", R.drawable.image1,
                NotificationItem.Action.FOLLOWING));
        list.add(new NotificationItem("vanshmandanka, ansh.sherasiya and 1 other",
                "accepted your follow request", "3d", R.drawable.image1,
                NotificationItem.Action.ACCEPTED));
        list.add(new NotificationItem("mr_.sojitra._1276",
                "started following you", "4d", R.drawable.image1,
                NotificationItem.Action.FOLLOWING));

        return list;
    }
}
