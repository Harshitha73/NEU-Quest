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
import android.util.Log;
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

import edu.northeastern.numad24su_group9.firebase.repository.database.TripRepository;
import edu.northeastern.numad24su_group9.firebase.repository.database.UserRepository;
import edu.northeastern.numad24su_group9.firebase.repository.storage.UserProfileRepository;
import edu.northeastern.numad24su_group9.model.Trip;
import edu.northeastern.numad24su_group9.model.User;
import edu.northeastern.numad24su_group9.recycler.TripAdapter;
import android.provider.MediaStore;


public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, interestsTextView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private Button editInterestsButton, deleteAccountButton, logoutButton;
    private ActivityResultLauncher<Intent> launcher;
    private ImageView userProfileImage;
    private Uri imageUri;
    private String uid;
    private User user;
    private UserRepository userRepository;
    private UserProfileRepository userProfileRepo;
    private TextView userNameTextView;
    private List<Trip> trips;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up the click listener on the user's profile image view
        userProfileImage = findViewById(R.id.user_profile_image);
        userProfileImage.setOnClickListener(v -> showImageSourceDialog());

        // Get the current user's ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        userRepository = new UserRepository(uid);
        userProfileRepo = new UserProfileRepository(uid);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        userNameTextView = findViewById(R.id.user_name);
        TextView changeProfileImageTextView = findViewById(R.id.change_profile_image);

        // Set the click listener on the "Change Profile Image" TextView
        changeProfileImageTextView.setOnClickListener(v -> showImageSourceDialog());

        getUser(uid);

        TextView plannedTripsTextView = findViewById(R.id.planned_trips_title);
        plannedTripsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PlanningTripActivity.class);
            startActivity(intent);
            finish();
        });

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getExtras() != null) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    imageUri = getImageUri(this, photo);
                    Picasso.get().load(imageUri).into(userProfileImage);
                    userProfileRepo.uploadProfileImage(imageUri, uid);

                    DatabaseReference userRef = userRepository.getUserRef();
                    userRef.child("profileImage").setValue(uid);
                }
                else if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        // Load the selected image into the ImageView
                        Picasso.get().load(selectedImageUri).into(userProfileImage);

                        // Update the profile image in Firebase
                        userProfileRepo.uploadProfileImage(selectedImageUri, uid);

                        // Update the profile image reference in the database
                        DatabaseReference userRef = userRepository.getUserRef();
                        userRef.child("profileImage").setValue(uid);
                    }
                }
            }
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
            } else if (itemId == R.id.navigation_profile) {
                return true;
            }
            return false;
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
    private void dispatchTakePictureIntent() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        } else {
            // Request camera permission
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

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        launcher.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUri(this, photo);
                Picasso.get().load(imageUri).into(userProfileImage);
                userProfileRepo.uploadProfileImage(imageUri, uid);

                DatabaseReference userRef = userRepository.getUserRef();
                userRef.child("profileImage").setValue(uid);
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    public void getUser(String uid) {
        user = new User();
        user.setUserID(uid);

        Task<DataSnapshot> task = userRepository.getUserRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                user.setName(dataSnapshot.child("name").getValue(String.class));
                user.setProfileImage(dataSnapshot.child("profileImage").getValue(String.class));
                List<String> tripIDs = new ArrayList<>();
                for (DataSnapshot tripSnapshot : dataSnapshot.child("plannedTrips").getChildren()) {
                    String tripID = tripSnapshot.getValue(String.class);
                    tripIDs.add(tripID);
                }
                user.setTrips(tripIDs);
                updateUI();
            }
        }).addOnFailureListener(e -> {
            // Handle any exceptions that occur during the database query
            Log.e("UserRepository", "Error retrieving user data: " + e.getMessage());
        });
    }

    public void updateUI() {
        assert user != null;
        userNameTextView.setText(user.getName());

        Uri profileImageUri = userProfileRepo.getProfileImage(user.getProfileImage());
        Picasso.get().load(profileImageUri).into(userProfileImage);

        if (user.getTrips() != null) {
            getTrips();
        }
    }

    public void getTrips() {
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

                // Setup recycler view and show all trips
                RecyclerView tripRecyclerView = findViewById(R.id.trips_recycler_view);
                tripRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                TripAdapter tripAdapter = new TripAdapter(trips);
                tripAdapter.setOnItemClickListener((trip) -> {
                    Intent intent = new Intent(ProfileActivity.this, TripDetailsActivity.class);
                    intent.putExtra("trip", trip);
                    startActivity(intent);
                    finish();
                });
                tripRecyclerView.setAdapter(tripAdapter);
            }
        }).addOnFailureListener(Throwable::printStackTrace);
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
