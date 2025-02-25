package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.northeastern.numad24su_group9.gemini.GeminiClient;
import edu.northeastern.numad24su_group9.model.Trip;

public class PlanningTripActivity extends AppCompatActivity {
    private RangeSlider budgetRangeSlider;
    private TextView minBudgetTextView, maxBudgetTextView;
    private EditText eventLocationEditText;
    private TextInputEditText eventStartTimeEditText, eventEndTimeEditText, eventStartDateEditText, eventEndDateEditText;
    private CheckBox mealsCheckbox, transportCheckbox;
    private Button submitButton;
    private String minBudget, maxBudget, mealsIncluded, transportIncluded, location, startDate, endDate, startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_trip);
        bindViews();
        setupBudgetSlider();
        setupSubmitButton();
        setupDateTimePicker();

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(PlanningTripActivity.this, RightNowActivity.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                return true; // Already in the PlanningTripActivity
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(PlanningTripActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void bindViews() {
        budgetRangeSlider = findViewById(R.id.budget_range_slider);
        minBudgetTextView = findViewById(R.id.min_budget_text_view);
        maxBudgetTextView = findViewById(R.id.max_budget_text_view);
        mealsCheckbox = findViewById(R.id.meals_checkbox);
        transportCheckbox = findViewById(R.id.transport_checkbox);
        submitButton = findViewById(R.id.submit_button);
        eventLocationEditText = findViewById(R.id.event_location_edittext);
        eventStartTimeEditText = findViewById(R.id.event_start_time_edittext);
        eventEndTimeEditText = findViewById(R.id.event_end_time_edittext);
        eventStartDateEditText = findViewById(R.id.event_start_date_edittext);
        eventEndDateEditText = findViewById(R.id.event_end_date_edittext);
    }

    private void setupDateTimePicker() {
        // Set click listeners for the date and time edit text views
        eventStartDateEditText.setOnClickListener(v -> showDatePicker(eventStartDateEditText));
        eventStartTimeEditText.setOnClickListener(v -> showTimePicker(eventStartTimeEditText));
        eventEndDateEditText.setOnClickListener(v -> showDatePicker(eventEndDateEditText));
        eventEndTimeEditText.setOnClickListener(v -> showTimePicker(eventEndTimeEditText));
    }

    private void showDatePicker(TextInputEditText editText) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date as a string
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);

                    // Set the selected date value in the TextView
                    editText.setText(selectedDate);
                },
                year, month, day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    private void showTimePicker(TextInputEditText editText) {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    // Format the selected time as a string
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);

                    // Set the selected time value in the TextView
                    editText.setText(selectedTime);
                },
                currentHour, currentMinute, true // true for 24-hour format
        );

        // Show the time picker dialog
        timePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void setupBudgetSlider() {
        minBudget = String.valueOf(budgetRangeSlider.getValues().get(0));
        maxBudget = String.valueOf(budgetRangeSlider.getValues().get(1));
        budgetRangeSlider.setValueFrom(0f);
        budgetRangeSlider.setValueTo(1000f);
        budgetRangeSlider.setStepSize(50f);
        budgetRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            minBudgetTextView.setText("$" + slider.getValues().get(0));
            maxBudgetTextView.setText("$" + slider.getValues().get(1));
        });
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            Trip trip = new Trip();

            // Create a ThreadPoolExecutor
            int numThreads = Runtime.getRuntime().availableProcessors();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

            GeminiClient geminiClient = new GeminiClient();
            if (eventLocationEditText.getText().toString().isEmpty()) {
                eventLocationEditText.setError("Please enter a location");
                return;
            }
            if (eventStartTimeEditText.getText().toString().isEmpty()) {
                eventStartTimeEditText.setError("Please enter a start time");
                return;
            }

            ListenableFuture<GenerateContentResponse> response = geminiClient.generateResult("Give me just one trip name for a trip starting on " + Objects.requireNonNull(eventStartTimeEditText.getText()) + " to " + eventLocationEditText.getText().toString());

            // Generate trip name using Gemini API
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    Log.e("TripAdapter", "Success");
                    Pattern pattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
                    Matcher matcher = pattern.matcher(result.getText());
                    if (matcher.find()) {
                        trip.setTitle(matcher.group(1));
                        trip.setMinBudget(String.valueOf(budgetRangeSlider.getValues().get(0)));
                        trip.setMaxBudget(String.valueOf(budgetRangeSlider.getValues().get(1)));
                        trip.setMealsIncluded(String.valueOf(mealsCheckbox.isChecked()));
                        trip.setTransportIncluded(String.valueOf(transportCheckbox.isChecked()));
                        trip.setLocation(eventLocationEditText.getText().toString());
                        trip.setStartDate(eventStartDateEditText.getText().toString());
                        trip.setEndDate(eventEndDateEditText.getText().toString());
                        trip.setStartTime(eventStartTimeEditText.getText().toString());
                        trip.setEndTime(eventEndTimeEditText.getText().toString());
                        trip.setTripID(String.valueOf(System.currentTimeMillis()));

                        Intent intent = new Intent(PlanningTripActivity.this, AddEventsActivity.class);
                        intent.putExtra("trip", trip);
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    // Handle the failure on the main thread
                    Log.e("TripAdapter", "Error: " + t.getMessage());
                }
            }, executor);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        minBudget = String.valueOf(budgetRangeSlider.getValues().get(0));
        maxBudget = String.valueOf(budgetRangeSlider.getValues().get(1));
        mealsIncluded = String.valueOf(mealsCheckbox.isChecked());
        transportIncluded = String.valueOf(transportCheckbox.isChecked());
        location = eventLocationEditText.getText().toString();
        startDate = eventStartDateEditText.getText().toString();
        endDate = eventEndDateEditText.getText().toString();
        startTime = eventStartTimeEditText.getText().toString();
        endTime = eventEndTimeEditText.getText().toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        budgetRangeSlider.setValues(Float.parseFloat(minBudget), Float.parseFloat(maxBudget));
        mealsCheckbox.setChecked(Boolean.parseBoolean(mealsIncluded));
        transportCheckbox.setChecked(Boolean.parseBoolean(transportIncluded));
        eventLocationEditText.setText(location);
        eventStartDateEditText.setText(startDate);
        eventEndDateEditText.setText(endDate);
        eventStartTimeEditText.setText(startTime);
        eventEndTimeEditText.setText(endTime);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PlanningTripActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}