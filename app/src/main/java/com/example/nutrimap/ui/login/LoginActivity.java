package com.example.nutrimap.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.data.session.SessionManager;
import com.example.nutrimap.databinding.ActivityLoginBinding;
import com.example.nutrimap.domain.model.User;
import com.example.nutrimap.ui.main.MainActivity;

/**
 * Login screen activity with Firebase authentication.
 * Validates user credentials against Firebase Firestore.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        // Seed users to ensure they exist in Firebase
        UserRepository.getInstance().deleteAllUsersAndSeedDefaults(new UserRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Users seeded successfully");
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to seed users: " + message);
            }
        });

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonSignIn.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        // Clear previous error
        binding.textViewError.setVisibility(View.GONE);

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_empty_credentials));
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        // Show loading state
        binding.buttonSignIn.setEnabled(false);
        binding.buttonSignIn.setText("Signing in...");

        Log.d(TAG, "Attempting login for: " + email);

        // Validate against Firebase
        UserRepository.getInstance().validateCredentials(email, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Login successful for: " + user.getEmail() + " with role: " + user.getRole());
                // Save session
                sessionManager.createSession(user);
                navigateToMain();
            }

            @Override
            public void onInvalidCredentials() {
                Log.d(TAG, "Invalid credentials for: " + email);
                runOnUiThread(() -> {
                    binding.buttonSignIn.setEnabled(true);
                    binding.buttonSignIn.setText(R.string.btn_sign_in);
                    showError(getString(R.string.error_invalid_credentials));
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Login error: " + message);
                runOnUiThread(() -> {
                    binding.buttonSignIn.setEnabled(true);
                    binding.buttonSignIn.setText(R.string.btn_sign_in);
                    showError("Login error: " + message);
                });
            }
        });
    }

    private void showError(String message) {
        binding.textViewError.setText(message);
        binding.textViewError.setVisibility(View.VISIBLE);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
