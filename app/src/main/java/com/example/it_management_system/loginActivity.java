package com.example.it_management_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    TextView forgotPasswordTextView, signUpRedirectTextView;
    Button loginButton;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef; // Firebase Realtime Database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Reference to users in Realtime DB

        // UI element initialization
        emailEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);
        signUpRedirectTextView = findViewById(R.id.signUpRedirect);
        loginButton = findViewById(R.id.loginButton);

        // Handle Login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(loginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(email, password);
                }
            }
        });

        // Handle Forgot Password click
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(loginActivity.this, "Enter your email first", Toast.LENGTH_SHORT).show();
                }  else if (!isValidDomain(email)) {
                    Toast.makeText(loginActivity.this, "Please use a GD Goenka email to reset your password.", Toast.LENGTH_SHORT).show();
                } else {
                    resetPassword(email);
                }
            }
        });

        // Navigate to Sign-Up activity
        signUpRedirectTextView.setOnClickListener(v -> {
            Intent intent = new Intent(loginActivity.this, SignUp.class);
            startActivity(intent);
        });
    }
    private boolean isValidDomain(String email) {
        return email.endsWith("@gdgu.org") || email.endsWith("@gdgoenka.ac.in") || email.endsWith("@gdgoenka.com");
    }

    // Login user and check role from Firebase Realtime Database
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                            // Check user role from Firebase Realtime Database
                            checkUserRole(firebaseUser.getUid(), email);
                        } else {
                            Toast.makeText(loginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(loginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Check user role and redirect to appropriate dashboard
    private void checkUserRole(String userId, String email) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    Intent intent;
                    if ("admin".equals(role) || email.equals("220160203106.subodh@gdgu.org")) {
                        intent = new Intent(loginActivity.this, AdminDashboard.class);
                    } else if ("executive".equals(role)) {
                        intent = new Intent(loginActivity.this, ExecutiveDashboard.class);
                    } else {
                        intent = new Intent(loginActivity.this, UserDashboard.class);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(loginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(loginActivity.this, "Error fetching user role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Forgot password functionality
    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(loginActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(loginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
