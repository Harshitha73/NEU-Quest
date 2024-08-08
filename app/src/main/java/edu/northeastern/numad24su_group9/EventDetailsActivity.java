package edu.northeastern.numad24su_group9;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.firebase.repository.storage.EventImageRepository;
import edu.northeastern.numad24su_group9.model.Event;

public class EventDetailsActivity extends AppCompatActivity {

    private String previousActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Find the UI components
        TextView eventNameTextView = findViewById(R.id.event_name);
        TextView eventDescriptionTextView = findViewById(R.id.event_description);
        TextView eventStartDateTextView = findViewById(R.id.event_start_date);
        TextView eventEndDateTextView = findViewById(R.id.event_end_date);
        TextView eventStartTimeTextView = findViewById(R.id.event_start_time);
        TextView eventEndTimeTextView = findViewById(R.id.event_end_time);
        TextView eventPriceTextView = findViewById(R.id.event_price);
        TextView eventLocationTextView = findViewById(R.id.event_location);
        ImageView eventImageView = findViewById(R.id.event_image);
        Button registerButton = findViewById(R.id.register_button);
        FloatingActionButton showLocationButton = findViewById(R.id.show_location_fab);
        Button reportButton = findViewById(R.id.report_button);
        TextView alreadyReported = findViewById(R.id.already_reported_label);

        Event event = (Event) getIntent().getSerializableExtra("event");
        previousActivity = getIntent().getStringExtra("previousActivity");

        // Set the event details in the UI components
        assert event != null;
        eventNameTextView.setText(event.getTitle());
        eventDescriptionTextView.setText(event.getDescription());
        eventStartDateTextView.setText(event.getStartDate());
        eventEndDateTextView.setText(event.getEndDate());
        eventStartTimeTextView.setText(event.getStartTime());
        eventEndTimeTextView.setText(event.getEndTime());
        eventPriceTextView.setText(event.getPrice());
        eventLocationTextView.setText(event.getLocation());

        // Hide the report button if the event is already reported
        if(event.getIsReported()) {
            alreadyReported.setVisibility(View.VISIBLE);
            reportButton.setVisibility(View.GONE);
        }

        //On Location click, open maps
        showLocationButton.setOnClickListener(v -> {
            if (!event.getLocation().isEmpty()) {
                // Open the Maps application with the specified address
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(event.getLocation()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        // Load the event image
        EventImageRepository eventImageRepository = new EventImageRepository();
        Picasso.get().load(eventImageRepository.getEventImage(event.getImage())).into(eventImageView);

        // Set the report button click listener
        reportButton.setOnClickListener(v -> {
            event.setIsReported(true);
            alreadyReported.setVisibility(View.VISIBLE);
            reportButton.setVisibility(View.GONE);
            EventRepository eventRepository = new EventRepository();
            DatabaseReference eventRef = eventRepository.getEventRef().child(event.getEventID());
            eventRef.setValue(event);
        });

        // Set the register button click listener
        registerButton.setOnClickListener(v -> {
            // Launch the browser or an in-app registration flow with the registerUrl
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getRegisterLink()));
            try {
                startActivity(intent);
                finish();
            }
            catch(Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Registration URL Error")
                        .setMessage("There is an error with the registration link: " + e)
                        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Handle OK button click
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });

        if(event.getRegisterLink() == null) {
            registerButton.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent;
        if ("RightNowActivity".equals(previousActivity)) {
            intent = new Intent(EventDetailsActivity.this, RightNowActivity.class);
        } else {
            super.onBackPressed();
            return;
        }
        startActivity(intent);
        finish();
    }
}