package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.northeastern.numad24su_group9.firebase.AuthConnector;
import edu.northeastern.numad24su_group9.firebase.repository.database.TripRepository;
import edu.northeastern.numad24su_group9.firebase.repository.database.UserRepository;
import edu.northeastern.numad24su_group9.firebase.repository.storage.UserProfileRepository;
import edu.northeastern.numad24su_group9.model.Trip;
import edu.northeastern.numad24su_group9.model.User;
import edu.northeastern.numad24su_group9.recycler.TripAdapter;

public class ProfileActivity extends AppCompatActivity {

    private TextView interestsTextView;
    private TextView campusTextView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private ActivityResultLauncher<Intent> launcher;
    private ImageView userProfileImage;
    private Uri imageUri;
    private String uid;
    private User user;
    private UserRepository userRepository;
    private UserProfileRepository userProfileRepo;
    private TextView userNameTextView;
    private List<Trip> trips;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Button adminConsoleButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        userNameTextView = findViewById(R.id.user_name);
        TextView nameTextView = findViewById(R.id.profile_name_text_view);
        TextView emailTextView = findViewById(R.id.profile_email_text_view);
        campusTextView = findViewById(R.id.profile_campus_text_view);
        interestsTextView = findViewById(R.id.profile_interests_text_view);
        Button editInterestsButton = findViewById(R.id.edit_interests_button);
        Button logoutButton = findViewById(R.id.logout_button);
        Button deleteAccountButton = findViewById(R.id.delete_account_button);
        adminConsoleButton = findViewById(R.id.admin_console);
        userProfileImage = findViewById(R.id.user_profile_image);
        TextView changeProfileImageTextView = findViewById(R.id.change_profile_image);

        // Set up the click listener on the user's profile image view
        userProfileImage.setOnClickListener(v -> showImageSourceDialog());

        // Get the current user's ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        userRepository = new UserRepository(uid);
        userProfileRepo = new UserProfileRepository(uid);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set the click listener on the "Change Profile Image" TextView
        changeProfileImageTextView.setOnClickListener(v -> showImageSourceDialog());

