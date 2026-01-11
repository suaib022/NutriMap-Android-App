package com.example.nutrimap.ui.users;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.BranchRepository;
import com.example.nutrimap.data.repository.UserRepository;
import com.example.nutrimap.databinding.FragmentUsersBinding;
import com.example.nutrimap.domain.model.Branch;
import com.example.nutrimap.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment implements UserAdapter.OnUserActionListener {

    private FragmentUsersBinding binding;
    private UserAdapter adapter;

    // Filter state
    private List<Branch> branches = new ArrayList<>();
    private String selectedBranchId = "";

    // Pagination state
    private static final int PAGE_SIZE = 10;
    private List<User> allFilteredUsers = new ArrayList<>();
    private int currentDisplayCount = PAGE_SIZE;

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
        setupFilters();
        setupFab();
        setupPagination();
        loadBranches();
        loadUsers();
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(this);
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewUsers.setAdapter(adapter);
    }

    @Override
    public void onViewUser(User user) {
        UserDetailDialog dialog = UserDetailDialog.newInstance(user);
        dialog.show(getParentFragmentManager(), "UserDetailDialog");
    }

    private void setupSearch() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSearch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        binding.spinnerBranch.setOnItemClickListener((parent, v, position, id) -> {
            if (position < branches.size()) {
                selectedBranchId = branches.get(position).getId();
                applyFiltersAndSearch();
            }
        });

        binding.buttonReset.setOnClickListener(v -> resetFilters());
    }

    private void setupFab() {
        binding.fabAddUser.setOnClickListener(v -> {
            CreateUserDialog dialog = new CreateUserDialog();
            dialog.setOnUserCreatedListener(() -> loadUsers());
            dialog.show(getParentFragmentManager(), "CreateUserDialog");
        });
    }

    private void setupPagination() {
        binding.buttonLoadMore.setOnClickListener(v -> {
            currentDisplayCount += PAGE_SIZE;
            updateDisplayedList();
        });
    }

    private void loadBranches() {
        BranchRepository.getInstance().getBranches(new BranchRepository.BranchCallback() {
            @Override
            public void onSuccess(List<Branch> branchList) {
                branches = branchList;
                List<String> names = new ArrayList<>();
                for (Branch b : branchList) {
                    names.add(b.getName());
                }
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    binding.spinnerBranch.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUsers() {
        UserRepository.getInstance().getAllUsers(new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                allFilteredUsers = users;
                currentDisplayCount = PAGE_SIZE;
                updateDisplayedList();
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                }
                allFilteredUsers = new ArrayList<>();
                updateDisplayedList();
            }
        });
    }

    private void applyFiltersAndSearch() {
        String query = binding.editTextSearch.getText().toString().trim().toLowerCase();
        
        if (selectedBranchId.isEmpty()) {
            UserRepository.getInstance().getAllUsers(new UserRepository.UsersCallback() {
                @Override
                public void onSuccess(List<User> users) {
                    applySearchToList(users, query);
                }

                @Override
                public void onError(String message) {
                    allFilteredUsers = new ArrayList<>();
                    updateDisplayedList();
                }
            });
        } else {
            UserRepository.getInstance().filterUsersByBranch(selectedBranchId, new UserRepository.UsersCallback() {
                @Override
                public void onSuccess(List<User> users) {
                    applySearchToList(users, query);
                }

                @Override
                public void onError(String message) {
                    allFilteredUsers = new ArrayList<>();
                    updateDisplayedList();
                }
            });
        }
    }

    private void applySearchToList(List<User> users, String query) {
        if (query.isEmpty()) {
            allFilteredUsers = users;
        } else {
            List<User> searchFiltered = new ArrayList<>();
            for (User u : users) {
                if (u.getName().toLowerCase().contains(query) ||
                        u.getEmail().toLowerCase().contains(query)) {
                    searchFiltered.add(u);
                }
            }
            allFilteredUsers = searchFiltered;
        }
        currentDisplayCount = PAGE_SIZE;
        updateDisplayedList();
    }

    private void resetFilters() {
        selectedBranchId = "";
        binding.spinnerBranch.setText("", false);
        binding.editTextSearch.setText("");
        loadUsers();
    }

    private void updateDisplayedList() {
        if (binding == null) return;
        
        int totalCount = allFilteredUsers.size();
        int displayCount = Math.min(currentDisplayCount, totalCount);
        
        List<User> displayList = allFilteredUsers.subList(0, displayCount);
        adapter.submitList(new ArrayList<>(displayList));
        
        binding.textViewEmpty.setVisibility(totalCount == 0 ? View.VISIBLE : View.GONE);
        binding.recyclerViewUsers.setVisibility(totalCount == 0 ? View.GONE : View.VISIBLE);
        
        // Update pagination UI
        binding.textViewShowingCount.setText(getString(R.string.showing_x_of_y, displayCount, totalCount));
        binding.buttonLoadMore.setVisibility(displayCount < totalCount ? View.VISIBLE : View.GONE);
        binding.paginationSection.setVisibility(totalCount > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
