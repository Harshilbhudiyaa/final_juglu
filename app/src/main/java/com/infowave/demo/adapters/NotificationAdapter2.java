package com.infowave.demo.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.NotificationItem;
import com.infowave.demo.models.NotificationListItem;
import com.infowave.demo.models.SectionHeader;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter2
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<NotificationListItem> items;

    private static final int VIEW_TYPE_HEADER       = 0;
    private static final int VIEW_TYPE_NOTIFICATION = 1;

    public NotificationAdapter2(Context context, List<NotificationListItem> items) {
        this.context = context;
        this.items   = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof SectionHeader)
                ? VIEW_TYPE_HEADER
                : VIEW_TYPE_NOTIFICATION;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull @Override
    public RecyclerView.ViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_notification2, parent, false);
            return new NotificationViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((SectionHeader) items.get(position));
        } else {
            ((NotificationViewHolder) holder)
                    .bind((NotificationItem) items.get(position));
        }
    }

    // === Section Header ViewHolder ===
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtSection;
        HeaderViewHolder(View itemView) {
            super(itemView);
            txtSection = itemView.findViewById(R.id.textSectionHeader);
        }
        void bind(SectionHeader header) {
            txtSection.setText(header.getTitle());
        }
    }

    // === Notification Item ViewHolder ===
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final TextView tvDescription, tvTime;
        private final MaterialButton btnAction;
        private final ImageView ivThumb;

        NotificationViewHolder(View itemView) {
            super(itemView);
            ivAvatar      = itemView.findViewById(R.id.ivAvatar);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime        = itemView.findViewById(R.id.tvTime);
            btnAction     = itemView.findViewById(R.id.btnAction);
            ivThumb       = itemView.findViewById(R.id.ivThumb);
        }

        void bind(NotificationItem notif) {
            // set avatar and text
            ivAvatar.setImageResource(notif.getAvatarResId());
            String desc = "<b>" + notif.getUserName() + "</b> " + notif.getDescription();
            tvDescription.setText(Html.fromHtml(desc));
            tvTime.setText(notif.getTimeAgo());

            // hide both action button and thumbnail by default
            btnAction.setVisibility(View.GONE);
            ivThumb.setVisibility(View.GONE);

            // show appropriate UI based on notification type
            switch (notif.getActionType()) {
                case FOLLOW_REQUEST:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText("Accept");
                    btnAction.setStrokeColorResource(R.color.btnAccent);
                    break;
                case FOLLOWING:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText("Following");
                    btnAction.setStrokeColorResource(R.color.btnNeutral);
                    break;
                case ACCEPTED:
                    // no button for accepted
                    break;
                case LIKE:
                    ivThumb.setVisibility(View.VISIBLE);
                    ivThumb.setImageResource(notif.getAvatarResId()); // or post thumbnail
                    break;
                case STORY:
                    ivThumb.setVisibility(View.VISIBLE);
                    ivThumb.setImageResource(notif.getAvatarResId()); // or story preview
                    break;
            }
        }
    }
}
