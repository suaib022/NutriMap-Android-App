package com.example.nutrimap.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.ChildRepository;
import com.example.nutrimap.data.repository.VisitRepository;
import com.example.nutrimap.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Home/Dashboard fragment displaying statistics and charts.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Get statistics
        ChildRepository.DashboardStats stats = ChildRepository.getInstance().getDashboardStats();
        int totalVisits = VisitRepository.getInstance().getTotalVisitCount();

        // Update stat cards
        binding.textViewTotalChildren.setText(String.valueOf(stats.totalChildren));
        binding.textViewTotalVisits.setText(String.valueOf(totalVisits));
        binding.textViewHighRisk.setText(String.valueOf(stats.highRisk));
        binding.textViewMediumRisk.setText(String.valueOf(stats.mediumRisk));
        binding.textViewLowRisk.setText(String.valueOf(stats.lowRisk));

        // Setup charts
        setupPieChart(stats);
        setupLineChart();
        setupAreaSummary();
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
        Map<String, Integer> visitsPerMonth = VisitRepository.getInstance().getVisitsPerMonth();

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
        binding.lineChart.getLegend().setEnabled(false);
        binding.lineChart.animateX(800);
        binding.lineChart.invalidate();
    }

    private void setupAreaSummary() {
        // For simplicity, show a basic summary
        // In a full implementation, this would aggregate by district/branch
        binding.recyclerViewAreaSummary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewAreaSummary.setAdapter(new AreaSummaryAdapter(createAreaSummary()));
    }

    private List<AreaSummaryItem> createAreaSummary() {
        // Simple aggregation for demo
        List<AreaSummaryItem> items = new ArrayList<>();
        items.add(new AreaSummaryItem("Chattagram Division", 4, 10));
        items.add(new AreaSummaryItem("Dhaka Division", 3, 8));
        items.add(new AreaSummaryItem("Rajshahi Division", 3, 5));
        return items;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Inner class for area summary
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
