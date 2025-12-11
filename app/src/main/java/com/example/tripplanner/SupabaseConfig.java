package com.example.tripplanner;

public class SupabaseConfig {
    public static final String SUPABASE_URL = "https://fdyanjvpqyjkcsngyset.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZkeWFuanZwcXlqa2Nzbmd5c2V0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU0Mjk0MTAsImV4cCI6MjA4MTAwNTQxMH0.-tyYvkudMLnYA4C3SFx89wGbtIRcd_ykQeHy3jKChBE";

    public static final String AUTH_SIGNUP_URL = SUPABASE_URL + "/auth/v1/signup";
    public static final String AUTH_SIGNIN_URL = SUPABASE_URL + "/auth/v1/token?grant_type=password";

    public static final String TRIPS_TABLE_URL = SUPABASE_URL + "/rest/v1/trips";

    public static String readStream(java.io.InputStream inputStream) {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
