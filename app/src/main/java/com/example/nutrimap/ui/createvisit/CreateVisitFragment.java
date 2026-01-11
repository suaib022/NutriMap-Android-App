package com.example.nutrimap.ui.createvisit;

import android.app.DatePickerDialog;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.ChildRepository;
import com.example.nutrimap.data.repository.VisitRepository;
import com.example.nutrimap.databinding.FragmentCreateVisitBinding;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateVisitFragment extends Fragment {

    private FragmentCreateVisitBinding binding;
    private String childDocumentId = "";
    private String visitDocumentId = "";
    private List<Child> children = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateVisitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            childDocumentId = getArguments().getString("childDocumentId", "");
            visitDocumentId = getArguments().getString("visitDocumentId", "");
        }

        setupChildSpinner();
        setupDatePicker();
        setupMuacWatcher();
        setupSaveButton();

        // Set today's date as default
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        binding.editTextDate.setText(today);
    }

    private void setupChildSpinner() {
        ChildRepository.getInstance().getAllChildren(new ChildRepository.ChildrenCallback() {
            @Override
            public void onSuccess(List<Child> childList) {
                children = childList;
                List<String> names = new ArrayList<>();
                int selectedIndex = -1;

                for (int i = 0; i < children.size(); i++) {
                    Child c = children.get(i);
                    names.add(c.getFullName());
                    if (c.getDocumentId() != null && c.getDocumentId().equals(childDocumentId)) {
                        selectedIndex = i;
                    }
                }

                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    binding.spinnerChild.setAdapter(adapter);

                    if (selectedIndex >= 0) {
                        binding.spinnerChild.setText(names.get(selectedIndex), false);
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Error loading children: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.spinnerChild.setOnItemClickListener((parent, v, position, id) -> {
            childDocumentId = children.get(position).getDocumentId();
        });
    }

    private void setupDatePicker() {
        binding.editTextDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, day) -> {
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                binding.editTextDate.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupMuacWatcher() {
        binding.editTextMuac.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateRiskDisplay(s.toString());
            }
        });
    }

    private void updateRiskDisplay(String unused) {
        // We trigger based on any text change, so 'unused' param isn't strictly needed if we read all fields.
        // But to be efficient we might wait.
        
        String weightStr = binding.editTextWeight.getText().toString().trim();
        String heightStr = binding.editTextHeight.getText().toString().trim();
        String muacStr = binding.editTextMuac.getText().toString().trim();
        
        if (weightStr.isEmpty() || heightStr.isEmpty() || muacStr.isEmpty() || childDocumentId.isEmpty()) {
            binding.cardRiskLevel.setVisibility(View.GONE);
            return;
        }

        try {
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);
            int muac = Integer.parseInt(muacStr);
            
            Child child = getSelectedChild();
            if (child == null) return;
            
            // Create temp visit for calculation
            Visit tempVisit = new Visit();
            tempVisit.setVisitDate(binding.editTextDate.getText().toString());
            tempVisit.setWeightKg(weight);
            tempVisit.setHeightCm(height);
            tempVisit.setMuacMm(muac);
            
            // Calculate without previous visit (Trend analysis skipped for live preview)
            NutritionRiskCalculator.RiskResult result = NutritionRiskCalculator.evaluateFromVisitData(tempVisit, null, child);
            
            binding.cardRiskLevel.setVisibility(View.VISIBLE);
            binding.textViewRiskLevel.setText(result.riskLevel.toUpperCase());
            
            int colorRes = R.color.colorRiskNA;
            int bgRes = R.color.colorSurfaceVariant; // Default
            
            switch (result.riskLevel.toLowerCase()) {
                case "high":
                    colorRes = R.color.colorRiskHigh;
                    bgRes = R.color.colorRiskHighBackground;
                    break;
                case "medium":
                    colorRes = R.color.colorRiskMedium;
                    bgRes = R.color.colorRiskMediumBackground;
                    break;
                case "low":
                    colorRes = R.color.colorRiskLow;
                    bgRes = R.color.colorRiskLowBackground;
                    break;
            }
            
            binding.textViewRiskLevel.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
            binding.cardRiskLevel.setCardBackgroundColor(ContextCompat.getColor(requireContext(), bgRes));

        } catch (NumberFormatException e) {
            binding.cardRiskLevel.setVisibility(View.GONE);
        }
    }

    private Child getSelectedChild() {
        for (Child c : children) {
            if (c.getDocumentId() != null && c.getDocumentId().equals(childDocumentId)) {
                return c;
            }
        }
        return null;
    }

    private void setupSaveButton() {
        binding.buttonSave.setOnClickListener(v -> initiateSave());
    }

    private void initiateSave() {
        if (childDocumentId == null || childDocumentId.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a child", Toast.LENGTH_SHORT).show();
            return;
        }

        String weightStr = binding.editTextWeight.getText().toString().trim();
        String heightStr = binding.editTextHeight.getText().toString().trim();
        String muacStr = binding.editTextMuac.getText().toString().trim();
        String dateStr = binding.editTextDate.getText().toString().trim();

        if (weightStr.isEmpty() || heightStr.isEmpty() || muacStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all measurements", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);
            int muac = Integer.parseInt(muacStr);

            Visit visit = new Visit(
                    0,
                    0,
                    dateStr,
                    weight,
                    height,
                    muac,
                    binding.editTextNotes.getText().toString().trim(),
                    "N/A", null, null, null, false
            );
            visit.setChildDocumentId(childDocumentId);
            if (!visitDocumentId.isEmpty()) {
                visit.setDocumentId(visitDocumentId);
            }

            // Fetch previous visits to calculate risk with trend
            VisitRepository.getInstance().getVisitsForChild(childDocumentId, new VisitRepository.VisitsCallback() {
                @Override
                public void onSuccess(List<Visit> visits) {
                    processRiskAndSave(visit, visits);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), "Error fetching history: " + message, Toast.LENGTH_SHORT).show();
                    // Fallback to saving without trend? Or fail? 
                    // Better to try saving without previous visit data to avoid blocking user.
                    processRiskAndSave(visit, new ArrayList<>());
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    private void processRiskAndSave(Visit currentVisit, List<Visit> history) {
        Child child = getSelectedChild();
        if (child == null) return; // Should not happen

        Visit previousVisit = null;
        // Find previous visit: latest one BEFORE currentVisit.date
        // If editing, exclude self.
        
        String currentDate = currentVisit.getVisitDate();
        Visit bestPrev = null;
        
        for (Visit v : history) {
            // Skip if it is the current visit being edited
            if (visitDocumentId != null && !visitDocumentId.isEmpty() && 
                v.getDocumentId() != null && v.getDocumentId().equals(visitDocumentId)) {
                continue;
            }
            
            // Allow same date? Spec says "Previous visit". Usually means < date. 
            // If multiple on same day, logic is ambiguous. Assume one per day or use logic < date.
            if (v.getVisitDate().compareTo(currentDate) < 0) {
                if (bestPrev == null || v.getVisitDate().compareTo(bestPrev.getVisitDate()) > 0) {
                    bestPrev = v;
                }
            }
        }
        previousVisit = bestPrev;

        NutritionRiskCalculator.RiskResult result = NutritionRiskCalculator.evaluateFromVisitData(currentVisit, previousVisit, child);
        currentVisit.setRiskLevel(result.riskLevel);
        
        // Now save
        if (!visitDocumentId.isEmpty()) {
            VisitRepository.getInstance().updateVisit(currentVisit, new VisitRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                }

                @Override
                public void onError(String message) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            VisitRepository.getInstance().addVisit(currentVisit, new VisitRepository.VisitCallback() {
                @Override
                public void onSuccess(Visit newVisit) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                }

                @Override
                public void onError(String message) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
