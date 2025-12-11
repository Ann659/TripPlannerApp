package com.example.tripplanner;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tripplanner.TripAdapter;
import com.example.tripplanner.Trip;
import com.example.tripplanner.SupabaseConfig;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvTrips;
    private FloatingActionButton fabAdd;
    private TripAdapter adapter;
    private String accessToken;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", null);

        if (accessToken == null || accessToken.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadTrips();

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTripActivity.class));
        });
    }

    private void initViews() {
        rvTrips = findViewById(R.id.rvTrips);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new TripAdapter();
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        rvTrips.setAdapter(adapter);
    }

    private void loadTrips() {
        networkExecutor.execute(() -> {
            try {
                URL url = new URL(SupabaseConfig.TRIPS_TABLE_URL + "?select=*&order=start_date.desc");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    List<Trip> trips = parseTrips(response.toString());
                    mainHandler.post(() -> adapter.setTrips(trips));

                } else {
                    mainHandler.post(() ->
                            Toast.makeText(MainActivity.this, "Ошибка загрузки: " + responseCode, Toast.LENGTH_SHORT).show()
                    );
                }

                connection.disconnect();
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(MainActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private List<Trip> parseTrips(String json) {
        List<Trip> trips = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Trip trip = new Trip();
                trip.setId(obj.getString("id"));
                trip.setTripName(obj.getString("trip_name"));
                trip.setTripBudget(obj.getDouble("trip_budget"));

                String dateStr = obj.getString("start_date");
                trip.setStartDate(sdf.parse(dateStr));

                trips.add(trip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trips;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkExecutor.shutdown();
    }
}