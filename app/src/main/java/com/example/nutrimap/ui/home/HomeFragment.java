package com.example.nutrimap.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutrimap.R;
import com.example.nutrimap.data.firebase.FirebaseDataService;
import com.example.nutrimap.data.repository.ChildRepository;
import com.example.nutrimap.data.repository.LocationRepository;
import com.example.nutrimap.data.repository.VisitRepository;
import com.example.nutrimap.databinding.FragmentHomeBinding;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Division;
import com.example.nutrimap.domain.model.Visit;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Home/Dashboard fragment displaying statistics and charts.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<Division> divisions = new ArrayList<>();
    private String selectedDivisionId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Seed sample data if needed
        FirebaseDataService.getInstance().seedSampleData(new FirebaseDataService.OperationCallback() {
            @Override
            public void onSuccess() {}
            @Override
            public void onError(String message) {}
        });
        
        setupAreaDivisionFilter();
        loadDashboardData();
        loadDivisionsAndAreaSummary();
    }

    private void setupAreaDivisionFilter() {
        binding.spinnerAreaDivision.setOnItemClickListener((parent, v, position, id) -> {
            if (position == 0) {
                // "All" selected
                selectedDivisionId = "";
            } else if (position - 1 < divisions.size()) {
                // Adjust for "All" at position 0
                selectedDivisionId = divisions.get(position - 1).getId();
            }
            updateAreaSummary();
        });
    }

    private void loadDivisionsAndAreaSummary() {
        // Setup recycler view layout manager
        binding.recyclerViewAreaSummary.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        LocationRepository.getInstance().getDivisions(new LocationRepository.DivisionCallback() {
            @Override
            public void onSuccess(List<Division> divs) {
                divisions = divs;
                
                // Setup dropdown adapter
                List<String> names = new ArrayList<>();
                names.add("All"); // Add "All" option at the start
                for (Division d : divs) {
                    names.add(d.getName());
                }
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    binding.spinnerAreaDivision.setAdapter(adapter);
                }
                
                // Now update area summary since we have divisions
                updateAreaSummary();
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
                // Still try to show area summary from child data
                updateAreaSummaryFallback();
            }
        });
    }

    private void loadDashboardData() {
        // Get statistics async
        ChildRepository.getInstance().getDashboardStats(new ChildRepository.DashboardStatsCallback() {
            @Override
            public void onSuccess(ChildRepository.DashboardStats stats) {
                if (binding == null) return;
                
                // Update stat cards
                binding.textViewTotalChildren.setText(String.valueOf(stats.totalChildren));
                binding.textViewHighRisk.setText(String.valueOf(stats.highRisk));
                binding.textViewMediumRisk.setText(String.valueOf(stats.mediumRisk));
                binding.textViewLowRisk.setText(String.valueOf(stats.lowRisk));

                // Setup pie chart
                setupPieChart(stats);
            }

            @Override
            public void onError(String message) {
                if (binding == null) return;
                binding.textViewTotalChildren.setText("0");
                binding.textViewHighRisk.setText("0");
                binding.textViewMediumRisk.setText("0");
                binding.textViewLowRisk.setText("0");
            }
        });

        // Get visit count async
        VisitRepository.getInstance().getTotalVisitCount(new VisitRepository.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (binding != null) {
                    binding.textViewTotalVisits.setText(String.valueOf(count));
                }
            }

            @Override
            public void onError(String message) {
                if (binding != null) {
                    binding.textViewTotalVisits.setText("0");
                }
            }
        });

        // Setup line chart
        setupLineChart();
    }

    private void setupPieChart(ChildRepository.DashboardStats stats) {
        List<PieEntry> entries = new ArrayList<>();
        if (stats.highRisk > 0) entries.add(new PieEntry(stats.highRisk, "High Risk"));
        if (stats.mediumRisk > 0) entries.add(new PieEntry(stats.mediumRisk, "Medium Risk"));
        if (stats.lowRisk > 0) entries.add(new PieEntry(stats.lowRisk, "Low Risk"));

        if (entries.isEmpty()) {
            binding.pieChart.setNoDataText("No data available");
            return;
        }

        List<Integer> colors = new ArrayList<>();
        if (stats.highRisk > 0) colors.add(ContextCompat.getColor(requireContext(), R.color.colorRiskHigh));
        if (stats.mediumRisk > 0) colors.add(ContextCompat.getColor(requireContext(), R.color.colorRiskMedium));
        if (stats.lowRisk > 0) colors.add(ContextCompat.getColor(requireContext(), R.color.colorRiskLow));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleRadius(45f);
        binding.pieChart.setTransparentCircleRadius(50f);
        binding.pieChart.setHoleColor(ContextCompat.getColor(requireContext(), R.color.colorSurface));
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setEntryLabelTextSize(11f);
        binding.pieChart.setEntryLabelColor(Color.WHITE);

        Legend legend = binding.pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        binding.pieChart.animateY(800);
        binding.pieChart.invalidate();
    }

    private void setupLineChart() {
        VisitRepository.getInstance().getVisitsPerMonth(new VisitRepository.VisitsPerMonthCallback() {
            @Override
            public void onSuccess(Map<String, Integer> visitsPerMonth) {
                if (binding == null) return;
                
                if (visitsPerMonth.isEmpty()) {
                    binding.lineChart.setNoDataText("No visit data");
                    return;
                }

                // Sort by month
                TreeMap<String, Integer> sortedVisits = new TreeMap<>(visitsPerMonth);

                List<Entry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                int index = 0;
                for (Map.Entry<String, Integer> entry : sortedVisits.entrySet()) {
                    entries.add(new Entry(index, entry.getValue()));
                    labels.add(entry.getKey().substring(5)); // MM only
                    index++;
                }

                LineDataSet dataSet = new LineDataSet(entries, "Visits");
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
                binding.lineChart.setData(lineData);
                binding.lineChart.getDescription().setEnabled(false);
                binding.lineChart.getXAxis().setDrawGridLines(false);
                binding.lineChart.getAxisRight().setEnabled(false);
                binding.lineChart.animateX(800);
                binding.lineChart.invalidate();
            }

            @Override
            public void onError(String message) {
                if (binding != null) {
                    binding.lineChart.setNoDataText("No visit data");
                }
            }
        });
    }

    private void updateAreaSummary() {
        if (divisions.isEmpty()) return;
        
        ChildRepository.getInstance().getAllChildren(new ChildRepository.ChildrenCallback() {
            @Override
            public void onSuccess(List<Child> children) {
                if (binding == null) return;
                
                List<AreaSummaryItem> items = new ArrayList<>();
                
                if (selectedDivisionId.isEmpty()) {
                    // Show all divisions
                    for (Division d : divisions) {
                        int count = 0;
                        for (Child c : children) {
                            if (d.getId().equals(c.getDivisionId())) {
                                count++;
                            }
                        }
                        items.add(new AreaSummaryItem(d.getName(), count, 0));
                    }
                } else {
                    // Show only selected division
                    for (Division d : divisions) {
                        if (d.getId().equals(selectedDivisionId)) {
                            int count = 0;
                            for (Child c : children) {
                                if (d.getId().equals(c.getDivisionId())) {
                                    count++;
                                }
                            }
                            items.add(new AreaSummaryItem(d.getName(), count, 0));
                            break;
                        }
                    }
                }
                
                AreaSummaryAdapter adapter = new AreaSummaryAdapter(items);
                binding.recyclerViewAreaSummary.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void updateAreaSummaryFallback() {
        ChildRepository.getInstance().getAllChildren(new ChildRepository.ChildrenCallback() {
            @Override
            public void onSuccess(List<Child> children) {
                if (binding == null) return;
                
                // Group by division ID
                Map<String, Integer> divisionCounts = new HashMap<>();
                for (Child c : children) {
                    String divId = c.getDivisionId();
                    divisionCounts.put(divId, divisionCounts.getOrDefault(divId, 0) + 1);
                }

                List<AreaSummaryItem> items = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : divisionCounts.entrySet()) {
                    items.add(new AreaSummaryItem("Division " + entry.getKey(), entry.getValue(), 0));
                }
                
                AreaSummaryAdapter adapter = new AreaSummaryAdapter(items);
                binding.recyclerViewAreaSummary.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Data class for area summary items.
     */
    public static class AreaSummaryItem {
        public final String areaName;
        public final int childrenCount;
        public final int visitCount;

        public AreaSummaryItem(String areaName, int childrenCount, int visitCount) {
            this.areaName = areaName;
            this.childrenCount = childrenCount;
            this.visitCount = visitCount;
        }
    }
}
