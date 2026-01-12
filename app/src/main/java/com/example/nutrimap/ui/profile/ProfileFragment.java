package com.example.nutrimap.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.databinding.FragmentProfileBinding;
import com.example.nutrimap.domain.model.User;
import com.example.nutrimap.ui.login.LoginActivity;
import com.example.nutrimap.ui.main.MainActivity;
import com.example.nutrimap.data.session.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private User currentUser;
    private String selectedImageBase64 = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireContext().getContentResolver(), imageUri);
                            binding.imageViewProfile.setImageBitmap(bitmap);
                            selectedImageBase64 = bitmapToBase64(bitmap);
                            
                            // Auto-save the photo
                            saveProfilePhoto();
                        } catch (IOException e) {
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserProfile();
        setupClickListeners();
    }

    private void loadUserProfile() {
        // Get email from SessionManager
        SessionManager sessionManager = SessionManager.getInstance(requireContext());
        String email = sessionManager.getUserEmail();

        final String userEmail = email;

        UserRepository.getInstance().getUserByEmail(email, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (binding == null) return;
                currentUser = user;
                
                // Populate fields
                binding.editTextName.setText(user.getName());
                binding.editTextEmail.setText(user.getEmail());
                binding.textViewRole.setText(user.getRole().toUpperCase());
                
                // Load profile image
                if (user.getImagePath() != null && !user.getImagePath().isEmpty()) {
                    Glide.with(requireContext())
                            .load(user.getImagePath())
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(binding.imageViewProfile);
                }
            }

            @Override
            public void onError(String message) {
                if (binding == null) return;
                binding.editTextName.setText("NutriMap User");
                binding.editTextEmail.setText(userEmail);
                binding.textViewRole.setText("USER");
            }
        });
    }

    private void setupClickListeners() {
        // Logout button
        binding.buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Save changes button
        binding.buttonSaveChanges.setOnClickListener(v -> {
            String newName = binding.editTextName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_empty_credentials, Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentUser != null) {
                currentUser.setName(newName);
                UserRepository.getInstance().updateUser(currentUser, new UserRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), R.string.profile_name_saved, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), "Error saving: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Change password button
        binding.buttonChangePassword.setOnClickListener(v -> {
            String currentPassword = binding.editTextCurrentPassword.getText().toString();
            String newPassword = binding.editTextNewPassword.getText().toString();
            String confirmPassword = binding.editTextConfirmPassword.getText().toString();

            // Validate fields
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), R.string.profile_password_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(requireContext(), R.string.profile_password_mismatch, Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify current password and update
            if (currentUser != null && currentPassword.equals(currentUser.getPassword())) {
                currentUser.setPassword(newPassword);
                UserRepository.getInstance().updateUser(currentUser, new UserRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        if (getContext() != null) {
                            // Clear password fields
                            binding.editTextCurrentPassword.setText("");
                            binding.editTextNewPassword.setText("");
                            binding.editTextConfirmPassword.setText("");
                            Toast.makeText(requireContext(), R.string.profile_password_success, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        // Change photo click - image picker and profile image click
        View.OnClickListener photoClickListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        };
        binding.textViewChangePhoto.setOnClickListener(photoClickListener);
        binding.imageViewProfile.setOnClickListener(photoClickListener);
    }

    private void saveProfilePhoto() {
        if (currentUser != null && selectedImageBase64 != null) {
            currentUser.setImagePath(selectedImageBase64);
            UserRepository.getInstance().updateUser(currentUser, new UserRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Photo updated!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String message) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Error updating photo", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
