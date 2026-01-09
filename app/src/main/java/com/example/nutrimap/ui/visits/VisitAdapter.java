package com.example.nutrimap.ui.visits;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.VisitRepository;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;

public class VisitAdapter extends ListAdapter<Visit, VisitAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Visit> DIFF_CALLBACK = new DiffUtil.ItemCallback<Visit>() {
        @Override
        public boolean areItemsTheSame(@NonNull Visit oldItem, @NonNull Visit newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Visit oldItem, @NonNull Visit newItem) {
            return oldItem.getVisitDate().equals(newItem.getVisitDate());
        }
    };

    public VisitAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewChildName, textViewRisk, textViewDate;
        private final TextView textViewWeight, textViewHeight, textViewMuac, textViewNotes;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewRisk = itemView.findViewById(R.id.textViewRisk);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewWeight = itemView.findViewById(R.id.textViewWeight);
            textViewHeight = itemView.findViewById(R.id.textViewHeight);
            textViewMuac = itemView.findViewById(R.id.textViewMuac);
            textViewNotes = itemView.findViewById(R.id.textViewNotes);
        }

        void bind(Visit visit) {
            String childName = VisitRepository.getInstance().getChildNameForVisit(visit.getChildId());
            textViewChildName.setText(childName);
            textViewDate.setText("Date: " + visit.getVisitDate());
            textViewWeight.setText("Weight: " + visit.getWeightKg() + " kg");
            textViewHeight.setText("Height: " + visit.getHeightCm() + " cm");
            textViewMuac.setText("MUAC: " + visit.getMuacMm() + " mm");

            String risk = NutritionRiskCalculator.calculateRiskFromMuac(visit.getMuacMm());
            textViewRisk.setText(risk);
            textViewRisk.setBackgroundResource(NutritionRiskCalculator.getRiskBackgroundResource(risk));
            textViewRisk.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    NutritionRiskCalculator.getRiskTextColorResource(risk)));

            if (visit.getNotes() != null && !visit.getNotes().isEmpty()) {
                textViewNotes.setText(visit.getNotes());
                textViewNotes.setVisibility(View.VISIBLE);
            } else {
                textViewNotes.setVisibility(View.GONE);
            }
        }
    }
}
