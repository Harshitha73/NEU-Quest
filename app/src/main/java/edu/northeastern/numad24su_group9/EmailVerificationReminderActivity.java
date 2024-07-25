package edu.northeastern.numad24su_group9;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationReminderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification_reminder);
        TextView messageTextView = findViewById(R.id.message_text_view);
        Button resendVerificationButton = findViewById(R.id.resend_verification_button);
        Button backToLoginButton = findViewById(R.id.back_to_login_button);
        messageTextView.setText("Please verify your email address before logging in.");
        resendVerificationButton.setOnClickListener(v -> resendVerificationEmail());
        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(EmailVerificationReminderActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void resendVerificationEmail() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailVerificationReminderActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EmailVerificationReminderActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
