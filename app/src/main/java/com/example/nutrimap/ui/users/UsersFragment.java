package com.example.nutrimap.ui.users;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.databinding.FragmentUsersBinding;
import com.example.nutrimap.domain.model.User;

import java.util.List;

public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearch();
        setupFab();
        loadUsers();
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter();
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFab() {
        binding.fabAddUser.setOnClickListener(v -> {
            CreateUserDialog dialog = new CreateUserDialog();
            dialog.setOnUserCreatedListener(() -> loadUsers());
            dialog.show(getParentFragmentManager(), "CreateUserDialog");
        });
    }

    private void loadUsers() {
        List<User> users = UserRepository.getInstance().getAllUsers();
        updateList(users);
    }

    private void filterUsers(String query) {
        List<User> filtered = UserRepository.getInstance().searchUsers(query);
        updateList(filtered);
    }

    private void updateList(List<User> users) {
        adapter.submitList(users);
        binding.textViewEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerViewUsers.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
