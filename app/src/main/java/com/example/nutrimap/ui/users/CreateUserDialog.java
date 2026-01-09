package com.example.nutrimap.ui.users;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.domain.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CreateUserDialog extends DialogFragment {

    private OnUserCreatedListener listener;

    public interface OnUserCreatedListener {
        void onUserCreated();
    }

    public void setOnUserCreatedListener(OnUserCreatedListener listener) {
        this.listener = listener;
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

        TextInputEditText editTextName = view.findViewById(R.id.editTextName);
        TextInputEditText editTextEmail = view.findViewById(R.id.editTextEmail);
        TextInputEditText editTextPassword = view.findViewById(R.id.editTextPassword);
        AutoCompleteTextView spinnerRole = view.findViewById(R.id.spinnerRole);
        MaterialButton buttonCancel = view.findViewById(R.id.buttonCancel);
        MaterialButton buttonSave = view.findViewById(R.id.buttonSave);

        // Setup role spinner
        String[] roles = {"USER", "ADMIN"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, roles);
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setText("USER", false);

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

            User user = new User(0, name, email, password, role);
            UserRepository.getInstance().addUser(user);
            Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onUserCreated();
            }
            dismiss();
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
