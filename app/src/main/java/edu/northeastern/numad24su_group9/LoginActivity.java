package edu.northeastern.numad24su_group9;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import edu.northeastern.numad24su_group9.firebase.AuthConnector;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        Button loginButton = findViewById(R.id.login_button);


        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        // Get the email and password from the EditText fields
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        AuthConnector.getFirebaseAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = AuthConnector.getFirebaseAuth().getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                String uid = user.getUid();

                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(AppConstants.UID_KEY, uid);
                                editor.putString(AppConstants.USER_NAME, user.getDisplayName());
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, RightNowActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify your email address before logging in.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginActivity.this, EmailVerificationReminderActivity.class);
                                startActivity(intent);
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
