package com.temmahadi.packyourbag;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.Data.TripSession;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.Trip;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class TripProfileActivity extends AppCompatActivity {

    private EditText edtTripName;
    private EditText edtDestination;
    private Spinner spinnerTripType;
    private TextView txtStartDateValue;
    private TextView txtEndDateValue;

    private roomDB database;
    private Trip currentTrip;
    private int tripId;

    private long selectedStartDate = -1L;
    private long selectedEndDate = -1L;

    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.trip_profile_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        edtTripName = findViewById(R.id.edtTripName);
        edtDestination = findViewById(R.id.edtDestination);
        spinnerTripType = findViewById(R.id.spinnerTripType);
        txtStartDateValue = findViewById(R.id.txtStartDateValue);
        txtEndDateValue = findViewById(R.id.txtEndDateValue);

        Button btnPickStartDate = findViewById(R.id.btnPickStartDate);
        Button btnPickEndDate = findViewById(R.id.btnPickEndDate);
        Button btnSaveTripProfile = findViewById(R.id.btnSaveTripProfile);

        database = roomDB.getInstance(this);
        int requestedTripId = getIntent().getIntExtra(MyConstants.TRIP_ID_INTENT, -1);
        if (requestedTripId > 0) {
            tripId = requestedTripId;
        } else {
            tripId = TripSession.getOrCreateActiveTripId(this, database);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.trip_type_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTripType.setAdapter(adapter);

        loadProfile();

        btnPickStartDate.setOnClickListener(view -> showDatePicker(true));
        btnPickEndDate.setOnClickListener(view -> showDatePicker(false));
        btnSaveTripProfile.setOnClickListener(view -> saveProfile());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadProfile() {
        currentTrip = database.tripDAO().getTripById(tripId);
        if (currentTrip == null) {
            currentTrip = new Trip();
            currentTrip.setName(getString(R.string.default_trip_name));
            currentTrip.setDestination("");
            currentTrip.setTripType("Leisure");
            currentTrip.setStartDate(-1L);
            currentTrip.setEndDate(-1L);
            currentTrip.setCreatedAt(System.currentTimeMillis());
        }

        edtTripName.setText(currentTrip.getName());
        edtDestination.setText(currentTrip.getDestination());
        setTripTypeSelection(currentTrip.getTripType());

        selectedStartDate = currentTrip.getStartDate();
        selectedEndDate = currentTrip.getEndDate();
        updateDateViews();
    }

    private void setTripTypeSelection(String tripType) {
        for (int i = 0; i < spinnerTripType.getCount(); i++) {
            Object item = spinnerTripType.getItemAtPosition(i);
            if (item != null && item.toString().equalsIgnoreCase(tripType)) {
                spinnerTripType.setSelection(i);
                return;
            }
        }
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        long initial = isStartDate ? selectedStartDate : selectedEndDate;
        if (initial > 0L) {
            calendar.setTimeInMillis(initial);
        }

        DatePickerDialog pickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selected.set(Calendar.HOUR_OF_DAY, 0);
                    selected.set(Calendar.MINUTE, 0);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);

                    if (isStartDate) {
                        selectedStartDate = selected.getTimeInMillis();
                    } else {
                        selectedEndDate = selected.getTimeInMillis();
                    }
                    updateDateViews();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        pickerDialog.show();
    }

    private void updateDateViews() {
        txtStartDateValue.setText(selectedStartDate > 0
                ? dateFormat.format(new Date(selectedStartDate))
                : getString(R.string.trip_pick_date));

        txtEndDateValue.setText(selectedEndDate > 0
                ? dateFormat.format(new Date(selectedEndDate))
                : getString(R.string.trip_pick_date));
    }

    private void saveProfile() {
        String tripName = edtTripName.getText().toString().trim();
        String destination = edtDestination.getText().toString().trim();
        if (destination.isEmpty()) {
            Toast.makeText(this, getString(R.string.trip_destination_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (tripName.isEmpty()) {
            tripName = destination;
        }

        if (selectedStartDate > 0 && selectedEndDate > 0 && selectedEndDate < selectedStartDate) {
            Toast.makeText(this, getString(R.string.trip_date_validation), Toast.LENGTH_SHORT).show();
            return;
        }

        String tripType = spinnerTripType.getSelectedItem() == null
                ? "Leisure"
                : spinnerTripType.getSelectedItem().toString();

        currentTrip.setName(tripName);
        currentTrip.setDestination(destination);
        currentTrip.setTripType(tripType);
        currentTrip.setStartDate(selectedStartDate);
        currentTrip.setEndDate(selectedEndDate);

        Trip existingTrip = currentTrip.getId() > 0 ? database.tripDAO().getTripById(currentTrip.getId()) : null;
        if (existingTrip != null) {
            database.tripDAO().updateTrip(currentTrip);
        } else {
            long created = database.tripDAO().insertTrip(currentTrip);
            currentTrip.setId((int) created);
        }

        TripSession.setActiveTripId(this, currentTrip.getId());

        Toast.makeText(this, getString(R.string.trip_profile_saved), Toast.LENGTH_SHORT).show();
        finish();
    }
}
