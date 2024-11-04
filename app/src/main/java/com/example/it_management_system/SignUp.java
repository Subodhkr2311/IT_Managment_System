package com.example.it_management_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {
    EditText nameEditText, usernameEditText, passwordEditText;
    TextView loginTextView;
    Button signUpButton;
    FirebaseAuth auth;
    DatabaseReference databaseReference;

    // Define the admin email
    private final String ADMIN_EMAIL = "220160203106.subodh@gdgu.org";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        nameEditText = findViewById(R.id.nameEditTextSignup);
        usernameEditText = findViewById(R.id.usernameEditTextSignup);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginTextView = findViewById(R.id.signUpRedirect);
        signUpButton = findViewById(R.id.SigupButton);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String email = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }  else if (!isValidDomain(email)) {
                    Toast.makeText(SignUp.this, "Please use a GD Goenka email to reset your password.", Toast.LENGTH_SHORT).show();
                } else {
                    checkIfEmailExists(name, email, password);
                }
            }
        });

        // Navigate to Login activity
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, loginActivity.class);
            startActivity(intent);
        });
    }
    private boolean isValidDomain(String email) {
        return email.endsWith("@gdgu.org") || email.endsWith("@gdgoenka.ac.in") || email.endsWith("@gdgoenka.com");
    }


    // Sign up user and store details in Firebase
    private void signUpUser(String name, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Send email verification
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            // Notify the user to verify their email
                                            Toast.makeText(SignUp.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();

                                            // Save user data after sending email verification
                                            String userId = firebaseUser.getUid();
                                            saveUserData(userId, name, email, password);

                                            // Redirect user to login page after signup and email verification
                                            auth.signOut(); // Log the user out after signing up
                                            Intent intent = new Intent(SignUp.this, loginActivity.class);
                                            startActivity(intent);
                                            finish(); // Close sign-up activity
                                        } else {
                                            Toast.makeText(SignUp.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfEmailExists(String name, String email, String password) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                if (result != null && !result.getSignInMethods().isEmpty()) {
                    Toast.makeText(SignUp.this, "Email already in use. Please use another email.", Toast.LENGTH_SHORT).show();
                } else {
                    checkIfPasswordExists(name, email, password);
                }
            } else {
                Toast.makeText(SignUp.this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkIfPasswordExists(String name, String email, String password) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean passwordExists = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User existingUser = snapshot.getValue(User.class);
                    if (existingUser != null && existingUser.getPassword().equals(password)) {
                        passwordExists = true;
                        break;
                    }
                }

                if (passwordExists) {
                    Toast.makeText(SignUp.this, "This password is already in use. Please use a different one.", Toast.LENGTH_SHORT).show();
                } else {
                    signUpUser(name, email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUp.this, "Error checking password: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save user data in Firebase Realtime Database
    private void saveUserData(String userId, String name, String email, String password) {
        String role; // Default role

        // If the email matches the admin email, assign admin role
        if (email.equals(ADMIN_EMAIL)) {
            role = "admin";
        } else {
            role = "user";
        }

        // Create a User object to store user data (password will be hashed inside the User class)
        User user = new User(name, email, role, password);

        // Save user data to Firebase
        databaseReference.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUp.this, "User registered successfully. Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignUp.this, "Failed to store user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
