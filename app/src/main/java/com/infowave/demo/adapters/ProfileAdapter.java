package com.infowave.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.infowave.demo.R;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private Context context;
    private List<String> profileItems;

    public ProfileAdapter(Context context, List<String> profileItems) {
        this.context = context;
        this.profileItems = profileItems;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        String item = profileItems.get(position);
        holder.itemText.setText(item);
    }

    @Override
    public int getItemCount() {
        return profileItems.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.item_text);
        }
    }
} 