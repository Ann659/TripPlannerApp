package com.example.tripplanner;

import android.os.Bundle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tripplanner.TripAdapter;
import com.example.tripplanner.Trip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ADD_TRIP_REQUEST = 1;

    private RecyclerView rvTrips;
    private FloatingActionButton fabAdd;
    private Button btnLogout;
    private TripAdapter adapter;
    private List<Trip> tripList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity created");

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (!isLoggedIn) {
            Toast.makeText(this, "Сначала войдите в систему", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupListeners();
        loadTrips();
    }

    private void initViews() {
        rvTrips = findViewById(R.id.rvTrips);
        fabAdd = findViewById(R.id.fabAdd);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupRecyclerView() {
        tripList = new ArrayList<>();
        adapter = new TripAdapter();
        adapter.setTrips(tripList);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        rvTrips.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            Log.d(TAG, "Добавить поездку");
            Intent intent = new Intent(this, AddTripActivity.class);
            startActivityForResult(intent, ADD_TRIP_REQUEST);
        });

        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }
    private void loadTrips() {
        tripList.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            tripList.add(createTrip("Отпуск в Сочи", 50000, "2024-06-15", sdf));
            tripList.add(createTrip("Поездка в Казань", 25000, "2024-07-20", sdf));
            tripList.add(createTrip("Выходные в Питере", 15000, "2024-05-10", sdf));
        } catch (Exception e) {
            Log.e(TAG, "Error creating test trips: " + e.getMessage());
        }

        adapter.setTrips(tripList);

        Toast.makeText(this, "Загружено " + tripList.size() + " поездок", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Loaded " + tripList.size() + " trips");
    }

    public void addNewTrip(String tripName, double budget, Date startDate) {
        Log.d(TAG, "Adding new trip: " + tripName);

        Trip newTrip = new Trip();
        newTrip.setTripName(tripName);
        newTrip.setTripBudget(budget);
        newTrip.setStartDate(startDate);

        tripList.add(0, newTrip);

        adapter.setTrips(tripList);

        Toast.makeText(this, "Поездка '" + tripName + "' добавлена!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Trip added successfully. Total trips: " + tripList.size());
    }

    private Trip createTrip(String name, double budget, String date, SimpleDateFormat sdf) {
        Trip trip = new Trip();
        trip.setTripName(name);
        trip.setTripBudget(budget);
        try {
            trip.setStartDate(sdf.parse(date));
        } catch (Exception e) {
            trip.setStartDate(new Date());
        }
        return trip;
    }

    private void logout() {
        Log.d(TAG, "Выход из системы");

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(this, "Выход выполнен", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == ADD_TRIP_REQUEST && resultCode == RESULT_OK && data != null) {
            String tripName = data.getStringExtra("trip_name");
            double budget = data.getDoubleExtra("trip_budget", 0);
            long dateMillis = data.getLongExtra("trip_date", System.currentTimeMillis());

            Date startDate = new Date(dateMillis);

            Log.d(TAG, "Received new trip: " + tripName + ", budget: " + budget);

            addNewTrip(tripName, budget, startDate);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: refreshing trips list");
    }
}