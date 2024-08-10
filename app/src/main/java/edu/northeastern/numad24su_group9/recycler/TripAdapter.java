package edu.northeastern.numad24su_group9.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.numad24su_group9.R;
import edu.northeastern.numad24su_group9.model.Trip;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private List<Trip> trips;
    private TripAdapter.OnItemClickListener listener;
    private TripAdapter.OnItemSelectListener selectListener;


    public TripAdapter() {
    }

    public void updateTrips(List<Trip> trips) {
        this.trips = trips;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(Trip trip);
    }

    public interface OnItemSelectListener {
        void onItemSelect(Trip trip);
    }

    public void setOnItemClickListener(TripAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemSelectListener(TripAdapter.OnItemSelectListener listener) {
        this.selectListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.tripNameTextView.setText(trip.getTitle());
        holder.tripDateTextView.setText(trip.getStartDate());
        holder.tripDestinationTextView.setText(trip.getLocation());
        holder.itemView.setOnClickListener(v -> handleTripClick(trip));
        holder.itemView.setOnLongClickListener(v -> {
            handleTripSelect(trip);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    private void handleTripSelect(Trip trip) {
        if (selectListener != null) {
            selectListener.onItemSelect(trip);
        }
    }

    private void handleTripClick(Trip trip) {
        // Handle the trip click event
        if (listener != null) {
            listener.onItemClick(trip);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tripNameTextView;
        public TextView tripDateTextView;
        public TextView tripDestinationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripNameTextView = itemView.findViewById(R.id.trip_name);
            tripDateTextView = itemView.findViewById(R.id.trip_date);
            tripDestinationTextView = itemView.findViewById(R.id.trip_destination);
        }
    }
}
