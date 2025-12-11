package com.example.tripplanner;

import android.annotation.SuppressLint;
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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class AddTripActivity extends AppCompatActivity {

    private static final String TAG = "AddTripActivity";
    private EditText etTripName, etBudget;
    private DatePicker datePicker;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        Log.d(TAG, "AddTripActivity created");

        initViews();
        setupListeners();
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

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveTrip());
    }

    private void saveTrip() {
        String tripName = etTripName.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();

        Log.d(TAG, "Saving trip: " + tripName + ", budget: " + budgetStr);

        if (tripName.isEmpty()) {
            etTripName.setError("Введите название поездки");
            etTripName.requestFocus();
            return;
        }

        if (budgetStr.isEmpty()) {
            etBudget.setError("Введите бюджет");
            etBudget.requestFocus();
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetStr);
            if (budget <= 0) {
                etBudget.setError("Бюджет должен быть больше 0");
                etBudget.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etBudget.setError("Введите корректное число");
            etBudget.requestFocus();
            return;
        }

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        long dateMillis = calendar.getTimeInMillis();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("trip_name", tripName);
        resultIntent.putExtra("trip_budget", budget);
        resultIntent.putExtra("trip_date", dateMillis);

        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Поездка '" + tripName + "' сохранена", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Trip saved, returning to MainActivity");

        finish();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}