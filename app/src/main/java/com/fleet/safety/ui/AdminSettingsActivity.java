package com.fleet.safety.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fleet.safety.databinding.ActivityAdminSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AdminSettings";

    private ActivityAdminSettingsBinding binding;
    private SettingsStore settingsStore;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userName = "Admin";
    private String userEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsStore = new SettingsStore(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Recibir datos del usuario desde Intent extras
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("USER_NAME");
            userEmail = intent.getStringExtra("USER_EMAIL");

            if (userName != null) {
                Log.d(TAG, "Admin user: " + userName + " (" + userEmail + ")");
            }
        }

        loadSettings();
        loadSettingsFromFirestore();

        binding.buttonSave.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        binding.inputMinSpeed.setText(String.valueOf(settingsStore.getMin()));
        binding.inputMaxSpeed.setText(String.valueOf(settingsStore.getMax()));
        binding.inputBaseSpeed.setText(String.valueOf(settingsStore.getBase()));
    }

    private void loadSettingsFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in");
            return;
        }

        db.collection("user").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Settings from Firestore: " + document.getData());

                            // Obtener configuraciones del usuario desde Firestore
                            Long minSpeed = document.getLong("minSpeedLimit");
                            Long maxSpeed = document.getLong("maxSpeedLimit");
                            Long baseSpeed = document.getLong("baseSpeed");

                            if (minSpeed != null && maxSpeed != null && baseSpeed != null) {
                                binding.inputMinSpeed.setText(String.valueOf(minSpeed));
                                binding.inputMaxSpeed.setText(String.valueOf(maxSpeed));
                                binding.inputBaseSpeed.setText(String.valueOf(baseSpeed));

                                Toast.makeText(this, "Settings loaded from cloud", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.w(TAG, "Error loading settings from Firestore", task.getException());
                    }
                });
    }

    private void saveSettings() {
        try {
            String minStr = binding.inputMinSpeed.getText().toString().trim();
            String maxStr = binding.inputMaxSpeed.getText().toString().trim();
            String baseStr = binding.inputBaseSpeed.getText().toString().trim();

            if (minStr.isEmpty() || maxStr.isEmpty() || baseStr.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            int min = Integer.parseInt(minStr);
            int max = Integer.parseInt(maxStr);
            int base = Integer.parseInt(baseStr);

            if (min > max) {
                Toast.makeText(this, "Min speed must be <= max speed", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar localmente
            settingsStore.save(min, max, base);

            // Guardar en Firestore
            saveSettingsToFirestore(min, max, base);

            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSettingsToFirestore(int min, int max, int base) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in, skipping Firestore save");
            return;
        }

        Map<String, Object> settingsData = new HashMap<>();
        settingsData.put("minSpeedLimit", min);
        settingsData.put("maxSpeedLimit", max);
        settingsData.put("baseSpeed", base);
        settingsData.put("updatedAt", System.currentTimeMillis());

        db.collection("user").document(currentUser.getUid())
                .update(settingsData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Settings saved to Firestore");
                    Toast.makeText(this, "Settings synced to cloud", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving settings to Firestore", e);
                    Toast.makeText(this, "Cloud sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
