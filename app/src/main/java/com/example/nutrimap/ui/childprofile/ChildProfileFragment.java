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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

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
                binding.textViewName.setText(child.getFullName());
                binding.textViewAge.setText(child.getAgeString() + " • " + child.getGender());
                binding.textViewFather.setText(child.getFathersName());
                binding.textViewMother.setText(child.getMothersName());
                binding.textViewContact.setText(child.getContactNumber());
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
                
                // Sort visits by date descending (Latest first) for List
                visits.sort((v1, v2) -> v2.getVisitDate().compareTo(v1.getVisitDate()));
                
                if (visits.isEmpty()) {
                    binding.textViewNoVisits.setVisibility(View.VISIBLE);
                    binding.recyclerViewVisits.setVisibility(View.GONE);
                    binding.lineChartGrowth.setNoDataText("No visit data for chart");
                    binding.textViewRisk.setText("N/A");
                    binding.textViewRisk.setBackgroundResource(R.color.colorSurfaceVariant);
                } else {
                    binding.textViewNoVisits.setVisibility(View.GONE);
                    binding.recyclerViewVisits.setVisibility(View.VISIBLE);
                    visitAdapter.submitList(visits);
                    
                    // Specific Logic for Chart: Sort Ascending (Oldest First)
                    List<Visit> chartVisits = new ArrayList<>(visits);
                    chartVisits.sort((v1, v2) -> v1.getVisitDate().compareTo(v2.getVisitDate()));
                    setupGrowthChart(chartVisits);
                    
                    // Dynamic Recalculation
                    Visit latestVisit = visits.get(0);
                    Visit previousVisit = visits.size() > 1 ? visits.get(1) : null;
                    
                    NutritionRiskCalculator.RiskResult result = NutritionRiskCalculator.evaluateFromVisitData(latestVisit, previousVisit, currentChild);
                    
                    String disp = result.riskLevel.toUpperCase();
                    binding.textViewRisk.setText(disp);
                    
                    int colorRes = R.color.colorRiskNA;
                    int bgRes = R.color.colorSurfaceVariant;
                    
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

                    binding.textViewRisk.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
                    binding.textViewRisk.setBackgroundColor(ContextCompat.getColor(requireContext(), bgRes));
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
        if (visits == null || visits.isEmpty()) {
            binding.lineChartGrowth.clear();
            binding.lineChartGrowth.setNoDataText("No valid visit data");
            binding.lineChartGrowth.invalidate();
            return;
        }

        binding.lineChartGrowth.setVisibility(View.VISIBLE);

        List<Entry> weightEntries = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();
        
        for (int i = 0; i < visits.size(); i++) {
            Visit v = visits.get(i);
            float weight = (float) v.getWeightKg();
            String date = v.getVisitDate() != null ? v.getVisitDate() : "N/A";
            
            weightEntries.add(new Entry(i, weight));
            dateLabels.add(date);
        }

        LineDataSet dataSet = new LineDataSet(weightEntries, "Weight (kg)");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryLight));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        binding.lineChartGrowth.setData(lineData);
        binding.lineChartGrowth.getDescription().setEnabled(false);
        
        // XAxis configuration
        XAxis xAxis = binding.lineChartGrowth.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setGranularity(1f); 
        xAxis.setLabelRotationAngle(-45);
        if (dateLabels.size() > 0) {
            xAxis.setLabelCount(Math.min(dateLabels.size(), 6), true);
        }
        
        binding.lineChartGrowth.getAxisRight().setEnabled(false);
        binding.lineChartGrowth.getAxisLeft().setAxisMinimum(0f); 
        
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
