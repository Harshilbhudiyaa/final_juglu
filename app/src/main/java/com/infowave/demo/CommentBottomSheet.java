package com.infowave.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.infowave.demo.adapters.CommentAdapter;
import com.infowave.demo.models.Comment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView commentsRecycler;
    private EditText commentInput;
    private ImageButton sendButton;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    public static CommentBottomSheet newInstance(String postId) {
        return new CommentBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        commentsRecycler = view.findViewById(R.id.comments_recycler);
        commentInput = view.findViewById(R.id.comment_input);
        sendButton = view.findViewById(R.id.send_button);

        // Sample data
        commentList = new ArrayList<>();
        commentList.add(new Comment("John", "Nice pic!", "2h", R.drawable.image1));
        commentList.add(new Comment("Emma", "Amazing ❤️", "4h", R.drawable.image2));
        commentList.add(new Comment("Mike", "Love this!", "1d", R.drawable.image3));

        commentAdapter = new CommentAdapter(commentList);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecycler.setAdapter(commentAdapter);

        sendButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                commentList.add(new Comment("You", commentText, "Now", R.drawable.default_profile));
                commentAdapter.notifyItemInserted(commentList.size() - 1);
                commentInput.setText("");
                commentsRecycler.scrollToPosition(commentList.size() - 1);
            } else {
                Toast.makeText(getContext(), "Enter comment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
