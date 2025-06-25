package com.infowave.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infowave.demo.R;
import com.infowave.demo.StoryViewerActivity;
import com.infowave.demo.models.StatusItem;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {
    private List<StatusItem> statusList;
    private Context context;
    public StatusAdapter(Context context, List<StatusItem> statusList) {
        this.context = context;
        this.statusList = statusList;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        StatusItem item = statusList.get(position);

        String imageUrl = item.getImageUrl();
        long now = System.currentTimeMillis() / 1000L;
        long rounded = (now / 15) * 15; // For 15 seconds
        String bustUrl = imageUrl + "?ts=" + rounded;

        Glide.with(context)
                .load(bustUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(holder.statusImage);

        holder.statusLabel.setText(item.getLabel());
        holder.statusLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
        holder.statusAddIcon.setVisibility(item.isAdd() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StoryViewerActivity.class);
            intent.putExtra("story_id", item.getStoryId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public void updateStatusList(List<StatusItem> newStatusList) {
        this.statusList = newStatusList;
        notifyDataSetChanged();
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder {
        ImageView statusImage;
        TextView statusLabel;
        ImageView statusAddIcon;

        StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            statusImage = itemView.findViewById(R.id.status_image);
            statusLabel = itemView.findViewById(R.id.status_label);
            statusAddIcon = itemView.findViewById(R.id.status_add_icon);
        }
    }
}
