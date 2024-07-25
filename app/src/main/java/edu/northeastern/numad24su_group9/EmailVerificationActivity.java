package edu.northeastern.numad24su_group9;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private static final int CHECK_INTERVAL = 5000; // 5 seconds
    private Handler handler;
    private Runnable checkVerificationTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            TextView infoTextView = findViewById(R.id.info_text_view);
            infoTextView.setText("A verification email has been sent to " + currentUser.getEmail() + ". Please check your inbox.");

            handler = new Handler();
            checkVerificationTask = new Runnable() {
                @Override
                public void run() {
                    checkEmailVerification();
                    handler.postDelayed(this, CHECK_INTERVAL);
                }
            };
            handler.post(checkVerificationTask);
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkEmailVerification() {
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        handler.removeCallbacks(checkVerificationTask);
                        Intent intent = new Intent(EmailVerificationActivity.this, InterestsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(EmailVerificationActivity.this, "Failed to reload user info", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && checkVerificationTask != null) {
            handler.removeCallbacks(checkVerificationTask);
        }
    }
}
