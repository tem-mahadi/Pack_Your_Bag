package com.temmahadi.packyourbag.Data;

import android.content.Context;
import android.content.SharedPreferences;

import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.Trip;

import java.util.List;

public final class TripSession {

    private static final String PREF_NAME = "trip_session";
    private static final String KEY_ACTIVE_TRIP_ID = "activeTripId";

    private TripSession() {
    }

    public static int getActiveTripId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ACTIVE_TRIP_ID, -1);
    }

    public static void setActiveTripId(Context context, int tripId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_ACTIVE_TRIP_ID, tripId).apply();
    }

    public static int getOrCreateActiveTripId(Context context, roomDB database) {
        int activeTripId = getActiveTripId(context);
        if (activeTripId > 0 && database.tripDAO().getTripById(activeTripId) != null) {
            return activeTripId;
        }

        List<Trip> trips = database.tripDAO().getAllTrips();
        if (trips == null || trips.isEmpty()) {
            Trip defaultTrip = new Trip();
            defaultTrip.setName(MyConstants.DEFAULT_TRIP_NAME);
            defaultTrip.setDestination("Destination");
            defaultTrip.setTripType("Leisure");
            defaultTrip.setStartDate(-1L);
            defaultTrip.setEndDate(-1L);
            defaultTrip.setCreatedAt(System.currentTimeMillis());
            long createdId = database.tripDAO().insertTrip(defaultTrip);
            activeTripId = (int) createdId;
        } else {
            activeTripId = trips.get(0).getId();
        }

        setActiveTripId(context, activeTripId);
        return activeTripId;
    }
}
