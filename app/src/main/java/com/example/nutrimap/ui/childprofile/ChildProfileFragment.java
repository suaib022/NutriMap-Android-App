package com.example.nutrimap.ui.childprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.ChildRepository;
import com.example.nutrimap.data.repository.VisitRepository;
import com.example.nutrimap.databinding.FragmentChildProfileBinding;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;
import com.example.nutrimap.ui.visits.VisitAdapter;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ChildProfileFragment extends Fragment {

    private FragmentChildProfileBinding binding;
    private String childDocumentId = "";
    private Child currentChild = null;
    private VisitAdapter visitAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChildProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            childDocumentId = getArguments().getString("childDocumentId", "");
        }

        setupVisitsList();
        setupFab();
        loadChildProfile();
    }

    private void setupVisitsList() {
        visitAdapter = new VisitAdapter();
        binding.recyclerViewVisits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewVisits.setAdapter(visitAdapter);
    }

    private void setupFab() {
        binding.fabAddVisit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("childDocumentId", childDocumentId);
            args.putString("visitDocumentId", "");
            Navigation.findNavController(requireView()).navigate(R.id.createVisitFragment, args);
        });
    }

    private void loadChildProfile() {
        if (childDocumentId.isEmpty()) return;
        
        ChildRepository.getInstance().getChildById(childDocumentId, new ChildRepository.ChildCallback() {
            @Override
            public void onSuccess(Child child) {
                currentChild = child;
                if (binding == null) return;
                
                // Display child info
                binding.textViewName.setText(child.getName());
                binding.textViewAge.setText(child.getAgeString() + " • " + child.getGender());
                binding.textViewFather.setText(child.getFatherName());
                binding.textViewMother.setText(child.getMotherName());
                binding.textViewContact.setText(child.getContact());
                binding.textViewLocation.setText("Division " + child.getDivisionId() + 
                        ", District " + child.getDistrictId());

                // Load visits and calculate risk
                loadVisits();
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadVisits() {
        VisitRepository.getInstance().getVisitsForChild(childDocumentId, new VisitRepository.VisitsCallback() {
            @Override
            public void onSuccess(List<Visit> visits) {
                if (binding == null) return;
                
                if (visits.isEmpty()) {
                    binding.textViewNoVisits.setVisibility(View.VISIBLE);
                    binding.recyclerViewVisits.setVisibility(View.GONE);
                    binding.lineChartGrowth.setNoDataText("No visit data for chart");
                    binding.textViewRisk.setText("N/A");
                    binding.textViewRisk.setBackgroundResource(R.color.colorBackground);
                } else {
                    binding.textViewNoVisits.setVisibility(View.GONE);
                    binding.recyclerViewVisits.setVisibility(View.VISIBLE);
                    visitAdapter.submitList(visits);
                    setupGrowthChart(visits);
                    
                    // Calculate risk from latest visit
                    Visit latestVisit = visits.get(0); // Already sorted by date desc
                    String risk = NutritionRiskCalculator.calculateRiskFromMuac(latestVisit.getMuacMm());
                    binding.textViewRisk.setText(risk);
                    binding.textViewRisk.setBackgroundResource(NutritionRiskCalculator.getRiskBackgroundResource(risk));
                    binding.textViewRisk.setTextColor(ContextCompat.getColor(requireContext(),
                            NutritionRiskCalculator.getRiskTextColorResource(risk)));
                }
            }

            @Override
            public void onError(String message) {
                if (binding != null) {
                    binding.textViewNoVisits.setVisibility(View.VISIBLE);
                    binding.recyclerViewVisits.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupGrowthChart(List<Visit> visits) {
        List<Entry> muacEntries = new ArrayList<>();
        
        for (int i = 0; i < visits.size(); i++) {
            Visit v = visits.get(i);
            muacEntries.add(new Entry(i, v.getMuacMm()));
        }

        LineDataSet dataSet = new LineDataSet(muacEntries, "MUAC (mm)");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryLight));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        binding.lineChartGrowth.setData(lineData);
        binding.lineChartGrowth.getDescription().setEnabled(false);
        binding.lineChartGrowth.getXAxis().setDrawGridLines(false);
        binding.lineChartGrowth.getAxisRight().setEnabled(false);
        
        Legend legend = binding.lineChartGrowth.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        
        binding.lineChartGrowth.animateX(500);
        binding.lineChartGrowth.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!childDocumentId.isEmpty()) {
            loadVisits();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
