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

    private final List<Integer> postImages;
    private final LayoutInflater inflater;

    public PostsAdapter(Context context, List<Integer> postImages) {
        this.inflater = LayoutInflater.from(context);
        this.postImages = postImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_friends_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.postImage.setImageResource(postImages.get(position));
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
}