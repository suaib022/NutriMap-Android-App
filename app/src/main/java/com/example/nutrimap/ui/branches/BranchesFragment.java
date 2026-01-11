package com.example.nutrimap.ui.branches;

import android.content.Intent;
import android.net.Uri;
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
import com.example.nutrimap.data.repository.LocationRepository;
import com.example.nutrimap.databinding.FragmentBranchesBinding;
import com.example.nutrimap.domain.model.Branch;
import com.example.nutrimap.domain.model.Division;

import java.util.ArrayList;
import java.util.List;

public class BranchesFragment extends Fragment implements BranchAdapter.OnBranchClickListener {

    private FragmentBranchesBinding binding;
    private BranchAdapter adapter;
    private List<Division> divisions = new ArrayList<>();
    private String selectedDivision = "";

    // Pagination state
    private static final int PAGE_SIZE = 10;
    private List<Branch> allFilteredBranches = new ArrayList<>();
    private int currentDisplayCount = PAGE_SIZE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBranchesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupPagination();
        loadDivisions();
        loadBranches();
    }

    private void setupRecyclerView() {
        adapter = new BranchAdapter(this);
        binding.recyclerViewBranches.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewBranches.setAdapter(adapter);
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
        binding.spinnerDivision.setOnItemClickListener((parent, v, position, id) -> {
            if (position < divisions.size()) {
                selectedDivision = divisions.get(position).getName();
                applyFiltersAndSearch();
            }
        });

        binding.buttonReset.setOnClickListener(v -> {
            selectedDivision = "";
            binding.spinnerDivision.setText("", false);
            binding.editTextSearch.setText("");
            loadBranches();
        });
    }

    private void setupPagination() {
        binding.buttonLoadMore.setOnClickListener(v -> {
            currentDisplayCount += PAGE_SIZE;
            updateDisplayedList();
        });
    }

    private void loadDivisions() {
        LocationRepository.getInstance().getDivisions(new LocationRepository.DivisionCallback() {
            @Override
            public void onSuccess(List<Division> divs) {
                divisions = divs;
                List<String> names = new ArrayList<>();
                for (Division d : divs) {
                    names.add(d.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, names);
                binding.spinnerDivision.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBranches() {
        binding.progressBar.setVisibility(View.VISIBLE);
        BranchRepository.getInstance().getBranches(new BranchRepository.BranchCallback() {
            @Override
            public void onSuccess(List<Branch> branches) {
                binding.progressBar.setVisibility(View.GONE);
                allFilteredBranches = new ArrayList<>(branches);
                currentDisplayCount = PAGE_SIZE;
                updateDisplayedList();
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFiltersAndSearch() {
        String query = binding.editTextSearch.getText().toString().trim().toLowerCase();
        
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // First filter by division
        if (selectedDivision.isEmpty()) {
            BranchRepository.getInstance().getBranches(new BranchRepository.BranchCallback() {
                @Override
                public void onSuccess(List<Branch> branches) {
                    binding.progressBar.setVisibility(View.GONE);
                    applySearchFilter(branches, query);
                }

                @Override
                public void onError(String message) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            BranchRepository.getInstance().getBranchesByLocation(selectedDivision, "", "",
                    new BranchRepository.BranchCallback() {
                        @Override
                        public void onSuccess(List<Branch> branches) {
                            binding.progressBar.setVisibility(View.GONE);
                            applySearchFilter(branches, query);
                        }

                        @Override
                        public void onError(String message) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void applySearchFilter(List<Branch> branches, String query) {
        if (query.isEmpty()) {
            allFilteredBranches = new ArrayList<>(branches);
        } else {
            allFilteredBranches = new ArrayList<>();
            for (Branch b : branches) {
                if (b.getName().toLowerCase().contains(query) ||
                        b.getDivision().toLowerCase().contains(query) ||
                        b.getDistrict().toLowerCase().contains(query)) {
                    allFilteredBranches.add(b);
                }
            }
        }
        currentDisplayCount = PAGE_SIZE;
        updateDisplayedList();
    }

    private void updateDisplayedList() {
        int totalCount = allFilteredBranches.size();
        int displayCount = Math.min(currentDisplayCount, totalCount);
        
        List<Branch> displayList = allFilteredBranches.subList(0, displayCount);
        adapter.submitList(new ArrayList<>(displayList));
        
        binding.textViewEmpty.setVisibility(totalCount == 0 ? View.VISIBLE : View.GONE);
        binding.recyclerViewBranches.setVisibility(totalCount == 0 ? View.GONE : View.VISIBLE);
        
        // Update pagination UI
        binding.textViewShowingCount.setText(getString(R.string.showing_x_of_y, displayCount, totalCount));
        binding.buttonLoadMore.setVisibility(displayCount < totalCount ? View.VISIBLE : View.GONE);
        binding.paginationSection.setVisibility(totalCount > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onOpenUrl(Branch branch) {
        String url = branch.getUrl();
        if (url != null && !url.isEmpty()) {
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
