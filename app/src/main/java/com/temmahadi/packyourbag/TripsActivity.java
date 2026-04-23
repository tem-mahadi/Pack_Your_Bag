package com.temmahadi.packyourbag;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.temmahadi.packyourbag.Adapter.TripListAdapter;
import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.Data.TripSession;
import com.temmahadi.packyourbag.Data.appData;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripsActivity extends AppCompatActivity implements TripListAdapter.TripClickListener {

    private RecyclerView recyclerTrips;
    private FloatingActionButton fabAddTrip;
    private Button btnEditTrip;

    private roomDB database;
    private final List<Trip> trips = new ArrayList<>();
    private TripListAdapter adapter;

    private int activeTripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.trips_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerTrips = findViewById(R.id.recyclerTrips);
        fabAddTrip = findViewById(R.id.fabAddTrip);
        btnEditTrip = findViewById(R.id.btnEditTrip);

        database = roomDB.getInstance(this);
        activeTripId = TripSession.getOrCreateActiveTripId(this, database);

        recyclerTrips.setLayoutManager(new LinearLayoutManager(this));

        fabAddTrip.setOnClickListener(view -> showCreateTripDialog());

        btnEditTrip.setOnClickListener(view -> {
            Intent intent = new Intent(TripsActivity.this, TripProfileActivity.class);
            intent.putExtra(MyConstants.TRIP_ID_INTENT, activeTripId);
            startActivity(intent);
        });

        loadTrips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeTripId = TripSession.getOrCreateActiveTripId(this, database);
        loadTrips();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onTripClicked(Trip trip) {
        TripSession.setActiveTripId(TripsActivity.this, trip.getId());
        Toast.makeText(TripsActivity.this, getString(R.string.trips_switched, trip.getName()), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onTripLongPressed(Trip trip) {
        showDeleteDialog(trip);
    }

    private void loadTrips() {
        trips.clear();
        List<Trip> dbTrips = database.tripDAO().getAllTrips();
        if (dbTrips != null) {
            trips.addAll(dbTrips);
        }

        adapter = new TripListAdapter(trips, activeTripId, database, this);
        recyclerTrips.setAdapter(adapter);
    }

    private void showCreateTripDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        EditText edtName = new EditText(this);
        edtName.setHint(getString(R.string.trip_name_hint));
        edtName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        container.addView(edtName);

        EditText edtDestination = new EditText(this);
        edtDestination.setHint(getString(R.string.trip_destination_hint));
        edtDestination.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        container.addView(edtDestination);

        TextView lblType = new TextView(this);
        lblType.setText(getString(R.string.trip_type_label));
        container.addView(lblType);

        Spinner spinnerType = new Spinner(this);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.trip_type_options,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        container.addView(spinnerType);

        // Clear the inline error when the user starts typing
        edtDestination.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    edtDestination.setError(null);
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.trips_add))
                .setView(container)
                .setPositiveButton(getString(R.string.trip_save), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String destination = edtDestination.getText().toString().trim();
            if (destination.isEmpty()) {
                edtDestination.setError(getString(R.string.trip_destination_required));
                edtDestination.requestFocus();
                shakeView(edtDestination);
                return;
            }

            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                name = destination;
            }

            String tripType = spinnerType.getSelectedItem() == null
                    ? "Leisure"
                    : spinnerType.getSelectedItem().toString();

            Trip trip = new Trip();
            trip.setName(name);
            trip.setDestination(destination);
            trip.setTripType(tripType);
            trip.setStartDate(-1L);
            trip.setEndDate(-1L);
            trip.setCreatedAt(System.currentTimeMillis());

            long createdId = database.tripDAO().insertTrip(trip);
            int newTripId = (int) createdId;
            new appData(database).persistAlldata(newTripId);
            TripSession.setActiveTripId(TripsActivity.this, newTripId);
            activeTripId = newTripId;

            loadTrips();
            Toast.makeText(TripsActivity.this, getString(R.string.trips_created, name), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    private void shakeView(View view) {
        android.animation.ObjectAnimator shake = android.animation.ObjectAnimator.ofFloat(
                view, "translationX", 0, 12, -12, 10, -10, 6, -6, 0
        );
        shake.setDuration(400);
        shake.start();
    }

    private void showDeleteDialog(Trip trip) {
        if (trips.size() <= 1) {
            Toast.makeText(this, getString(R.string.trips_delete_last_blocked), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.trips_delete_title, trip.getName()))
                .setMessage(getString(R.string.trips_delete_message))
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                    database.mainDAO().deleteAllByTripId(trip.getId());
                    database.tripDAO().deleteTrip(trip);

                    if (trip.getId() == activeTripId) {
                        List<Trip> latestTrips = database.tripDAO().getAllTrips();
                        if (latestTrips != null && !latestTrips.isEmpty()) {
                            activeTripId = latestTrips.get(0).getId();
                            TripSession.setActiveTripId(this, activeTripId);
                        }
                    }

                    loadTrips();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
