package edu.northeastern.numad24su_group9.checks;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LocationChecker {

    private static String getNormalizedLocation(String location) throws Exception {

        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";

        final Cache<String, String> LOCATION_CACHE = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        String encodedLocation = URLEncoder.encode(location, "UTF-8");
        String apiUrl = NOMINATIM_API_URL + "?format=json&q=" + encodedLocation + "&addressdetails=1";

        return LOCATION_CACHE.get(location, () -> {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                StringBuilder response = new StringBuilder();
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return response.toString();
            }, executorService);

            JSONArray jsonArray = new JSONArray(future.get());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONObject address = jsonObject.getJSONObject("address");
            String city = address.getString("city");
            String state = address.getString("state");
            String country = address.getString("country");

            return String.format("%s, %s, %s", city, state, country);
        });
    }

    public static boolean isSameLocation(String location1, String location2) {
        try {
            // Normalize the input locations
            String normalizedLocation1 = getNormalizedLocation(location1);
            String normalizedLocation2 = getNormalizedLocation(location2);

            // Compare the normalized locations
            return normalizedLocation1.equalsIgnoreCase(normalizedLocation2);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}