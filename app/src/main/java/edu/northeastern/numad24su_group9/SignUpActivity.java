package edu.northeastern.numad24su_group9;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameEditText, emailEditText, passwordEditText;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        nameEditText = findViewById(R.id.name_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        Button signUpButton = findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (!email.endsWith("@northeastern.edu")) {
            Toast.makeText(SignUpActivity.this, "We only accept 'northeastern.edu' email ids", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            Log.d("name:" , name);
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileUpdateTask -> {
                                        if (profileUpdateTask.isSuccessful()) {
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Toast.makeText(SignUpActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                                                            String uid = user.getUid();
                                                            SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.putString(AppConstants.UID_KEY, uid);
                                                            editor.apply();
                                                            Intent intent = new Intent(SignUpActivity.this, EmailVerificationActivity.class);
                                                            intent.putExtra("uid", uid);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(SignUpActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(SignUpActivity.this, "Error sending verification email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Profile update failed: " + profileUpdateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign-up failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
