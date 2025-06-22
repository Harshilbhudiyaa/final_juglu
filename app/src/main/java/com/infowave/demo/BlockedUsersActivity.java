package com.infowave.demo;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.infowave.demo.models.BlockedUser;
import com.infowave.demo.adapters.BlockedUsersAdapter;

public class BlockedUsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<BlockedUser> blockedUsers;
    BlockedUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);
        View decoreview = getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                v.setPadding(left,top,right,bottom);
                return insets.consumeSystemWindowInsets();

            }
        });
        recyclerView = findViewById(R.id.blockedUsersRecyclerView);

        blockedUsers = new ArrayList<>();
        blockedUsers.add(new BlockedUser("Meera Singh", "Blocked for inappropriate messages", R.drawable.image1));
        blockedUsers.add(new BlockedUser("Ravi Patel", "Blocked for spamming", R.drawable.image2));
        blockedUsers.add(new BlockedUser("Aanya Jain", "Blocked for privacy violation", R.drawable.image3));

        adapter = new BlockedUsersAdapter(this, blockedUsers);
        recyclerView.setAdapter(adapter);
    }
}
