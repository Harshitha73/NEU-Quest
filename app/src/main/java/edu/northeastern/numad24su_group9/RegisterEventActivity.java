package edu.northeastern.numad24su_group9;

import static edu.northeastern.numad24su_group9.AppConstants.DEFAULT_EVENT_IMAGE_NAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.firebase.repository.storage.EventImageRepository;
import edu.northeastern.numad24su_group9.model.Event;

public class RegisterEventActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private EditText eventNameEditText, eventDescriptionEditText, eventPriceEditText, eventLocationEditText, eventRegisterLinkEditText;
    private TextInputEditText eventStartTimeEditText, eventEndTimeEditText, eventStartDateEditText, eventEndDateEditText;
    private ImageView imageView;
    private String uid, eventID;
    private Uri imageUri;
    private Boolean imageUploaded;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_event);

        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        eventID = String.valueOf(System.currentTimeMillis());

        EventImageRepository eventImageRepo = new EventImageRepository();

        // Find the views
        eventNameEditText = findViewById(R.id.event_name_edittext);
        eventDescriptionEditText = findViewById(R.id.event_description_edittext);
        eventPriceEditText = findViewById(R.id.event_price_edittext);
        eventPriceEditText.setFilters(new InputFilter[]{new CurrencyInputFilter()});
        eventLocationEditText = findViewById(R.id.event_location_edittext);
        eventStartTimeEditText = findViewById(R.id.event_start_time_edittext);
        eventEndTimeEditText = findViewById(R.id.event_end_time_edittext);
        eventStartDateEditText = findViewById(R.id.event_start_date_edittext);
        eventEndDateEditText = findViewById(R.id.event_end_date_edittext);
        eventRegisterLinkEditText = findViewById(R.id.event_register_link_edittext);
        imageView = findViewById(R.id.imageView);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        Button createEventButton = findViewById(R.id.create_event_button);
        Button cancelEventButton = findViewById(R.id.cancelCreateEvent);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        eventStartDateEditText.setOnClickListener(v -> showDatePicker(eventStartDateEditText));
        eventStartTimeEditText.setOnClickListener(v -> showTimePicker(eventStartTimeEditText));
        eventEndDateEditText.setOnClickListener(v -> showDatePicker(eventEndDateEditText));
        eventEndTimeEditText.setOnClickListener(v -> showTimePicker(eventEndTimeEditText));

        // Set the image uploaded field to false so the default image loads
        imageUploaded = false;

        // The launcher runs after the uploaded image activity is done
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        imageUri = data.getData();
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        imageView.setImageURI(imageUri);
                        eventImageRepo.uploadEventImage(imageUri, eventID);
                        imageUploaded = true;
                    }
                });

        // Set click listeners for image upload
        buttonSelectImage.setOnClickListener(v -> openGallery());

        // Set the click listener for the create event button
        createEventButton.setOnClickListener(v -> saveEvent());

        // Set the click listener for the cancel create event button
        cancelEventButton.setOnClickListener(v -> startNextActivity());


        // Set up Bottom Navigation
        if (bottomNavigationView == null) {
            Log.e("RightNowActivity", "bottomNavigationView is null");
        } else {
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_home) {
                        startActivity(new Intent(RegisterEventActivity.this, RightNowActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_budget) {
                        startActivity(new Intent(RegisterEventActivity.this, PlanningTripActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_profile) {
                        startActivity(new Intent(RegisterEventActivity.this, ProfileActivity.class));
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void saveEvent() {
        Event event = new Event();
        if(!(Objects.requireNonNull(eventStartTimeEditText.getText()).toString().isEmpty() ||
                Objects.requireNonNull(eventEndTimeEditText.getText()).toString().isEmpty() ||
                Objects.requireNonNull(eventStartDateEditText.getText()).toString().isEmpty() ||
                Objects.requireNonNull(eventEndDateEditText.getText()).toString().isEmpty())) {
            // Get the values from the EditText fields
            event.setEventID(eventID);
            event.setTitle(eventNameEditText.getText().toString());
            event.setDescription(eventDescriptionEditText.getText().toString());
            event.setPrice(eventPriceEditText.getText().toString());
            event.setLocation(eventLocationEditText.getText().toString());
            event.setStartTime(Objects.requireNonNull(eventStartTimeEditText.getText()).toString());
            event.setEndTime(Objects.requireNonNull(eventEndTimeEditText.getText()).toString());
            event.setStartDate(Objects.requireNonNull(eventStartDateEditText.getText()).toString());
            event.setEndDate(Objects.requireNonNull(eventEndDateEditText.getText()).toString());
            event.setRegisterLink(createValidURL(eventRegisterLinkEditText.getText().toString()));
            event.setCreatedBy(uid);
            event.setIsReported(false);
            if (imageUploaded) {
                event.setImage(eventID);
            } else {
                event.setImage(DEFAULT_EVENT_IMAGE_NAME);
            }

            EventRepository eventRepository = new EventRepository();
            DatabaseReference eventRef = eventRepository.getEventRef().child(event.getEventID());

            eventRef.setValue(event);

            triggerNotification(event);

            Intent intent = new Intent(RegisterEventActivity.this, RightNowActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Toast.makeText(this, "Event dates and times must not be empty", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void triggerNotification(Event event) {
        NotificationHelper.createNotificationChannel(this);

        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("event", event);  // Pass the event object to the activity
        intent.putExtra("previousActivity", "RegisterEventActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create the PendingIntent that wraps the intent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Event Created: " + event.getTitle())
                .setContentText("Location: " + event.getLocation())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @SuppressLint("IntentReset")
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*"); // This allows the user to select files of any type
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        launcher.launch(intent);
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

    private void startNextActivity() {
        Intent intent = new Intent(RegisterEventActivity.this, RightNowActivity.class);
        startActivity(intent);
        finish();
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Go back to RightNowActivity instead of logging out
        Intent intent = new Intent(RegisterEventActivity.this, RightNowActivity.class);
        startActivity(intent);
        finish();
    }

    private String createValidURL(String originalURL) {
        if (originalURL != null) {
            if(originalURL.isEmpty()) {
                return(null);
            }
            // Check if the URL starts with "https://www."
            else if (originalURL.length() >= 12 && originalURL.startsWith("https://www.") || originalURL.startsWith("http://www.")) {
                // Valid URL prefix
                return originalURL;
            } else if (originalURL.startsWith("www.")) {
                return ("https://" + originalURL);
            } else {
                Log.d("NEW URL", "https://www." + originalURL);
                return ("https://www." + originalURL);
            }
        }
        return null;
    }
}