package edu.northeastern.numad24su_group9;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, interestsTextView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private Button editInterestsButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Button rightNowButton = findViewById(R.id.right_now);
        Button exploreButton = findViewById(R.id.explore);
        Button registerEventButton = findViewById(R.id.register_event);
        Button profileButton = findViewById(R.id.profile);

        profileButton.setEnabled(false);

        exploreButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExploreActivity.class);
            startActivity(intent);
            finish();
        });

        registerEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterEventActivity.class);
            startActivity(intent);
            finish();
        });

        rightNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RightNowActivity.class);
            startActivity(intent);
            finish();
        });

        nameTextView = findViewById(R.id.profile_name_text_view);
        emailTextView = findViewById(R.id.profile_email_text_view);
        interestsTextView = findViewById(R.id.profile_interests_text_view);
        editInterestsButton = findViewById(R.id.edit_interests_button);

        editInterestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, InterestsActivity.class);
            intent.putExtra("uid", firebaseUser.getUid());
            intent.putExtra("name", firebaseUser.getDisplayName());
            startActivity(intent);
        });

        if (firebaseUser != null) {
            String name = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();

            nameTextView.setText(name != null ? "Name: " + name : "Name: " + "Name not set");
            emailTextView.setText("Email: " + email);
            loadUserInterests();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserInterests();
    }
    private void loadUserInterests() {
        String uid = firebaseUser.getUid();
        databaseReference.child("Users").child(uid).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@Nullable DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    StringBuilder interestsBuilder = new StringBuilder("Interests: ");
                    for (DataSnapshot interestSnapshot : snapshot.getChildren()) {
                        interestsBuilder.append(interestSnapshot.getValue(String.class)).append(", ");
                    }
                    String interests = interestsBuilder.toString();
                    if (!interests.isEmpty()) {
                        interests = interests.substring(0, interests.length() - 2); // Remove the last comma and space
                    }
                    interestsTextView.setText(interests.isEmpty() ? "Interests: No interests set" : interests);
                } else {
                    interestsTextView.setText("Interests: No interests set");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                interestsTextView.setText("Error loading interests");
            }
        });
    }
}
