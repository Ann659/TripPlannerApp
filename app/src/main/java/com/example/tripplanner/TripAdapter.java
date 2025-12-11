package com.example.tripplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tripplanner.R;
import com.example.tripplanner.Trip;
import java.util.ArrayList;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> trips = new ArrayList<>();

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.bind(trip);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTripName, tvBudget, tvStartDate;

        TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripName = itemView.findViewById(R.id.tvTripName);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
        }

        void bind(Trip trip) {
            tvTripName.setText(trip.getTripName());
            tvBudget.setText(trip.getFormattedBudget());
            tvStartDate.setText(trip.getFormattedDate());
        }
    }
}