        getUser(uid);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getExtras() != null) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    assert photo != null;
                    imageUri = getImageUri(this, photo);
                    Picasso.get().load(imageUri).into(userProfileImage);
                    userProfileRepo.uploadProfileImage(imageUri, uid);

                    DatabaseReference userRef = userRepository.getUserRef();
                    userRef.child("profileImage").setValue(uid);
                } else if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        Picasso.get().load(selectedImageUri).into(userProfileImage);
                        userProfileRepo.uploadProfileImage(selectedImageUri, uid);
                        DatabaseReference userRef = userRepository.getUserRef();
                        userRef.child("profileImage").setValue(uid);
                    }
                }
            }
        });

        editInterestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, InterestsActivity.class);
            intent.putExtra("uid", firebaseUser.getUid());
            intent.putExtra("name", firebaseUser.getDisplayName());
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());

        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());

        adminConsoleButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminConsole.class);
            startActivity(intent);
        });

        if (firebaseUser != null) {
            String name = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();

            setFormattedText(nameTextView, "Name: ", name != null ? name : "Name not set");
            setFormattedText(emailTextView, "Email: ", email);
            loadUserInterests();
        }

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(ProfileActivity.this, RightNowActivity.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                startActivity(new Intent(ProfileActivity.this, PlanningTripActivity.class));
                return true;
            } else return itemId == R.id.navigation_profile;
        });
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Camera
                            dispatchTakePictureIntent();
                            break;
                        case 1: // Gallery
                            pickImageFromGallery();
                            break;
                    }
                })
                .show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Log.d("ProfileActivity", "Camera permission granted: " +
                    (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED));
            Log.d("ProfileActivity", "Camera Intent can be resolved: " +
                    (takePictureIntent.resolveActivity(getPackageManager()) != null));
            try {
                launcher.launch(takePictureIntent);
            } catch (Exception e) {
                Log.e("ProfileActivity", "Failed to launch camera", e);
                Toast.makeText(this, "Failed to open the camera. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("ProfileActivity", "Requesting");

            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("IntentReset")
    private void pickImageFromGallery() {
        @SuppressLint("IntentReset") Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        launcher.launch(intent);
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    public void getUser(String uid) {
        new Thread(() -> {
            user = new User();
            user.setUserID(uid);

            Task<DataSnapshot> task = userRepository.getUserRef().get();
            task.addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    user.setName(dataSnapshot.child("name").getValue(String.class));
                    user.setProfileImage(dataSnapshot.child("profileImage").getValue(String.class));
                    user.setCampus(dataSnapshot.child("campus").getValue(String.class));
                    user.setIsAdmin(dataSnapshot.child("isAdmin").getValue(Boolean.class));
                    Log.d("isADMIN", user.getIsAdmin().toString());
                    List<String> tripIDs = new ArrayList<>();
                    for (DataSnapshot tripSnapshot : dataSnapshot.child("plannedTrips").getChildren()) {
                        String tripID = tripSnapshot.getValue(String.class);
                        tripIDs.add(tripID);
                    }
                    user.setTrips(tripIDs);

                    // Update the UI on the main thread
                    runOnUiThread(() -> {
                        updateUI();
                    });
                }
            }).addOnFailureListener(e -> {
                // Handle the failure case on the main thread
                runOnUiThread(() -> {
                    Log.e("UserRepository", "Error retrieving user data: " + e.getMessage());
                });
            });
        }).start();
    }

    public void updateUI() {
        assert user != null;
        userNameTextView.setText(user.getName());

        String campus = user.getCampus();
        setFormattedText(campusTextView, "Campus: ", campus != null ? campus : "Not Set");

        Uri profileImageUri = userProfileRepo.getProfileImage(user.getProfileImage());
        Picasso.get().load(profileImageUri).into(userProfileImage);

        if (user.getIsAdmin()) {
            adminConsoleButton.setVisibility(View.VISIBLE);
        }

        if (user.getTrips() != null) {
            getTrips();
        }
    }

    private void updateTripUI() {
        RecyclerView tripRecyclerView = findViewById(R.id.trips_recycler_view);
        tripRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        TripAdapter tripAdapter = new TripAdapter();
        tripAdapter.updateTrips(trips);
        tripAdapter.setOnItemClickListener((trip) -> {
            Intent intent = new Intent(ProfileActivity.this, TripDetailsActivity.class);
            intent.putExtra("trip", trip);
            startActivity(intent);
            finish();
        });
        tripAdapter.setOnItemSelectListener(this::removeTrip);
        tripRecyclerView.setAdapter(tripAdapter);
    }

    private void removeTrip(Trip trip) {
        new Thread(() -> {
            TripRepository tripRepository = new TripRepository();
            DatabaseReference tripRef = tripRepository.getTripRef();

            // Remove the trip from the database
            tripRef.child(trip.getTripID()).removeValue();

            UserRepository userRepository = new UserRepository(uid);
            DatabaseReference userRef = userRepository.getUserRef();
            Task<DataSnapshot> task = userRef.child("plannedTrips").get();

            task.addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                        if (Objects.equals(tripSnapshot.getValue(String.class), trip.getTripID())) {
                            tripSnapshot.getRef().removeValue();
                            break;
                        }
                    }
                }

                // Update the UI on the main thread
                runOnUiThread(() -> {
                    trips.remove(trip);
                    updateTripUI();
                });
            }).addOnFailureListener(e -> runOnUiThread(() -> {
                e.printStackTrace();
                // Handle the failure case
            }));
        }).start();
    }

    public void getTrips() {
        new Thread(() -> {
            TripRepository tripRepository = new TripRepository();
            trips = new ArrayList<>();

            Task<DataSnapshot> task = tripRepository.getTripRef().get();
            task.addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    for (String tripID : user.getTrips()) {
                        Trip trip = new Trip();
                        trip.setTripID(tripID);
                        trip.setTitle(dataSnapshot.child(tripID).child("title").getValue(String.class));
                        trip.setMinBudget(dataSnapshot.child(tripID).child("minBudget").getValue(String.class));
                        trip.setMaxBudget(dataSnapshot.child(tripID).child("maxBudget").getValue(String.class));
                        trip.setMealsIncluded(dataSnapshot.child(tripID).child("mealsIncluded").getValue(String.class));
                        trip.setTransportIncluded(dataSnapshot.child(tripID).child("transportIncluded").getValue(String.class));
                        trip.setLocation(dataSnapshot.child(tripID).child("location").getValue(String.class));
                        trip.setStartDate(dataSnapshot.child(tripID).child("startDate").getValue(String.class));
                        trip.setStartTime(dataSnapshot.child(tripID).child("startTime").getValue(String.class));
                        trip.setEndDate(dataSnapshot.child(tripID).child("endDate").getValue(String.class));
                        trip.setEndTime(dataSnapshot.child(tripID).child("endTime").getValue(String.class));
                        List<String> eventIDs = new ArrayList<>();
                        for (DataSnapshot eventSnapshot : dataSnapshot.child(tripID).child("eventIDs").getChildren()) {
                            String eventID = eventSnapshot.getValue(String.class);
                            eventIDs.add(eventID);
                        }
                        trip.setEventIDs(eventIDs);
                        trips.add(trip);
                    }

                    runOnUiThread(this::updateTripUI);
                }
            }).addOnFailureListener(e -> runOnUiThread(() -> {
                e.printStackTrace();
                // Handle the failure case
            }));
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInterests();
    }

    private void loadUserInterests() {
        new Thread(() -> {
            String uid = firebaseUser.getUid();
            databaseReference.child("Users").child(uid).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@Nullable DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        StringBuilder interestsBuilder = new StringBuilder();
                        for (DataSnapshot interestSnapshot : snapshot.getChildren()) {
                            interestsBuilder.append(interestSnapshot.getValue(String.class)).append(", ");
                        }
                        String interests = interestsBuilder.toString();
                        if (!interests.isEmpty()) {
                            interests = interests.substring(0, interests.length() - 2); // Remove the last comma and space
                        }
                        String finalInterests = interests;
                        runOnUiThread(() -> setFormattedText(interestsTextView, "Interests: ", finalInterests.isEmpty() ? "No interests set" : finalInterests));
                    } else {
                        runOnUiThread(() -> setFormattedText(interestsTextView, "Interests: ", "No interests set"));
                    }
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    runOnUiThread(() -> setFormattedText(interestsTextView, "Interests: ", "Error loading interests"));
                }
            });
        }).start();
    }

    private void setFormattedText(TextView textView, String label, String value) {
        SpannableString spannableString = new SpannableString(label + value);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
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

        // Delete from Firebase Authentication
        if (firebaseUser != null) {
            firebaseAuth.signOut();
            new Thread(() -> {
                TripRepository tripRepository = new TripRepository();
                DatabaseReference tripRef = tripRepository.getTripRef();
                user.getTrips().forEach(tripID -> tripRef.child(tripID).removeValue());

                UserRepository userRepository = new UserRepository(uid);
                DatabaseReference userRef = userRepository.getUserRef();
                userRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Delete the user
                        firebaseUser.delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // User has been successfully deleted
                                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "User data deleted successfully", Toast.LENGTH_SHORT).show());
                                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // An error occurred while deleting the user
                                        Log.e("Firebase Auth", "Error deleting user: " + Objects.requireNonNull(task.getException()).getMessage());
                                    }
                                });
                    } else {
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to delete user data", Toast.LENGTH_SHORT).show());
                    }
                });

            }).start();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileActivity.this, RightNowActivity.class);
        startActivity(intent);
        finish();
    }
}