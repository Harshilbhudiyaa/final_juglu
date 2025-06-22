package com.infowave.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.FeatureItem;

import java.util.List;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder> {

    private List<FeatureItem> featureItems;
    private final OnFeatureClickListener clickListener;

    public interface OnFeatureClickListener {
        void onFeatureClick(int position);
    }

    public FeatureAdapter(List<FeatureItem> featureItems, OnFeatureClickListener clickListener) {
        this.featureItems = featureItems;
        this.clickListener = clickListener;
    }

    public void updateData(List<FeatureItem> newFeatures) {
        this.featureItems = newFeatures;
        notifyDataSetChanged();
    }

    // ✅ Method to get a FeatureItem at a specific position (used in ProfileFragment)
    public FeatureItem getItem(int position) {
        return featureItems.get(position);
    }

    @NonNull
    @Override
    public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feature, parent, false);
        return new FeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {
        FeatureItem item = featureItems.get(position);

        holder.icon.setImageResource(item.getIconRes());
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFeatureClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return featureItems.size();
    }

    static class FeatureViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView description;

        FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.feature_icon);
            title = itemView.findViewById(R.id.feature_title);
            description = itemView.findViewById(R.id.feature_description);
        }
    }
}
