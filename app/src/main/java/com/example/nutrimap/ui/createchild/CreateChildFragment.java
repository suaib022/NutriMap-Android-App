package com.example.nutrimap.ui.createchild;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.ChildRepository;
import com.example.nutrimap.data.repository.LocationRepository;
import com.example.nutrimap.databinding.FragmentCreateChildBinding;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.District;
import com.example.nutrimap.domain.model.Division;
import com.example.nutrimap.domain.model.Union;
import com.example.nutrimap.domain.model.Upazila;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateChildFragment extends Fragment {

    private FragmentCreateChildBinding binding;
    private String childDocumentId = "";
    private Child existingChild = null;
    private List<Division> divisions = new ArrayList<>();
    private List<District> districts = new ArrayList<>();
    private List<Upazila> upazilas = new ArrayList<>();
    private List<Union> unions = new ArrayList<>();
    private String selectedDivisionId, selectedDistrictId, selectedUpazilaId, selectedUnionId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateChildBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            childDocumentId = getArguments().getString("childDocumentId", "");
        }

        setupGenderSpinner();
        setupDatePicker();
        setupLocationSpinners();
        setupSaveButton();

        if (!childDocumentId.isEmpty()) {
            loadChild();
        }
    }

    private void setupGenderSpinner() {
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, genders);
        binding.spinnerGender.setAdapter(adapter);
    }

    private void setupDatePicker() {
        binding.editTextDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, day) -> {
                String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                binding.editTextDob.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupLocationSpinners() {
        // Load divisions
        LocationRepository.getInstance().getDivisions(new LocationRepository.DivisionCallback() {
            @Override
            public void onSuccess(List<Division> divs) {
                divisions = divs;
                List<String> names = new ArrayList<>();
                for (Division d : divs) names.add(d.getName());
                if (getContext() != null) {
                    binding.spinnerDivision.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names));
                }
            }
            @Override
            public void onError(String message) {}
        });

        binding.spinnerDivision.setOnItemClickListener((p, v, pos, id) -> {
            selectedDivisionId = divisions.get(pos).getId();
            loadDistricts(selectedDivisionId);
        });

        binding.spinnerDistrict.setOnItemClickListener((p, v, pos, id) -> {
            selectedDistrictId = districts.get(pos).getId();
            loadUpazilas(selectedDistrictId);
        });

        binding.spinnerUpazila.setOnItemClickListener((p, v, pos, id) -> {
            selectedUpazilaId = upazilas.get(pos).getId();
            loadUnions(selectedUpazilaId);
        });

        binding.spinnerUnion.setOnItemClickListener((p, v, pos, id) -> {
            selectedUnionId = unions.get(pos).getId();
        });
    }

    private void loadDistricts(String divisionId) {
        LocationRepository.getInstance().getDistrictsByDivision(divisionId, new LocationRepository.DistrictCallback() {
            @Override
            public void onSuccess(List<District> dists) {
                districts = dists;
                List<String> names = new ArrayList<>();
                for (District d : dists) names.add(d.getName());
                if (getContext() != null) {
                    binding.spinnerDistrict.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names));
                    binding.spinnerDistrict.setText("", false);
                    binding.spinnerUpazila.setText("", false);
                    binding.spinnerUnion.setText("", false);
                }
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void loadUpazilas(String districtId) {
        LocationRepository.getInstance().getUpazilasByDistrict(districtId, new LocationRepository.UpazilaCallback() {
            @Override
            public void onSuccess(List<Upazila> ups) {
                upazilas = ups;
                List<String> names = new ArrayList<>();
                for (Upazila u : ups) names.add(u.getName());
                if (getContext() != null) {
                    binding.spinnerUpazila.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names));
                    binding.spinnerUpazila.setText("", false);
                    binding.spinnerUnion.setText("", false);
                }
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void loadUnions(String upazilaId) {
        LocationRepository.getInstance().getUnionsByUpazila(upazilaId, new LocationRepository.UnionCallback() {
            @Override
            public void onSuccess(List<Union> uns) {
                unions = uns;
                List<String> names = new ArrayList<>();
                for (Union u : uns) names.add(u.getName());
                if (getContext() != null) {
                    binding.spinnerUnion.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names));
                    binding.spinnerUnion.setText("", false);
                }
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void loadChild() {
        ChildRepository.getInstance().getChildById(childDocumentId, new ChildRepository.ChildCallback() {
            @Override
            public void onSuccess(Child child) {
                existingChild = child;
                if (binding != null) {
                    binding.editTextName.setText(child.getFullName());
                    binding.editTextFatherName.setText(child.getFathersName());
                    binding.editTextMotherName.setText(child.getMothersName());
                    binding.editTextContact.setText(child.getContactNumber());
                    binding.editTextDob.setText(child.getDateOfBirth());
                    binding.spinnerGender.setText(child.getGender(), false);
                    
                    // Store existing location IDs
                    selectedDivisionId = child.getDivisionId();
                    selectedDistrictId = child.getDistrictId();
                    selectedUpazilaId = child.getUpazilaId();
                    selectedUnionId = child.getUnionId();
                }
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Error loading child: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSaveButton() {
        binding.buttonSave.setOnClickListener(v -> saveChild());
    }

    private void saveChild() {
        String name = binding.editTextName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter child name", Toast.LENGTH_SHORT).show();
            return;
        }

        Child child = new Child(
                0,
                name,
                binding.editTextFatherName.getText().toString().trim(),
                binding.editTextMotherName.getText().toString().trim(),
                binding.editTextContact.getText().toString().trim(),
                binding.spinnerDivision.getText().toString(),
                binding.spinnerDistrict.getText().toString(),
                binding.spinnerUpazila.getText().toString(),
                binding.spinnerUnion.getText().toString(),
                "1",
                "Default Branch",
                "",
                binding.spinnerGender.getText().toString(),
                binding.editTextDob.getText().toString(),
                selectedDivisionId != null ? selectedDivisionId : "1",
                selectedDistrictId != null ? selectedDistrictId : "1",
                selectedUpazilaId != null ? selectedUpazilaId : "1",
                selectedUnionId != null ? selectedUnionId : "1"
        );

        if (!childDocumentId.isEmpty() && existingChild != null) {
            child.setDocumentId(childDocumentId);
            ChildRepository.getInstance().updateChild(child, new ChildRepository.OperationCallback() {
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
            ChildRepository.getInstance().addChild(child, new ChildRepository.ChildCallback() {
                @Override
                public void onSuccess(Child newChild) {
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
