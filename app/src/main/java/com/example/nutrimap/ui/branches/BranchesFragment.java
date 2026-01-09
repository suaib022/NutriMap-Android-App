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
    private String selectedDistrict = "";

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
                searchBranches(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        binding.spinnerDivision.setOnItemClickListener((parent, v, position, id) -> {
            if (position < divisions.size()) {
                selectedDivision = divisions.get(position).getName();
                filterByLocation();
            }
        });

        binding.buttonReset.setOnClickListener(v -> {
            selectedDivision = "";
            selectedDistrict = "";
            binding.spinnerDivision.setText("", false);
            binding.spinnerDistrict.setText("", false);
            binding.editTextSearch.setText("");
            loadBranches();
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
                updateList(branches);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterByLocation() {
        binding.progressBar.setVisibility(View.VISIBLE);
        BranchRepository.getInstance().getBranchesByLocation(selectedDivision, selectedDistrict, "",
                new BranchRepository.BranchCallback() {
                    @Override
                    public void onSuccess(List<Branch> branches) {
                        binding.progressBar.setVisibility(View.GONE);
                        updateList(branches);
                    }

                    @Override
                    public void onError(String message) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchBranches(String query) {
        BranchRepository.getInstance().searchBranches(query, new BranchRepository.BranchCallback() {
            @Override
            public void onSuccess(List<Branch> branches) {
                updateList(branches);
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void updateList(List<Branch> branches) {
        adapter.submitList(branches);
        binding.textViewEmpty.setVisibility(branches.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerViewBranches.setVisibility(branches.isEmpty() ? View.GONE : View.VISIBLE);
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
