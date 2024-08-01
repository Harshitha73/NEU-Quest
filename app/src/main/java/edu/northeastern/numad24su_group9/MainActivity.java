package edu.northeastern.numad24su_group9;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private long backPressedTime;
    private Toast backToast;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Check if user is logged in
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            // User is logged in and email is verified, navigate to RightNowActivity
            Intent intent = new Intent(MainActivity.this, RightNowActivity.class);
            startActivity(intent);
            finish();
            return; // Exit the method
        }

        // Find the buttons in the layout
        Button signUpButton = findViewById(R.id.signUpButton);
        Button loginButton = findViewById(R.id.loginButton);

        // Set click listeners for the buttons
        signUpButton.setOnClickListener(v -> {
            // Start the sign-up activity
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        loginButton.setOnClickListener(v -> {
            // Start the login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        // Initialize RecyclerView for square buttons
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        String[] buttonTitles = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9"};
        ButtonAdapter adapter = new ButtonAdapter(this, buttonTitles);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns for square buttons
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) backToast.cancel();
            moveTaskToBack(true);
        } else {
            backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}