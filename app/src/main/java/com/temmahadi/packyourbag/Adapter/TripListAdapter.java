package com.temmahadi.packyourbag.Adapter;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.Trip;
import com.temmahadi.packyourbag.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.TripViewHolder> {

    public interface TripClickListener {
        void onTripClicked(Trip trip);
        void onTripLongPressed(Trip trip);
    }

    private final List<Trip> trips;
    private final int activeTripId;
    private final roomDB database;
    private final TripClickListener listener;
    private final DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public TripListAdapter(List<Trip> trips, int activeTripId, roomDB database, TripClickListener listener) {
        this.trips = trips;
        this.activeTripId = activeTripId;
        this.database = database;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_list_item, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        boolean isActive = trip.getId() == activeTripId;

        holder.txtTripName.setText(trip.getName());
        holder.txtTripDestination.setText("📍 " + (trip.getDestination() != null ? trip.getDestination() : ""));

        // Dates
        String start = trip.getStartDate() > 0 ? formatter.format(new Date(trip.getStartDate())) : "Not set";
        String end = trip.getEndDate() > 0 ? formatter.format(new Date(trip.getEndDate())) : "Not set";
        holder.txtTripDates.setText(start + " – " + end);

        // Type badge
        holder.txtTripTypeBadge.setText(trip.getTripType());
        applyBadgeColors(holder, trip.getTripType());

        // Active dot
        if (isActive) {
            holder.viewActiveDot.setVisibility(View.VISIBLE);
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_packed));
            holder.viewActiveDot.setBackground(dot);
            holder.tripCard.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_light));
            holder.tripCard.setStrokeWidth(2);
        } else {
            holder.viewActiveDot.setVisibility(View.GONE);
            holder.tripCard.setStrokeWidth(0);
        }

        // Packing progress
        int total = safeInt(database.mainDAO().getItemsCount(trip.getId()));
        int packed = safeInt(database.mainDAO().getPackedCount(trip.getId()));
        int percent = total > 0 ? Math.round((packed * 100f) / total) : 0;

        holder.txtTripProgress.setText(packed + "/" + total + " packed");
        holder.progressTrip.setProgress(percent);

        int progressColor;
        if (percent >= 100) {
            progressColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_packed);
        } else if (percent > 0) {
            progressColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_partial);
        } else {
            progressColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_empty);
        }
        holder.progressTrip.setProgressTintList(ColorStateList.valueOf(progressColor));

        // Click listeners
        holder.tripCard.setOnClickListener(v -> {
            if (listener != null) listener.onTripClicked(trip);
        });
        holder.tripCard.setOnLongClickListener(v -> {
            if (listener != null) listener.onTripLongPressed(trip);
            return true;
        });
    }

    private void applyBadgeColors(TripViewHolder holder, String tripType) {
        int bgColor, textColor;
        switch (tripType) {
            case "Business":
                bgColor = R.color.badge_business; textColor = R.color.badge_business_text; break;
            case "Family":
                bgColor = R.color.badge_family; textColor = R.color.badge_family_text; break;
            case "Adventure":
                bgColor = R.color.badge_adventure; textColor = R.color.badge_adventure_text; break;
            case "Camping":
                bgColor = R.color.badge_camping; textColor = R.color.badge_camping_text; break;
            default:
                bgColor = R.color.badge_leisure; textColor = R.color.badge_leisure_text; break;
        }
        GradientDrawable bg = (GradientDrawable) holder.txtTripTypeBadge.getBackground().mutate();
        bg.setColor(ContextCompat.getColor(holder.itemView.getContext(), bgColor));
        holder.txtTripTypeBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), textColor));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView tripCard;
        View viewActiveDot;
        TextView txtTripName, txtTripDestination, txtTripDates, txtTripTypeBadge, txtTripProgress;
        ProgressBar progressTrip;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripCard = itemView.findViewById(R.id.tripCard);
            viewActiveDot = itemView.findViewById(R.id.viewActiveDot);
            txtTripName = itemView.findViewById(R.id.txtTripName);
            txtTripDestination = itemView.findViewById(R.id.txtTripDestination);
            txtTripDates = itemView.findViewById(R.id.txtTripDates);
            txtTripTypeBadge = itemView.findViewById(R.id.txtTripTypeBadge);
            txtTripProgress = itemView.findViewById(R.id.txtTripProgress);
            progressTrip = itemView.findViewById(R.id.progressTrip);
        }
    }
}
