package com.example.nutrimap.ui.visits;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.VisitRepository;
import com.example.nutrimap.databinding.FragmentVisitsBinding;
import com.example.nutrimap.domain.model.Visit;

import java.util.List;

public class VisitsFragment extends Fragment {

    private FragmentVisitsBinding binding;
    private VisitAdapter adapter;
    private NavController navController;

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
        setupFab();
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
                filterVisits(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFab() {
        binding.fabAddVisit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("childId", -1);
            args.putInt("visitId", -1);
            navController.navigate(R.id.createVisitFragment, args);
        });
    }

    private void loadVisits() {
        List<Visit> visits = VisitRepository.getInstance().getAllVisits();
        updateList(visits);
    }

    private void filterVisits(String query) {
        List<Visit> filtered = VisitRepository.getInstance().searchVisits(query);
        updateList(filtered);
    }

    private void updateList(List<Visit> visits) {
        adapter.submitList(visits);
        binding.textViewEmpty.setVisibility(visits.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerViewVisits.setVisibility(visits.isEmpty() ? View.GONE : View.VISIBLE);
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
