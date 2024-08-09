package edu.northeastern.numad24su_group9;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad24su_group9.firebase.DatabaseConnector;
import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.firebase.repository.storage.EventImageRepository;
import edu.northeastern.numad24su_group9.model.Comment;
import edu.northeastern.numad24su_group9.model.Event;
import edu.northeastern.numad24su_group9.recycler.CommentsAdapter;


// Comments will not post :(
public class EventDetailsActivity extends AppCompatActivity {

    private String previousActivity;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(EventDetailsActivity.this, RightNowActivity.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                return true; // Already in the EventDetailsActivity or similar context
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(EventDetailsActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

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
        Button postCommentButton = findViewById(R.id.post_comment_button);
        EditText commentInput = findViewById(R.id.comment_input);

        // Initialize the RecyclerView for comments
        commentsRecyclerView = findViewById(R.id.comments_recyclerview);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsAdapter = new CommentsAdapter(commentsList);
        commentsRecyclerView.setAdapter(commentsAdapter);

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

        // Load comments from the database
        loadComments(event.getEventID());

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

        postCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();

            if (!commentText.isEmpty()) {
                // Add the comment to the database
                addCommentToDatabase(event.getEventID(), commentText);

                // Clear the input field after posting
                commentInput.setText("");
            } else {
                // Show an error message if the comment is empty
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Empty Comment")
                        .setMessage("Please enter a comment before posting.")
                        .setPositiveButton("Dismiss", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        });
    }

    private void addCommentToDatabase(String eventId, String commentText) {
        // Get a reference to the comments section in the Firebase database
        DatabaseReference commentsRef = DatabaseConnector.getInstance().getEventsReference().child(eventId).child("comments");

        // Generate a unique ID for the new comment
        String commentId = commentsRef.push().getKey();

        // Create a Comment object
        Comment comment = new Comment(commentId, commentText, System.currentTimeMillis());

        // Save the comment to the database
        assert commentId != null;
        commentsRef.child(commentId).setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Comment was successfully written
                Log.d("Comment", "Comment posted: " + commentText);
                loadComments(eventId);
            } else {
                // Handle the error
                Log.e("Comment", "Failed to post comment.", task.getException());
            }
        });
    }

    private void loadComments(String eventId) {
        DatabaseReference commentsRef = DatabaseConnector.getInstance().getEventsReference().child(eventId).child("comments");

        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = new Comment();
                    comment.setCommentId(snapshot.child("commentId").getValue(String.class));
                    comment.setCommentText(snapshot.child("commentText").getValue(String.class));
                    comment.setTimestamp(snapshot.child("timestamp").getValue(Long.class));
                    commentsList.add(comment);
                }
                commentsAdapter.updateList(commentsList);
                commentsRecyclerView.setAdapter(commentsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("EventDetailsActivity", "Failed to load comments.", databaseError.toException());
            }
        });
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