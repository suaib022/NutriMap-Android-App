package com.example.nutrimap.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.databinding.ActivityLoginBinding;
import com.example.nutrimap.ui.main.MainActivity;

/**
 * Login screen activity.
 * For this phase, any non-empty email/password combination is accepted.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            showError(getString(com.example.nutrimap.R.string.error_empty_credentials));
            return;
        }

        // For this phase, accept any non-empty credentials
        // Optionally validate against static users
        if (UserRepository.getInstance().validateLogin(email, password) || 
            (!email.isEmpty() && !password.isEmpty())) {
            // Login successful - navigate to main
            navigateToMain(email);
        } else {
            showError(getString(com.example.nutrimap.R.string.error_invalid_credentials));
        }
    }

    private void showError(String message) {
        binding.textViewError.setText(message);
        binding.textViewError.setVisibility(View.VISIBLE);
    }

    private void navigateToMain(String email) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_email", email);
        startActivity(intent);
        finish();
    }
}
