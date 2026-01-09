package com.example.nutrimap.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrimap.R;

import java.util.List;

/**
 * Adapter for area summary RecyclerView on dashboard.
 */
public class AreaSummaryAdapter extends RecyclerView.Adapter<AreaSummaryAdapter.ViewHolder> {

    private final List<HomeFragment.AreaSummaryItem> items;

    public AreaSummaryAdapter(List<HomeFragment.AreaSummaryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_area_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeFragment.AreaSummaryItem item = items.get(position);
        holder.textViewAreaName.setText(item.areaName);
        holder.textViewStats.setText(item.childrenCount + " children • " + item.visitCount + " visits");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAreaName;
        TextView textViewStats;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAreaName = itemView.findViewById(R.id.textViewAreaName);
            textViewStats = itemView.findViewById(R.id.textViewStats);
        }
    }
}
