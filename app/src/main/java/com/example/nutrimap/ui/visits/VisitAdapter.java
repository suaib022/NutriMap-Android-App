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
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitAdapter extends ListAdapter<Visit, VisitAdapter.ViewHolder> {

    private Map<String, String> childNames = new HashMap<>();

    private static final DiffUtil.ItemCallback<Visit> DIFF_CALLBACK = new DiffUtil.ItemCallback<Visit>() {
        @Override
        public boolean areItemsTheSame(@NonNull Visit oldItem, @NonNull Visit newItem) {
            String oldId = oldItem.getDocumentId();
            String newId = newItem.getDocumentId();
            if (oldId == null || newId == null) {
                return oldItem.getId() == newItem.getId();
            }
            return oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Visit oldItem, @NonNull Visit newItem) {
            return oldItem.getVisitDate().equals(newItem.getVisitDate());
        }
    };

    public VisitAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Set child names map for displaying child name in visit cards.
     */
    public void setChildNames(Map<String, String> names) {
        this.childNames = names;
        notifyDataSetChanged();
    }

    /**
     * Build child names map from a list of children.
     */
    public void loadChildNamesFromList(List<Child> children) {
        childNames.clear();
        for (Child child : children) {
            if (child.getDocumentId() != null) {
                childNames.put(child.getDocumentId(), child.getName());
            }
        }
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
        holder.bind(getItem(position), childNames);
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

        void bind(Visit visit, Map<String, String> childNames) {
            // Get child name from pre-loaded map
            String childName = "Child";
            if (visit.getChildDocumentId() != null && childNames.containsKey(visit.getChildDocumentId())) {
                childName = childNames.get(visit.getChildDocumentId());
            }
            
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
