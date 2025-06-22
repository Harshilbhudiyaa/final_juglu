package com.infowave.demo;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.adapters.FriendsAdapter;
import com.infowave.demo.models.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FriendsAdapter adapter;
    List<Friend> friendList;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
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
        recyclerView = findViewById(R.id.recycler_friends);
        back = findViewById(R.id.back_icon);
        back.setOnClickListener(v -> onBackPressed());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        friendList = new ArrayList<>();
        friendList.add(new Friend("Kunal Bhatt", "5 mutual friends", R.drawable.image1));
        friendList.add(new Friend("Harshita Singh", "2 mutual friends", R.drawable.image2));
        friendList.add(new Friend("Aman Patel", "1 mutual friend", R.drawable.image3));

        adapter = new FriendsAdapter(friendList, friend -> {
            Toast.makeText(this, "Unfollowed " + friend.getName(), Toast.LENGTH_SHORT).show();
            // remove friend or handle logic
        });

        recyclerView.setAdapter(adapter);
    }
}
