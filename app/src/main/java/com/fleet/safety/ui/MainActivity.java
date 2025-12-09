package com.fleet.safety.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleet.safety.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userName = "Driver";
    private String userEmail = "";
    private String userRole = "driver"; // Por defecto es driver

    private MaterialButton buttonGoAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialButton buttonGoDriver = findViewById(R.id.button_go_driver);
        buttonGoAdmin = findViewById(R.id.button_go_admin);
        MaterialButton buttonLogout = findViewById(R.id.button_logout);

        // Ocultar botón de admin por defecto hasta verificar el rol
        buttonGoAdmin.setVisibility(View.GONE);

        // Recibir datos del usuario desde Intent extras
        Intent intent = getIntent();
        if (intent != null) {
            String userId = intent.getStringExtra("USER_ID");
            userEmail = intent.getStringExtra("USER_EMAIL");
            userName = intent.getStringExtra("USER_NAME");

            Log.d(TAG, "Received user data - ID: " + userId + ", Email: " + userEmail + ", Name: " + userName);

            if (userId != null) {
                loadUserDataFromFirestore(userId);
            }
        }

        buttonGoDriver.setOnClickListener(v -> {
            try {
                Intent driverIntent = new Intent(MainActivity.this, DriverDashboardActivity.class);
                // Pasar datos del usuario a DriverDashboardActivity
                driverIntent.putExtra("USER_NAME", userName);
                driverIntent.putExtra("USER_EMAIL", userEmail);
                startActivity(driverIntent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting DriverDashboardActivity", e);
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        buttonGoAdmin.setOnClickListener(v -> {
            try {
                Intent adminIntent = new Intent(MainActivity.this, AdminSettingsActivity.class);
                // Pasar datos del usuario a AdminSettingsActivity
                adminIntent.putExtra("USER_NAME", userName);
                adminIntent.putExtra("USER_EMAIL", userEmail);
                startActivity(adminIntent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting AdminSettingsActivity", e);
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        });
    }

    private void loadUserDataFromFirestore(String userId) {
        db.collection("user").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "User data from Firestore: " + document.getData());

                            // Obtener datos del documento según tu estructura: mail, name, role
                            String name = document.getString("name");
                            String mail = document.getString("mail");
                            String role = document.getString("role");

                            if (name != null) {
                                userName = name;
                            }
                            if (mail != null) {
                                userEmail = mail;
                            }
                            if (role != null) {
                                userRole = role;

                                // Mostrar botón de Admin SOLO si el usuario es admin
                                if ("admin".equalsIgnoreCase(role)) {
                                    buttonGoAdmin.setVisibility(View.VISIBLE);
                                    Toast.makeText(MainActivity.this,
                                            "Welcome Admin " + userName + "!", Toast.LENGTH_SHORT).show();
                                } else {
                                    buttonGoAdmin.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this,
                                            "Welcome " + userName + "!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Si no tiene rol definido, es driver por defecto
                                buttonGoAdmin.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this,
                                        "Welcome " + userName + "!", Toast.LENGTH_SHORT).show();
                            }

                            Log.d(TAG, "Loaded user: " + userName + " (" + userRole + ")");
                        } else {
                            Log.d(TAG, "No user document found in Firestore");
                            Toast.makeText(MainActivity.this,
                                    "User profile not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Error getting user document", task.getException());
                        Toast.makeText(MainActivity.this,
                                "Error loading user data: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
