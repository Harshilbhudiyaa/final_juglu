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

import com.bumptech.glide.Glide;
import com.infowave.demo.R;
import com.infowave.demo.models.NotificationItem;
import com.infowave.demo.models.NotificationListItem;
import com.infowave.demo.models.SectionHeader;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnActionClickListener {
        void onAcceptClicked(NotificationItem item, int position);
        void onDeclineClicked(NotificationItem item, int position);
    }

    private final Context context;
    private final List<NotificationListItem> items;
    private final OnActionClickListener actionListener;

    private static final int VIEW_TYPE_HEADER       = 0;
    private static final int VIEW_TYPE_NOTIFICATION = 1;

    public NotificationAdapter2(Context context,
                                List<NotificationListItem> items,
                                OnActionClickListener listener) {
        this.context        = context;
        this.items          = items;
        this.actionListener = listener;
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((SectionHeader) items.get(position));
        } else {
            ((NotificationViewHolder) holder)
                    .bind((NotificationItem) items.get(position), position, actionListener, context);
        }
    }

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

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final ImageView ivHeartBadge;
        private final ImageView ivThumb;
        private final TextView tvDescription, tvTime;
        private final MaterialButton btnAction;

        NotificationViewHolder(View itemView) {
            super(itemView);
            ivAvatar      = itemView.findViewById(R.id.ivAvatar);
            ivHeartBadge  = itemView.findViewById(R.id.ivHeartBadge);
            ivThumb       = itemView.findViewById(R.id.ivThumb);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime        = itemView.findViewById(R.id.tvTime);
            btnAction     = itemView.findViewById(R.id.btnAction);
        }

        void bind(NotificationItem notif, int position,
                  OnActionClickListener listener, Context ctx) {

            if (notif.getAvatarUrl() != null && !notif.getAvatarUrl().isEmpty()) {
                Glide.with(ivAvatar.getContext())
                        .load(notif.getAvatarUrl())
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(ivAvatar);
            } else if (notif.getAvatarResId() != 0) {
                ivAvatar.setImageResource(notif.getAvatarResId());
            } else {
                ivAvatar.setImageResource(R.drawable.default_profile);
            }

            if (notif.getActionType() == NotificationItem.Action.LIKE_POST
                    || notif.getActionType() == NotificationItem.Action.LIKE_STORY
                    || notif.getActionType() == NotificationItem.Action.LIKE) {
                ivHeartBadge.setVisibility(View.VISIBLE);
            } else {
                ivHeartBadge.setVisibility(View.GONE);
            }

            String desc = "<b>" + notif.getUserName() + "</b> " + notif.getDescription();
            tvDescription.setText(Html.fromHtml(desc));
            tvTime.setText(notif.getTimeAgo());

            if (notif.getThumbUrl() != null && !notif.getThumbUrl().isEmpty()) {
                ivThumb.setVisibility(View.VISIBLE);
                Glide.with(ivThumb.getContext())
                        .load(notif.getThumbUrl())
                        .placeholder(R.drawable.ic_id_placeholder)
                        .error(R.drawable.ic_id_placeholder)
                        .into(ivThumb);
            } else {
                ivThumb.setVisibility(View.GONE);
            }

            btnAction.setVisibility(View.GONE);
            btnAction.setOnClickListener(null);
            btnAction.setOnLongClickListener(null);

            switch (notif.getActionType()) {
                case FOLLOW_REQUEST:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText("Accept");
                    btnAction.setStrokeColorResource(R.color.btnAccent);
                    btnAction.setOnClickListener(v -> listener.onAcceptClicked(notif, position));
                    btnAction.setOnLongClickListener(v -> {
                        listener.onDeclineClicked(notif, position);
                        return true;
                    });
                    break;

                case FOLLOWING:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText("Following");
                    btnAction.setStrokeColorResource(R.color.btnNeutral);
                    btnAction.setEnabled(false);
                    break;

                default:
                    // no button
                    break;
            }
        }
    }
}
