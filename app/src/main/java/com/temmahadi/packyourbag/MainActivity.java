package com.temmahadi.packyourbag;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.temmahadi.packyourbag.BdApps_Backend.ApiService;
import com.temmahadi.packyourbag.BdApps_Backend.LandingActivity;
import com.temmahadi.packyourbag.BdApps_Backend.RetrofitClient;
import com.temmahadi.packyourbag.BdApps_Backend.SubscriptionManager;
import com.temmahadi.packyourbag.BdApps_Backend.UnsubscribeRequest;
import com.temmahadi.packyourbag.BdApps_Backend.UnsubscribeResponse;

import com.google.android.material.card.MaterialCardView;
import com.temmahadi.packyourbag.Adapter.MyAdapter;
import com.temmahadi.packyourbag.Constants.MyConstants;
import com.temmahadi.packyourbag.Data.TripSession;
import com.temmahadi.packyourbag.Data.appData;
import com.temmahadi.packyourbag.DataBase.roomDB;
import com.temmahadi.packyourbag.Models.Trip;
import com.temmahadi.packyourbag.Models.items;
import com.temmahadi.packyourbag.utils.CircularProgressView;
import com.temmahadi.packyourbag.utils.PackingStatsUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.text.DateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<String> titles; List<Integer> images; MyAdapter adapter;
    roomDB database;

    // Dashboard views
    TextView txtTripName;
    TextView txtTripDates;
    TextView txtTripCountdown;
    TextView txtTripType;
    CircularProgressView circularProgress;
    TextView txtPackedSummary;
    TextView txtWeatherSummary;
    TextView txtWeatherTemp;
    TextView txtWeatherEmoji;
    LinearLayout weatherCardInner;
    MaterialCardView cardWeather;

    Button btnTripProfile;
    Button btnManageTrips;
    Button btnReminder;
    Button btnWeatherSuggestions;

    int activeTripId;

    private final ExecutorService weatherExecutor = Executors.newSingleThreadExecutor();
    private WeatherSnapshot latestWeatherSnapshot;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REMINDER_REQUEST_CODE = 41;
    private boolean openReminderPickerAfterPermission = false;

    private static class WeatherSnapshot {
        String destination;
        double minTemp;
        double maxTemp;
        int weatherCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences= getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username","");
        Toast.makeText(getApplicationContext(),"Welcome "+username,Toast.LENGTH_SHORT).show();

        recyclerView= findViewById(R.id.recyclerView);
        txtTripName = findViewById(R.id.txtTripName);
        txtTripDates = findViewById(R.id.txtTripDates);
        txtTripCountdown = findViewById(R.id.txtTripCountdown);
        txtTripType = findViewById(R.id.txtTripType);
        circularProgress = findViewById(R.id.circularProgress);
        txtPackedSummary = findViewById(R.id.txtPackedSummary);
        txtWeatherSummary = findViewById(R.id.txtWeatherSummary);
        txtWeatherTemp = findViewById(R.id.txtWeatherTemp);
        txtWeatherEmoji = findViewById(R.id.txtWeatherEmoji);
        weatherCardInner = findViewById(R.id.weatherCardInner);
        cardWeather = findViewById(R.id.cardWeather);
        btnTripProfile = findViewById(R.id.btnTripProfile);
        btnManageTrips = findViewById(R.id.btnManageTrips);
        btnReminder = findViewById(R.id.btnReminder);
        btnWeatherSuggestions = findViewById(R.id.btnWeatherSuggestions);

        addAllTitles();
        addAllImages();
        database = roomDB.getInstance(this);
        activeTripId = TripSession.getOrCreateActiveTripId(this, database);
        persistAppData(activeTripId);

        adapter = new MyAdapter(this, titles, images, MainActivity.this, activeTripId, database);
        GridLayoutManager gridLayoutManager= new GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        PackingReminderReceiver.createNotificationChannel(this);
        setButtonListeners();
        refreshDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeTripId = TripSession.getOrCreateActiveTripId(this, database);
        adapter = new MyAdapter(this, titles, images, MainActivity.this, activeTripId, database);
        recyclerView.setAdapter(adapter);
        refreshDashboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        weatherExecutor.shutdownNow();
    }

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_INTERVAL>System.currentTimeMillis()){
            super.onBackPressed();
        }
        else {
            Toast.makeText(this, "Tap back Button in order to exit",Toast.LENGTH_SHORT).show();
        }
        mBackPressed= System.currentTimeMillis();
    }
    private void persistAppData(int tripId){
        database = roomDB.getInstance(this);
        appData appdata = new appData(database);
        if (safeInt(database.mainDAO().getItemsCount(tripId)) == 0) {
            appdata.persistAlldata(tripId);
        }
    }

    private void setButtonListeners() {
        btnTripProfile.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TripProfileActivity.class);
            intent.putExtra(MyConstants.TRIP_ID_INTENT, activeTripId);
            startActivity(intent);
        });

        btnManageTrips.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TripsActivity.class);
            startActivity(intent);
        });

        btnReminder.setOnClickListener(view -> {
            if (hasOrRequestNotificationPermission()) {
                showReminderTimePicker();
            }
        });

        btnWeatherSuggestions.setOnClickListener(view -> addWeatherSuggestions());

        // Header menu ("⋮" button)
        View btnHeaderMenu = findViewById(R.id.btnHeaderMenu);
        btnHeaderMenu.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            popup.getMenu().add(0, 1, 0, "About Us");
            popup.getMenu().add(0, 2, 1, "Unsubscribe");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    startActivity(new Intent(this, AboutUs.class));
                    return true;
                } else if (item.getItemId() == 2) {
                    showUnsubscribeDialog();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private boolean hasOrRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        openReminderPickerAfterPermission = true;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQUEST_CODE);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != NOTIFICATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted && openReminderPickerAfterPermission) {
            openReminderPickerAfterPermission = false;
            showReminderTimePicker();
            return;
        }

        Toast.makeText(this, getString(R.string.permission_notification_needed), Toast.LENGTH_SHORT).show();
    }

    private void showReminderTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog pickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> scheduleDailyReminder(selectedHour, selectedMinute),
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        pickerDialog.show();
    }

    private void scheduleDailyReminder(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Calendar schedule = Calendar.getInstance();
        schedule.set(Calendar.HOUR_OF_DAY, hour);
        schedule.set(Calendar.MINUTE, minute);
        schedule.set(Calendar.SECOND, 0);
        schedule.set(Calendar.MILLISECOND, 0);

        if (schedule.before(Calendar.getInstance())) {
            schedule.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, PackingReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                schedule.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );

        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(schedule.getTimeInMillis()));
        Toast.makeText(this, getString(R.string.reminder_set_success, time), Toast.LENGTH_SHORT).show();
    }

    private void refreshDashboard() {
        int total = safeInt(database.mainDAO().getItemsCount(activeTripId));
        int packed = safeInt(database.mainDAO().getPackedCount(activeTripId));
        int readiness = PackingStatsUtils.getReadinessPercent(packed, total);

        // Circular progress
        circularProgress.setProgress(readiness);
        txtPackedSummary.setText(getString(R.string.dashboard_packed_summary, packed, total));

        Trip activeTrip = updateTripSummary();
        loadWeatherInsights(activeTrip);
    }

    private Trip updateTripSummary() {
        Trip activeTrip = database.tripDAO().getTripById(activeTripId);
        if (activeTrip == null) {
            activeTripId = TripSession.getOrCreateActiveTripId(this, database);
            activeTrip = database.tripDAO().getTripById(activeTripId);
        }

        if (activeTrip == null) {
            txtTripName.setText(getString(R.string.dashboard_trip_missing));
            txtTripDates.setVisibility(View.GONE);
            txtTripCountdown.setVisibility(View.GONE);
            txtTripType.setVisibility(View.GONE);
            return null;
        }

        // Trip name
        txtTripName.setText(activeTrip.getName());

        // Dates
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
        String startText = activeTrip.getStartDate() > 0
                ? formatter.format(new Date(activeTrip.getStartDate()))
                : getString(R.string.trip_pick_date);
        String endText = activeTrip.getEndDate() > 0
                ? formatter.format(new Date(activeTrip.getEndDate()))
                : getString(R.string.trip_pick_date);
        txtTripDates.setText(startText + " – " + endText);
        txtTripDates.setVisibility(View.VISIBLE);

        // Countdown
        if (activeTrip.getStartDate() > 0) {
            long daysUntil = getDaysUntil(activeTrip.getStartDate());
            if (daysUntil > 0) {
                txtTripCountdown.setText(getString(R.string.trip_countdown, daysUntil));
                txtTripCountdown.setVisibility(View.VISIBLE);
            } else if (daysUntil == 0) {
                txtTripCountdown.setText(getString(R.string.trip_countdown_today));
                txtTripCountdown.setVisibility(View.VISIBLE);
            } else {
                txtTripCountdown.setText(getString(R.string.trip_countdown_past));
                txtTripCountdown.setVisibility(View.VISIBLE);
            }
        } else {
            txtTripCountdown.setVisibility(View.GONE);
        }

        // Trip type badge
        txtTripType.setText(activeTrip.getTripType());
        applyTripTypeBadgeColors(txtTripType, activeTrip.getTripType());
        txtTripType.setVisibility(View.VISIBLE);

        return activeTrip;
    }

    private long getDaysUntil(long startDateMillis) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startDateMillis);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        long diffMs = start.getTimeInMillis() - today.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diffMs);
    }

    private void applyTripTypeBadgeColors(TextView badge, String tripType) {
        int bgColor;
        int textColor;

        switch (tripType) {
            case "Business":
                bgColor = R.color.badge_business;
                textColor = R.color.badge_business_text;
                break;
            case "Family":
                bgColor = R.color.badge_family;
                textColor = R.color.badge_family_text;
                break;
            case "Adventure":
                bgColor = R.color.badge_adventure;
                textColor = R.color.badge_adventure_text;
                break;
            case "Camping":
                bgColor = R.color.badge_camping;
                textColor = R.color.badge_camping_text;
                break;
            default: // Leisure
                bgColor = R.color.badge_leisure;
                textColor = R.color.badge_leisure_text;
                break;
        }

        GradientDrawable bg = (GradientDrawable) badge.getBackground().mutate();
        bg.setColor(ContextCompat.getColor(this, bgColor));
        badge.setTextColor(ContextCompat.getColor(this, textColor));
    }

    private void loadWeatherInsights(Trip trip) {
        if (trip == null || trip.getDestination() == null || trip.getDestination().trim().isEmpty()) {
            txtWeatherSummary.setText(getString(R.string.weather_missing_destination));
            txtWeatherEmoji.setText("🌤️");
            txtWeatherTemp.setVisibility(View.GONE);
            latestWeatherSnapshot = null;
            return;
        }

        String destination = trip.getDestination().trim();
        txtWeatherSummary.setText(getString(R.string.weather_loading, destination));
        txtWeatherEmoji.setText("⏳");

        weatherExecutor.execute(() -> {
            WeatherSnapshot snapshot = fetchWeatherSnapshot(destination);
            runOnUiThread(() -> {
                if (snapshot == null) {
                    txtWeatherSummary.setText(getString(R.string.weather_unavailable));
                    txtWeatherEmoji.setText("❓");
                    txtWeatherTemp.setVisibility(View.GONE);
                    latestWeatherSnapshot = null;
                    return;
                }

                latestWeatherSnapshot = snapshot;
                txtWeatherEmoji.setText(getWeatherEmoji(snapshot.weatherCode));
                txtWeatherSummary.setText(getString(R.string.weather_summary_label,
                        snapshot.destination, getWeatherLabel(snapshot.weatherCode)));
                txtWeatherTemp.setText(getString(R.string.weather_temp_range,
                        snapshot.minTemp, snapshot.maxTemp));
                txtWeatherTemp.setVisibility(View.VISIBLE);

                // Tint weather card background
                tintWeatherCard(snapshot);
            });
        });
    }

    private void tintWeatherCard(WeatherSnapshot snapshot) {
        int tintColor;
        double avgTemp = (snapshot.minTemp + snapshot.maxTemp) / 2.0;

        if (isRainOrStorm(snapshot.weatherCode) || snapshot.weatherCode == 95
                || snapshot.weatherCode == 96 || snapshot.weatherCode == 99) {
            tintColor = R.color.weather_storm;
        } else if (avgTemp >= 28) {
            tintColor = R.color.weather_hot;
        } else if (avgTemp <= 12) {
            tintColor = R.color.weather_cold;
        } else {
            tintColor = R.color.weather_mild;
        }

        cardWeather.setCardBackgroundColor(ContextCompat.getColor(this, tintColor));
    }

    private WeatherSnapshot fetchWeatherSnapshot(String destination) {
        try {
            String encodedDestination = URLEncoder.encode(destination, "UTF-8");
            String geocodeUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedDestination + "&count=1&language=en&format=json";

            String geocodeResponse = readUrl(geocodeUrl);
            JSONObject geoObject = new JSONObject(geocodeResponse);
            JSONArray results = geoObject.optJSONArray("results");
            if (results == null || results.length() == 0) {
                return null;
            }

            JSONObject first = results.getJSONObject(0);
            double latitude = first.getDouble("latitude");
            double longitude = first.getDouble("longitude");
            String normalizedDestination = first.optString("name", destination);

            String forecastUrl = String.format(
                    Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1",
                    latitude,
                    longitude
            );

            String forecastResponse = readUrl(forecastUrl);
            JSONObject forecastObject = new JSONObject(forecastResponse);
            JSONObject daily = forecastObject.optJSONObject("daily");
            if (daily == null) {
                return null;
            }

            JSONArray weatherCodes = daily.optJSONArray("weather_code");
            JSONArray tempMax = daily.optJSONArray("temperature_2m_max");
            JSONArray tempMin = daily.optJSONArray("temperature_2m_min");
            if (weatherCodes == null || tempMax == null || tempMin == null || weatherCodes.length() == 0 || tempMax.length() == 0 || tempMin.length() == 0) {
                return null;
            }

            WeatherSnapshot snapshot = new WeatherSnapshot();
            snapshot.destination = normalizedDestination;
            snapshot.weatherCode = weatherCodes.getInt(0);
            snapshot.maxTemp = tempMax.getDouble(0);
            snapshot.minTemp = tempMin.getDouble(0);
            return snapshot;
        } catch (Exception ex) {
            return null;
        }
    }

    private String readUrl(String value) throws Exception {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(value);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");

            int statusCode = connection.getResponseCode();
            inputStream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (inputStream == null) {
                throw new IllegalStateException("Empty response stream");
            }

            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            if (statusCode >= 400) {
                throw new IllegalStateException("HTTP error: " + statusCode);
            }
            return builder.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void addWeatherSuggestions() {
        if (latestWeatherSnapshot == null) {
            Toast.makeText(this, getString(R.string.weather_fetch_first), Toast.LENGTH_SHORT).show();
            return;
        }

        List<items> suggestionItems = new ArrayList<>();
        int weatherCode = latestWeatherSnapshot.weatherCode;
        double min = latestWeatherSnapshot.minTemp;
        double max = latestWeatherSnapshot.maxTemp;

        if (isRainOrStorm(weatherCode)) {
            suggestionItems.add(buildSuggestionItem("Umbrella", MyConstants.BASIC_NEEDS_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Rain Jacket", MyConstants.CLOTHING_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Waterproof Bag", MyConstants.BASIC_NEEDS_CAMEL_CASE));
        }

        if (isSnow(weatherCode) || min <= 8) {
            suggestionItems.add(buildSuggestionItem("Warm Jacket", MyConstants.CLOTHING_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Thermal Socks", MyConstants.CLOTHING_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Gloves", MyConstants.CLOTHING_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Scarf", MyConstants.CLOTHING_CAMEL_CASE));
        }

        if (max >= 30) {
            suggestionItems.add(buildSuggestionItem("Sunscreen", MyConstants.PERSONAL_CARE_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Sunglasses", MyConstants.PERSONAL_CARE_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Cap / Hat", MyConstants.CLOTHING_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Extra Water Bottle", MyConstants.FOOD_CAMEL_CASE));
            suggestionItems.add(buildSuggestionItem("Light Cotton Clothes", MyConstants.CLOTHING_CAMEL_CASE));
        }

        if (weatherCode == 45 || weatherCode == 48) {
            suggestionItems.add(buildSuggestionItem("Flashlight", MyConstants.BASIC_NEEDS_CAMEL_CASE));
        }

        if (suggestionItems.isEmpty()) {
            Toast.makeText(this, getString(R.string.weather_no_new_suggestions), Toast.LENGTH_SHORT).show();
            return;
        }

        // Build the dialog content
        showWeatherSuggestionsDialog(suggestionItems);
    }

    private void showWeatherSuggestionsDialog(List<items> suggestionItems) {
        // Separate new vs already-added items
        List<items> newItems = new ArrayList<>();
        List<items> existingItems = new ArrayList<>();

        for (items suggestion : suggestionItems) {
            if (safeInt(database.mainDAO().getCountByCategoryAndName(
                    suggestion.getCategory(),
                    suggestion.getItemName(),
                    activeTripId)) > 0) {
                existingItems.add(suggestion);
            } else {
                newItems.add(suggestion);
            }
        }

        // Build dialog view
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad / 2);

        // Weather summary header
        TextView header = new TextView(this);
        String emoji = getWeatherEmoji(latestWeatherSnapshot.weatherCode);
        String label = getWeatherLabel(latestWeatherSnapshot.weatherCode);
        header.setText(String.format("%s  %s in %s\n%.0f°C – %.0f°C",
                emoji, label, latestWeatherSnapshot.destination,
                latestWeatherSnapshot.minTemp, latestWeatherSnapshot.maxTemp));
        header.setTextSize(15);
        header.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        header.setPadding(0, 0, 0, pad / 2);
        container.addView(header);

        // Divider
        View divider = new View(this);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (1 * getResources().getDisplayMetrics().density));
        divParams.bottomMargin = pad / 2;
        divider.setLayoutParams(divParams);
        container.addView(divider);

        // New items section
        if (!newItems.isEmpty()) {
            TextView newLabel = new TextView(this);
            newLabel.setText("📦  Items to add (" + newItems.size() + ")");
            newLabel.setTextSize(14);
            newLabel.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            newLabel.setPadding(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
            container.addView(newLabel);

            for (items item : newItems) {
                TextView row = new TextView(this);
                row.setText("  ＋  " + item.getItemName() + "  →  " + item.getCategory());
                row.setTextSize(13);
                row.setTextColor(ContextCompat.getColor(this, R.color.status_packed));
                row.setPadding(0, (int) (3 * getResources().getDisplayMetrics().density),
                        0, (int) (3 * getResources().getDisplayMetrics().density));
                container.addView(row);
            }
        }

        // Already-added items section
        if (!existingItems.isEmpty()) {
            TextView existingLabel = new TextView(this);
            existingLabel.setText("\n✅  Already in your list (" + existingItems.size() + ")");
            existingLabel.setTextSize(14);
            existingLabel.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            existingLabel.setPadding(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
            container.addView(existingLabel);

            for (items item : existingItems) {
                TextView row = new TextView(this);
                row.setText("  ✓  " + item.getItemName() + "  →  " + item.getCategory());
                row.setTextSize(13);
                row.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
                row.setPadding(0, (int) (3 * getResources().getDisplayMetrics().density),
                        0, (int) (3 * getResources().getDisplayMetrics().density));
                container.addView(row);
            }
        }

        // Wrap in scrollview for safety
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.addView(container);

        // Show dialog
        String positiveText = newItems.isEmpty() ? "OK" : "Add " + newItems.size() + " item(s)";

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🌤️ Weather Suggestions")
                .setView(scrollView);

        if (newItems.isEmpty()) {
            builder.setPositiveButton(positiveText, null);
        } else {
            List<items> finalNewItems = newItems;
            builder.setPositiveButton(positiveText, (dialog, which) -> {
                int inserted = 0;
                for (items item : finalNewItems) {
                    database.mainDAO().saveItem(item);
                    inserted++;
                }
                Toast.makeText(this, getString(R.string.weather_suggestions_added, inserted), Toast.LENGTH_SHORT).show();
                refreshDashboard();
            });
            builder.setNegativeButton(getString(R.string.cancel), null);
        }

        builder.show();
    }

    private items buildSuggestionItem(String name, String category) {
        items item = new items();
        item.setItemName(name);
        item.setCategory(category);
        item.setChecked(false);
        item.setAddedBy(MyConstants.SYSTEM_SMALL);
        item.setTripId(activeTripId);
        return item;
    }

    private boolean isRainOrStorm(int weatherCode) {
        return weatherCode == 51 || weatherCode == 53 || weatherCode == 55
                || weatherCode == 56 || weatherCode == 57
                || weatherCode == 61 || weatherCode == 63 || weatherCode == 65
                || weatherCode == 66 || weatherCode == 67
                || weatherCode == 80 || weatherCode == 81 || weatherCode == 82
                || weatherCode == 95 || weatherCode == 96 || weatherCode == 99;
    }

    private boolean isSnow(int weatherCode) {
        return weatherCode == 71 || weatherCode == 73 || weatherCode == 75
                || weatherCode == 77 || weatherCode == 85 || weatherCode == 86;
    }

    private String getWeatherEmoji(int code) {
        if (code == 0) return "☀️";
        if (code <= 2) return "⛅";
        if (code == 3) return "☁️";
        if (code == 45 || code == 48) return "🌫️";
        if (code >= 51 && code <= 55) return "🌦️";
        if (code == 56 || code == 57) return "🌦️";
        if (code >= 61 && code <= 65) return "🌧️";
        if (code == 66 || code == 67) return "🧊";
        if (code >= 71 && code <= 75) return "❄️";
        if (code == 77) return "❄️";
        if (code >= 80 && code <= 82) return "🌧️";
        if (code == 85 || code == 86) return "🌨️";
        if (code == 95) return "⛈️";
        if (code == 96 || code == 99) return "⛈️";
        return "🌤️";
    }

    private String getWeatherLabel(int code) {
        if (code == 0) return getString(R.string.weather_code_clear);
        if (code == 1 || code == 2) return getString(R.string.weather_code_partly_cloudy);
        if (code == 3) return getString(R.string.weather_code_overcast);
        if (code == 45 || code == 48) return getString(R.string.weather_code_fog);
        if (code >= 51 && code <= 55) return getString(R.string.weather_code_drizzle);
        if (code == 56 || code == 57) return getString(R.string.weather_code_freezing_drizzle);
        if (code >= 61 && code <= 65) return getString(R.string.weather_code_rain);
        if (code == 66 || code == 67) return getString(R.string.weather_code_freezing_rain);
        if (code >= 71 && code <= 75) return getString(R.string.weather_code_snow);
        if (code == 77) return getString(R.string.weather_code_snow_grains);
        if (code >= 80 && code <= 82) return getString(R.string.weather_code_rain_showers);
        if (code == 85 || code == 86) return getString(R.string.weather_code_snow_showers);
        if (code == 95) return getString(R.string.weather_code_thunderstorm);
        if (code == 96 || code == 99) return getString(R.string.weather_code_thunderstorm_hail);
        return getString(R.string.weather_code_unknown);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void addAllTitles(){
        titles= new ArrayList<>();
        titles.add(MyConstants.BASIC_NEEDS_CAMEL_CASE);
        titles.add(MyConstants.CLOTHING_CAMEL_CASE);
        titles.add(MyConstants.PERSONAL_CARE_CAMEL_CASE);
        titles.add(MyConstants.BABY_NEEDS_CAMEL_CASE);
        titles.add(MyConstants.HEALTH_CAMEL_CASE);
        titles.add(MyConstants.TECHNOLOGY_CAMEL_CASE);
        titles.add(MyConstants.FOOD_CAMEL_CASE);
        titles.add(MyConstants.BEACH_SUPPLIES_CAMEL_CASE);
        titles.add(MyConstants.CAR_SUPPLIES_CAMEL_CASE);
        titles.add(MyConstants.NEEDS_CAMEL_CASE);
        titles.add(MyConstants.MY_LIST_CAMEL_CASE);
        titles.add(MyConstants.MY_SELECTIONS_CAMEL_CASE);
    }

    private void addAllImages(){
        images= new ArrayList<>();
        images.add(R.drawable.p1);
        images.add(R.drawable.p2);
        images.add(R.drawable.p3);
        images.add(R.drawable.p4);
        images.add(R.drawable.p5);
        images.add(R.drawable.p6);
        images.add(R.drawable.p7);
        images.add(R.drawable.p8);
        images.add(R.drawable.p9);
        images.add(R.drawable.p10);
        images.add(R.drawable.p11);
        images.add(R.drawable.p12);
    }

    // ═══════════ Options Menu ═══════════

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "About Us");
        menu.add(0, 2, 1, "Unsubscribe");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            startActivity(new Intent(this, AboutUs.class));
            return true;
        } else if (item.getItemId() == 2) {
            showUnsubscribeDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ═══════════ Unsubscribe Flow ═══════════

    private void showUnsubscribeDialog() {
        String userMobile = SubscriptionManager.getUserMobile(this);
        if (userMobile == null || userMobile.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_mobile_found), Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_unsubscribe, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            performUnsubscribe();
        });

        dialog.show();
    }

    private void performUnsubscribe() {
        String userMobile = SubscriptionManager.getUserMobile(this);
        if (userMobile == null || userMobile.isEmpty()) {
            Toast.makeText(this, getString(R.string.unsubscribe_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(this, getString(R.string.unsubscribing), Toast.LENGTH_SHORT).show();

        // Format mobile number for BDApps API (requires 88 country code prefix)
        String formattedMobile;
        if (userMobile.startsWith("88")) {
            formattedMobile = userMobile;
        } else if (userMobile.startsWith("0")) {
            formattedMobile = "88" + userMobile;
        } else {
            formattedMobile = "880" + userMobile;
        }

        UnsubscribeRequest request = new UnsubscribeRequest(formattedMobile);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        apiService.unsubscribe(request).enqueue(new retrofit2.Callback<UnsubscribeResponse>() {
            @Override
            public void onResponse(retrofit2.Call<UnsubscribeResponse> call, retrofit2.Response<UnsubscribeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UnsubscribeResponse body = response.body();

                    if ("S1000".equals(body.getStatusCode())) {
                        // Clear subscription data
                        SubscriptionManager.clearSubscriptionData(MainActivity.this);

                        Toast.makeText(MainActivity.this,
                                getString(R.string.unsubscribe_success), Toast.LENGTH_LONG).show();

                        // Navigate to landing page
                        Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = body.getStatusDetail();
                        if (errorMsg == null) errorMsg = getString(R.string.unsubscribe_failed);
                        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.unsubscribe_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UnsubscribeResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.unsubscribe_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
}