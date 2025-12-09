package com.fleet.safety.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleet.safety.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonRegister.setOnClickListener(v -> registerUser());
        binding.buttonBackToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = binding.inputName.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();
        String confirmPassword = binding.inputConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.buttonRegister.setEnabled(false);

        Log.d(TAG, "Starting registration for: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase Auth registration successful");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            Log.d(TAG, "User UID: " + user.getUid());
                            Log.d(TAG, "Attempting to save to Firestore...");

                            // Guardar información en Firestore con la estructura correcta
                            saveUserToFirestore(user, name, email, password);
                        } else {
                            Log.e(TAG, "User is null after successful registration!");
                            binding.buttonRegister.setEnabled(true);
                        }
                    } else {
                        binding.buttonRegister.setEnabled(true);
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String name, String email, String password) {
        if (user == null) {
            Log.e(TAG, "saveUserToFirestore called with null user");
            return;
        }

        Log.d(TAG, "Creating user data map...");

        // Estructura según tu Firestore: mail, name, password, role
        Map<String, Object> userData = new HashMap<>();
        userData.put("mail", email);
        userData.put("name", name);
        userData.put("password", password);
        userData.put("role", "driver");

        String userId = user.getUid();
        Log.d(TAG, "Saving to Firestore - Collection: user, Document: " + userId);
        Log.d(TAG, "Data: " + userData.toString());

        // Guardar en la collection "user" usando el UID de Firebase Auth como ID del documento
        db.collection("user").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ SUCCESS! User profile created in Firestore");
                    binding.buttonRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ FAILED! Error creating user profile in Firestore", e);
                    Log.e(TAG, "Error type: " + e.getClass().getSimpleName());
                    Log.e(TAG, "Error message: " + e.getMessage());

                    binding.buttonRegister.setEnabled(true);

                    Toast.makeText(RegisterActivity.this,
                            "Auth OK but Firestore failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Aún así cerramos la pantalla porque el usuario está registrado en Auth
                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
