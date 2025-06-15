package com.infowave.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.infowave.demo.R;
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
        holder.statusImage.setImageResource(item.getImageResId());
        holder.statusLabel.setText(item.getLabel());
        holder.statusAddIcon.setVisibility(item.isAdd() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView statusImage;
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