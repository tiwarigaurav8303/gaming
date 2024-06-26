package com.example.gameproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {
    EditText signupUserName, signupEmail, signupPassword;
    MaterialButton signupbtn;
    TextView tv_sign;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signupUserName = findViewById(R.id.username);
        signupEmail = findViewById(R.id.email);
        signupPassword = findViewById(R.id.password);
        signupbtn = findViewById(R.id.signupbtn);
        tv_sign = findViewById(R.id.tv_sign);

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tv_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Registration.this, MainActivity.class));
            }
        });
    }
    private void registerUser() {
        String email = signupEmail.getText().toString().trim();
        String username = signupUserName.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            signupEmail.setError("Email is required");
            signupEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Enter a valid email");
            signupEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            signupUserName.setError("Username is required");
            signupUserName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            signupPassword.setError("Password is required");
            signupPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            signupPassword.setError("Password must be at least 6 characters long");
            signupPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Save username to Firestore
                                            DocumentReference docRef = db.collection("users").document(user.getUid());
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("username", username);
                                            docRef.set(userMap).addOnSuccessListener(aVoid -> {
                                                Toast.makeText(Registration.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Registration.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }).addOnFailureListener(e -> {
                                                Toast.makeText(Registration.this, "Failed to save username", Toast.LENGTH_SHORT).show();
                                            });
                                        } else {
                                            Toast.makeText(Registration.this, "Failed to set username", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Registration.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
