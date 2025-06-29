// PostsAdapter.java
package com.infowave.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.infowave.demo.R;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private Context context;
    private List<Integer> postImages;
    private OnItemClickListener listener;

    // New constructor
    public PostsAdapter(Context context, List<Integer> postImages, OnItemClickListener listener) {
        this.context = context;
        this.postImages = postImages;
        this.listener = listener;
    }
    public PostsAdapter(Context context, List<Integer> postImages) {
        this(context, postImages, null);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friends_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.postImage.setImageResource(postImages.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return postImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_image);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}