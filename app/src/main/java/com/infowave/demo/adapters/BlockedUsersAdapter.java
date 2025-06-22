package com.infowave.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.BlockedUser;
import java.util.List;

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder> {

    private Context context;
    private List<BlockedUser> blockedUsers;

    public BlockedUsersAdapter(Context context, List<BlockedUser> blockedUsers) {
        this.context = context;
        this.blockedUsers = blockedUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blocked_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlockedUser user = blockedUsers.get(position);
        holder.name.setText(user.name);
        holder.status.setText(user.status);
        holder.image.setImageResource(user.profileImage);

        holder.unblockButton.setOnClickListener(v -> {
            blockedUsers.remove(position);
            notifyItemRemoved(position);
            // TODO: Trigger actual unblock logic here
        });
    }

    @Override
    public int getItemCount() {
        return blockedUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, status;
        Button unblockButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.userName);
            status = itemView.findViewById(R.id.userStatus);
            unblockButton = itemView.findViewById(R.id.unblockButton);
        }
    }
}
