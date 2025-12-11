package com.example.tripplanner;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tripplanner.SupabaseConfig;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "Activity created");

        initViews();
        setupListeners();
        checkExistingSession();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            signIn();
        });
        btnRegister.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            signUp();
        });
    }

    private void checkExistingSession() {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);

        Log.d(TAG, "Checking session, token exists: " + (accessToken != null));

        if (accessToken != null && !accessToken.isEmpty()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void signIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Attempting sign in for email: " + email);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        networkExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String urlString = SupabaseConfig.AUTH_SIGNIN_URL;
                Log.d(TAG, "Connecting to URL: " + urlString);

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);

                String jsonInputString = jsonBody.toString();
                Log.d(TAG, "Request JSON: " + jsonInputString);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                String response;
                if (responseCode >= 200 && responseCode < 300) {
                    response = SupabaseConfig.readStream(connection.getInputStream());
                    Log.d(TAG, "Success response: " + response);
                } else {
                    response = SupabaseConfig.readStream(connection.getErrorStream());
                    Log.e(TAG, "Error response: " + response);
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonResponse = new JSONObject(response);
                    String accessToken = jsonResponse.getString("access_token");
                    String refreshToken = jsonResponse.getString("refresh_token");
                    String userId = jsonResponse.getJSONObject("user").getString("id");

                    Log.d(TAG, "Login successful, userId: " + userId);

                    SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
                    prefs.edit()
                            .putString("access_token", accessToken)
                            .putString("refresh_token", refreshToken)
                            .putString("user_id", userId)
                            .apply();

                    mainHandler.post(() -> {
                        Toast.makeText(LoginActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                } else {
                    mainHandler.post(() -> {
                        String errorMsg = "Ошибка входа: " + responseCode;
                        if (response != null && response.contains("error")) {
                            try {
                                JSONObject errorJson = new JSONObject(response);
                                errorMsg = errorJson.getString("error_description");
                            } catch (Exception e) {
                            }
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Sign in error: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    String errorMsg = "Ошибка подключения: " + e.getMessage();
                    if (e.getMessage().contains("failed to connect")) {
                        errorMsg = "Нет подключения к интернету";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void signUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Attempting sign up for email: " + email);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        networkExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String urlString = SupabaseConfig.AUTH_SIGNUP_URL;
                Log.d(TAG, "Connecting to URL: " + urlString);

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);

                JSONObject data = new JSONObject();
                data.put("email_confirm", false);
                jsonBody.put("data", data);

                String jsonInputString = jsonBody.toString();
                Log.d(TAG, "Request JSON: " + jsonInputString);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                String response;
                if (responseCode >= 200 && responseCode < 300) {
                    response = SupabaseConfig.readStream(connection.getInputStream());
                    Log.d(TAG, "Success response: " + response);
                } else {
                    response = SupabaseConfig.readStream(connection.getErrorStream());
                    Log.e(TAG, "Error response: " + response);
                }

                mainHandler.post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(LoginActivity.this,
                                "Регистрация успешна! Теперь войдите",
                                Toast.LENGTH_SHORT).show();

                        etPassword.setText("");
                    } else {
                        String errorMsg = "Ошибка регистрации: " + responseCode;
                        if (response != null && response.contains("error")) {
                            try {
                                JSONObject errorJson = new JSONObject(response);
                                errorMsg = errorJson.getString("message");
                            } catch (Exception e) {
                            }
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Sign up error: " + e.getMessage(), e);
                mainHandler.post(() ->
                        Toast.makeText(LoginActivity.this,
                                "Ошибка: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkExecutor != null && !networkExecutor.isShutdown()) {
            networkExecutor.shutdown();
        }
    }
}