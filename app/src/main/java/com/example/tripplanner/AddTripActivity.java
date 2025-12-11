package com.example.tripplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tripplanner.Trip;
import com.example.tripplanner.SupabaseConfig;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTripActivity extends AppCompatActivity {

    private EditText etTripName, etBudget;
    private DatePicker datePicker;
    private Button btnSave;
    private String accessToken;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", null);

        if (accessToken == null || accessToken.isEmpty()) {
            finish();
            return;
        }

        initViews();

        btnSave.setOnClickListener(v -> saveTrip());
    }

    private void initViews() {
        etTripName = findViewById(R.id.etTripName);
        etBudget = findViewById(R.id.etBudget);
        datePicker = findViewById(R.id.datePicker);
        btnSave = findViewById(R.id.btnSave);

        Calendar calendar = Calendar.getInstance();
        datePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                null
        );
    }

    private void saveTrip() {
        String tripName = etTripName.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();

        if (tripName.isEmpty()) {
            Toast.makeText(this, "Введите название поездки", Toast.LENGTH_SHORT).show();
            return;
        }

        double budget = 0;
        try {
            budget = Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректный бюджет", Toast.LENGTH_SHORT).show();
            return;
        }

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(calendar.getTime());

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");

        networkExecutor.execute(() -> {
            try {
                URL url = new URL(SupabaseConfig.TRIPS_TABLE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Prefer", "return=minimal");
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("trip_name", tripName);
                jsonBody.put("trip_budget", budgetStr);
                jsonBody.put("start_date", dateStr);
                jsonBody.put("user_id", userId);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();

                mainHandler.post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        Toast.makeText(AddTripActivity.this, "Поездка добавлена", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddTripActivity.this, "Ошибка: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

                connection.disconnect();
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(AddTripActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkExecutor.shutdown();
    }
}