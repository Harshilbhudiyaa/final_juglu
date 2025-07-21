package com.infowave.demo.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.infowave.demo.R;
import com.infowave.demo.models.PersonNearby;

import java.util.ArrayList;
import java.util.List;

public class PersonNearbyAdapter extends RecyclerView.Adapter<PersonNearbyAdapter.ViewHolder> {

    public List<PersonNearby> nearbyList;
    private final OnPersonNearbyClickListener listener;

    public interface OnPersonNearbyClickListener {
        void onClick(PersonNearby person);
    }

    public PersonNearbyAdapter(List<PersonNearby> nearbyList, OnPersonNearbyClickListener listener) {
        this.nearbyList = nearbyList != null ? nearbyList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person_nearby, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonNearby person = nearbyList.get(position);

        holder.name.setText(person.getName());
        holder.distance.setText(person.getDistance());

        String imageUrl = person.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.profileImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile);
        }
        holder.itemView.setOnClickListener(v -> listener.onClick(person));
    }

    @Override
    public int getItemCount() {
        return nearbyList != null ? nearbyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profileImage;
        TextView name, distance;
        MaterialButton connectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            distance = itemView.findViewById(R.id.distance);
            connectButton = itemView.findViewById(R.id.connectButton);
        }
    }

//    @SuppressLint("NotifyDataSetChanged")
//    public void setNearbyList(List<PersonNearby> list) {
//        this.nearbyList = list != null ? list : new ArrayList<>();
//        notifyDataSetChanged();
//    }
}
