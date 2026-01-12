package com.example.nutrimap.ui.users;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.domain.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateUserDialog extends DialogFragment {

    private OnUserCreatedListener listener;
    private CircleImageView imageViewUser;
    private String selectedImageBase64 = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireContext().getContentResolver(), imageUri);
                            imageViewUser.setImageBitmap(bitmap);
                            selectedImageBase64 = bitmapToBase64(bitmap);
                        } catch (IOException e) {
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    public interface OnUserCreatedListener {
        void onUserCreated();
    }

    public void setOnUserCreatedListener(OnUserCreatedListener listener) {
        this.listener = listener;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Resize bitmap to reduce storage size
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_NutriMap_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_create_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageViewUser = view.findViewById(R.id.imageViewUser);
        TextView textViewSelectPhoto = view.findViewById(R.id.textViewSelectPhoto);
        TextInputEditText editTextName = view.findViewById(R.id.editTextName);
        TextInputEditText editTextEmail = view.findViewById(R.id.editTextEmail);
        TextInputEditText editTextPassword = view.findViewById(R.id.editTextPassword);
        AutoCompleteTextView spinnerRole = view.findViewById(R.id.spinnerRole);
        MaterialButton buttonCancel = view.findViewById(R.id.buttonCancel);
        MaterialButton buttonSave = view.findViewById(R.id.buttonSave);

        // Setup role spinner with proper roles
        String[] roles = {"ADMIN", "SUPERVISOR", "FIELD_WORKER"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, roles);
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setText("FIELD_WORKER", false);

        // Image picker
        View.OnClickListener imageClickListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        };
        imageViewUser.setOnClickListener(imageClickListener);
        textViewSelectPhoto.setOnClickListener(imageClickListener);

        buttonCancel.setOnClickListener(v -> dismiss());

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String role = spinnerRole.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User(0, name, email, password, role);
            if (selectedImageBase64 != null) {
                user.setImagePath(selectedImageBase64);
            }

            UserRepository.getInstance().addUser(user, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
                    }
                    if (listener != null) {
                        listener.onUserCreated();
                    }
                    dismiss();
                }

                @Override
                public void onError(String message) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
