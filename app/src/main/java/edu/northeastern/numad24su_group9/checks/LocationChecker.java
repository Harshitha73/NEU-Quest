package edu.northeastern.numad24su_group9.checks;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class LocationChecker {
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";

    public static boolean isSameLocation(String location1, String location2) {
        try {
            // Normalize the input locations
            String normalizedLocation1 = getNormalizedLocation(location1);
            String normalizedLocation2 = getNormalizedLocation(location2);

            // Compare the normalized locations
            Log.e("LocationChecker", "Normalized location 1: " + normalizedLocation1);
            Log.e("LocationChecker", "Normalized location 2: " + normalizedLocation2);
            return normalizedLocation1.equalsIgnoreCase(normalizedLocation2);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getNormalizedLocation(String location) throws Exception {
        String encodedLocation = URLEncoder.encode(location, "UTF-8");
        String apiUrl = NOMINATIM_API_URL + "?format=json&q=" + encodedLocation + "&addressdetails=1";

        StringBuilder response = new StringBuilder();
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                } else {
                    throw new Exception("Unable to normalize the location: " + location);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(response.toString());
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        JSONObject address = jsonObject.getJSONObject("address");
        String city = address.getString("city");
        String state = address.getString("state");
        String country = address.getString("country");

        return String.format("%s, %s, %s", city, state, country);
    }
}