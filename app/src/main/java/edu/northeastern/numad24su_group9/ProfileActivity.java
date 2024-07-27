package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
    private Button editInterestsButton, deleteAccountButton, logoutButton;

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
        logoutButton = findViewById(R.id.logout_button);
        deleteAccountButton = findViewById(R.id.delete_account_button);

        editInterestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, InterestsActivity.class);
            intent.putExtra("uid", firebaseUser.getUid());
            intent.putExtra("name", firebaseUser.getDisplayName());
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());

        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());

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
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteAccount())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        firebaseAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void deleteAccount() {
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            // Delete user data from Firebase Database
            databaseReference.child("Users").child(uid).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Delete user authentication
                    firebaseUser.delete().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to delete user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Go back to RightNowActivity instead of logging out
        Intent intent = new Intent(ProfileActivity.this, RightNowActivity.class);
        startActivity(intent);
        finish();
    }
}
