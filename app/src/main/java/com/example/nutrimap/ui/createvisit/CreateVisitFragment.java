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
                    names.add(c.getName());
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

    private void updateRiskDisplay(String muacStr) {
        if (muacStr.isEmpty()) {
            binding.cardRiskLevel.setVisibility(View.GONE);
            return;
        }

        try {
            int muac = Integer.parseInt(muacStr);
            String risk = NutritionRiskCalculator.calculateRiskFromMuac(muac);
            
            binding.cardRiskLevel.setVisibility(View.VISIBLE);
            binding.textViewRiskLevel.setText(risk);
            binding.textViewRiskLevel.setBackgroundResource(NutritionRiskCalculator.getRiskBackgroundResource(risk));
            binding.textViewRiskLevel.setTextColor(ContextCompat.getColor(requireContext(),
                    NutritionRiskCalculator.getRiskTextColorResource(risk)));
        } catch (NumberFormatException e) {
            binding.cardRiskLevel.setVisibility(View.GONE);
        }
    }

    private void setupSaveButton() {
        binding.buttonSave.setOnClickListener(v -> saveVisit());
    }

    private void saveVisit() {
        if (childDocumentId == null || childDocumentId.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a child", Toast.LENGTH_SHORT).show();
            return;
        }

        String weightStr = binding.editTextWeight.getText().toString().trim();
        String heightStr = binding.editTextHeight.getText().toString().trim();
        String muacStr = binding.editTextMuac.getText().toString().trim();

        if (weightStr.isEmpty() || heightStr.isEmpty() || muacStr.isEmpty()) {
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
                    binding.editTextDate.getText().toString(),
                    weight,
                    height,
                    muac,
                    binding.editTextNotes.getText().toString().trim()
            );
            visit.setChildDocumentId(childDocumentId);

            if (!visitDocumentId.isEmpty()) {
                visit.setDocumentId(visitDocumentId);
                VisitRepository.getInstance().updateVisit(visit, new VisitRepository.OperationCallback() {
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
                VisitRepository.getInstance().addVisit(visit, new VisitRepository.VisitCallback() {
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

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
