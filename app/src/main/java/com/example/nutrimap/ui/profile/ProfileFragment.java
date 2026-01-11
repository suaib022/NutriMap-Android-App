package com.example.nutrimap.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.databinding.FragmentProfileBinding;
import com.example.nutrimap.domain.model.User;
import com.example.nutrimap.ui.login.LoginActivity;
import com.example.nutrimap.ui.main.MainActivity;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

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
        setupLogout();
    }

    private void loadUserProfile() {
        String email = "";
        if (getActivity() instanceof MainActivity) {
            email = ((MainActivity) getActivity()).getCurrentUserEmail();
        }

        final String userEmail = email;
        
        UserRepository.getInstance().getUserByEmail(email, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (binding == null) return;
                binding.textViewName.setText(user.getName());
                binding.textViewEmail.setText(user.getEmail());
                binding.textViewRole.setText(user.getRole());
            }

            @Override
            public void onError(String message) {
                if (binding == null) return;
                binding.textViewName.setText("NutriMap User");
                binding.textViewEmail.setText(userEmail);
                binding.textViewRole.setText("USER");
            }
        });
    }

    private void setupLogout() {
        binding.buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
