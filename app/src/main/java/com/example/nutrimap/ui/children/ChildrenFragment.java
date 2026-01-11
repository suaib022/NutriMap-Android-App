package com.example.nutrimap.ui.children;

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
import com.example.nutrimap.databinding.FragmentChildrenBinding;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.District;
import com.example.nutrimap.domain.model.Division;
import com.example.nutrimap.domain.model.Union;
import com.example.nutrimap.domain.model.Upazila;
import com.example.nutrimap.domain.model.Visit;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying list of children with search, filters, and pagination.
 * Uses Firebase Firestore for data operations.
 */
public class ChildrenFragment extends Fragment implements ChildAdapter.OnChildActionListener {

    private FragmentChildrenBinding binding;
    private ChildAdapter adapter;
    private NavController navController;

    // Filter state
    private List<Division> divisions = new ArrayList<>();
    private List<District> districts = new ArrayList<>();
    private List<Upazila> upazilas = new ArrayList<>();
    private List<Union> unions = new ArrayList<>();
    private String selectedDivisionId = "";
    private String selectedDistrictId = "";
    private String selectedUpazilaId = "";
    private String selectedUnionId = "";

    // Pagination state
    private static final int PAGE_SIZE = 10;
    private List<Child> allFilteredChildren = new ArrayList<>();
    private int currentDisplayCount = PAGE_SIZE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChildrenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupButtons();
        setupPagination();
        loadDivisions();
        loadChildren();
    }

    private void setupRecyclerView() {
        adapter = new ChildAdapter(this);
        binding.recyclerViewChildren.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewChildren.setAdapter(adapter);
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
                selectedUpazilaId = "";
                selectedUnionId = "";
                binding.spinnerDistrict.setText("", false);
                binding.spinnerUpazila.setText("", false);
                binding.spinnerUnion.setText("", false);
                loadDistrictsForDivision(selectedDivisionId);
                applyFiltersAndSearch();
            }
        });

        binding.spinnerDistrict.setOnItemClickListener((parent, v, position, id) -> {
            if (position < districts.size()) {
                selectedDistrictId = districts.get(position).getId();
                selectedUpazilaId = "";
                selectedUnionId = "";
                binding.spinnerUpazila.setText("", false);
                binding.spinnerUnion.setText("", false);
                loadUpazilasForDistrict(selectedDistrictId);
                applyFiltersAndSearch();
            }
        });

        binding.spinnerUpazila.setOnItemClickListener((parent, v, position, id) -> {
            if (position < upazilas.size()) {
                selectedUpazilaId = upazilas.get(position).getId();
                selectedUnionId = "";
                binding.spinnerUnion.setText("", false);
                loadUnionsForUpazila(selectedUpazilaId);
                applyFiltersAndSearch();
            }
        });

        binding.spinnerUnion.setOnItemClickListener((parent, v, position, id) -> {
            if (position < unions.size()) {
                selectedUnionId = unions.get(position).getId();
                applyFiltersAndSearch();
            }
        });

        binding.buttonReset.setOnClickListener(v -> resetFilters());
    }

    private void setupButtons() {
        binding.fabAddChild.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("childDocumentId", "");
            navController.navigate(R.id.createChildFragment, args);
        });

        binding.buttonExportCsv.setOnClickListener(v -> 
            Toast.makeText(requireContext(), R.string.export_not_implemented, Toast.LENGTH_SHORT).show());

        binding.buttonExportPdf.setOnClickListener(v -> 
            Toast.makeText(requireContext(), R.string.export_not_implemented, Toast.LENGTH_SHORT).show());
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

    private void loadUpazilasForDistrict(String districtId) {
        LocationRepository.getInstance().getUpazilasByDistrict(districtId, new LocationRepository.UpazilaCallback() {
            @Override
            public void onSuccess(List<Upazila> upas) {
                upazilas = upas;
                List<String> names = new ArrayList<>();
                for (Upazila u : upas) {
                    names.add(u.getName());
                }
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    binding.spinnerUpazila.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadUnionsForUpazila(String upazilaId) {
        LocationRepository.getInstance().getUnionsByUpazila(upazilaId, new LocationRepository.UnionCallback() {
            @Override
            public void onSuccess(List<Union> uns) {
                unions = uns;
                List<String> names = new ArrayList<>();
                for (Union u : uns) {
                    names.add(u.getName());
                }
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    binding.spinnerUnion.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadChildren() {
        // First load visits for the adapter
        VisitRepository.getInstance().getAllVisits(new VisitRepository.VisitsCallback() {
            @Override
            public void onSuccess(List<Visit> visits) {
                adapter.loadVisitsData(visits);
                
                // Then load children
                ChildRepository.getInstance().getAllChildren(new ChildRepository.ChildrenCallback() {
                    @Override
                    public void onSuccess(List<Child> children) {
                        allFilteredChildren = children;
                        currentDisplayCount = PAGE_SIZE;
                        updateDisplayedList();
                    }

                    @Override
                    public void onError(String message) {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                        allFilteredChildren = new ArrayList<>();
                        updateDisplayedList();
                    }
                });
            }

            @Override
            public void onError(String message) {
                // Still load children even if visits fail
                ChildRepository.getInstance().getAllChildren(new ChildRepository.ChildrenCallback() {
                    @Override
                    public void onSuccess(List<Child> children) {
                        allFilteredChildren = children;
                        currentDisplayCount = PAGE_SIZE;
                        updateDisplayedList();
                    }

                    @Override
                    public void onError(String msg) {
                        allFilteredChildren = new ArrayList<>();
                        updateDisplayedList();
                    }
                });
            }
        });
    }

    private void applyFiltersAndSearch() {
        String query = binding.editTextSearch.getText().toString().trim();

        // First apply location filters
        if (selectedDivisionId.isEmpty() && selectedDistrictId.isEmpty() && 
            selectedUpazilaId.isEmpty() && selectedUnionId.isEmpty()) {
            // No filters, load all
            ChildRepository.getInstance().getAllChildren(new ChildRepository.ChildrenCallback() {
                @Override
                public void onSuccess(List<Child> children) {
                    applySearchToList(children, query);
                }

                @Override
                public void onError(String message) {
                    allFilteredChildren = new ArrayList<>();
                    updateDisplayedList();
                }
            });
        } else {
            ChildRepository.getInstance().filterChildrenByLocation(
                    selectedDivisionId, selectedDistrictId, selectedUpazilaId, selectedUnionId,
                    new ChildRepository.ChildrenCallback() {
                        @Override
                        public void onSuccess(List<Child> children) {
                            applySearchToList(children, query);
                        }

                        @Override
                        public void onError(String message) {
                            allFilteredChildren = new ArrayList<>();
                            updateDisplayedList();
                        }
                    });
        }
    }

    private void applySearchToList(List<Child> children, String query) {
        if (query.isEmpty()) {
            allFilteredChildren = children;
        } else {
            String lowerQuery = query.toLowerCase();
            List<Child> searchFiltered = new ArrayList<>();
            for (Child c : children) {
                if (c.getName().toLowerCase().contains(lowerQuery) ||
                        c.getFatherName().toLowerCase().contains(lowerQuery) ||
                        c.getMotherName().toLowerCase().contains(lowerQuery)) {
                    searchFiltered.add(c);
                }
            }
            allFilteredChildren = searchFiltered;
        }
        currentDisplayCount = PAGE_SIZE;
        updateDisplayedList();
    }

    private void resetFilters() {
        selectedDivisionId = "";
        selectedDistrictId = "";
        selectedUpazilaId = "";
        selectedUnionId = "";
        binding.spinnerDivision.setText("", false);
        binding.spinnerDistrict.setText("", false);
        binding.spinnerUpazila.setText("", false);
        binding.spinnerUnion.setText("", false);
        binding.editTextSearch.setText("");
        loadChildren();
    }

    private void updateDisplayedList() {
        if (binding == null) return;
        
        int totalCount = allFilteredChildren.size();
        int displayCount = Math.min(currentDisplayCount, totalCount);
        
        List<Child> displayList = allFilteredChildren.subList(0, displayCount);
        adapter.submitList(new ArrayList<>(displayList));
        
        binding.textViewEmpty.setVisibility(totalCount == 0 ? View.VISIBLE : View.GONE);
        binding.recyclerViewChildren.setVisibility(totalCount == 0 ? View.GONE : View.VISIBLE);
        
        // Update pagination UI
        binding.textViewShowingCount.setText(getString(R.string.showing_x_of_y, displayCount, totalCount));
        binding.buttonLoadMore.setVisibility(displayCount < totalCount ? View.VISIBLE : View.GONE);
        binding.paginationSection.setVisibility(totalCount > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewChild(Child child) {
        Bundle args = new Bundle();
        args.putString("childDocumentId", child.getDocumentId());
        navController.navigate(R.id.childProfileFragment, args);
    }

    @Override
    public void onEditChild(Child child) {
        Bundle args = new Bundle();
        args.putString("childDocumentId", child.getDocumentId());
        navController.navigate(R.id.createChildFragment, args);
    }

    @Override
    public void onDeleteChild(Child child) {
        ChildRepository.getInstance().deleteChild(child.getDocumentId(), new ChildRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), R.string.success_deleted, Toast.LENGTH_SHORT).show();
                }
                applyFiltersAndSearch();
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChildren();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
