package com.infowave.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.infowave.demo.R;
import com.infowave.demo.models.PersonNearby;
import com.infowave.demo.models.RecommendedUser;

import java.util.List;

// PersonNearbyAdapter.java
public class PersonNearbyAdapter extends RecyclerView.Adapter<PersonNearbyAdapter.ViewHolder> {

    public List<PersonNearby> nearbyList;

    public interface OnConnectClickListener {
        void onConnectClick(int position);
    }

    public interface OnPersonNearbyClickListener {
        void onClick(PersonNearby person);
    }
    private final OnPersonNearbyClickListener listener;
    public PersonNearbyAdapter(List<PersonNearby> nearbyList, OnPersonNearbyClickListener listener) {
        this.nearbyList = nearbyList;
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
        holder.profileImage.setImageResource(person.getProfileImageRes());
        holder.itemView.setOnClickListener(v -> listener.onClick(person));
//        holder.connectButton.setOnClickListener(v -> {
//            if (connectClickListener != null) {
//                connectClickListener.onConnectClick(position);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return nearbyList.size();
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
}
