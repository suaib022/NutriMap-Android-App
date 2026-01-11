package com.example.nutrimap.ui.visits;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.ChildRepository;
import com.example.nutrimap.data.repository.LocationRepository;
import com.example.nutrimap.data.repository.VisitRepository;
import com.example.nutrimap.databinding.FragmentVisitsBinding;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.District;
import com.example.nutrimap.domain.model.Division;
import com.example.nutrimap.domain.model.Visit;

import java.util.ArrayList;
import java.util.List;

public class VisitsFragment extends Fragment {

    private FragmentVisitsBinding binding;
    private VisitAdapter adapter;
    private NavController navController;

    // Filter state
    private List<Division> divisions = new ArrayList<>();
    private List<District> districts = new ArrayList<>();
    private String selectedDivisionId = "";
    private String selectedDistrictId = "";

    // Pagination state
    private static final int PAGE_SIZE = 10;
    private List<Visit> allFilteredVisits = new ArrayList<>();
    private int currentDisplayCount = PAGE_SIZE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVisitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupFab();
        setupPagination();
        loadDivisions();
        loadVisits();
    }

    private void setupRecyclerView() {
        adapter = new VisitAdapter();
        binding.recyclerViewVisits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewVisits.setAdapter(adapter);
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
                selectedDivisionId = divisions.get(position).getId();
                selectedDistrictId = "";
                binding.spinnerDistrict.setText("", false);
                loadDistrictsForDivision(selectedDivisionId);
                applyFiltersAndSearch();
            }
        });

        binding.spinnerDistrict.setOnItemClickListener((parent, v, position, id) -> {
            if (position < districts.size()) {
                selectedDistrictId = districts.get(position).getId();
                applyFiltersAndSearch();
            }
        });

        binding.buttonReset.setOnClickListener(v -> resetFilters());
    }

    private void setupFab() {
        binding.fabAddVisit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("childDocumentId", "");
            args.putString("visitDocumentId", "");
            navController.navigate(R.id.createVisitFragment, args);
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
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    binding.spinnerDivision.setAdapter(adapter);
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

    private void loadDistrictsForDivision(String divisionId) {
        LocationRepository.getInstance().getDistrictsByDivision(divisionId, new LocationRepository.DistrictCallback() {
            @Override
            public void onSuccess(List<District> dists) {
                districts = dists;
                List<String> names = new ArrayList<>();
                for (District d : dists) {
                    names.add(d.getName());
                }
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    binding.spinnerDistrict.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadVisits() {
        // First load children to get names
        ChildRepository.getInstance().getAllChildren(new ChildRepository.ChildrenCallback() {
            @Override
            public void onSuccess(List<Child> children) {
                adapter.loadChildNamesFromList(children);
                
                // Then load visits
                VisitRepository.getInstance().getAllVisits(new VisitRepository.VisitsCallback() {
                    @Override
                    public void onSuccess(List<Visit> visits) {
                        allFilteredVisits = visits;
                        currentDisplayCount = PAGE_SIZE;
                        updateDisplayedList();
                    }

                    @Override
                    public void onError(String message) {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                        allFilteredVisits = new ArrayList<>();
                        updateDisplayedList();
                    }
                });
            }

            @Override
            public void onError(String message) {
                // Still try to load visits without child names
                VisitRepository.getInstance().getAllVisits(new VisitRepository.VisitsCallback() {
                    @Override
                    public void onSuccess(List<Visit> visits) {
                        allFilteredVisits = visits;
                        currentDisplayCount = PAGE_SIZE;
                        updateDisplayedList();
                    }

                    @Override
                    public void onError(String msg) {
                        allFilteredVisits = new ArrayList<>();
                        updateDisplayedList();
                    }
                });
            }
        });
    }

    private void applyFiltersAndSearch() {
        String query = binding.editTextSearch.getText().toString().trim().toLowerCase();
        
        VisitRepository.getInstance().getAllVisits(new VisitRepository.VisitsCallback() {
            @Override
            public void onSuccess(List<Visit> visits) {
                // Apply search filter
                List<Visit> filtered = new ArrayList<>();
                for (Visit v : visits) {
                    boolean matchesSearch = query.isEmpty() || 
                            v.getVisitDate().contains(query) || 
                            (v.getNotes() != null && v.getNotes().toLowerCase().contains(query));
                    
                    if (matchesSearch) {
                        filtered.add(v);
                    }
                }
                
                allFilteredVisits = filtered;
                currentDisplayCount = PAGE_SIZE;
                updateDisplayedList();
            }

            @Override
            public void onError(String message) {
                allFilteredVisits = new ArrayList<>();
                updateDisplayedList();
            }
        });
    }

    private void resetFilters() {
        selectedDivisionId = "";
        selectedDistrictId = "";
        binding.spinnerDivision.setText("", false);
        binding.spinnerDistrict.setText("", false);
        binding.editTextSearch.setText("");
        loadVisits();
    }

    private void updateDisplayedList() {
        if (binding == null) return;
        
        int totalCount = allFilteredVisits.size();
        int displayCount = Math.min(currentDisplayCount, totalCount);
        
        List<Visit> displayList = allFilteredVisits.subList(0, displayCount);
        adapter.submitList(new ArrayList<>(displayList));
        
        binding.textViewEmpty.setVisibility(totalCount == 0 ? View.VISIBLE : View.GONE);
        binding.recyclerViewVisits.setVisibility(totalCount == 0 ? View.GONE : View.VISIBLE);
        
        // Update pagination UI
        binding.textViewShowingCount.setText(getString(R.string.showing_x_of_y, displayCount, totalCount));
        binding.buttonLoadMore.setVisibility(displayCount < totalCount ? View.VISIBLE : View.GONE);
        binding.paginationSection.setVisibility(totalCount > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVisits();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
